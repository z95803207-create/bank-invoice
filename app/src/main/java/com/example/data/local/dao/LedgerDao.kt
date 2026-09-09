package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.LedgerTransactionEntity
import kotlinx.coroutines.flow.Flow

data class AccountLedgerSummary(
    val accountId: Long,
    val totalDebit: Long,
    val totalCredit: Long
)

@Dao
interface LedgerDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertLedgerTransactions(transactions: List<LedgerTransactionEntity>)

    @Query("SELECT * FROM ledger_transactions ORDER BY date DESC, id DESC")
    fun getAllTransactionsFlow(): Flow<List<LedgerTransactionEntity>>

    @Query("SELECT * FROM ledger_transactions ORDER BY date DESC, id DESC")
    suspend fun getAllTransactions(): List<LedgerTransactionEntity>

    @Query("SELECT * FROM ledger_transactions WHERE accountId = :accountId ORDER BY date ASC, id ASC")
    suspend fun getTransactionsForAccount(accountId: Long): List<LedgerTransactionEntity>

    @Query("SELECT * FROM ledger_transactions WHERE voucherId = :voucherId ORDER BY id ASC")
    suspend fun getTransactionsForVoucher(voucherId: Long): List<LedgerTransactionEntity>

    @Query("SELECT * FROM ledger_transactions ORDER BY date DESC, id DESC LIMIT :limit")
    suspend fun getRecentTransactions(limit: Int = 10): List<LedgerTransactionEntity>

    @Query("SELECT COALESCE(SUM(debitCents), 0) FROM ledger_transactions")
    suspend fun getTotalDebitCents(): Long

    @Query("SELECT COALESCE(SUM(creditCents), 0) FROM ledger_transactions")
    suspend fun getTotalCreditCents(): Long

    @Query("SELECT accountId, COALESCE(SUM(debitCents), 0) as totalDebit, COALESCE(SUM(creditCents), 0) as totalCredit FROM ledger_transactions GROUP BY accountId")
    suspend fun getAccountSummaries(): List<AccountLedgerSummary>
}
