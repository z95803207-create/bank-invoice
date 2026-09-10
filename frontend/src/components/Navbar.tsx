import React from 'react';
import { RefreshCw, CheckCircle2, AlertTriangle, Building2, Download } from 'lucide-react';
import { useAccounting } from '../context/AccountingContext';

export const Navbar: React.FC = () => {
  const { isBackendConnected, refreshAll, isLoading } = useAccounting();

  return (
    <header className="bg-slate-900 text-white border-b border-slate-800 sticky top-0 z-30 shadow-md">
      <div className="max-w-7xl mx-auto px-3 sm:px-6 lg:px-8 h-14 sm:h-16 flex items-center justify-between">
        <div className="flex items-center space-x-2.5 sm:space-x-3">
          <div className="w-8 h-8 sm:w-10 sm:h-10 rounded-lg bg-emerald-600 flex items-center justify-center shadow-md shrink-0">
            <Building2 className="w-5 h-5 sm:w-6 sm:h-6 text-white" />
          </div>
          <div>
            <h1 className="text-sm sm:text-lg font-bold tracking-tight leading-tight">Banking & Accounting</h1>
            <p className="text-[10px] sm:text-xs text-slate-400 truncate max-w-[160px] sm:max-w-none">
              ASP.NET Core 8 + React Mobile
            </p>
          </div>
        </div>

        <div className="flex items-center space-x-2 sm:space-x-3">
          <a
            href="./accounting-app.apk"
            download="bank-accounting.apk"
            className="flex items-center space-x-1 px-2.5 py-1.5 rounded-lg text-xs font-semibold bg-emerald-600 hover:bg-emerald-500 active:bg-emerald-700 text-white shadow-sm transition"
            title="Download Android APK"
          >
            <Download className="w-3.5 h-3.5 shrink-0" />
            <span className="hidden sm:inline">Download APK</span>
            <span className="sm:hidden">APK</span>
          </a>

          <div className="hidden sm:flex items-center space-x-1.5 px-2.5 py-1 rounded-full text-xs font-medium bg-slate-800 border border-slate-700">
            {isBackendConnected ? (
              <>
                <CheckCircle2 className="w-3.5 h-3.5 text-emerald-400" />
                <span className="text-emerald-300">API Online</span>
              </>
            ) : (
              <>
                <AlertTriangle className="w-3.5 h-3.5 text-amber-400" />
                <span className="text-amber-300">Ready</span>
              </>
            )}
          </div>

          <button
            onClick={() => refreshAll()}
            disabled={isLoading}
            className="p-1.5 sm:px-3 sm:py-1.5 rounded-md text-xs sm:text-sm font-medium bg-slate-800 hover:bg-slate-700 text-slate-200 transition disabled:opacity-50"
            title="Refresh Data"
          >
            <RefreshCw className={`w-3.5 h-3.5 sm:w-4 sm:h-4 ${isLoading ? 'animate-spin' : ''}`} />
          </button>
        </div>
      </div>
    </header>
  );
};
