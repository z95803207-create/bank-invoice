package com.example.domain.model

data class DashboardSummary(
    val totalAccounts: Int = 0,
    val level1Count: Int = 0,
    val level2Count: Int = 0,
    val level3Count: Int = 0,
    val level4Count: Int = 0,
    val totalVouchers: Int = 0,
    val draftVouchers: Int = 0,
    val postedVouchers: Int = 0,
    val totalDebitCents: Long = 0L,
    val totalCreditCents: Long = 0L,
    val trialBalanceDifferenceCents: Long = 0L,
    val isTrialBalanceBalanced: Boolean = true,
    val recentTransactions: List<LedgerTransaction> = emptyList()
)
