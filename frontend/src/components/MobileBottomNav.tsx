import React from 'react';
import { NavLink } from 'react-router-dom';
import {
  LayoutDashboard,
  FolderTree,
  PlusCircle,
  Receipt,
  Scale
} from 'lucide-react';

const mobileNavItems = [
  { to: '/', label: 'Dashboard', icon: LayoutDashboard },
  { to: '/accounts', label: 'Accounts', icon: FolderTree },
  { to: '/create-voucher', label: 'New CP', icon: PlusCircle, isAction: true },
  { to: '/vouchers', label: 'Vouchers', icon: Receipt },
  { to: '/trial-balance', label: 'Reports', icon: Scale },
];

export const MobileBottomNav: React.FC = () => {
  return (
    <nav className="md:hidden fixed bottom-0 left-0 right-0 z-40 bg-white border-t border-slate-200 shadow-lg px-2 py-1 flex items-center justify-around">
      {mobileNavItems.map((item) => {
        const Icon = item.icon;
        if (item.isAction) {
          return (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `flex flex-col items-center justify-center -mt-5 transition group`
              }
            >
              {({ isActive }) => (
                <>
                  <div
                    className={`w-12 h-12 rounded-full flex items-center justify-center shadow-lg transition transform active:scale-95 ${
                      isActive
                        ? 'bg-emerald-700 ring-4 ring-emerald-100 text-white'
                        : 'bg-emerald-600 text-white hover:bg-emerald-700'
                    }`}
                  >
                    <Icon className="w-6 h-6" />
                  </div>
                  <span className="text-[10px] font-bold mt-0.5 text-emerald-700">
                    {item.label}
                  </span>
                </>
              )}
            </NavLink>
          );
        }

        return (
          <NavLink
            key={item.to}
            to={item.to}
            end={item.to === '/'}
            className={({ isActive }) =>
              `flex flex-col items-center justify-center py-1.5 px-3 rounded-lg text-xs font-medium transition ${
                isActive
                  ? 'text-emerald-700 font-bold'
                  : 'text-slate-500 hover:text-slate-800'
              }`
            }
          >
            {({ isActive }) => (
              <>
                <Icon
                  className={`w-5 h-5 mb-0.5 ${
                    isActive ? 'text-emerald-600 stroke-[2.5]' : 'text-slate-400'
                  }`}
                />
                <span className="text-[11px] leading-tight">{item.label}</span>
              </>
            )}
          </NavLink>
        );
      })}
    </nav>
  );
};
