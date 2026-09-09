package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.domain.model.Account
import com.example.domain.model.AccountType

@Entity(
    tableName = "accounts",
    indices = [
        Index(value = ["code"], unique = true),
        Index(value = ["parentId"]),
        Index(value = ["level"])
    ]
)
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val code: String,
    val name: String,
    val parentId: Long?,
    val level: Int,
    val accountType: String,
    val description: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Account = Account(
        id = id,
        code = code,
        name = name,
        parentId = parentId,
        level = level,
        accountType = AccountType.fromString(accountType),
        description = description,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromDomain(account: Account): AccountEntity = AccountEntity(
            id = account.id,
            code = account.code.trim(),
            name = account.name.trim(),
            parentId = account.parentId,
            level = account.level,
            accountType = account.accountType.name,
            description = account.description.trim(),
            isActive = account.isActive,
            createdAt = account.createdAt,
            updatedAt = account.updatedAt
        )
    }
}
