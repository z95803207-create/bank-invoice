import React, { useState } from 'react';
import {
  FolderTree,
  Plus,
  Search,
  Filter,
  CheckCircle2,
  Lock,
  Layers,
  Building
} from 'lucide-react';
import { useAccounting } from '../context/AccountingContext';
import { AccountType, AccountTypeLabels, CreateAccountDto } from '../types/accounting';

export const ChartOfAccounts: React.FC = () => {
  const { accounts, createAccount, isLoading } = useAccounting();
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedType, setSelectedType] = useState<number | 'all'>('all');
  const [selectedLevel, setSelectedLevel] = useState<number | 'all'>('all');
  const [showModal, setShowModal] = useState(false);

  // Form state for adding account
  const [formData, setFormData] = useState<CreateAccountDto>({
    code: '',
    name: '',
    level: 4,
    parentId: null,
    accountType: AccountType.Asset,
    description: ''
  });

  const filteredAccounts = accounts.filter((acc) => {
    const matchesSearch =
      acc.code.toLowerCase().includes(searchTerm.toLowerCase()) ||
      acc.name.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesType = selectedType === 'all' || acc.accountType === selectedType;
    const matchesLevel = selectedLevel === 'all' || acc.level === selectedLevel;
    return matchesSearch && matchesType && matchesLevel;
  });

  // Valid parents for the selected level (must be level - 1)
  const validParents = accounts.filter((a) => a.level === formData.level - 1);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const success = await createAccount(formData);
    if (success) {
      setShowModal(false);
      setFormData({
        code: '',
        name: '',
        level: 4,
        parentId: null,
        accountType: AccountType.Asset,
        description: ''
      });
    }
  };

  const getLevelBadge = (level: number) => {
    switch (level) {
      case 1:
        return <span className="px-2 py-0.5 rounded text-xs font-bold bg-purple-100 text-purple-800">L1 Major Head</span>;
      case 2:
        return <span className="px-2 py-0.5 rounded text-xs font-semibold bg-blue-100 text-blue-800">L2 Sub-Head</span>;
      case 3:
        return <span className="px-2 py-0.5 rounded text-xs font-semibold bg-amber-100 text-amber-800">L3 Control Group</span>;
      case 4:
        return <span className="px-2 py-0.5 rounded text-xs font-semibold bg-emerald-100 text-emerald-800">L4 Posting</span>;
      default:
        return null;
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h2 className="text-2xl font-bold text-gray-900">4-Level Chart of Accounts</h2>
          <p className="text-sm text-gray-500">
            Hierarchical structure enforcing Major Heads, Sub-heads, Control Groups, and Posting Accounts
          </p>
        </div>
        <button
          onClick={() => setShowModal(true)}
          className="inline-flex items-center space-x-2 px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg text-sm font-semibold shadow-sm transition"
        >
          <Plus className="w-4 h-4" />
          <span>Add Account</span>
        </button>
      </div>

      {/* Filter Controls */}
      <div className="bg-white p-4 rounded-xl border border-gray-200 shadow-xs flex flex-col md:flex-row gap-4 items-center justify-between">
        <div className="relative w-full md:w-80">
          <Search className="w-4 h-4 text-gray-400 absolute left-3 top-3" />
          <input
            type="text"
            placeholder="Search code or account name..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="w-full pl-9 pr-4 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none"
          />
        </div>

        <div className="flex flex-wrap items-center gap-3 w-full md:w-auto">
          <div className="flex items-center space-x-2">
            <Filter className="w-4 h-4 text-gray-400" />
            <select
              value={selectedType}
              onChange={(e) => setSelectedType(e.target.value === 'all' ? 'all' : Number(e.target.value))}
              className="border border-gray-300 rounded-lg text-sm px-3 py-2 focus:ring-2 focus:ring-emerald-500 focus:outline-none"
            >
              <option value="all">All Types</option>
              <option value={AccountType.Asset}>Asset</option>
              <option value={AccountType.Liability}>Liability</option>
              <option value={AccountType.Equity}>Equity</option>
              <option value={AccountType.Revenue}>Revenue</option>
              <option value={AccountType.Expense}>Expense</option>
            </select>
          </div>

          <select
            value={selectedLevel}
            onChange={(e) => setSelectedLevel(e.target.value === 'all' ? 'all' : Number(e.target.value))}
            className="border border-gray-300 rounded-lg text-sm px-3 py-2 focus:ring-2 focus:ring-emerald-500 focus:outline-none"
          >
            <option value="all">All Levels (1 - 4)</option>
            <option value={1}>Level 1 (Major Head)</option>
            <option value={2}>Level 2 (Sub-Head)</option>
            <option value={3}>Level 3 (Control Group)</option>
            <option value={4}>Level 4 (Posting Account)</option>
          </select>
        </div>
      </div>

      {/* Accounts Table */}
      <div className="bg-white rounded-xl border border-gray-200 shadow-xs overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-left text-sm">
            <thead className="bg-gray-50 text-xs uppercase font-semibold text-gray-500 border-b border-gray-200">
              <tr>
                <th className="px-6 py-3">Code</th>
                <th className="px-6 py-3">Account Name</th>
                <th className="px-6 py-3">Level</th>
                <th className="px-6 py-3">Parent Account</th>
                <th className="px-6 py-3">Type</th>
                <th className="px-6 py-3 text-center">Posting Allowed</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-200">
              {filteredAccounts.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-6 py-8 text-center text-gray-400">
                    No accounts match the current filter.
                  </td>
                </tr>
              ) : (
                filteredAccounts.map((acc) => (
                  <tr key={acc.id} className="hover:bg-gray-50 transition">
                    <td className="px-6 py-4 font-mono font-bold text-gray-900">{acc.code}</td>
                    <td className="px-6 py-4">
                      <div
                        className="font-medium text-gray-900"
                        style={{ paddingLeft: `${(acc.level - 1) * 16}px` }}
                      >
                        {acc.level > 1 && <span className="text-gray-400 mr-1">└─</span>}
                        {acc.name}
                      </div>
                    </td>
                    <td className="px-6 py-4">{getLevelBadge(acc.level)}</td>
                    <td className="px-6 py-4 text-gray-500 text-xs">
                      {acc.parentName || <span className="italic text-gray-400">None (Root Head)</span>}
                    </td>
                    <td className="px-6 py-4">
                      <span className="text-xs font-semibold px-2 py-1 rounded bg-slate-100 text-slate-700">
                        {AccountTypeLabels[acc.accountType]}
                      </span>
                    </td>
                    <td className="px-6 py-4 text-center">
                      {acc.isPostingAccount ? (
                        <span className="inline-flex items-center text-xs font-semibold text-emerald-700 bg-emerald-50 px-2 py-0.5 rounded">
                          <CheckCircle2 className="w-3.5 h-3.5 mr-1 text-emerald-600" />
                          Yes (Level 4)
                        </span>
                      ) : (
                        <span className="inline-flex items-center text-xs text-gray-400 bg-gray-50 px-2 py-0.5 rounded">
                          <Lock className="w-3.5 h-3.5 mr-1 text-gray-400" />
                          No (Control Only)
                        </span>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Add Account Modal */}
      {showModal && (
        <div className="fixed inset-0 z-50 bg-black/50 backdrop-blur-xs flex items-center justify-center p-4">
          <div className="bg-white rounded-xl shadow-xl max-w-lg w-full p-6 space-y-4">
            <div className="flex items-center justify-between border-b pb-3">
              <h3 className="text-lg font-bold text-gray-900 flex items-center space-x-2">
                <FolderTree className="w-5 h-5 text-emerald-600" />
                <span>Create New Account</span>
              </h3>
              <button onClick={() => setShowModal(false)} className="text-gray-400 hover:text-gray-600 font-bold">
                ✕
              </button>
            </div>

            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase">Account Code</label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. 5114"
                    value={formData.code}
                    onChange={(e) => setFormData({ ...formData, code: e.target.value })}
                    className="mt-1 w-full border border-gray-300 rounded-md p-2 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                  />
                </div>

                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase">Hierarchy Level</label>
                  <select
                    value={formData.level}
                    onChange={(e) => {
                      const lvl = Number(e.target.value);
                      setFormData({ ...formData, level: lvl, parentId: null });
                    }}
                    className="mt-1 w-full border border-gray-300 rounded-md p-2 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                  >
                    <option value={1}>Level 1: Major Head</option>
                    <option value={2}>Level 2: Sub-Head</option>
                    <option value={3}>Level 3: Control Group</option>
                    <option value={4}>Level 4: Posting Account</option>
                  </select>
                </div>
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase">Account Name</label>
                <input
                  type="text"
                  required
                  placeholder="e.g. Office Equipment Depreciation"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                  className="mt-1 w-full border border-gray-300 rounded-md p-2 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                />
              </div>

              {formData.level > 1 && (
                <div>
                  <label className="block text-xs font-semibold text-gray-700 uppercase">
                    Parent Account (Must be Level {formData.level - 1})
                  </label>
                  <select
                    required
                    value={formData.parentId || ''}
                    onChange={(e) => {
                      const pid = Number(e.target.value);
                      const parent = accounts.find((a) => a.id === pid);
                      setFormData({
                        ...formData,
                        parentId: pid,
                        accountType: parent ? parent.accountType : formData.accountType
                      });
                    }}
                    className="mt-1 w-full border border-gray-300 rounded-md p-2 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                  >
                    <option value="">-- Select Parent (Level {formData.level - 1}) --</option>
                    {validParents.map((p) => (
                      <option key={p.id} value={p.id}>
                        {p.code} - {p.name}
                      </option>
                    ))}
                  </select>
                </div>
              )}

              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase">Account Type</label>
                <select
                  disabled={formData.level > 1 && !!formData.parentId}
                  value={formData.accountType}
                  onChange={(e) => setFormData({ ...formData, accountType: Number(e.target.value) })}
                  className="mt-1 w-full border border-gray-300 rounded-md p-2 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none disabled:bg-gray-100"
                >
                  <option value={AccountType.Asset}>Asset</option>
                  <option value={AccountType.Liability}>Liability</option>
                  <option value={AccountType.Equity}>Equity</option>
                  <option value={AccountType.Revenue}>Revenue</option>
                  <option value={AccountType.Expense}>Expense</option>
                </select>
                {formData.level > 1 && (
                  <p className="text-xs text-gray-500 mt-1">Inherited automatically from parent account.</p>
                )}
              </div>

              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase">Description / Notes</label>
                <textarea
                  rows={2}
                  placeholder="Optional account notes..."
                  value={formData.description || ''}
                  onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                  className="mt-1 w-full border border-gray-300 rounded-md p-2 text-sm focus:ring-2 focus:ring-emerald-500 focus:outline-none"
                />
              </div>

              <div className="flex justify-end space-x-3 pt-3 border-t">
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={isLoading}
                  className="px-4 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded-md text-sm font-semibold shadow-sm disabled:opacity-50"
                >
                  Save Account
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
