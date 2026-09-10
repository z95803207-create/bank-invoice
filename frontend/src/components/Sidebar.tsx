import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  FolderTree,
  Receipt,
  PlusCircle,
  BookOpen,
  ListOrdered,
  Scale
} from 'lucide-react';

const navItems = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/accounts', label: 'Chart of Accounts', icon: FolderTree },
  { to: '/vouchers', label: 'CP Vouchers', icon: Receipt },
  { to: '/create-voucher', label: 'New CP Voucher', icon: PlusCircle },
  { to: '/ledger', label: 'Account Ledger', icon: BookOpen },
  { to: '/transactions', label: 'Transaction Register', icon: ListOrdered },
  { to: '/trial-balance', label: 'Trial Balance', icon: Scale },
];

export const Sidebar: React.FC = () => {
  return (
    <aside className="hidden md:flex w-64 bg-white border-r border-gray-200 min-h-[calc(100vh-4rem)] p-4 flex-col justify-between shrink-0">
      <nav className="space-y-1.5">
        <div className="px-3 py-2 text-xs font-semibold text-gray-400 uppercase tracking-wider">
          Financial Navigation
        </div>
        {navItems.map((item) => {
          const Icon = item.icon;
          return (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.to === '/'}
              className={({ isActive }) =>
                `flex items-center space-x-3 px-3.5 py-2.5 rounded-lg text-sm font-medium transition ${
                  isActive
                    ? 'bg-emerald-50 text-emerald-700 font-semibold shadow-xs'
                    : 'text-gray-700 hover:bg-gray-100'
                }`
              }
            >
              <Icon className="w-5 h-5 shrink-0" />
              <span>{item.label}</span>
            </NavLink>
          );
        })}
      </nav>

      <div className="p-3 bg-slate-50 border border-slate-200 rounded-lg text-xs text-slate-600">
        <p className="font-semibold text-slate-800">Double-Entry Standard</p>
        <p className="mt-1 text-[11px] leading-relaxed">
          Debits = Credits strictly enforced on all vouchers before General Ledger posting.
        </p>
      </div>
    </aside>
  );
};
