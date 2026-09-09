package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.model.AccountType
import com.example.domain.model.LedgerTransaction

@Entity(
    tableName = "ledger_transactions",
    foreignKeys = [
        ForeignKey(
            entity = VoucherEntity::class,
            parentColumns = ["id"],
            childColumns = ["voucherId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["date"]),
        Index(value = ["voucherId"]),
        Index(value = ["voucherNumber"]),
        Index(value = ["accountId"])
    ]
)
data class LedgerTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long,
    val voucherId: Long,
    val voucherNumber: String,
    val voucherType: String = "CP",
    val accountId: Long,
    val description: String = "",
    val debitCents: Long = 0L,
    val creditCents: Long = 0L
) {
    fun toDomain(
        accountCode: String = "",
        accountName: String = "",
        accountType: AccountType = AccountType.ASSET,
        runningBalance: Long = 0L
    ): LedgerTransaction = LedgerTransaction(
        id = id,
        date = date,
        voucherId = voucherId,
        voucherNumber = voucherNumber,
        voucherType = voucherType,
        accountId = accountId,
        accountCode = accountCode,
        accountName = accountName,
        accountType = accountType,
        description = description,
        debitCents = debitCents,
        creditCents = creditCents,
        runningBalanceCents = runningBalance
    )
}
