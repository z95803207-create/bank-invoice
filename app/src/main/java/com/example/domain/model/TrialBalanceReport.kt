package com.example.domain.model

data class TrialBalanceRow(
    val accountId: Long,
    val accountCode: String,
    val accountName: String,
    val accountType: AccountType,
    val totalDebitCents: Long,
    val totalCreditCents: Long,
    val netDebitBalanceCents: Long,
    val netCreditBalanceCents: Long
)

data class TrialBalanceReport(
    val asOfDate: Long = System.currentTimeMillis(),
    val rows: List<TrialBalanceRow> = emptyList(),
    val totalDebitCents: Long = 0L,
    val totalCreditCents: Long = 0L,
    val differenceCents: Long = 0L,
    val isBalanced: Boolean = false
)
