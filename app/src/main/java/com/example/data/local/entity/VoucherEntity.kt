package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.model.Voucher

@Entity(
    tableName = "vouchers",
    indices = [
        Index(value = ["voucherNumber"], unique = true),
        Index(value = ["voucherDate"]),
        Index(value = ["isPosted"])
    ]
)
data class VoucherEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val voucherNumber: String,
    val voucherType: String = "CP",
    val voucherDate: Long,
    val reference: String = "",
    val narration: String = "",
    val totalDebitCents: Long = 0L,
    val totalCreditCents: Long = 0L,
    val isPosted: Boolean = false,
    val postedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Voucher = Voucher(
        id = id,
        voucherNumber = voucherNumber,
        voucherType = voucherType,
        voucherDate = voucherDate,
        reference = reference,
        narration = narration,
        isPosted = isPosted,
        postedAt = postedAt,
        lines = emptyList(),
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(voucher: Voucher): VoucherEntity = VoucherEntity(
            id = voucher.id,
            voucherNumber = voucher.voucherNumber,
            voucherType = voucher.voucherType,
            voucherDate = voucher.voucherDate,
            reference = voucher.reference.trim(),
            narration = voucher.narration.trim(),
            totalDebitCents = voucher.totalDebitCents,
            totalCreditCents = voucher.totalCreditCents,
            isPosted = voucher.isPosted,
            postedAt = voucher.postedAt,
            createdAt = voucher.createdAt,
            updatedAt = voucher.updatedAt
        )
    }
}
