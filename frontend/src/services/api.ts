import {
  AccountDto,
  AccountType,
  CreateAccountDto,
  CreateVoucherDto,
  DashboardSummaryDto,
  LedgerTransactionDto,
  TrialBalanceDto,
  VoucherDto
} from '../types/accounting';

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api';

async function fetchJson<T>(endpoint: string, options?: RequestInit): Promise<T> {
  try {
    const response = await fetch(`${API_BASE_URL}${endpoint}`, {
      headers: {
        'Content-Type': 'application/json',
        ...options?.headers,
      },
      ...options,
    });

    if (!response.ok) {
      const errorBody = await response.text();
      let errorJson;
      try {
        errorJson = JSON.parse(errorBody);
      } catch {
        // Not JSON
      }
      throw new Error(errorJson?.message || errorBody || `API error ${response.status}`);
    }

    return await response.json();
  } catch (err: any) {
    console.warn(`[AccountingAPI] Request failed for ${endpoint}:`, err.message);
    throw err;
  }
}

export const accountingApi = {
  // Accounts
  async getAccounts(): Promise<AccountDto[]> {
    return fetchJson<AccountDto[]>('/accounts');
  },

  async getPostingAccounts(): Promise<AccountDto[]> {
    return fetchJson<AccountDto[]>('/accounts/posting');
  },

  async getAccountById(id: number): Promise<AccountDto> {
    return fetchJson<AccountDto>(`/accounts/${id}`);
  },

  async createAccount(dto: CreateAccountDto): Promise<AccountDto> {
    return fetchJson<AccountDto>('/accounts', {
      method: 'POST',
      body: JSON.stringify(dto),
    });
  },

  // Vouchers
  async getVouchers(): Promise<VoucherDto[]> {
    return fetchJson<VoucherDto[]>('/vouchers');
  },

  async getVoucherById(id: number): Promise<VoucherDto> {
    return fetchJson<VoucherDto>(`/vouchers/${id}`);
  },

  async createVoucher(dto: CreateVoucherDto): Promise<VoucherDto> {
    return fetchJson<VoucherDto>('/vouchers', {
      method: 'POST',
      body: JSON.stringify(dto),
    });
  },

  async postVoucher(id: number): Promise<VoucherDto> {
    return fetchJson<VoucherDto>(`/vouchers/${id}/post`, {
      method: 'POST',
    });
  },

  // Ledger & Register
  async getAccountLedger(accountId: number, fromDate?: string, toDate?: string): Promise<LedgerTransactionDto[]> {
    const params = new URLSearchParams();
    if (fromDate) params.append('fromDate', fromDate);
    if (toDate) params.append('toDate', toDate);
    const query = params.toString() ? `?${params.toString()}` : '';
    return fetchJson<LedgerTransactionDto[]>(`/ledger/account/${accountId}${query}`);
  },

  async getAllTransactions(limit = 100): Promise<LedgerTransactionDto[]> {
    return fetchJson<LedgerTransactionDto[]>(`/ledger/transactions?limit=${limit}`);
  },

  // Reports
  async getTrialBalance(asOfDate?: string): Promise<TrialBalanceDto> {
    const query = asOfDate ? `?asOfDate=${asOfDate}` : '';
    return fetchJson<TrialBalanceDto>(`/reports/trial-balance${query}`);
  },

  // Dashboard
  async getDashboardSummary(): Promise<DashboardSummaryDto> {
    return fetchJson<DashboardSummaryDto>('/dashboard/summary');
  }
};
