package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.domain.model.Account
import com.example.domain.model.AccountType
import com.example.domain.model.CurrencyUtils
import com.example.domain.model.Voucher
import com.example.domain.model.VoucherLine
import com.example.domain.service.HierarchyValidationException
import com.example.domain.service.PostingException
import com.example.domain.service.VoucherValidationException
import com.example.repository.AccountingRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AccountingCoreTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: AccountingRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = AccountingRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun `currency utils parses and formats with exact cent precision`() {
        assertEquals(500000L, CurrencyUtils.parseToCents("5,000.00"))
        assertEquals(1050L, CurrencyUtils.parseToCents("10.50"))
        assertEquals(12345L, CurrencyUtils.doubleToCents(123.45))
        assertEquals("$5,000.00", CurrencyUtils.format(500000L, includeSymbol = true))
        assertEquals("5,000.00", CurrencyUtils.format(500000L, includeSymbol = false))
    }

    @Test
    fun `four level hierarchy is enforced strictly`() = runBlocking {
        // Level 1: Major Head (no parent)
        val l1Result = repository.createAccount(
            Account(code = "1000", name = "Assets", level = 1, parentId = null, accountType = AccountType.ASSET)
        )
        assertTrue(l1Result.isSuccess)
        val l1 = l1Result.getOrThrow()
        assertEquals(1, l1.level)

        // Level 2: Sub-head under Level 1
        val l2Result = repository.createAccount(
            Account(code = "1100", name = "Current Assets", level = 2, parentId = l1.id, accountType = AccountType.ASSET)
        )
        assertTrue(l2Result.isSuccess)
        val l2 = l2Result.getOrThrow()
        assertEquals(2, l2.level)

        // Level 3: Control Group under Level 2
        val l3Result = repository.createAccount(
            Account(code = "1110", name = "Cash & Bank", level = 3, parentId = l2.id, accountType = AccountType.ASSET)
        )
        assertTrue(l3Result.isSuccess)
        val l3 = l3Result.getOrThrow()
        assertEquals(3, l3.level)

        // Level 4: Posting Account under Level 3
        val l4Result = repository.createAccount(
            Account(code = "111001", name = "Cash in Hand", level = 4, parentId = l3.id, accountType = AccountType.ASSET)
        )
        assertTrue(l4Result.isSuccess)
        val l4 = l4Result.getOrThrow()
        assertEquals(4, l4.level)
        assertTrue(l4.isPostingAccount)

        // Attempt Level 2 without parent must fail
        val invalidL2 = repository.createAccount(
            Account(code = "1200", name = "No Parent L2", level = 2, parentId = null, accountType = AccountType.ASSET)
        )
        assertTrue(invalidL2.isFailure)
        assertTrue(invalidL2.exceptionOrNull() is HierarchyValidationException)

        // Attempt Level 4 under Level 1 (skipping levels) must fail
        val invalidL4 = repository.createAccount(
            Account(code = "111002", name = "Invalid Parent L4", level = 4, parentId = l1.id, accountType = AccountType.ASSET)
        )
        assertTrue(invalidL4.isFailure)
        assertTrue(invalidL4.exceptionOrNull() is HierarchyValidationException)

        // Duplicate code must fail
        val duplicateCode = repository.createAccount(
            Account(code = "1000", name = "Duplicate Assets", level = 1, parentId = null, accountType = AccountType.ASSET)
        )
        assertTrue(duplicateCode.isFailure)
        assertTrue(duplicateCode.exceptionOrNull() is HierarchyValidationException)
    }

    @Test
    fun `cp voucher validation accepts balanced entries and rejects unbalanced entries`() = runBlocking {
        // Setup hierarchy
        val l1 = repository.createAccount(Account(code = "1000", name = "Assets", level = 1, parentId = null, accountType = AccountType.ASSET)).getOrThrow()
        val l2 = repository.createAccount(Account(code = "1100", name = "Current Assets", level = 2, parentId = l1.id, accountType = AccountType.ASSET)).getOrThrow()
        val l3 = repository.createAccount(Account(code = "1110", name = "Cash & Bank", level = 3, parentId = l2.id, accountType = AccountType.ASSET)).getOrThrow()

        val l1Exp = repository.createAccount(Account(code = "5000", name = "Expenses", level = 1, parentId = null, accountType = AccountType.EXPENSE)).getOrThrow()
        val l2Exp = repository.createAccount(Account(code = "5100", name = "Admin Expenses", level = 2, parentId = l1Exp.id, accountType = AccountType.EXPENSE)).getOrThrow()
        val l3Exp = repository.createAccount(Account(code = "5110", name = "Office Admin", level = 3, parentId = l2Exp.id, accountType = AccountType.EXPENSE)).getOrThrow()

        val cashAcc = repository.createAccount(Account(code = "111001", name = "Cash in Hand", level = 4, parentId = l3.id, accountType = AccountType.ASSET)).getOrThrow()
        val expenseAcc = repository.createAccount(Account(code = "511001", name = "Office Supplies", level = 4, parentId = l3Exp.id, accountType = AccountType.EXPENSE)).getOrThrow()

        // 1. Balanced Voucher (Debit $5,000 = Credit $5,000)
        val balancedVoucher = Voucher(
            voucherNumber = "CP-000001",
            voucherType = "CP",
            voucherDate = System.currentTimeMillis(),
            reference = "REF-100",
            narration = "Office Supplies purchase",
            lines = listOf(
                VoucherLine(accountId = expenseAcc.id, debitCents = 500000L, creditCents = 0L),
                VoucherLine(accountId = cashAcc.id, debitCents = 0L, creditCents = 500000L)
            )
        )
        val createResult = repository.createVoucher(balancedVoucher)
        assertTrue(createResult.isSuccess)
        val saved = createResult.getOrThrow()
        assertEquals(500000L, saved.totalDebitCents)
        assertEquals(500000L, saved.totalCreditCents)
        assertEquals(0L, saved.differenceCents)
        assertFalse(saved.isPosted)

        // 2. Unbalanced Voucher (Debit $5,000 != Credit $4,000)
        val unbalancedVoucher = Voucher(
            voucherNumber = "CP-000002",
            voucherType = "CP",
            voucherDate = System.currentTimeMillis(),
            reference = "REF-101",
            narration = "Unbalanced test",
            lines = listOf(
                VoucherLine(accountId = expenseAcc.id, debitCents = 500000L, creditCents = 0L),
                VoucherLine(accountId = cashAcc.id, debitCents = 0L, creditCents = 400000L)
            )
        )
        val unbalResult = repository.createVoucher(unbalancedVoucher)
        assertTrue(unbalResult.isFailure)
        assertTrue(unbalResult.exceptionOrNull() is VoucherValidationException)

        // 3. Line with both Debit and Credit must fail
        val invalidLineVoucher = Voucher(
            voucherNumber = "CP-000003",
            voucherType = "CP",
            voucherDate = System.currentTimeMillis(),
            reference = "REF-102",
            narration = "Invalid line test",
            lines = listOf(
                VoucherLine(accountId = expenseAcc.id, debitCents = 10000L, creditCents = 10000L),
                VoucherLine(accountId = cashAcc.id, debitCents = 0L, creditCents = 10000L)
            )
        )
        val invalidLineResult = repository.createVoucher(invalidLineVoucher)
        assertTrue(invalidLineResult.isFailure)
        assertTrue(invalidLineResult.exceptionOrNull() is VoucherValidationException)

        // 4. Non-posting account (Level 1) in voucher line must fail
        val nonPostingVoucher = Voucher(
            voucherNumber = "CP-000004",
            voucherType = "CP",
            voucherDate = System.currentTimeMillis(),
            reference = "REF-103",
            narration = "Non-posting account test",
            lines = listOf(
                VoucherLine(accountId = l1Exp.id, debitCents = 10000L, creditCents = 0L),
                VoucherLine(accountId = cashAcc.id, debitCents = 0L, creditCents = 10000L)
            )
        )
        val nonPostingResult = repository.createVoucher(nonPostingVoucher)
        assertTrue(nonPostingResult.isFailure)
        assertTrue(nonPostingResult.exceptionOrNull() is VoucherValidationException)
    }

    @Test
    fun `posting engine creates ledger transactions and enforces atomic one-time posting`() = runBlocking {
        // Setup hierarchy
        val l1Assets = repository.createAccount(Account(code = "1000", name = "Assets", level = 1, parentId = null, accountType = AccountType.ASSET)).getOrThrow()
        val l2Assets = repository.createAccount(Account(code = "1100", name = "Current Assets", level = 2, parentId = l1Assets.id, accountType = AccountType.ASSET)).getOrThrow()
        val l3Assets = repository.createAccount(Account(code = "1110", name = "Cash & Bank", level = 3, parentId = l2Assets.id, accountType = AccountType.ASSET)).getOrThrow()
        val cash = repository.createAccount(Account(code = "111001", name = "Cash in Hand", level = 4, parentId = l3Assets.id, accountType = AccountType.ASSET)).getOrThrow()

        val l1Exp = repository.createAccount(Account(code = "5000", name = "Expenses", level = 1, parentId = null, accountType = AccountType.EXPENSE)).getOrThrow()
        val l2Exp = repository.createAccount(Account(code = "5100", name = "Admin", level = 2, parentId = l1Exp.id, accountType = AccountType.EXPENSE)).getOrThrow()
        val l3Exp = repository.createAccount(Account(code = "5110", name = "Office Admin", level = 3, parentId = l2Exp.id, accountType = AccountType.EXPENSE)).getOrThrow()
        val expense = repository.createAccount(Account(code = "511001", name = "Stationery", level = 4, parentId = l3Exp.id, accountType = AccountType.EXPENSE)).getOrThrow()

        // Create draft voucher
        val draft = repository.createVoucher(
            Voucher(
                voucherNumber = "CP-000001",
                voucherType = "CP",
                voucherDate = System.currentTimeMillis(),
                reference = "CHK-99",
                narration = "Stationery payment",
                lines = listOf(
                    VoucherLine(accountId = expense.id, debitCents = 250000L, creditCents = 0L),
                    VoucherLine(accountId = cash.id, debitCents = 0L, creditCents = 250000L)
                )
            )
        ).getOrThrow()

        assertFalse(draft.isPosted)

        // Post voucher
        val postResult = repository.postVoucher(draft.id)
        assertTrue(postResult.isSuccess)
        val posted = postResult.getOrThrow()
        assertTrue(posted.isPosted)
        assertNotNull(posted.postedAt)

        // Verify ledger transactions
        val cashLedger = repository.getAccountLedger(cash.id)
        assertEquals(1, cashLedger.size)
        assertEquals(250000L, cashLedger[0].creditCents)
        assertEquals(0L, cashLedger[0].debitCents)

        val expenseLedger = repository.getAccountLedger(expense.id)
        assertEquals(1, expenseLedger.size)
        assertEquals(250000L, expenseLedger[0].debitCents)
        assertEquals(0L, expenseLedger[0].creditCents)

        // Verify trial balance reconciles
        val trialBalance = repository.getTrialBalance()
        assertEquals(250000L, trialBalance.totalDebitCents)
        assertEquals(250000L, trialBalance.totalCreditCents)
        assertEquals(0L, trialBalance.differenceCents)
        assertTrue(trialBalance.isBalanced)

        // Duplicate posting attempt must fail
        val duplicatePost = repository.postVoucher(draft.id)
        assertTrue(duplicatePost.isFailure)
        assertTrue(duplicatePost.exceptionOrNull() is PostingException)
    }
}
