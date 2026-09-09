using Accounting.Domain.Entities;
using Microsoft.EntityFrameworkCore;

namespace Accounting.Infrastructure.Data;

public static class DatabaseSeeder
{
    public static async Task SeedAsync(AccountingDbContext context)
    {
        if (await context.Accounts.AnyAsync())
        {
            return; // Already seeded
        }

        // --- LEVEL 1: Major Heads ---
        var l1Assets = new Account { Code = "1000", Name = "Assets", Level = 1, ParentId = null, AccountType = AccountType.Asset };
        var l1Liabilities = new Account { Code = "2000", Name = "Liabilities", Level = 1, ParentId = null, AccountType = AccountType.Liability };
        var l1Equity = new Account { Code = "3000", Name = "Equity", Level = 1, ParentId = null, AccountType = AccountType.Equity };
        var l1Revenue = new Account { Code = "4000", Name = "Revenue", Level = 1, ParentId = null, AccountType = AccountType.Revenue };
        var l1Expenses = new Account { Code = "5000", Name = "Expenses", Level = 1, ParentId = null, AccountType = AccountType.Expense };

        await context.Accounts.AddRangeAsync(l1Assets, l1Liabilities, l1Equity, l1Revenue, l1Expenses);
        await context.SaveChangesAsync();

        // --- LEVEL 2: Sub-heads ---
        var l2CurrentAssets = new Account { Code = "1100", Name = "Current Assets", Level = 2, ParentId = l1Assets.Id, AccountType = AccountType.Asset };
        var l2FixedAssets = new Account { Code = "1200", Name = "Fixed Assets", Level = 2, ParentId = l1Assets.Id, AccountType = AccountType.Asset };
        var l2CurrentLiab = new Account { Code = "2100", Name = "Current Liabilities", Level = 2, ParentId = l1Liabilities.Id, AccountType = AccountType.Liability };
        var l2Capital = new Account { Code = "3100", Name = "Capital & Reserves", Level = 2, ParentId = l1Equity.Id, AccountType = AccountType.Equity };
        var l2OperatingRev = new Account { Code = "4100", Name = "Operating Revenue", Level = 2, ParentId = l1Revenue.Id, AccountType = AccountType.Revenue };
        var l2AdminExp = new Account { Code = "5100", Name = "Administrative Expenses", Level = 2, ParentId = l1Expenses.Id, AccountType = AccountType.Expense };

        await context.Accounts.AddRangeAsync(l2CurrentAssets, l2FixedAssets, l2CurrentLiab, l2Capital, l2OperatingRev, l2AdminExp);
        await context.SaveChangesAsync();

        // --- LEVEL 3: Control Groups ---
        var l3CashBank = new Account { Code = "1110", Name = "Cash and Bank Balances", Level = 3, ParentId = l2CurrentAssets.Id, AccountType = AccountType.Asset };
        var l3Receivables = new Account { Code = "1120", Name = "Accounts Receivable", Level = 3, ParentId = l2CurrentAssets.Id, AccountType = AccountType.Asset };
        var l3OfficeEquipGroup = new Account { Code = "1210", Name = "Office Equipment", Level = 3, ParentId = l2FixedAssets.Id, AccountType = AccountType.Asset };
        var l3Payables = new Account { Code = "2110", Name = "Accounts Payable", Level = 3, ParentId = l2CurrentLiab.Id, AccountType = AccountType.Liability };
        var l3OwnersEquity = new Account { Code = "3110", Name = "Owner's Equity", Level = 3, ParentId = l2Capital.Id, AccountType = AccountType.Equity };
        var l3Sales = new Account { Code = "4110", Name = "Sales Income", Level = 3, ParentId = l2OperatingRev.Id, AccountType = AccountType.Revenue };
        var l3OfficeOps = new Account { Code = "5110", Name = "Office Operations", Level = 3, ParentId = l2AdminExp.Id, AccountType = AccountType.Expense };
        var l3Utilities = new Account { Code = "5120", Name = "Utility Expenses", Level = 3, ParentId = l2AdminExp.Id, AccountType = AccountType.Expense };

        await context.Accounts.AddRangeAsync(l3CashBank, l3Receivables, l3OfficeEquipGroup, l3Payables, l3OwnersEquity, l3Sales, l3OfficeOps, l3Utilities);
        await context.SaveChangesAsync();

        // --- LEVEL 4: Posting Accounts ---
        var l4CashInHand = new Account { Code = "111001", Name = "Cash in Hand", Level = 4, ParentId = l3CashBank.Id, AccountType = AccountType.Asset };
        var l4MainBank = new Account { Code = "111002", Name = "Main Commercial Bank", Level = 4, ParentId = l3CashBank.Id, AccountType = AccountType.Asset };
        var l4Computers = new Account { Code = "121001", Name = "Computers & Laptops", Level = 4, ParentId = l3OfficeEquipGroup.Id, AccountType = AccountType.Asset };
        var l4TradeCreditors = new Account { Code = "211001", Name = "Trade Creditors Control", Level = 4, ParentId = l3Payables.Id, AccountType = AccountType.Liability };
        var l4ShareCapital = new Account { Code = "311001", Name = "Share Capital", Level = 4, ParentId = l3OwnersEquity.Id, AccountType = AccountType.Equity };
        var l4SoftwareConsulting = new Account { Code = "411001", Name = "Software Consulting Revenue", Level = 4, ParentId = l3Sales.Id, AccountType = AccountType.Revenue };
        var l4RentExpense = new Account { Code = "511001", Name = "Office Rent Expense", Level = 4, ParentId = l3OfficeOps.Id, AccountType = AccountType.Expense };
        var l4Supplies = new Account { Code = "511002", Name = "Office Stationery & Supplies", Level = 4, ParentId = l3OfficeOps.Id, AccountType = AccountType.Expense };
        var l4Electricity = new Account { Code = "512001", Name = "Electricity & Power", Level = 4, ParentId = l3Utilities.Id, AccountType = AccountType.Expense };
        var l4Internet = new Account { Code = "512002", Name = "Internet & Communication", Level = 4, ParentId = l3Utilities.Id, AccountType = AccountType.Expense };

        await context.Accounts.AddRangeAsync(
            l4CashInHand, l4MainBank, l4Computers, l4TradeCreditors,
            l4ShareCapital, l4SoftwareConsulting, l4RentExpense, l4Supplies,
            l4Electricity, l4Internet
        );
        await context.SaveChangesAsync();

        // --- Seed Sample Posted CP Voucher (Cash Payment) ---
        var sampleDate = DateTime.UtcNow.AddDays(-2);
        var voucher = new Voucher
        {
            VoucherNumber = "CP-000001",
            VoucherType = "CP",
            VoucherDate = sampleDate,
            Reference = "INV-2026-001",
            Narration = "Payment of office rent and utility charges via cash",
            TotalDebit = 5000.00m,
            TotalCredit = 5000.00m,
            IsPosted = true,
            PostedAtUtc = sampleDate.AddMinutes(5),
            Lines = new List<VoucherLine>
            {
                new() { AccountId = l4RentExpense.Id, Description = "Office rent for current month", Debit = 3500.00m, Credit = 0m },
                new() { AccountId = l4Electricity.Id, Description = "Electricity utility bill", Debit = 1500.00m, Credit = 0m },
                new() { AccountId = l4CashInHand.Id, Description = "Cash disbursed from drawer", Debit = 0m, Credit = 5000.00m }
            }
        };

        await context.Vouchers.AddAsync(voucher);
        await context.SaveChangesAsync();

        // Ledger Transactions for CP-000001
        var ledgerEntries = new List<LedgerTransaction>
        {
            new() { Date = sampleDate, VoucherId = voucher.Id, VoucherNumber = voucher.VoucherNumber, VoucherType = "CP", AccountId = l4RentExpense.Id, Description = "Office rent for current month", Debit = 3500.00m, Credit = 0m },
            new() { Date = sampleDate, VoucherId = voucher.Id, VoucherNumber = voucher.VoucherNumber, VoucherType = "CP", AccountId = l4Electricity.Id, Description = "Electricity utility bill", Debit = 1500.00m, Credit = 0m },
            new() { Date = sampleDate, VoucherId = voucher.Id, VoucherNumber = voucher.VoucherNumber, VoucherType = "CP", AccountId = l4CashInHand.Id, Description = "Cash disbursed from drawer", Debit = 0m, Credit = 5000.00m }
        };

        await context.LedgerTransactions.AddRangeAsync(ledgerEntries);
        await context.SaveChangesAsync();
    }
}
