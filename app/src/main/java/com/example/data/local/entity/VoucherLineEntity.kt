package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.model.VoucherLine

@Entity(
    tableName = "voucher_lines",
    foreignKeys = [
        ForeignKey(
            entity = VoucherEntity::class,
            parentColumns = ["id"],
            childColumns = ["voucherId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["voucherId"]),
        Index(value = ["accountId"])
    ]
)
data class VoucherLineEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val voucherId: Long,
    val accountId: Long,
    val description: String = "",
    val debitCents: Long = 0L,
    val creditCents: Long = 0L
) {
    fun toDomain(accountCode: String = "", accountName: String = ""): VoucherLine = VoucherLine(
        id = id,
        voucherId = voucherId,
        accountId = accountId,
        accountCode = accountCode,
        accountName = accountName,
        description = description,
        debitCents = debitCents,
        creditCents = creditCents
    )

    companion object {
        fun fromDomain(line: VoucherLine, voucherId: Long = line.voucherId): VoucherLineEntity = VoucherLineEntity(
            id = line.id,
            voucherId = voucherId,
            accountId = line.accountId,
            description = line.description.trim(),
            debitCents = line.debitCents,
            creditCents = line.creditCents
        )
    }
}
