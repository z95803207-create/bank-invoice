package com.example.domain.model

data class LedgerTransaction(
    val id: Long = 0,
    val date: Long,
    val voucherId: Long,
    val voucherNumber: String,
    val voucherType: String = "CP",
    val accountId: Long,
    val accountCode: String = "",
    val accountName: String = "",
    val accountType: AccountType = AccountType.ASSET,
    val description: String = "",
    val debitCents: Long = 0L,
    val creditCents: Long = 0L,
    val runningBalanceCents: Long = 0L
) {
    val runningBalance: Long
        get() = runningBalanceCents

    val netAmountCents: Long
        get() = debitCents - creditCents
}
