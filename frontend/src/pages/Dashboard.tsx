import React from 'react';
import { Link } from 'react-router-dom';
import {
  ShieldCheck,
  AlertOctagon,
  ArrowDownRight,
  ArrowUpRight,
  Receipt,
  FolderTree,
  PlusCircle,
  Scale
} from 'lucide-react';
import { useAccounting } from '../context/AccountingContext';

export const Dashboard: React.FC = () => {
  const { dashboardSummary, vouchers } = useAccounting();

  const isBalanced = dashboardSummary?.isTrialBalanceBalanced ?? true;
  const totalDebits = dashboardSummary?.totalDebits ?? 0;
  const totalCredits = dashboardSummary?.totalCredits ?? 0;
  const difference = Math.abs(totalDebits - totalCredits);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Financial Dashboard</h2>
          <p className="text-sm text-gray-500">General Ledger status, double-entry audit, and activity summary</p>
        </div>
        <div className="flex items-center space-x-3">
          <Link
            to="/create-voucher"
            className="inline-flex items-center space-x-2 px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-sm font-semibold shadow-sm transition"
          >
            <PlusCircle className="w-4 h-4" />
            <span>New CP Voucher</span>
          </Link>
          <Link
            to="/trial-balance"
            className="inline-flex items-center space-x-2 px-4 py-2 bg-white border border-gray-300 hover:bg-gray-50 text-gray-700 rounded-lg text-sm font-medium transition"
          >
            <Scale className="w-4 h-4" />
            <span>Trial Balance</span>
          </Link>
        </div>
      </div>

      {/* Hero Financial Health Banner */}
      <div
        className={`rounded-2xl p-6 text-white shadow-lg transition ${
          isBalanced
            ? 'bg-gradient-to-r from-emerald-600 to-teal-700'
            : 'bg-gradient-to-r from-rose-600 to-red-700'
        }`}
      >
        <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div className="flex items-center space-x-3">
            <div className="w-12 h-12 rounded-xl bg-white/20 backdrop-blur-xs flex items-center justify-center">
              {isBalanced ? (
                <ShieldCheck className="w-7 h-7 text-white" />
              ) : (
                <AlertOctagon className="w-7 h-7 text-white animate-pulse" />
              )}
            </div>
            <div>
              <h3 className="text-xl font-bold">
                {isBalanced ? 'Trial Balance Reconciled' : 'Out of Balance Warning!'}
              </h3>
              <p className="text-sm text-white/80">
                {isBalanced
                  ? 'All ledger transactions maintain strict double-entry equality (Debits = Credits).'
                  : `Difference detected: $${difference.toFixed(2)}. Requires immediate audit.`}
              </p>
            </div>
          </div>

          <div className="flex items-center space-x-6 bg-black/15 px-4 py-3 rounded-xl backdrop-blur-xs">
            <div>
              <span className="text-xs text-white/75 block uppercase font-medium">Total Debits</span>
              <span className="text-xl font-bold">${totalDebits.toLocaleString('en-US', { minimumFractionDigits: 2 })}</span>
            </div>
            <div className="h-8 w-px bg-white/25"></div>
            <div>
              <span className="text-xs text-white/75 block uppercase font-medium">Total Credits</span>
              <span className="text-xl font-bold">${totalCredits.toLocaleString('en-US', { minimumFractionDigits: 2 })}</span>
            </div>
          </div>
        </div>
      </div>

      {/* Metrics Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white p-5 rounded-xl border border-gray-200 shadow-xs">
          <div className="flex items-center justify-between text-gray-500 mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider">Total Accounts</span>
            <FolderTree className="w-5 h-5 text-indigo-500" />
          </div>
          <div className="text-2xl font-bold text-gray-900">
            {dashboardSummary?.totalAccounts ?? 0}
          </div>
          <p className="text-xs text-gray-500 mt-1">
            {dashboardSummary?.totalPostingAccounts ?? 0} Level 4 Posting accounts
          </p>
        </div>

        <div className="bg-white p-5 rounded-xl border border-gray-200 shadow-xs">
          <div className="flex items-center justify-between text-gray-500 mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider">CP Vouchers</span>
            <Receipt className="w-5 h-5 text-emerald-500" />
          </div>
          <div className="text-2xl font-bold text-gray-900">
            {dashboardSummary?.totalVouchers ?? vouchers.length}
          </div>
          <p className="text-xs text-gray-500 mt-1">
            {dashboardSummary?.postedVouchers ?? 0} Posted · {dashboardSummary?.draftVouchers ?? 0} Draft
          </p>
        </div>

        <div className="bg-white p-5 rounded-xl border border-gray-200 shadow-xs">
          <div className="flex items-center justify-between text-gray-500 mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider">Gross Inflow/Dr</span>
            <ArrowUpRight className="w-5 h-5 text-blue-500" />
          </div>
          <div className="text-2xl font-bold text-gray-900">
            ${totalDebits.toLocaleString('en-US', { minimumFractionDigits: 2 })}
          </div>
          <p className="text-xs text-gray-500 mt-1">Posted transaction volume</p>
        </div>

        <div className="bg-white p-5 rounded-xl border border-gray-200 shadow-xs">
          <div className="flex items-center justify-between text-gray-500 mb-2">
            <span className="text-xs font-semibold uppercase tracking-wider">Gross Outflow/Cr</span>
            <ArrowDownRight className="w-5 h-5 text-purple-500" />
          </div>
          <div className="text-2xl font-bold text-gray-900">
            ${totalCredits.toLocaleString('en-US', { minimumFractionDigits: 2 })}
          </div>
          <p className="text-xs text-gray-500 mt-1">Balanced credit disbursements</p>
        </div>
      </div>

      {/* Recent Activity Table */}
      <div className="bg-white rounded-xl border border-gray-200 shadow-xs overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-200 flex items-center justify-between">
          <h3 className="font-semibold text-gray-900">Recent Cash Payment Vouchers</h3>
          <Link to="/vouchers" className="text-sm font-medium text-emerald-600 hover:text-emerald-700">
            View All Vouchers →
          </Link>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="bg-gray-50 text-xs uppercase font-semibold text-gray-500 border-b border-gray-200">
              <tr>
                <th className="px-6 py-3">Voucher #</th>
                <th className="px-6 py-3">Date</th>
                <th className="px-6 py-3">Narration</th>
                <th className="px-6 py-3 text-right">Amount</th>
                <th className="px-6 py-3 text-center">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {vouchers.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-6 py-8 text-center text-gray-400">
                    No vouchers recorded yet.
                  </td>
                </tr>
              ) : (
                vouchers.slice(-5).reverse().map((v) => (
                  <tr key={v.id} className="hover:bg-gray-50 transition">
                    <td className="px-6 py-4 font-semibold text-gray-900">{v.voucherNumber}</td>
                    <td className="px-6 py-4 text-gray-600">
                      {new Date(v.voucherDate).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4 text-gray-700 max-w-xs truncate">{v.narration}</td>
                    <td className="px-6 py-4 text-right font-bold text-gray-900">
                      ${v.totalDebit.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                    </td>
                    <td className="px-6 py-4 text-center">
                      <span
                        className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold ${
                          v.isPosted
                            ? 'bg-emerald-100 text-emerald-800'
                            : 'bg-amber-100 text-amber-800'
                        }`}
                      >
                        {v.isPosted ? 'Posted' : 'Draft'}
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
