import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import { AccountingProvider } from './context/AccountingContext';
import { Navbar } from './components/Navbar';
import { Sidebar } from './components/Sidebar';
import { AlertBanner } from './components/AlertBanner';
import { Dashboard } from './pages/Dashboard';
import { ChartOfAccounts } from './pages/ChartOfAccounts';
import { CreateVoucher } from './pages/CreateVoucher';
import { VouchersList } from './pages/VouchersList';
import { AccountLedger } from './pages/AccountLedger';
import { TransactionRegister } from './pages/TransactionRegister';
import { TrialBalance } from './pages/TrialBalance';

export const App: React.FC = () => {
  return (
    <AccountingProvider>
      <BrowserRouter>
        <div className="min-h-screen bg-slate-50 flex flex-col font-sans text-slate-900">
          <Navbar />
          <div className="flex flex-1">
            <Sidebar />
            <main className="flex-1 p-6 md:p-8 max-w-7xl mx-auto w-full">
              <Routes>
                <Route path="/" element={<Dashboard />} />
                <Route path="/accounts" element={<ChartOfAccounts />} />
                <Route path="/vouchers" element={<VouchersList />} />
                <Route path="/create-voucher" element={<CreateVoucher />} />
                <Route path="/ledger" element={<AccountLedger />} />
                <Route path="/transactions" element={<TransactionRegister />} />
                <Route path="/trial-balance" element={<TrialBalance />} />
              </Routes>
            </main>
          </div>
          <AlertBanner />
        </div>
      </BrowserRouter>
    </AccountingProvider>
  );
};

export default App;
