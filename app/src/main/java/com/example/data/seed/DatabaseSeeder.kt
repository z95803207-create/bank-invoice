package com.example.data.seed

import com.example.domain.model.Account
import com.example.domain.model.AccountType
import com.example.domain.model.CurrencyUtils
import com.example.domain.model.Voucher
import com.example.domain.model.VoucherLine
import com.example.repository.AccountingRepository

object DatabaseSeeder {
    suspend fun seedInitialData(repository: AccountingRepository) {
        val accounts = repository.getAllAccounts()
        if (accounts.isNotEmpty()) {
            return
        }

        // ==========================================
        // 1. LEVEL 1: MAJOR HEADS (No Parent)
        // ==========================================
        val l1Assets = repository.createAccount(
            Account(code = "1000", name = "Assets", parentId = null, level = 1, accountType = AccountType.ASSET, description = "Economic resources owned by the business")
        ).getOrThrow()

        val l1Liabilities = repository.createAccount(
            Account(code = "2000", name = "Liabilities", parentId = null, level = 1, accountType = AccountType.LIABILITY, description = "Present obligations of the enterprise")
        ).getOrThrow()

        val l1Equity = repository.createAccount(
            Account(code = "3000", name = "Equity", parentId = null, level = 1, accountType = AccountType.EQUITY, description = "Residual interest in the assets of the enterprise")
        ).getOrThrow()

        val l1Revenue = repository.createAccount(
            Account(code = "4000", name = "Revenue", parentId = null, level = 1, accountType = AccountType.REVENUE, description = "Gross inflow of economic benefits")
        ).getOrThrow()

        val l1Expenses = repository.createAccount(
            Account(code = "5000", name = "Expenses", parentId = null, level = 1, accountType = AccountType.EXPENSE, description = "Decreases in economic benefits during the accounting period")
        ).getOrThrow()

        // ==========================================
        // 2. LEVEL 2: SUB-HEADS (Parent: Level 1)
        // ==========================================
        val l2CurrentAssets = repository.createAccount(
            Account(code = "1100", name = "Current Assets", parentId = l1Assets.id, level = 2, accountType = AccountType.ASSET, description = "Short-term operating assets")
        ).getOrThrow()

        val l2NonCurrentAssets = repository.createAccount(
            Account(code = "1200", name = "Non-Current Assets", parentId = l1Assets.id, level = 2, accountType = AccountType.ASSET, description = "Long-term fixed equipment and property")
        ).getOrThrow()

        val l2CurrentLiabilities = repository.createAccount(
            Account(code = "2100", name = "Current Liabilities", parentId = l1Liabilities.id, level = 2, accountType = AccountType.LIABILITY, description = "Short-term debts and payables")
        ).getOrThrow()

        val l2OwnersEquity = repository.createAccount(
            Account(code = "3100", name = "Owner's Equity", parentId = l1Equity.id, level = 2, accountType = AccountType.EQUITY, description = "Contributed capital and reserves")
        ).getOrThrow()

        val l2OperatingRevenue = repository.createAccount(
            Account(code = "4100", name = "Operating Revenue", parentId = l1Revenue.id, level = 2, accountType = AccountType.REVENUE, description = "Core service and sales income")
        ).getOrThrow()

        val l2OperatingExpenses = repository.createAccount(
            Account(code = "5100", name = "Operating Expenses", parentId = l1Expenses.id, level = 2, accountType = AccountType.EXPENSE, description = "Day-to-day business operational expenses")
        ).getOrThrow()

        // ==========================================
        // 3. LEVEL 3: CONTROL GROUPS (Parent: Level 2)
        // ==========================================
        val l3CashAndBank = repository.createAccount(
            Account(code = "1110", name = "Cash & Bank", parentId = l2CurrentAssets.id, level = 3, accountType = AccountType.ASSET, description = "Liquid cash funds and bank holdings")
        ).getOrThrow()

        val l3Receivables = repository.createAccount(
            Account(code = "1120", name = "Accounts Receivable", parentId = l2CurrentAssets.id, level = 3, accountType = AccountType.ASSET, description = "Customer balances due")
        ).getOrThrow()

        val l3OfficeEquipment = repository.createAccount(
            Account(code = "1210", name = "Office Equipment", parentId = l2NonCurrentAssets.id, level = 3, accountType = AccountType.ASSET, description = "IT hardware and office machinery")
        ).getOrThrow()

        val l3Payables = repository.createAccount(
            Account(code = "2110", name = "Accounts Payable", parentId = l2CurrentLiabilities.id, level = 3, accountType = AccountType.LIABILITY, description = "Trade creditors and short-term payables")
        ).getOrThrow()

        val l3Capital = repository.createAccount(
            Account(code = "3110", name = "Capital Accounts", parentId = l2OwnersEquity.id, level = 3, accountType = AccountType.EQUITY, description = "Partner and shareholder capital")
        ).getOrThrow()

        val l3ServiceRevenue = repository.createAccount(
            Account(code = "4110", name = "Professional Services", parentId = l2OperatingRevenue.id, level = 3, accountType = AccountType.REVENUE, description = "Professional client fees")
        ).getOrThrow()

        val l3AdminExpenses = repository.createAccount(
            Account(code = "5110", name = "Administrative Expenses", parentId = l2OperatingExpenses.id, level = 3, accountType = AccountType.EXPENSE, description = "Office and general admin expenditure")
        ).getOrThrow()

        val l3UtilityExpenses = repository.createAccount(
            Account(code = "5120", name = "Utilities & Facility", parentId = l2OperatingExpenses.id, level = 3, accountType = AccountType.EXPENSE, description = "Power, internet, water and lease costs")
        ).getOrThrow()

        // ==========================================
        // 4. LEVEL 4: POSTING ACCOUNTS (Parent: Level 3)
        // ==========================================
        val cashInHand = repository.createAccount(
            Account(code = "111001", name = "Cash in Hand", parentId = l3CashAndBank.id, level = 4, accountType = AccountType.ASSET, description = "Main physical cash register")
        ).getOrThrow()

        val pettyCash = repository.createAccount(
            Account(code = "111002", name = "Petty Cash", parentId = l3CashAndBank.id, level = 4, accountType = AccountType.ASSET, description = "Imprest petty cash float")
        ).getOrThrow()

        val bankAccount = repository.createAccount(
            Account(code = "111003", name = "Main Bank Account", parentId = l3CashAndBank.id, level = 4, accountType = AccountType.ASSET, description = "Operating commercial bank account")
        ).getOrThrow()

        val tradeDebtors = repository.createAccount(
            Account(code = "112001", name = "Trade Debtors", parentId = l3Receivables.id, level = 4, accountType = AccountType.ASSET, description = "Accounts receivable trade customers")
        ).getOrThrow()

        val officeComputers = repository.createAccount(
            Account(code = "121001", name = "Computers & IT Hardware", parentId = l3OfficeEquipment.id, level = 4, accountType = AccountType.ASSET, description = "Laptops and desktop workstations")
        ).getOrThrow()

        val tradeCreditors = repository.createAccount(
            Account(code = "211001", name = "Trade Creditors", parentId = l3Payables.id, level = 4, accountType = AccountType.LIABILITY, description = "Commercial suppliers payables")
        ).getOrThrow()

        val ownerCapital = repository.createAccount(
            Account(code = "311001", name = "Owner Capital Contribution", parentId = l3Capital.id, level = 4, accountType = AccountType.EQUITY, description = "Initial equity injection")
        ).getOrThrow()

        val consultingRevenue = repository.createAccount(
            Account(code = "411001", name = "Technical Consulting Revenue", parentId = l3ServiceRevenue.id, level = 4, accountType = AccountType.REVENUE, description = "Technical consultancy billings")
        ).getOrThrow()

        val officeExpense = repository.createAccount(
            Account(code = "511001", name = "Office Expense", parentId = l3AdminExpenses.id, level = 4, accountType = AccountType.EXPENSE, description = "Office supplies, stationery and printer costs")
        ).getOrThrow()

        val travelExpense = repository.createAccount(
            Account(code = "511002", name = "Travel & Local Transport", parentId = l3AdminExpenses.id, level = 4, accountType = AccountType.EXPENSE, description = "Local client visits and taxi receipts")
        ).getOrThrow()

        val electricityExpense = repository.createAccount(
            Account(code = "512001", name = "Electricity & Power", parentId = l3UtilityExpenses.id, level = 4, accountType = AccountType.EXPENSE, description = "Monthly office electricity billing")
        ).getOrThrow()

        val internetExpense = repository.createAccount(
            Account(code = "512002", name = "Internet & Communication", parentId = l3UtilityExpenses.id, level = 4, accountType = AccountType.EXPENSE, description = "High-speed broadband and telephony")
        ).getOrThrow()

        val rentExpense = repository.createAccount(
            Account(code = "512003", name = "Office Rent Expense", parentId = l3UtilityExpenses.id, level = 4, accountType = AccountType.EXPENSE, description = "Commercial premises rent")
        ).getOrThrow()

        // ==========================================
        // 5. SAMPLE CP VOUCHER (CP-000001)
        // ==========================================
        val initialVoucher = Voucher(
            voucherNumber = "CP-000001",
            voucherType = "CP",
            voucherDate = System.currentTimeMillis() - (86400000L * 2), // 2 days ago
            reference = "RCPT-2026-001",
            narration = "Payment for Office Supplies and Stationery",
            lines = listOf(
                VoucherLine(
                    accountId = officeExpense.id,
                    description = "Office Stationery and Supplies",
                    debitCents = CurrencyUtils.doubleToCents(5000.0), // 5,000.00
                    creditCents = 0L
                ),
                VoucherLine(
                    accountId = cashInHand.id,
                    description = "Cash Disbursed from Main Cash",
                    debitCents = 0L,
                    creditCents = CurrencyUtils.doubleToCents(5000.0) // 5,000.00
                )
            )
        )

        val createdVoucher = repository.createVoucher(initialVoucher).getOrThrow()
        repository.postVoucher(createdVoucher.id).getOrThrow()
    }
}
