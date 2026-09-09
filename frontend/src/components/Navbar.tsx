import React from 'react';
import { RefreshCw, CheckCircle2, AlertTriangle, Building2 } from 'lucide-react';
import { useAccounting } from '../context/AccountingContext';

export const Navbar: React.FC = () => {
  const { isBackendConnected, refreshAll, isLoading } = useAccounting();

  return (
    <header className="bg-slate-900 text-white border-b border-slate-800 sticky top-0 z-30">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        <div className="flex items-center space-x-3">
          <div className="w-10 h-10 rounded-lg bg-emerald-600 flex items-center justify-center shadow-md">
            <Building2 className="w-6 h-6 text-white" />
          </div>
          <div>
            <h1 className="text-lg font-bold tracking-tight leading-tight">General Accounting System</h1>
            <p className="text-xs text-slate-400">ASP.NET Core Web API + React Clean Architecture</p>
          </div>
        </div>

        <div className="flex items-center space-x-4">
          <div className="flex items-center space-x-2 px-3 py-1 rounded-full text-xs font-medium bg-slate-800 border border-slate-700">
            {isBackendConnected ? (
              <>
                <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
                <span className="text-emerald-300">ASP.NET API Online</span>
              </>
            ) : (
              <>
                <AlertTriangle className="w-3.5 h-3.5 text-amber-400" />
                <span className="text-amber-300">Standalone / Ready</span>
              </>
            )}
          </div>

          <button
            onClick={() => refreshAll()}
            disabled={isLoading}
            className="flex items-center space-x-1.5 px-3 py-1.5 rounded-md text-sm font-medium bg-slate-800 hover:bg-slate-700 transition disabled:opacity-50"
            title="Refresh Ledger & Accounts"
          >
            <RefreshCw className={`w-4 h-4 ${isLoading ? 'animate-spin' : ''}`} />
            <span className="hidden sm:inline">Refresh</span>
          </button>
        </div>
      </div>
    </header>
  );
};
