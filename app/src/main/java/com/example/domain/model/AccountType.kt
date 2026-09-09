package com.example.domain.model

enum class NormalBalance {
    DEBIT,
    CREDIT
}

enum class AccountType(
    val displayName: String,
    val normalBalance: NormalBalance,
    val defaultPrefix: String
) {
    ASSET("Asset", NormalBalance.DEBIT, "1"),
    LIABILITY("Liability", NormalBalance.CREDIT, "2"),
    EQUITY("Equity", NormalBalance.CREDIT, "3"),
    REVENUE("Revenue", NormalBalance.CREDIT, "4"),
    EXPENSE("Expense", NormalBalance.DEBIT, "5");

    companion object {
        fun fromString(value: String): AccountType {
            return entries.firstOrNull { 
                it.name.equals(value, ignoreCase = true) || it.displayName.equals(value, ignoreCase = true) 
            } ?: ASSET
        }
    }
}
