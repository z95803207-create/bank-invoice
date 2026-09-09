package com.example.domain.model

data class Voucher(
    val id: Long = 0,
    val voucherNumber: String,
    val voucherType: String = "CP",
    val voucherDate: Long,
    val reference: String = "",
    val narration: String = "",
    val isPosted: Boolean = false,
    val postedAt: Long? = null,
    val lines: List<VoucherLine> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val totalDebitCents: Long
        get() = lines.sumOf { it.debitCents }

    val totalCreditCents: Long
        get() = lines.sumOf { it.creditCents }

    val differenceCents: Long
        get() = totalDebitCents - totalCreditCents

    val isBalanced: Boolean
        get() = totalDebitCents > 0L && totalDebitCents == totalCreditCents
}
