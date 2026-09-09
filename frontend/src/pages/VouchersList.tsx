import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import {
  Receipt,
  Plus,
  Search,
  CheckCircle2,
  Clock,
  ExternalLink,
  Send,
  Eye
} from 'lucide-react';
import { useAccounting } from '../context/AccountingContext';
import { VoucherDto } from '../types/accounting';

export const VouchersList: React.FC = () => {
  const { vouchers, postVoucher, isLoading } = useAccounting();
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState<'all' | 'posted' | 'draft'>('all');
  const [selectedVoucher, setSelectedVoucher] = useState<VoucherDto | null>(null);

  const filtered = vouchers.filter((v) => {
    const matchesSearch =
      v.voucherNumber.toLowerCase().includes(searchTerm.toLowerCase()) ||
      v.narration.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (v.reference && v.reference.toLowerCase().includes(searchTerm.toLowerCase()));
    const matchesStatus =
      statusFilter === 'all' ||
      (statusFilter === 'posted' && v.isPosted) ||
      (statusFilter === 'draft' && !v.isPosted);
    return matchesSearch && matchesStatus;
  });

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">Cash Payment (CP) Vouchers</h2>
          <p className="text-sm text-gray-500">
            View voucher journal entries, audit double-entry balances, and post drafts to the Ledger
          </p>
        </div>
        <Link
          to="/create-voucher"
          className="inline-flex items-center space-x-2 px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-sm font-semibold shadow-sm transition"
        >
          <Plus className="w-4 h-4" />
          <span>New Voucher</span>
        </Link>
      </div>

      {/* Filter bar */}
      <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-xs flex flex-col sm:flex-row gap-4 items-center justify-between">
        <div className="relative w-full sm:w-80">
          <Search className="w-4 h-4 text-gray-400 absolute left-3 top-3" />
          <input
            type="text"
            placeholder="Search voucher #, narration, reference..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-9 pr-4 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none"
          />
        </div>

        <div className="flex items-center space-x-2 w-full sm:w-auto">
          <button
            onClick={() => setStatusFilter('all')}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition ${
              statusFilter === 'all' ? 'bg-slate-900 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
            }`}
          >
            All ({vouchers.length})
          </button>
          <button
            onClick={() => setStatusFilter('posted')}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition ${
              statusFilter === 'posted' ? 'bg-emerald-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
            }`}
          >
            Posted ({vouchers.filter((v) => v.isPosted).length})
          </button>
          <button
            onClick={() => setStatusFilter('draft')}
            className={`px-3 py-1.5 rounded-lg text-xs font-semibold transition ${
              statusFilter === 'draft' ? 'bg-amber-600 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
            }`}
          >
            Draft ({vouchers.filter((v) => !v.isPosted).length})
          </button>
        </div>
      </div>

      {/* Vouchers Table */}
      <div className="bg-white rounded-xl border border-gray-200 shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="bg-gray-50 text-xs uppercase font-semibold text-gray-500 border-b border-gray-200">
              <tr>
                <th className="px-6 py-3">Voucher #</th>
                <th className="px-6 py-3">Date</th>
                <th className="px-6 py-3">Reference</th>
                <th className="px-6 py-3">Narration</th>
                <th className="px-6 py-3 text-right">Debit ($)</th>
                <th className="px-6 py-3 text-right">Credit ($)</th>
                <th className="px-6 py-3 text-center">Status</th>
                <th className="px-6 py-3 text-center">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {filtered.length === 0 ? (
                <tr>
                  <td colSpan={8} className="px-6 py-8 text-center text-gray-400">
                    No vouchers found.
                  </td>
                </tr>
              ) : (
                filtered.map((v) => (
                  <tr key={v.id} className="hover:bg-gray-50 transition">
                    <td className="px-6 py-4 font-mono font-bold text-gray-900">{v.voucherNumber}</td>
                    <td className="px-6 py-4 text-gray-600">
                      {new Date(v.voucherDate).toLocaleDateString()}
                    </td>
                    <td className="px-6 py-4 text-gray-500 text-xs font-mono">{v.reference || '—'}</td>
                    <td className="px-6 py-4 text-gray-700 max-w-xs truncate">{v.narration}</td>
                    <td className="px-6 py-4 text-right font-mono font-bold text-gray-900">
                      ${v.totalDebit.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                    </td>
                    <td className="px-6 py-4 text-right font-mono font-bold text-gray-900">
                      ${v.totalCredit.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                    </td>
                    <td className="px-6 py-4 text-center">
                      <span
                        className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-semibold ${
                          v.isPosted
                            ? 'bg-emerald-100 text-emerald-800'
                            : 'bg-amber-100 text-amber-800'
                        }`}
                      >
                        {v.isPosted ? (
                          <>
                            <CheckCircle2 className="w-3 h-3 mr-1 text-emerald-600" />
                            Posted
                          </>
                        ) : (
                          <>
                            <Clock className="w-3 h-3 mr-1 text-amber-600" />
                            Draft
                          </>
                        )}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-center">
                      <div className="flex items-center justify-center space-x-2">
                        <button
                          onClick={() => setSelectedVoucher(v)}
                          className="p-1 text-gray-400 hover:text-emerald-600 transition"
                          title="View Lines"
                        >
                          <Eye className="w-4 h-4" />
                        </button>

                        {!v.isPosted && (
                          <button
                            onClick={() => postVoucher(v.id)}
                            disabled={isLoading}
                            className="inline-flex items-center space-x-1 px-2.5 py-1 bg-emerald-600 hover:bg-emerald-700 text-white rounded text-xs font-medium transition shadow-xs"
                            title="Post to General Ledger"
                          >
                            <Send className="w-3 h-3" />
                            <span>Post</span>
                          </button>
                        )}
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Voucher Detail Modal */}
      {selectedVoucher && (
        <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-2xl w-full p-6 space-y-4">
            <div className="flex items-center justify-between border-b pb-3">
              <div>
                <h3 className="text-lg font-bold text-gray-900 flex items-center space-x-2">
                  <Receipt className="w-5 h-5 text-emerald-600" />
                  <span>Voucher {selectedVoucher.voucherNumber}</span>
                </h3>
                <p className="text-xs text-gray-500">
                  Date: {new Date(selectedVoucher.voucherDate).toLocaleDateString()} · Type:{' '}
                  {selectedVoucher.voucherType}
                </p>
              </div>
              <button
                onClick={() => setSelectedVoucher(null)}
                className="text-gray-400 hover:text-gray-600 font-bold"
              >
                ✕
              </button>
            </div>

            <div className="p-3 bg-gray-50 rounded-lg text-sm text-gray-700">
              <span className="font-semibold text-gray-900 block text-xs uppercase text-gray-500">
                Narration
              </span>
              {selectedVoucher.narration}
            </div>

            <div className="border border-gray-200 rounded-lg overflow-hidden">
              <table className="w-full text-left text-xs">
                <thead className="bg-gray-100 text-gray-600 uppercase font-semibold">
                  <tr>
                    <th className="px-4 py-2">Account</th>
                    <th className="px-4 py-2">Line Description</th>
                    <th className="px-4 py-2 text-right">Debit ($)</th>
                    <th className="px-4 py-2 text-right">Credit ($)</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-200">
                  {selectedVoucher.lines.map((l) => (
                    <tr key={l.id}>
                      <td className="px-4 py-2 font-mono">
                        <span className="font-bold">{l.accountCode}</span> - {l.accountName}
                      </td>
                      <td className="px-4 py-2 text-gray-600">{l.description || '—'}</td>
                      <td className="px-4 py-2 text-right font-mono font-semibold">
                        {l.debit > 0 ? `$${l.debit.toLocaleString('en-US', { minimumFractionDigits: 2 })}` : '—'}
                      </td>
                      <td className="px-4 py-2 text-right font-mono font-semibold">
                        {l.credit > 0 ? `$${l.credit.toLocaleString('en-US', { minimumFractionDigits: 2 })}` : '—'}
                      </td>
                    </tr>
                  ))}
                </tbody>
                <tfoot className="bg-gray-50 font-bold border-t">
                  <tr>
                    <td colSpan={2} className="px-4 py-2 text-right uppercase text-gray-600">
                      Total
                    </td>
                    <td className="px-4 py-2 text-right font-mono text-emerald-700">
                      ${selectedVoucher.totalDebit.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                    </td>
                    <td className="px-4 py-2 text-right font-mono text-emerald-700">
                      ${selectedVoucher.totalCredit.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                    </td>
                  </tr>
                </tfoot>
              </table>
            </div>

            <div className="flex justify-end pt-2">
              <button
                onClick={() => setSelectedVoucher(null)}
                className="px-4 py-2 bg-slate-800 hover:bg-slate-700 text-white rounded-md text-sm font-medium"
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
