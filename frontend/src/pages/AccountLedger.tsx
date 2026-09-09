import React, { useState } from 'react';
import {
  BookOpen,
  Filter,
  Calendar,
  ArrowDownRight,
  ArrowUpRight,
  Scale
} from 'lucide-react';
import { useAccounting } from '../context/AccountingContext';
import { AccountType } from '../types/accounting';

export const AccountLedger: React.FC = () => {
  const { postingAccounts, transactions } = useAccounting();
  const [selectedAccountId, setSelectedAccountId] = useState<number>(
    postingAccounts[0]?.id || 0
  );
  const [fromDate, setFromDate] = useState('');
  const [toDate, setToDate] = useState('');

  const currentAccount = postingAccounts.find((a) => a.id === selectedAccountId);

  // Filter transactions for selected account and optional date range
  const accountTransactions = transactions
    .filter((t) => t.accountId === selectedAccountId)
    .filter((t) => (!fromDate || t.date >= fromDate) && (!toDate || t.date <= toDate))
    .sort((a, b) => new Date(a.date).getTime() - new Date(b.date).getTime());

  // Compute running balance
  // For Assets & Expenses: normal balance = Debit - Credit
  // For Liabilities, Equity, Revenue: normal balance = Credit - Debit
  const isDebitNormal =
    currentAccount?.accountType === AccountType.Asset ||
    currentAccount?.accountType === AccountType.Expense;

  let running = 0;
  const ledgerRows = accountTransactions.map((tx) => {
    if (isDebitNormal) {
      running += tx.debit - tx.credit;
    } else {
      running += tx.credit - tx.debit;
    }
    return {
      ...tx,
      computedRunningBalance: running
    };
  });

  const totalDebit = accountTransactions.reduce((s, t) => s + t.debit, 0);
  const totalCredit = accountTransactions.reduce((s, t) => s + t.credit, 0);
  const closingBalance = running;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h2 className="text-2xl font-bold text-gray-900 flex items-center space-x-2">
          <BookOpen className="w-6 h-6 text-emerald-600" />
          <span>General Ledger Account Statement</span>
        </h2>
        <p className="text-sm text-gray-500">
          Individual statement of account with line narratives, debits, credits, and progressive running balance
        </p>
      </div>

      {/* Account Selector & Date Range Filter */}
      <div className="bg-white p-5 rounded-xl border border-gray-200 shadow-xs flex flex-col md:flex-row gap-4 items-center justify-between">
        <div className="w-full md:w-96">
          <label className="block text-xs font-semibold text-gray-500 uppercase mb-1">
            Select Posting Account (Level 4)
          </label>
          <select
            value={selectedAccountId}
            onChange={(e) => setSelectedAccountId(Number(e.target.value))}
            className="w-full border border-gray-300 rounded-lg p-2 text-sm font-medium focus:ring-2 focus:ring-emerald-500 focus:outline-none"
          >
            {postingAccounts.map((a) => (
              <option key={a.id} value={a.id}>
                {a.code} - {a.name} ({a.accountTypeName})
              </option>
            ))}
          </select>
        </div>

        <div className="flex flex-wrap items-center gap-3 w-full md:w-auto">
          <div>
            <label className="block text-xs font-semibold text-gray-500 uppercase mb-1">From Date</label>
            <input
              type="date"
              value={fromDate}
              onChange={(e) => setFromDate(e.target.value)}
              className="border border-gray-300 rounded-lg p-2 text-xs focus:ring-2 focus:ring-emerald-500 focus:outline-none"
            />
          </div>
          <div>
            <label className="block text-xs font-semibold text-gray-500 uppercase mb-1">To Date</label>
            <input
              type="date"
              value={toDate}
              onChange={(e) => setToDate(e.target.value)}
              className="border border-gray-300 rounded-lg p-2 text-xs focus:ring-2 focus:ring-emerald-500 focus:outline-none"
            />
          </div>
        </div>
      </div>

      {/* Summary Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-xs">
          <span className="text-xs text-gray-500 uppercase font-semibold block">Total Debits</span>
          <span className="text-xl font-bold text-gray-900 font-mono">
            ${totalDebit.toLocaleString('en-US', { minimumFractionDigits: 2 })}
          </span>
        </div>

        <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-xs">
          <span className="text-xs text-gray-500 uppercase font-semibold block">Total Credits</span>
          <span className="text-xl font-bold text-gray-900 font-mono">
            ${totalCredit.toLocaleString('en-US', { minimumFractionDigits: 2 })}
          </span>
        </div>

        <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-xs">
          <span className="text-xs text-gray-500 uppercase font-semibold block">
            Net Closing Balance ({isDebitNormal ? 'Dr' : 'Cr'})
          </span>
          <span
            className={`text-xl font-bold font-mono ${
              closingBalance >= 0 ? 'text-emerald-700' : 'text-rose-700'
            }`}
          >
            ${Math.abs(closingBalance).toLocaleString('en-US', { minimumFractionDigits: 2 })}
            <span className="text-xs ml-1 font-sans text-gray-500">
              {closingBalance >= 0 ? (isDebitNormal ? 'Dr' : 'Cr') : isDebitNormal ? 'Cr' : 'Dr'}
            </span>
          </span>
        </div>
      </div>

      {/* Ledger Table */}
      <div className="bg-white rounded-xl border border-gray-200 shadow-xs overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-200 bg-gray-50 flex items-center justify-between">
          <div className="font-semibold text-gray-800 text-sm">
            {currentAccount?.code} — {currentAccount?.name}
          </div>
          <span className="text-xs text-gray-500">
            {accountTransactions.length} transaction entries
          </span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="bg-gray-100 text-xs uppercase font-semibold text-gray-600 border-b border-gray-200">
              <tr>
                <th className="px-6 py-3">Date</th>
                <th className="px-6 py-3">Voucher #</th>
                <th className="px-6 py-3">Description / Narration</th>
                <th className="px-6 py-3 text-right">Debit ($)</th>
                <th className="px-6 py-3 text-right">Credit ($)</th>
                <th className="px-6 py-3 text-right">Running Balance ($)</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {ledgerRows.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-6 py-8 text-center text-gray-400">
                    No transactions posted for this account in the selected date range.
                  </td>
                </tr>
              ) : (
                ledgerRows.map((row) => (
                  <tr key={row.id} className="hover:bg-gray-50 transition">
                    <td className="px-6 py-4 text-gray-600 font-mono text-xs">
                      {new Date(row.date).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4 font-mono font-bold text-gray-900">{row.voucherNumber}</td>
                    <td className="px-6 py-4 text-gray-700">{row.description}</td>
                    <td className="px-6 py-4 text-right font-mono font-semibold text-gray-900">
                      {row.debit > 0 ? `$${row.debit.toLocaleString('en-US', { minimumFractionDigits: 2 })}` : '—'}
                    </td>
                    <td className="px-6 py-4 text-right font-mono font-semibold text-gray-900">
                      {row.credit > 0 ? `$${row.credit.toLocaleString('en-US', { minimumFractionDigits: 2 })}` : '—'}
                    </td>
                    <td className="px-6 py-4 text-right font-mono font-bold text-slate-800">
                      ${Math.abs(row.computedRunningBalance).toLocaleString('en-US', { minimumFractionDigits: 2 })}
                      <span className="text-xs ml-1 text-gray-400">
                        {row.computedRunningBalance >= 0
                          ? isDebitNormal
                            ? 'Dr'
                            : 'Cr'
                          : isDebitNormal
                          ? 'Cr'
                          : 'Dr'}
                      </span>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
