package com.example.domain.model

data class VoucherLine(
    val id: Long = 0,
    val voucherId: Long = 0,
    val accountId: Long,
    val accountCode: String = "",
    val accountName: String = "",
    val description: String = "",
    val debitCents: Long = 0L,
    val creditCents: Long = 0L
) {
    val isDebit: Boolean get() = debitCents > 0L
    val isCredit: Boolean get() = creditCents > 0L
    val amountCents: Long get() = if (isDebit) debitCents else creditCents
}
