package com.example.domain.model

data class Account(
    val id: Long = 0,
    val code: String,
    val name: String,
    val parentId: Long?,
    val level: Int,
    val accountType: AccountType,
    val description: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    val isPostingAccount: Boolean
        get() = level == 4 && isActive

    val levelDescription: String
        get() = when (level) {
            1 -> "Level 1 · Major Head"
            2 -> "Level 2 · Sub-Head"
            3 -> "Level 3 · Control Group"
            4 -> "Level 4 · Posting Account"
            else -> "Level $level"
        }

    val displayLabel: String
        get() = "$code - $name"
}
