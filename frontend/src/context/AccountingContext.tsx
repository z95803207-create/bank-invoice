import React, { createContext, useContext, useState, useEffect } from 'react';
import {
  AccountDto,
  CreateAccountDto,
  CreateVoucherDto,
  DashboardSummaryDto,
  LedgerTransactionDto,
  TrialBalanceDto,
  VoucherDto
} from '../types/accounting';
import { accountingApi } from '../services/api';
import { initialAccounts, initialTransactions, initialVouchers } from '../services/mockData';

interface AccountingContextType {
  accounts: AccountDto[];
  postingAccounts: AccountDto[];
  vouchers: VoucherDto[];
  transactions: LedgerTransactionDto[];
  trialBalance: TrialBalanceDto | null;
  dashboardSummary: DashboardSummaryDto | null;
  isLoading: boolean;
  isBackendConnected: boolean;
  notification: string | null;
  errorMessage: string | null;
  setNotification: (msg: string | null) => void;
  setErrorMessage: (err: string | null) => void;
  refreshAll: () => Promise<void>;
  createAccount: (dto: CreateAccountDto) => Promise<boolean>;
  createVoucher: (dto: CreateVoucherDto) => Promise<boolean>;
  postVoucher: (voucherId: number) => Promise<boolean>;
}

const AccountingContext = createContext<AccountingContextType | undefined>(undefined);

export const AccountingProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [accounts, setAccounts] = useState<AccountDto[]>(initialAccounts);
  const [vouchers, setVouchers] = useState<VoucherDto[]>(initialVouchers);
  const [transactions, setTransactions] = useState<LedgerTransactionDto[]>(initialTransactions);
  const [trialBalance, setTrialBalance] = useState<TrialBalanceDto | null>(null);
  const [dashboardSummary, setDashboardSummary] = useState<DashboardSummaryDto | null>(null);
  const [isLoading, setIsLoading] = useState<boolean>(false);
  const [isBackendConnected, setIsBackendConnected] = useState<boolean>(false);
  const [notification, setNotification] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const postingAccounts = accounts.filter(a => a.level === 4 && a.isActive);

  // Helper to re-calculate local trial balance & summary if offline/mock
  const recalculateLocalState = (accs: AccountDto[], vchs: VoucherDto[], txs: LedgerTransactionDto[]) => {
    let totalDebit = 0;
    let totalCredit = 0;
    const rows = accs.filter(a => a.level === 4).map(acc => {
      const accTxs = txs.filter(t => t.accountId === acc.id);
      const deb = accTxs.reduce((sum, t) => sum + t.debit, 0);
      const cred = accTxs.reduce((sum, t) => sum + t.credit, 0);
      totalDebit += deb;
      totalCredit += cred;
      return {
        accountId: acc.id,
        accountCode: acc.code,
        accountName: acc.name,
        accountType: acc.accountType,
        totalDebit: deb,
        totalCredit: cred,
        netDebitBalance: deb > cred ? deb - cred : 0,
        netCreditBalance: cred > deb ? cred - deb : 0
      };
    });

    const tb: TrialBalanceDto = {
      asOfDate: new Date().toISOString(),
      totalDebit,
      totalCredit,
      difference: Math.abs(totalDebit - totalCredit),
      isBalanced: totalDebit === totalCredit,
      rows
    };
    setTrialBalance(tb);

    const postedCount = vchs.filter(v => v.isPosted).length;
    setDashboardSummary({
      totalAccounts: accs.length,
      totalPostingAccounts: accs.filter(a => a.level === 4).length,
      totalVouchers: vchs.length,
      postedVouchers: postedCount,
      draftVouchers: vchs.length - postedCount,
      totalDebits: totalDebit,
      totalCredits: totalCredit,
      isTrialBalanceBalanced: totalDebit === totalCredit,
      recentVouchers: vchs.slice(-5).reverse()
    });
  };

  const refreshAll = async () => {
    setIsLoading(true);
    try {
      const [accs, vchs, txs, tb, summary] = await Promise.all([
        accountingApi.getAccounts(),
        accountingApi.getVouchers(),
        accountingApi.getAllTransactions(100),
        accountingApi.getTrialBalance(),
        accountingApi.getDashboardSummary()
      ]);

      setAccounts(accs);
      setVouchers(vchs);
      setTransactions(txs);
      setTrialBalance(tb);
      setDashboardSummary(summary);
      setIsBackendConnected(true);
    } catch (err: any) {
      // Backend not running, use local state gracefully
      setIsBackendConnected(false);
      recalculateLocalState(accounts, vouchers, transactions);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    refreshAll();
  }, []);

  const createAccount = async (dto: CreateAccountDto): Promise<boolean> => {
    setIsLoading(true);
    try {
      if (isBackendConnected) {
        await accountingApi.createAccount(dto);
        await refreshAll();
        setNotification(`Account ${dto.code} - ${dto.name} created successfully!`);
        return true;
      } else {
        // Local state creation with hierarchy rule checks
        if (accounts.some(a => a.code === dto.code)) {
          throw new Error(`Account with code ${dto.code} already exists.`);
        }
        if (dto.level > 1 && !dto.parentId) {
          throw new Error(`Level ${dto.level} account must have a parent.`);
        }
        const parent = dto.parentId ? accounts.find(a => a.id === dto.parentId) : null;
        if (dto.level > 1 && (!parent || parent.level !== dto.level - 1)) {
          throw new Error(`Parent must be exactly Level ${dto.level - 1}.`);
        }

        const newAccount: AccountDto = {
          id: Date.now(),
          code: dto.code,
          name: dto.name,
          level: dto.level,
          parentId: dto.parentId,
          parentName: parent?.name,
          accountType: dto.accountType,
          accountTypeName: ['Asset', 'Liability', 'Equity', 'Revenue', 'Expense'][dto.accountType - 1] || 'Asset',
          description: dto.description,
          isActive: true,
          isPostingAccount: dto.level === 4
        };

        const updated = [...accounts, newAccount];
        setAccounts(updated);
        recalculateLocalState(updated, vouchers, transactions);
        setNotification(`Account ${newAccount.code} created locally.`);
        return true;
      }
    } catch (err: any) {
      setErrorMessage(err.message || 'Failed to create account.');
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  const createVoucher = async (dto: CreateVoucherDto): Promise<boolean> => {
    setIsLoading(true);
    try {
      const totalDebit = dto.lines.reduce((s, l) => s + Number(l.debit || 0), 0);
      const totalCredit = dto.lines.reduce((s, l) => s + Number(l.credit || 0), 0);

      if (Math.abs(totalDebit - totalCredit) > 0.001) {
        throw new Error(`Out of balance! Debits: $${totalDebit.toFixed(2)}, Credits: $${totalCredit.toFixed(2)}.`);
      }

      if (isBackendConnected) {
        await accountingApi.createVoucher(dto);
        await refreshAll();
        setNotification('CP Voucher created & posted successfully!');
        return true;
      } else {
        const nextNum = `CP-${String(vouchers.length + 1).padStart(6, '0')}`;
        const newVoucherId = Date.now();
        const vLines = dto.lines.map((l, idx) => {
          const acc = accounts.find(a => a.id === l.accountId);
          return {
            id: newVoucherId + idx,
            accountId: l.accountId,
            accountCode: acc?.code || '',
            accountName: acc?.name || '',
            description: l.description,
            debit: Number(l.debit || 0),
            credit: Number(l.credit || 0)
          };
        });

        const newVoucher: VoucherDto = {
          id: newVoucherId,
          voucherNumber: nextNum,
          voucherType: 'CP',
          voucherDate: dto.voucherDate,
          reference: dto.reference,
          narration: dto.narration,
          totalDebit,
          totalCredit,
          difference: 0,
          isBalanced: true,
          isPosted: dto.postImmediately,
          postedAtUtc: dto.postImmediately ? new Date().toISOString() : undefined,
          lines: vLines
        };

        const updatedVouchers = [...vouchers, newVoucher];
        let updatedTransactions = [...transactions];

        if (dto.postImmediately) {
          const newTxs: LedgerTransactionDto[] = vLines.map((vl, idx) => ({
            id: newVoucherId + 100 + idx,
            date: dto.voucherDate,
            voucherId: newVoucher.id,
            voucherNumber: newVoucher.voucherNumber,
            voucherType: 'CP',
            accountId: vl.accountId,
            accountCode: vl.accountCode,
            accountName: vl.accountName,
            description: vl.description || dto.narration,
            debit: vl.debit,
            credit: vl.credit,
            runningBalance: 0
          }));
          updatedTransactions = [...updatedTransactions, ...newTxs];
        }

        setVouchers(updatedVouchers);
        setTransactions(updatedTransactions);
        recalculateLocalState(accounts, updatedVouchers, updatedTransactions);
        setNotification(`Voucher ${newVoucher.voucherNumber} created!`);
        return true;
      }
    } catch (err: any) {
      setErrorMessage(err.message || 'Failed to create voucher.');
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  const postVoucher = async (voucherId: number): Promise<boolean> => {
    setIsLoading(true);
    try {
      if (isBackendConnected) {
        await accountingApi.postVoucher(voucherId);
        await refreshAll();
        setNotification(`Voucher #${voucherId} posted to General Ledger!`);
        return true;
      } else {
        const target = vouchers.find(v => v.id === voucherId);
        if (!target) throw new Error('Voucher not found.');
        if (target.isPosted) throw new Error('Voucher is already posted.');

        const updatedVouchers = vouchers.map(v =>
          v.id === voucherId ? { ...v, isPosted: true, postedAtUtc: new Date().toISOString() } : v
        );

        const newTxs: LedgerTransactionDto[] = target.lines.map((vl, idx) => ({
          id: Date.now() + idx,
          date: target.voucherDate,
          voucherId: target.id,
          voucherNumber: target.voucherNumber,
          voucherType: 'CP',
          accountId: vl.accountId,
          accountCode: vl.accountCode,
          accountName: vl.accountName,
          description: vl.description || target.narration,
          debit: vl.debit,
          credit: vl.credit,
          runningBalance: 0
        }));

        const updatedTransactions = [...transactions, ...newTxs];
        setVouchers(updatedVouchers);
        setTransactions(updatedTransactions);
        recalculateLocalState(accounts, updatedVouchers, updatedTransactions);
        setNotification(`Voucher ${target.voucherNumber} posted to General Ledger.`);
        return true;
      }
    } catch (err: any) {
      setErrorMessage(err.message || 'Posting failed.');
      return false;
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <AccountingContext.Provider
      value={{
        accounts,
        postingAccounts,
        vouchers,
        transactions,
        trialBalance,
        dashboardSummary,
        isLoading,
        isBackendConnected,
        notification,
        errorMessage,
        setNotification,
        setErrorMessage,
        refreshAll,
        createAccount,
        createVoucher,
        postVoucher
      }}
    >
      {children}
    </AccountingContext.Provider>
  );
};

export const useAccounting = () => {
  const context = useContext(AccountingContext);
  if (!context) {
    throw new Error('useAccounting must be used within an AccountingProvider');
  }
  return context;
};
