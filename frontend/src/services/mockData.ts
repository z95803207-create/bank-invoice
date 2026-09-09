import {
  AccountDto,
  AccountType,
  DashboardSummaryDto,
  LedgerTransactionDto,
  TrialBalanceDto,
  VoucherDto
} from '../types/accounting';

export const initialAccounts: AccountDto[] = [
  // Assets (Level 1)
  { id: 1, code: '1000', name: 'Assets', level: 1, parentId: null, accountType: AccountType.Asset, accountTypeName: 'Asset', isActive: true, isPostingAccount: false },
  { id: 2, code: '1100', name: 'Current Assets', level: 2, parentId: 1, parentName: 'Assets', accountType: AccountType.Asset, accountTypeName: 'Asset', isActive: true, isPostingAccount: false },
  { id: 3, code: '1110', name: 'Cash and Bank Balances', level: 3, parentId: 2, parentName: 'Current Assets', accountType: AccountType.Asset, accountTypeName: 'Asset', isActive: true, isPostingAccount: false },
  { id: 4, code: '1111', name: 'Petty Cash', level: 4, parentId: 3, parentName: 'Cash and Bank Balances', accountType: AccountType.Asset, accountTypeName: 'Asset', isActive: true, isPostingAccount: true },
  { id: 5, code: '1112', name: 'Main Cash Operating Account', level: 4, parentId: 3, parentName: 'Cash and Bank Balances', accountType: AccountType.Asset, accountTypeName: 'Asset', isActive: true, isPostingAccount: true },
  { id: 6, code: '1113', name: 'HBL Operational Bank', level: 4, parentId: 3, parentName: 'Cash and Bank Balances', accountType: AccountType.Asset, accountTypeName: 'Asset', isActive: true, isPostingAccount: true },

  // Liabilities (Level 1)
  { id: 7, code: '2000', name: 'Liabilities', level: 1, parentId: null, accountType: AccountType.Liability, accountTypeName: 'Liability', isActive: true, isPostingAccount: false },
  { id: 8, code: '2100', name: 'Current Liabilities', level: 2, parentId: 7, parentName: 'Liabilities', accountType: AccountType.Liability, accountTypeName: 'Liability', isActive: true, isPostingAccount: false },
  { id: 9, code: '2110', name: 'Trade Payables & Creditors', level: 3, parentId: 8, parentName: 'Current Liabilities', accountType: AccountType.Liability, accountTypeName: 'Liability', isActive: true, isPostingAccount: false },
  { id: 10, code: '2111', name: 'Accounts Payable - Vendors', level: 4, parentId: 9, parentName: 'Trade Payables & Creditors', accountType: AccountType.Liability, accountTypeName: 'Liability', isActive: true, isPostingAccount: true },

  // Equity (Level 1)
  { id: 11, code: '3000', name: 'Equity', level: 1, parentId: null, accountType: AccountType.Equity, accountTypeName: 'Equity', isActive: true, isPostingAccount: false },
  { id: 12, code: '3100', name: 'Shareholders Capital', level: 2, parentId: 11, parentName: 'Equity', accountType: AccountType.Equity, accountTypeName: 'Equity', isActive: true, isPostingAccount: false },
  { id: 13, code: '3110', name: 'Paid Up Capital', level: 3, parentId: 12, parentName: 'Shareholders Capital', accountType: AccountType.Equity, accountTypeName: 'Equity', isActive: true, isPostingAccount: false },
  { id: 14, code: '3111', name: 'Ordinary Share Capital', level: 4, parentId: 13, parentName: 'Paid Up Capital', accountType: AccountType.Equity, accountTypeName: 'Equity', isActive: true, isPostingAccount: true },

  // Revenue (Level 1)
  { id: 15, code: '4000', name: 'Revenue', level: 1, parentId: null, accountType: AccountType.Revenue, accountTypeName: 'Revenue', isActive: true, isPostingAccount: false },
  { id: 16, code: '4100', name: 'Operating Income', level: 2, parentId: 15, parentName: 'Revenue', accountType: AccountType.Revenue, accountTypeName: 'Revenue', isActive: true, isPostingAccount: false },
  { id: 17, code: '4110', name: 'Sales Revenue', level: 3, parentId: 16, parentName: 'Operating Income', accountType: AccountType.Revenue, accountTypeName: 'Revenue', isActive: true, isPostingAccount: false },
  { id: 18, code: '4111', name: 'Consulting & Services Revenue', level: 4, parentId: 17, parentName: 'Sales Revenue', accountType: AccountType.Revenue, accountTypeName: 'Revenue', isActive: true, isPostingAccount: true },

  // Expenses (Level 1)
  { id: 19, code: '5000', name: 'Expenses', level: 1, parentId: null, accountType: AccountType.Expense, accountTypeName: 'Expense', isActive: true, isPostingAccount: false },
  { id: 20, code: '5100', name: 'Administrative Expenses', level: 2, parentId: 19, parentName: 'Expenses', accountType: AccountType.Expense, accountTypeName: 'Expense', isActive: true, isPostingAccount: false },
  { id: 21, code: '5110', name: 'Office Operational Expenses', level: 3, parentId: 20, parentName: 'Administrative Expenses', accountType: AccountType.Expense, accountTypeName: 'Expense', isActive: true, isPostingAccount: false },
  { id: 22, code: '5111', name: 'Office Supplies & Stationery', level: 4, parentId: 21, parentName: 'Office Operational Expenses', accountType: AccountType.Expense, accountTypeName: 'Expense', isActive: true, isPostingAccount: true },
  { id: 23, code: '5112', name: 'Utilities & Internet Expense', level: 4, parentId: 21, parentName: 'Office Operational Expenses', accountType: AccountType.Expense, accountTypeName: 'Expense', isActive: true, isPostingAccount: true },
  { id: 24, code: '5113', name: 'Office Refreshments & Pantry', level: 4, parentId: 21, parentName: 'Office Operational Expenses', accountType: AccountType.Expense, accountTypeName: 'Expense', isActive: true, isPostingAccount: true },
];

export const initialVouchers: VoucherDto[] = [
  {
    id: 1,
    voucherNumber: 'CP-000001',
    voucherType: 'CP',
    voucherDate: '2025-01-15T10:00:00Z',
    reference: 'REF-OFFICE-01',
    narration: 'Office stationery and printer cartridges purchase via Petty Cash',
    totalDebit: 5000.0,
    totalCredit: 5000.0,
    difference: 0,
    isBalanced: true,
    isPosted: true,
    postedAtUtc: '2025-01-15T10:05:00Z',
    lines: [
      { id: 1, accountId: 22, accountCode: '5111', accountName: 'Office Supplies & Stationery', description: 'Monthly stationery supplies', debit: 5000.0, credit: 0 },
      { id: 2, accountId: 4, accountCode: '1111', accountName: 'Petty Cash', description: 'Cash disbursed for stationery', debit: 0, credit: 5000.0 }
    ]
  }
];

export const initialTransactions: LedgerTransactionDto[] = [
  {
    id: 1,
    date: '2025-01-15T10:00:00Z',
    voucherId: 1,
    voucherNumber: 'CP-000001',
    voucherType: 'CP',
    accountId: 22,
    accountCode: '5111',
    accountName: 'Office Supplies & Stationery',
    description: 'Monthly stationery supplies',
    debit: 5000.0,
    credit: 0,
    runningBalance: 5000.0
  },
  {
    id: 2,
    date: '2025-01-15T10:00:00Z',
    voucherId: 1,
    voucherNumber: 'CP-000001',
    voucherType: 'CP',
    accountId: 4,
    accountCode: '1111',
    accountName: 'Petty Cash',
    description: 'Cash disbursed for stationery',
    debit: 0,
    credit: 5000.0,
    runningBalance: -5000.0
  }
];
