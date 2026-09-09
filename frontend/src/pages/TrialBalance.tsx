import React, { useState } from 'react';
import {
  Scale,
  ShieldCheck,
  AlertOctagon,
  Calendar,
  Printer
} from 'lucide-react';
import { useAccounting } from '../context/AccountingContext';
import { AccountTypeLabels } from '../types/accounting';

export const TrialBalance: React.FC = () => {
  const { trialBalance, isLoading } = useAccounting();
  const [asOfDate, setAsOfDate] = useState(new Date().toISOString().split('T')[0]);

  const isBalanced = trialBalance?.isBalanced ?? true;
  const totalDebit = trialBalance?.totalDebit ?? 0;
  const totalCredit = trialBalance?.totalCredit ?? 0;
  const difference = trialBalance?.difference ?? Math.abs(totalDebit - totalCredit);

  const totalNetDebit = trialBalance?.rows.reduce((s, r) => s + r.netDebitBalance, 0) ?? 0;
  const totalNetCredit = trialBalance?.rows.reduce((s, r) => s + r.netCreditBalance, 0) ?? 0;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-gray-900 flex items-center space-x-2">
            <Scale className="w-6 h-6 text-emerald-600" />
            <span>Trial Balance Report</span>
          </h2>
          <p className="text-sm text-gray-500">
            Accounting verification of double-entry equality across all Level 4 posting accounts
          </p>
        </div>

        <button
          onClick={() => window.print()}
          className="inline-flex items-center space-x-2 px-4 py-2 bg-white border border-gray-300 hover:bg-gray-50 text-gray-700 rounded-lg text-sm font-medium shadow-xs transition"
        >
          <Printer className="w-4 h-4" />
          <span>Print / Export PDF</span>
        </button>
      </div>

      {/* Reconciliation Status Banner */}
      <div
        className={`p-6 rounded-2xl text-white shadow-md transition ${
          isBalanced ? 'bg-emerald-700' : 'bg-rose-700'
        }`}
      >
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <div className="flex items-center space-x-4">
            <div className="w-12 h-12 rounded-xl bg-white/20 flex items-center justify-center">
              {isBalanced ? (
                <ShieldCheck className="w-7 h-7 text-white" />
              ) : (
                <AlertOctagon className="w-7 h-7 text-white animate-bounce" />
              )}
            </div>
            <div>
              <h3 className="text-xl font-bold">
                {isBalanced ? 'Trial Balance is Reconciled & Balanced' : 'Out of Balance!'}
              </h3>
              <p className="text-xs text-white/80 mt-0.5">
                {isBalanced
                  ? 'All posting accounts balance perfectly. Net debits match net credits precisely.'
                  : `Debit/Credit mismatch of $${difference.toFixed(2)}. Check unposted or erroneous vouchers.`}
              </p>
            </div>
          </div>

          <div className="flex items-center space-x-6 bg-black/20 px-4 py-3 rounded-xl">
            <div>
              <span className="text-xs text-white/75 block uppercase font-medium">Sum Net Debits</span>
              <span className="text-xl font-mono font-bold">
                ${totalNetDebit.toLocaleString('en-US', { minimumFractionDigits: 2 })}
              </span>
            </div>
            <div className="h-8 w-px bg-white/20"></div>
            <div>
              <span className="text-xs text-white/75 block uppercase font-medium">Sum Net Credits</span>
              <span className="text-xl font-mono font-bold">
                ${totalNetCredit.toLocaleString('en-US', { minimumFractionDigits: 2 })}
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Report Table */}
      <div className="bg-white rounded-xl border border-gray-200 shadow-xs overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-200 bg-gray-50 flex items-center justify-between">
          <div>
            <span className="font-bold text-gray-900 text-sm">Statement of Posting Balances</span>
            <span className="text-xs text-gray-500 block">
              As of: {new Date().toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' })}
            </span>
          </div>
          <span className="text-xs font-semibold px-2.5 py-1 rounded bg-slate-200 text-slate-800">
            {trialBalance?.rows.length || 0} Accounts Included
          </span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="bg-gray-100 text-xs uppercase font-semibold text-gray-600 border-b border-gray-200">
              <tr>
                <th className="px-6 py-3">Code</th>
                <th className="px-6 py-3">Account Name</th>
                <th className="px-6 py-3">Type</th>
                <th className="px-6 py-3 text-right">Total Debit ($)</th>
                <th className="px-6 py-3 text-right">Total Credit ($)</th>
                <th className="px-6 py-3 text-right text-emerald-800">Net Debit Balance ($)</th>
                <th className="px-6 py-3 text-right text-purple-800">Net Credit Balance ($)</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {!trialBalance || trialBalance.rows.length === 0 ? (
                <tr>
                  <td colSpan={7} className="px-6 py-8 text-center text-gray-400">
                    No posting activity found to compile Trial Balance.
                  </td>
                </tr>
              ) : (
                trialBalance.rows.map((row) => (
                  <tr key={row.accountId} className="hover:bg-gray-50 transition">
                    <td className="px-6 py-4 font-mono font-bold text-gray-900">{row.accountCode}</td>
                    <td className="px-6 py-4 font-medium text-gray-900">{row.accountName}</td>
                    <td className="px-6 py-4">
                      <span className="text-xs px-2 py-0.5 rounded bg-slate-100 text-slate-700">
                        {AccountTypeLabels[row.accountType]}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right font-mono text-gray-600">
                      {row.totalDebit > 0 ? `$${row.totalDebit.toLocaleString('en-US', { minimumFractionDigits: 2 })}` : '—'}
                    </td>
                    <td className="px-6 py-4 text-right font-mono text-gray-600">
                      {row.totalCredit > 0 ? `$${row.totalCredit.toLocaleString('en-US', { minimumFractionDigits: 2 })}` : '—'}
                    </td>
                    <td className="px-6 py-4 text-right font-mono font-bold text-emerald-700">
                      {row.netDebitBalance > 0
                        ? `$${row.netDebitBalance.toLocaleString('en-US', { minimumFractionDigits: 2 })}`
                        : '—'}
                    </td>
                    <td className="px-6 py-4 text-right font-mono font-bold text-purple-700">
                      {row.netCreditBalance > 0
                        ? `$${row.netCreditBalance.toLocaleString('en-US', { minimumFractionDigits: 2 })}`
                        : '—'}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
            <tfoot className="bg-slate-100 font-bold border-t-2 border-slate-300 text-slate-900">
              <tr>
                <td colSpan={3} className="px-6 py-4 uppercase text-xs tracking-wider">
                  Reconciled Total
                </td>
                <td className="px-6 py-4 text-right font-mono">
                  ${totalDebit.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                </td>
                <td className="px-6 py-4 text-right font-mono">
                  ${totalCredit.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                </td>
                <td className="px-6 py-4 text-right font-mono text-emerald-800">
                  ${totalNetDebit.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                </td>
                <td className="px-6 py-4 text-right font-mono text-purple-800">
                  ${totalNetCredit.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                </td>
              </tr>
            </tfoot>
          </table>
        </div>
      </div>
    </div>
  );
};
