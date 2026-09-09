# Accounting Frontend — React + TypeScript + Tailwind CSS

A modern, responsive React web application built with **React 18**, **TypeScript**, **Tailwind CSS**, and **Vite**, designed to integrate with the **ASP.NET Core 8 Clean Architecture Web API**.

---

## 1. Features

1. **Dashboard**:
   - Live double-entry health status banner (Reconciled vs Out of Balance).
   - Gross debit and credit metrics.
   - Quick navigation to create vouchers and view reports.
   - *Note: Cleaned up — No "API" clutter in navigation or dashboard.*

2. **4-Level Chart of Accounts (`/accounts`)**:
   - **Level 1**: Major Head (Assets, Liabilities, Equity, Revenue, Expenses).
   - **Level 2**: Sub-Head (e.g. Current Assets, Current Liabilities).
   - **Level 3**: Control Group (e.g. Cash and Bank Balances).
   - **Level 4**: Posting Account (Only level permitted to take journal entries).
   - Search by code/name, filter by level and account type.
   - Add new account modal enforcing strict parent-child level hierarchy.

3. **Cash Payment (CP) Voucher Entry (`/create-voucher`)**:
   - Voucher Date, Reference Number, and Narration.
   - Dynamic multi-row accounting lines filtered to Level 4 posting accounts.
   - Real-time client-side double-entry balance check ($\sum \text{Debits} = \sum \text{Credits}$).
   - Atomic General Ledger posting option.

4. **Vouchers Register (`/vouchers`)**:
   - Filter by status (Posted vs Draft) and search by narration.
   - View detailed lines for any voucher in a modal.
   - One-click "Post to Ledger" action.

5. **General Ledger Account Statement (`/ledger`)**:
   - Filter by any Level 4 posting account and date range.
   - Running balance calculated according to normal account balance rules (Assets/Expenses vs Liabilities/Equity/Revenue).

6. **Transaction Register (`/transactions`)**:
   - Chronological audit trail of all posted debit and credit ledger entries.

7. **Trial Balance Report (`/trial-balance`)**:
   - Verification report showing total debits, total credits, and net balances for all posting accounts.
   - Print / PDF export support.

---

## 2. Quick Start

### Prerequisites
- [Node.js](https://nodejs.org/) (version 18+ or 20+)
- npm or yarn

### Steps to Run
```bash
# 1. Navigate to the frontend directory
cd frontend

# 2. Install dependencies
npm install

# 3. Start development server
npm run dev
```

The application will be running at `http://localhost:3000`.

---

## 3. Connecting to ASP.NET Core Backend

When the ASP.NET Core API is running on `http://localhost:5000` (or `https://localhost:5001`), the Vite dev server automatically proxies `/api` calls directly to the backend.

If running in production, set the environment variable:
```env
VITE_API_URL=http://your-backend-api-url/api
```

The application also includes automatic offline/standalone fallback mode so all features can be explored immediately even before launching the database.
