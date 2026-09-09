export enum AccountType {
  Asset = 1,
  Liability = 2,
  Equity = 3,
  Revenue = 4,
  Expense = 5
}

export const AccountTypeLabels: Record<AccountType, string> = {
  [AccountType.Asset]: 'Asset',
  [AccountType.Liability]: 'Liability',
  [AccountType.Equity]: 'Equity',
  [AccountType.Revenue]: 'Revenue',
  [AccountType.Expense]: 'Expense'
};

export interface AccountDto {
  id: number;
  code: string;
  name: string;
  level: number;
  parentId?: number | null;
  parentName?: string | null;
  accountType: AccountType;
  accountTypeName: string;
  description?: string | null;
  isActive: boolean;
  isPostingAccount: boolean;
}

export interface CreateAccountDto {
  code: string;
  name: string;
  level: number;
  parentId?: number | null;
  accountType: AccountType;
  description?: string;
}

export interface VoucherLineDto {
  id: number;
  accountId: number;
  accountCode: string;
  accountName: string;
  description?: string;
  debit: number;
  credit: number;
}

export interface VoucherDto {
  id: number;
  voucherNumber: string;
  voucherType: string;
  voucherDate: string;
  reference?: string;
  narration: string;
  totalDebit: number;
  totalCredit: number;
  difference: number;
  isBalanced: boolean;
  isPosted: boolean;
  postedAtUtc?: string;
  lines: VoucherLineDto[];
}

export interface CreateVoucherLineDto {
  accountId: number;
  description?: string;
  debit: number;
  credit: number;
}

export interface CreateVoucherDto {
  voucherDate: string;
  reference?: string;
  narration: string;
  postImmediately: boolean;
  lines: CreateVoucherLineDto[];
}

export interface LedgerTransactionDto {
  id: number;
  date: string;
  voucherId: number;
  voucherNumber: string;
  voucherType: string;
  accountId: number;
  accountCode: string;
  accountName: string;
  description: string;
  debit: number;
  credit: number;
  runningBalance: number;
}

export interface TrialBalanceRowDto {
  accountId: number;
  accountCode: string;
  accountName: string;
  accountType: AccountType;
  totalDebit: number;
  totalCredit: number;
  netDebitBalance: number;
  netCreditBalance: number;
}

export interface TrialBalanceDto {
  asOfDate: string;
  totalDebit: number;
  totalCredit: number;
  difference: number;
  isBalanced: boolean;
  rows: TrialBalanceRowDto[];
}

export interface DashboardSummaryDto {
  totalAccounts: number;
  totalPostingAccounts: number;
  totalVouchers: number;
  postedVouchers: number;
  draftVouchers: number;
  totalDebits: number;
  totalCredits: number;
  isTrialBalanceBalanced: boolean;
  recentVouchers: VoucherDto[];
}
