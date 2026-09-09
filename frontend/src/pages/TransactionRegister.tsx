import React, { useState } from 'react';
import {
  ListOrdered,
  Search,
  ArrowDownRight,
  ArrowUpRight,
  Calendar
} from 'lucide-react';
import { useAccounting } from '../context/AccountingContext';

export const TransactionRegister: React.FC = () => {
  const { transactions } = useAccounting();
  const [searchTerm, setSearchTerm] = useState('');

  const sortedTransactions = [...transactions].sort(
    (a, b) => new Date(b.date).getTime() - new Date(a.date).getTime()
  );

  const filtered = sortedTransactions.filter(
    (t) =>
      t.voucherNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
      t.accountCode.toLowerCase().includes(searchTerm.toLowerCase()) ||
      t.accountName.toLowerCase().includes(searchTerm.toLowerCase()) ||
      t.description.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const totalDebits = filtered.reduce((s, t) => s + t.debit, 0);
  const totalCredits = filtered.reduce((s, t) => s + t.credit, 0);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-gray-900 flex items-center space-x-2">
            <ListOrdered className="w-6 h-6 text-emerald-600" />
            <span>Transaction Register</span>
          </h2>
          <p className="text-sm text-gray-500">
            Audit register of individual debit and credit entries posted across all ledger accounts
          </p>
        </div>

        <div className="flex items-center space-x-4 bg-white px-4 py-2 rounded-xl border border-gray-200 shadow-xs">
          <div>
            <span className="text-xs text-gray-400 block uppercase font-semibold">Total Debits</span>
            <span className="text-sm font-mono font-bold text-gray-900">
              ${totalDebits.toLocaleString('en-US', { minimumFractionDigits: 2 })}
            </span>
          </div>
          <div className="h-6 w-px bg-gray-200"></div>
          <div>
            <span className="text-xs text-gray-400 block uppercase font-semibold">Total Credits</span>
            <span className="text-sm font-mono font-bold text-gray-900">
              ${totalCredits.toLocaleString('en-US', { minimumFractionDigits: 2 })}
            </span>
          </div>
        </div>
      </div>

      {/* Search Input */}
      <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-xs">
        <div className="relative w-full sm:w-96">
          <Search className="w-4 h-4 text-gray-400 absolute left-3 top-3" />
          <input
            type="text"
            placeholder="Search by voucher #, account, or description..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-9 pr-4 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none"
          />
        </div>
      </div>

      {/* Transactions Table */}
      <div className="bg-white rounded-xl border border-gray-200 shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="bg-gray-50 text-xs uppercase font-semibold text-gray-500 border-b border-gray-200">
              <tr>
                <th className="px-6 py-3">Date</th>
                <th className="px-6 py-3">Voucher #</th>
                <th className="px-6 py-3">Account</th>
                <th className="px-6 py-3">Description</th>
                <th className="px-6 py-3 text-right">Debit ($)</th>
                <th className="px-6 py-3 text-right">Credit ($)</th>
                <th className="px-6 py-3 text-center">Type</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {filtered.length === 0 ? (
                <tr>
                  <td colSpan={7} className="px-6 py-8 text-center text-gray-400">
                    No transactions found.
                  </td>
                </tr>
              ) : (
                filtered.map((t) => {
                  const isDebit = t.debit > 0;
                  return (
                    <tr key={t.id} className="hover:bg-gray-50 transition">
                      <td className="px-6 py-4 text-gray-600 font-mono text-xs">
                        {new Date(t.date).toLocaleDateString()}
                      </td>
                      <td className="px-6 py-4 font-mono font-bold text-gray-900">{t.voucherNumber}</td>
                      <td className="px-6 py-4">
                        <span className="font-mono font-bold text-gray-900 mr-1">{t.accountCode}</span>
                        <span className="text-gray-600 text-xs">{t.accountName}</span>
                      </td>
                      <td className="px-6 py-4 text-gray-700 max-w-sm truncate">{t.description}</td>
                      <td className="px-6 py-4 text-right font-mono font-semibold text-gray-900">
                        {t.debit > 0 ? `$${t.debit.toLocaleString('en-US', { minimumFractionDigits: 2 })}` : '—'}
                      </td>
                      <td className="px-6 py-4 text-right font-mono font-semibold text-gray-900">
                        {t.credit > 0 ? `$${t.credit.toLocaleString('en-US', { minimumFractionDigits: 2 })}` : '—'}
                      </td>
                      <td className="px-6 py-4 text-center">
                        <span
                          className={`inline-flex items-center px-2 py-0.5 rounded text-xs font-bold ${
                            isDebit ? 'bg-blue-50 text-blue-700' : 'bg-purple-50 text-purple-700'
                          }`}
                        >
                          {isDebit ? (
                            <>
                              <ArrowUpRight className="w-3 h-3 mr-1 text-blue-500" />
                              Dr
                            </>
                          ) : (
                            <>
                              <ArrowDownRight className="w-3 h-3 mr-1 text-purple-500" />
                              Cr
                            </>
                          )}
                        </span>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
