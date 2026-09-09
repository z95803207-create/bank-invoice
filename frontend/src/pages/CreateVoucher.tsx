import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import {
  Plus,
  Trash2,
  CheckCircle2,
  AlertCircle,
  Receipt,
  Scale,
  Calendar,
  FileText
} from 'lucide-react';
import { useAccounting } from '../context/AccountingContext';
import { CreateVoucherDto, CreateVoucherLineDto } from '../types/accounting';

export const CreateVoucher: React.FC = () => {
  const navigate = useNavigate();
  const { postingAccounts, createVoucher, isLoading } = useAccounting();

  const [voucherDate, setVoucherDate] = useState(new Date().toISOString().split('T')[0]);
  const [reference, setReference] = useState('');
  const [narration, setNarration] = useState('');
  const [postImmediately, setPostImmediately] = useState(true);

  const [lines, setLines] = useState<CreateVoucherLineDto[]>([
    { accountId: postingAccounts[0]?.id || 0, description: '', debit: 0, credit: 0 },
    { accountId: postingAccounts[1]?.id || 0, description: '', debit: 0, credit: 0 }
  ]);

  const totalDebit = lines.reduce((sum, l) => sum + (Number(l.debit) || 0), 0);
  const totalCredit = lines.reduce((sum, l) => sum + (Number(l.credit) || 0), 0);
  const difference = Math.abs(totalDebit - totalCredit);
  const isBalanced = totalDebit > 0 && difference < 0.001;

  const handleAddLine = () => {
    setLines([
      ...lines,
      { accountId: postingAccounts[0]?.id || 0, description: '', debit: 0, credit: 0 }
    ]);
  };

  const handleRemoveLine = (index: number) => {
    if (lines.length <= 2) return;
    setLines(lines.filter((_, i) => i !== index));
  };

  const handleLineChange = (index: number, field: keyof CreateVoucherLineDto, value: any) => {
    const updated = [...lines];
    updated[index] = {
      ...updated[index],
      [field]: value
    };
    // Mutual exclusivity: if debit entered, clear credit and vice versa
    if (field === 'debit' && Number(value) > 0) {
      updated[index].credit = 0;
    } else if (field === 'credit' && Number(value) > 0) {
      updated[index].debit = 0;
    }
    setLines(updated);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!isBalanced) return;

    const dto: CreateVoucherDto = {
      voucherDate: new Date(voucherDate).toISOString(),
      reference,
      narration,
      postImmediately,
      lines: lines.map((l) => ({
        accountId: Number(l.accountId),
        description: l.description,
        debit: Number(l.debit) || 0,
        credit: Number(l.credit) || 0
      }))
    };

    const success = await createVoucher(dto);
    if (success) {
      navigate('/vouchers');
    }
  };

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      {/* Header */}
      <div>
        <h2 className="text-2xl font-bold text-gray-900 flex items-center space-x-2">
          <Receipt className="w-6 h-6 text-emerald-600" />
          <span>New Cash Payment (CP) Voucher</span>
        </h2>
        <p className="text-sm text-gray-500">
          Record cash payment transaction with double-entry debits and credits
        </p>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Header Fields Card */}
        <div className="bg-white p-6 rounded-xl border border-gray-200 shadow-xs space-y-4">
          <h3 className="text-sm font-bold uppercase tracking-wider text-gray-500 flex items-center space-x-1.5">
            <FileText className="w-4 h-4" />
            <span>Voucher Details</span>
          </h3>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div>
              <label className="block text-xs font-semibold text-gray-700 uppercase">Voucher Date</label>
              <div className="relative mt-1">
                <input
                  type="date"
                  required
                  value={voucherDate}
                  onChange={(e) => setVoucherDate(e.target.value)}
                  className="w-full border border-gray-300 rounded-md p-2 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-700 uppercase">Reference #</label>
              <input
                type="text"
                placeholder="e.g. BILL-9821, CHQ-4012"
                value={reference}
                onChange={(e) => setReference(e.target.value)}
                className="mt-1 w-full border border-gray-300 rounded-md p-2 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none"
              />
            </div>

            <div>
              <label className="block text-xs font-semibold text-gray-700 uppercase">Voucher Type</label>
              <div className="mt-1 px-3 py-2 bg-gray-100 rounded-md text-sm font-semibold text-gray-700 border border-gray-200">
                Cash Payment (CP)
              </div>
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-gray-700 uppercase">Narration / Remarks</label>
            <textarea
              required
              rows={2}
              placeholder="Detailed description of the transaction and purpose..."
              value={narration}
              onChange={(e) => setNarration(e.target.value)}
              className="mt-1 w-full border border-gray-300 rounded-md p-2 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none"
            />
          </div>
        </div>

        {/* Lines Table Card */}
        <div className="bg-white rounded-xl border border-gray-200 shadow-xs overflow-hidden">
          <div className="p-4 border-b border-gray-200 flex items-center justify-between">
            <h3 className="text-sm font-bold uppercase tracking-wider text-gray-500 flex items-center space-x-1.5">
              <Scale className="w-4 h-4" />
              <span>Accounting Lines (Level 4 Accounts Only)</span>
            </h3>
            <button
              type="button"
              onClick={handleAddLine}
              className="inline-flex items-center space-x-1 px-3 py-1.5 bg-slate-100 hover:bg-slate-200 text-slate-700 text-xs font-semibold rounded-md transition"
            >
              <Plus className="w-3.5 h-3.5" />
              <span>Add Row</span>
            </button>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead className="bg-gray-50 text-xs uppercase font-semibold text-gray-500 border-b border-gray-200">
                <tr>
                  <th className="px-4 py-3 w-10">#</th>
                  <th className="px-4 py-3 min-w-[220px]">Account (Level 4)</th>
                  <th className="px-4 py-3 min-w-[180px]">Line Description</th>
                  <th className="px-4 py-3 w-36 text-right">Debit ($)</th>
                  <th className="px-4 py-3 w-36 text-right">Credit ($)</th>
                  <th className="px-4 py-3 w-12 text-center"></th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-200">
                {lines.map((line, idx) => (
                  <tr key={idx} className="hover:bg-gray-50/70 transition">
                    <td className="px-4 py-3 font-mono text-xs text-gray-400">{idx + 1}</td>
                    <td className="px-4 py-3">
                      <select
                        required
                        value={line.accountId}
                        onChange={(e) => handleLineChange(idx, 'accountId', Number(e.target.value))}
                        className="w-full border border-gray-300 rounded-md p-1.5 text-xs focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                      >
                        <option value="">-- Select Posting Account --</option>
                        {postingAccounts.map((acc) => (
                          <option key={acc.id} value={acc.id}>
                            {acc.code} - {acc.name}
                          </option>
                        ))}
                      </select>
                    </td>
                    <td className="px-4 py-3">
                      <input
                        type="text"
                        placeholder="Line memo (optional)"
                        value={line.description || ''}
                        onChange={(e) => handleLineChange(idx, 'description', e.target.value)}
                        className="w-full border border-gray-300 rounded-md p-1.5 text-xs focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                      />
                    </td>
                    <td className="px-4 py-3">
                      <input
                        type="number"
                        step="0.01"
                        min="0"
                        placeholder="0.00"
                        value={line.debit || ''}
                        onChange={(e) => handleLineChange(idx, 'debit', e.target.value)}
                        className="w-full text-right border border-gray-300 rounded-md p-1.5 text-xs font-mono focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                      />
                    </td>
                    <td className="px-4 py-3">
                      <input
                        type="number"
                        step="0.01"
                        min="0"
                        placeholder="0.00"
                        value={line.credit || ''}
                        onChange={(e) => handleLineChange(idx, 'credit', e.target.value)}
                        className="w-full text-right border border-gray-300 rounded-md p-1.5 text-xs font-mono focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                      />
                    </td>
                    <td className="px-4 py-3 text-center">
                      <button
                        type="button"
                        disabled={lines.length <= 2}
                        onClick={() => handleRemoveLine(idx)}
                        className="text-gray-400 hover:text-rose-600 disabled:opacity-20 transition"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>

          {/* Reconciliation Footer */}
          <div className="bg-slate-50 p-4 border-t border-gray-200 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <div className="flex items-center space-x-2">
              {isBalanced ? (
                <div className="flex items-center space-x-1.5 text-emerald-700 bg-emerald-100 px-3 py-1 rounded-full text-xs font-bold">
                  <CheckCircle2 className="w-4 h-4" />
                  <span>Double-Entry Balanced</span>
                </div>
              ) : (
                <div className="flex items-center space-x-1.5 text-rose-700 bg-rose-100 px-3 py-1 rounded-full text-xs font-bold">
                  <AlertCircle className="w-4 h-4" />
                  <span>
                    Out of Balance (Diff: ${difference.toLocaleString('en-US', { minimumFractionDigits: 2 })})
                  </span>
                </div>
              )}
            </div>

            <div className="flex items-center space-x-6 text-sm">
              <div>
                <span className="text-xs text-gray-500 uppercase font-semibold mr-2">Total Debit:</span>
                <span className="font-mono font-bold text-gray-900">
                  ${totalDebit.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                </span>
              </div>
              <div>
                <span className="text-xs text-gray-500 uppercase font-semibold mr-2">Total Credit:</span>
                <span className="font-mono font-bold text-gray-900">
                  ${totalCredit.toLocaleString('en-US', { minimumFractionDigits: 2 })}
                </span>
              </div>
            </div>
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex items-center justify-between bg-white p-4 rounded-xl border border-gray-200">
          <label className="flex items-center space-x-2 cursor-pointer text-sm font-medium text-gray-700">
            <input
              type="checkbox"
              checked={postImmediately}
              onChange={(e) => setPostImmediately(e.target.checked)}
              className="w-4 h-4 text-emerald-600 rounded border-gray-300 focus:ring-emerald-500"
            />
            <span>Post to General Ledger Immediately</span>
          </label>

          <div className="flex items-center space-x-3">
            <button
              type="button"
              onClick={() => navigate('/vouchers')}
              className="px-4 py-2 border border-gray-300 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-50 transition"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={!isBalanced || isLoading}
              className="px-6 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-sm font-bold shadow-sm disabled:opacity-50 transition"
            >
              {isLoading ? 'Saving...' : postImmediately ? 'Save & Post Voucher' : 'Save Draft Voucher'}
            </button>
          </div>
        </div>
      </form>
    </div>
  );
};
