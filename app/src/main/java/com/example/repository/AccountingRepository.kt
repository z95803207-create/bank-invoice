package com.example.repository

import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.local.entity.AccountEntity
import com.example.data.local.entity.LedgerTransactionEntity
import com.example.data.local.entity.VoucherEntity
import com.example.data.local.entity.VoucherLineEntity
import com.example.domain.model.Account
import com.example.domain.model.AccountType
import com.example.domain.model.CurrencyUtils
import com.example.domain.model.DashboardSummary
import com.example.domain.model.LedgerTransaction
import com.example.domain.model.NormalBalance
import com.example.domain.model.TrialBalanceReport
import com.example.domain.model.TrialBalanceRow
import com.example.domain.model.Voucher
import com.example.domain.model.VoucherLine
import com.example.domain.service.HierarchyValidationException
import com.example.domain.service.PostingException
import com.example.domain.service.VoucherValidationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.Locale

class AccountingRepository(
    private val database: AppDatabase
) {
    private val accountDao = database.accountDao()
    private val voucherDao = database.voucherDao()
    private val ledgerDao = database.ledgerDao()

    // ----------------------------------------------------
    // Chart of Accounts
    // ----------------------------------------------------

    fun getAccountsFlow(): Flow<List<Account>> {
        return accountDao.getAllAccountsFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getAllAccounts(): List<Account> = withContext(Dispatchers.IO) {
        accountDao.getAllAccounts().map { it.toDomain() }
    }

    suspend fun getAccountById(id: Long): Account? = withContext(Dispatchers.IO) {
        accountDao.getAccountById(id)?.toDomain()
    }

    fun getPostingAccountsFlow(): Flow<List<Account>> {
        return accountDao.getPostingAccountsFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    suspend fun getPostingAccounts(): List<Account> = withContext(Dispatchers.IO) {
        accountDao.getPostingAccounts().map { it.toDomain() }
    }

    suspend fun validateAccountHierarchy(
        code: String,
        name: String,
        level: Int,
        parentId: Long?,
        currentId: Long? = null
    ) = withContext(Dispatchers.IO) {
        if (code.isBlank()) {
            throw HierarchyValidationException("Account Code is required.")
        }
        if (name.isBlank()) {
            throw HierarchyValidationException("Account Name is required.")
        }
        if (level !in 1..4) {
            throw HierarchyValidationException("Account level must be exactly 1, 2, 3, or 4.")
        }

        // Check duplicate code
        val existingWithCode = accountDao.getAccountByCode(code.trim())
        if (existingWithCode != null && existingWithCode.id != (currentId ?: -1L)) {
            throw HierarchyValidationException("Account code '${code.trim()}' already exists. Account codes must be unique.")
        }

        // Level 1 rules: No parent allowed
        if (level == 1) {
            if (parentId != null) {
                throw HierarchyValidationException("Level 1 accounts cannot have a parent account.")
            }
            return@withContext
        }

        // Levels 2, 3, 4 require a parent
        if (parentId == null) {
            throw HierarchyValidationException("Level $level account must belong to a Level ${level - 1} parent account.")
        }

        val parent = accountDao.getAccountById(parentId)
            ?: throw HierarchyValidationException("Parent account with ID $parentId does not exist.")

        // Parent must be exactly level - 1
        if (parent.level != level - 1) {
            throw HierarchyValidationException(
                "Invalid hierarchy: A Level $level account can only have a Level ${level - 1} parent. Selected parent '${parent.name}' is Level ${parent.level}."
            )
        }

        // Circular hierarchy check for updates
        if (currentId != null) {
            if (parentId == currentId) {
                throw HierarchyValidationException("An account cannot be its own parent.")
            }
            // Trace ancestors to ensure currentId is not an ancestor of parentId
            var currentCheckParentId: Long? = parent.parentId
            while (currentCheckParentId != null) {
                if (currentCheckParentId == currentId) {
                    throw HierarchyValidationException("Circular hierarchy detected: cannot set a descendant as parent.")
                }
                val ancestor = accountDao.getAccountById(currentCheckParentId)
                currentCheckParentId = ancestor?.parentId
            }
        }
    }

    suspend fun createAccount(account: Account): Result<Account> = withContext(Dispatchers.IO) {
        try {
            validateAccountHierarchy(
                code = account.code,
                name = account.name,
                level = account.level,
                parentId = account.parentId,
                currentId = null
            )
            val entity = AccountEntity.fromDomain(account.copy(id = 0))
            val newId = accountDao.insertAccount(entity)
            val created = accountDao.getAccountById(newId)!!.toDomain()
            Result.success(created)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateAccount(account: Account): Result<Account> = withContext(Dispatchers.IO) {
        try {
            val existing = accountDao.getAccountById(account.id)
                ?: throw HierarchyValidationException("Account with ID ${account.id} not found.")

            validateAccountHierarchy(
                code = account.code,
                name = account.name,
                level = account.level,
                parentId = account.parentId,
                currentId = account.id
            )

            val updatedEntity = AccountEntity.fromDomain(
                account.copy(
                    createdAt = existing.createdAt,
                    updatedAt = System.currentTimeMillis()
                )
            )
            accountDao.updateAccount(updatedEntity)
            val reloaded = accountDao.getAccountById(account.id)!!.toDomain()
            Result.success(reloaded)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ----------------------------------------------------
    // CP Voucher Management
    // ----------------------------------------------------

    fun getVouchersFlow(): Flow<List<Voucher>> {
        return voucherDao.getAllVouchersFlow().map { entities ->
            entities.map { entity ->
                val lines = voucherDao.getLinesForVoucher(entity.id)
                val linesWithDetails = lines.map { lineEntity ->
                    val acc = accountDao.getAccountById(lineEntity.accountId)
                    lineEntity.toDomain(
                        accountCode = acc?.code ?: "",
                        accountName = acc?.name ?: ""
                    )
                }
                entity.toDomain().copy(lines = linesWithDetails)
            }
        }
    }

    suspend fun getVoucherById(id: Long): Voucher? = withContext(Dispatchers.IO) {
        val entity = voucherDao.getVoucherById(id) ?: return@withContext null
        val lines = voucherDao.getLinesForVoucher(id)
        val linesWithDetails = lines.map { lineEntity ->
            val acc = accountDao.getAccountById(lineEntity.accountId)
            lineEntity.toDomain(
                accountCode = acc?.code ?: "",
                accountName = acc?.name ?: ""
            )
        }
        entity.toDomain().copy(lines = linesWithDetails)
    }

    suspend fun generateNextVoucherNumber(): String = withContext(Dispatchers.IO) {
        val lastNumber = voucherDao.getLastCpVoucherNumber()
        if (lastNumber.isNullOrBlank()) {
            return@withContext "CP-000001"
        }
        try {
            val numPart = lastNumber.substringAfter("CP-").trim()
            val nextVal = numPart.toInt() + 1
            String.format(Locale.US, "CP-%06d", nextVal)
        } catch (e: Exception) {
            "CP-000001"
        }
    }

    suspend fun validateVoucherRules(
        voucherDate: Long,
        narration: String,
        lines: List<VoucherLine>
    ): Pair<Long, Long> = withContext(Dispatchers.IO) {
        if (voucherDate <= 0) {
            throw VoucherValidationException("A valid Voucher Date is required.")
        }
        if (lines.size < 2) {
            throw VoucherValidationException("A voucher must contain at least 2 lines (at least one Debit and one Credit).")
        }

        var totalDebit = 0L
        var totalCredit = 0L
        var hasDebit = false
        var hasCredit = false

        for ((index, line) in lines.withIndex()) {
            val lineNum = index + 1
            if (line.accountId <= 0) {
                throw VoucherValidationException("Line $lineNum: Please select an account.")
            }
            val account = accountDao.getAccountById(line.accountId)
                ?: throw VoucherValidationException("Line $lineNum: Referenced account does not exist.")

            if (account.level != 4 || !account.isActive) {
                throw VoucherValidationException(
                    "Line $lineNum: '${account.name}' is not a valid posting account. Only active Level 4 accounts can be used for voucher entries."
                )
            }

            if (line.debitCents < 0L || line.creditCents < 0L) {
                throw VoucherValidationException("Line $lineNum: Debit and Credit amounts cannot be negative.")
            }

            if (line.debitCents > 0L && line.creditCents > 0L) {
                throw VoucherValidationException("Line $lineNum: A voucher line cannot contain both Debit and Credit amounts.")
            }

            if (line.debitCents == 0L && line.creditCents == 0L) {
                throw VoucherValidationException("Line $lineNum: Line amount cannot be zero. Enter either a Debit or a Credit amount.")
            }

            if (line.debitCents > 0L) {
                totalDebit += line.debitCents
                hasDebit = true
            }
            if (line.creditCents > 0L) {
                totalCredit += line.creditCents
                hasCredit = true
            }
        }

        if (!hasDebit || !hasCredit) {
            throw VoucherValidationException("Voucher must have at least one debit line and one credit line.")
        }

        if (totalDebit <= 0L) {
            throw VoucherValidationException("Voucher total amount must be greater than zero.")
        }

        if (totalDebit != totalCredit) {
            val diff = totalDebit - totalCredit
            val diffText = CurrencyUtils.format(diff)
            throw VoucherValidationException(
                "Voucher is unbalanced! Total Debit (${CurrencyUtils.format(totalDebit)}) must exactly equal Total Credit (${CurrencyUtils.format(totalCredit)}). Difference: $diffText"
            )
        }

        Pair(totalDebit, totalCredit)
    }

    suspend fun createVoucher(voucher: Voucher): Result<Voucher> = withContext(Dispatchers.IO) {
        try {
            val (totalDebit, totalCredit) = validateVoucherRules(
                voucherDate = voucher.voucherDate,
                narration = voucher.narration,
                lines = voucher.lines
            )

            val voucherNumber = if (voucher.voucherNumber.isBlank()) {
                generateNextVoucherNumber()
            } else {
                voucher.voucherNumber.trim()
            }

            // Check duplicate voucher number
            val existing = voucherDao.getVoucherByNumber(voucherNumber)
            if (existing != null) {
                throw VoucherValidationException("Voucher number '$voucherNumber' already exists.")
            }

            val savedVoucher = database.withTransaction {
                val entity = VoucherEntity(
                    voucherNumber = voucherNumber,
                    voucherType = "CP",
                    voucherDate = voucher.voucherDate,
                    reference = voucher.reference.trim(),
                    narration = voucher.narration.trim(),
                    totalDebitCents = totalDebit,
                    totalCreditCents = totalCredit,
                    isPosted = false,
                    postedAt = null,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                val voucherId = voucherDao.insertVoucher(entity)

                val lineEntities = voucher.lines.map { line ->
                    VoucherLineEntity.fromDomain(line, voucherId = voucherId)
                }
                voucherDao.insertVoucherLines(lineEntities)

                voucherId
            }

            val reloaded = getVoucherById(savedVoucher)!!
            Result.success(reloaded)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateVoucher(voucher: Voucher): Result<Voucher> = withContext(Dispatchers.IO) {
        try {
            val existing = voucherDao.getVoucherById(voucher.id)
                ?: throw VoucherValidationException("Voucher not found.")

            if (existing.isPosted) {
                throw VoucherValidationException("Posted vouchers cannot be modified.")
            }

            val (totalDebit, totalCredit) = validateVoucherRules(
                voucherDate = voucher.voucherDate,
                narration = voucher.narration,
                lines = voucher.lines
            )

            database.withTransaction {
                val updatedEntity = existing.copy(
                    voucherDate = voucher.voucherDate,
                    reference = voucher.reference.trim(),
                    narration = voucher.narration.trim(),
                    totalDebitCents = totalDebit,
                    totalCreditCents = totalCredit,
                    updatedAt = System.currentTimeMillis()
                )
                voucherDao.updateVoucher(updatedEntity)
                voucherDao.deleteLinesForVoucher(voucher.id)

                val lineEntities = voucher.lines.map { line ->
                    VoucherLineEntity.fromDomain(line, voucherId = voucher.id)
                }
                voucherDao.insertVoucherLines(lineEntities)
            }

            val reloaded = getVoucherById(voucher.id)!!
            Result.success(reloaded)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ----------------------------------------------------
    // Accounting Posting Engine (Atomic SQL Transaction & Rollback)
    // ----------------------------------------------------

    suspend fun postVoucher(voucherId: Long): Result<Voucher> = withContext(Dispatchers.IO) {
        try {
            // Step 1: Load voucher
            val voucherEntity = voucherDao.getVoucherById(voucherId)
                ?: throw PostingException("Voucher with ID $voucherId does not exist.")

            // Step 3: Verify it has not already been posted
            if (voucherEntity.isPosted) {
                throw PostingException("Voucher '${voucherEntity.voucherNumber}' has already been posted.")
            }

            val lineEntities = voucherDao.getLinesForVoucher(voucherId)
            val lines = lineEntities.map { lineEntity ->
                val acc = accountDao.getAccountById(lineEntity.accountId)
                lineEntity.toDomain(acc?.code ?: "", acc?.name ?: "")
            }

            // Step 4..7: Validate all lines and recalculate totals on backend
            val (totalDebit, totalCredit) = validateVoucherRules(
                voucherDate = voucherEntity.voucherDate,
                narration = voucherEntity.narration,
                lines = lines
            )

            // Step 8..10: Atomic SQL transaction
            database.withTransaction {
                // Create ledger transaction entries
                val ledgerTransactions = lines.map { line ->
                    LedgerTransactionEntity(
                        date = voucherEntity.voucherDate,
                        voucherId = voucherEntity.id,
                        voucherNumber = voucherEntity.voucherNumber,
                        voucherType = voucherEntity.voucherType,
                        accountId = line.accountId,
                        description = line.description.ifBlank { voucherEntity.narration },
                        debitCents = line.debitCents,
                        creditCents = line.creditCents
                    )
                }
                ledgerDao.insertLedgerTransactions(ledgerTransactions)

                // Mark voucher as posted
                val updatedVoucher = voucherEntity.copy(
                    isPosted = true,
                    postedAt = System.currentTimeMillis(),
                    totalDebitCents = totalDebit,
                    totalCreditCents = totalCredit,
                    updatedAt = System.currentTimeMillis()
                )
                voucherDao.updateVoucher(updatedVoucher)
            }

            val postedVoucher = getVoucherById(voucherId)!!
            Result.success(postedVoucher)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ----------------------------------------------------
    // Ledger Transactions & Account Ledger
    // ----------------------------------------------------

    fun getAllLedgerTransactionsFlow(): Flow<List<LedgerTransaction>> {
        return ledgerDao.getAllTransactionsFlow().map { entities ->
            val accountsMap = accountDao.getAllAccounts().associateBy { it.id }
            entities.map { entity ->
                val acc = accountsMap[entity.accountId]
                entity.toDomain(
                    accountCode = acc?.code ?: "",
                    accountName = acc?.name ?: "",
                    accountType = acc?.let { AccountType.fromString(it.accountType) } ?: AccountType.ASSET
                )
            }
        }
    }

    suspend fun getAccountLedger(
        accountId: Long,
        startDate: Long? = null,
        endDate: Long? = null
    ): List<LedgerTransaction> = withContext(Dispatchers.IO) {
        val account = accountDao.getAccountById(accountId) ?: return@withContext emptyList()
        val accType = AccountType.fromString(account.accountType)
        val rawTransactions = ledgerDao.getTransactionsForAccount(accountId)

        var running = 0L
        val list = mutableListOf<LedgerTransaction>()

        for (tx in rawTransactions) {
            if (accType.normalBalance == NormalBalance.DEBIT) {
                running += (tx.debitCents - tx.creditCents)
            } else {
                running += (tx.creditCents - tx.debitCents)
            }

            val isInRange = (startDate == null || tx.date >= startDate) &&
                    (endDate == null || tx.date <= endDate)

            if (isInRange) {
                list.add(
                    tx.toDomain(
                        accountCode = account.code,
                        accountName = account.name,
                        accountType = accType,
                        runningBalance = running
                    )
                )
            }
        }

        list
    }

    // ----------------------------------------------------
    // Transaction Register
    // ----------------------------------------------------

    suspend fun getFilteredTransactions(
        query: String = "",
        accountId: Long? = null,
        voucherNumber: String? = null,
        startDate: Long? = null,
        endDate: Long? = null
    ): List<LedgerTransaction> = withContext(Dispatchers.IO) {
        val allTx = ledgerDao.getAllTransactions()
        val accountsMap = accountDao.getAllAccounts().associateBy { it.id }

        allTx.mapNotNull { entity ->
            val acc = accountsMap[entity.accountId]
            val accCode = acc?.code ?: ""
            val accName = acc?.name ?: ""
            val accType = acc?.let { AccountType.fromString(it.accountType) } ?: AccountType.ASSET

            val matchesAccount = accountId == null || entity.accountId == accountId
            val matchesVoucher = voucherNumber.isNullOrBlank() || entity.voucherNumber.contains(voucherNumber, ignoreCase = true)
            val matchesDate = (startDate == null || entity.date >= startDate) && (endDate == null || entity.date <= endDate)

            val q = query.trim()
            val matchesQuery = q.isEmpty() ||
                    entity.voucherNumber.contains(q, ignoreCase = true) ||
                    entity.description.contains(q, ignoreCase = true) ||
                    accCode.contains(q, ignoreCase = true) ||
                    accName.contains(q, ignoreCase = true)

            if (matchesAccount && matchesVoucher && matchesDate && matchesQuery) {
                entity.toDomain(
                    accountCode = accCode,
                    accountName = accName,
                    accountType = accType
                )
            } else {
                null
            }
        }
    }

    // ----------------------------------------------------
    // Trial Balance Report
    // ----------------------------------------------------

    suspend fun getTrialBalance(): TrialBalanceReport = withContext(Dispatchers.IO) {
        val summaries = ledgerDao.getAccountSummaries()
        val accountsMap = accountDao.getAllAccounts().associateBy { it.id }

        var totalDebit = 0L
        var totalCredit = 0L
        val rows = mutableListOf<TrialBalanceRow>()

        for (summary in summaries) {
            val acc = accountsMap[summary.accountId] ?: continue
            val accType = AccountType.fromString(acc.accountType)

            totalDebit += summary.totalDebit
            totalCredit += summary.totalCredit

            val netDebit: Long
            val netCredit: Long

            if (summary.totalDebit >= summary.totalCredit) {
                netDebit = summary.totalDebit - summary.totalCredit
                netCredit = 0L
            } else {
                netDebit = 0L
                netCredit = summary.totalCredit - summary.totalDebit
            }

            rows.add(
                TrialBalanceRow(
                    accountId = acc.id,
                    accountCode = acc.code,
                    accountName = acc.name,
                    accountType = accType,
                    totalDebitCents = summary.totalDebit,
                    totalCreditCents = summary.totalCredit,
                    netDebitBalanceCents = netDebit,
                    netCreditBalanceCents = netCredit
                )
            )
        }

        rows.sortBy { it.accountCode }

        val diff = totalDebit - totalCredit
        TrialBalanceReport(
            asOfDate = System.currentTimeMillis(),
            rows = rows,
            totalDebitCents = totalDebit,
            totalCreditCents = totalCredit,
            differenceCents = diff,
            isBalanced = diff == 0L
        )
    }

    // ----------------------------------------------------
    // Dashboard Summary
    // ----------------------------------------------------

    suspend fun getDashboardSummary(): DashboardSummary = withContext(Dispatchers.IO) {
        val totalAccounts = accountDao.getAccountsCount()
        val l1 = accountDao.countByLevel(1)
        val l2 = accountDao.countByLevel(2)
        val l3 = accountDao.countByLevel(3)
        val l4 = accountDao.countByLevel(4)

        val totalVouchers = voucherDao.getVouchersCount()
        val draftVouchers = voucherDao.getDraftCount()
        val postedVouchers = voucherDao.getPostedCount()

        val totalDebit = ledgerDao.getTotalDebitCents()
        val totalCredit = ledgerDao.getTotalCreditCents()
        val trialBalanceDiff = totalDebit - totalCredit

        val rawRecent = ledgerDao.getRecentTransactions(limit = 10)
        val accountsMap = accountDao.getAllAccounts().associateBy { it.id }
        val recentTransactions = rawRecent.map { tx ->
            val acc = accountsMap[tx.accountId]
            tx.toDomain(
                accountCode = acc?.code ?: "",
                accountName = acc?.name ?: "",
                accountType = acc?.let { AccountType.fromString(it.accountType) } ?: AccountType.ASSET
            )
        }

        DashboardSummary(
            totalAccounts = totalAccounts,
            level1Count = l1,
            level2Count = l2,
            level3Count = l3,
            level4Count = l4,
            totalVouchers = totalVouchers,
            draftVouchers = draftVouchers,
            postedVouchers = postedVouchers,
            totalDebitCents = totalDebit,
            totalCreditCents = totalCredit,
            trialBalanceDifferenceCents = trialBalanceDiff,
            isTrialBalanceBalanced = trialBalanceDiff == 0L,
            recentTransactions = recentTransactions
        )
    }
}
