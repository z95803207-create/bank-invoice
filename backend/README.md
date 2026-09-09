# Accounting Application — ASP.NET Core Web API Backend

A production-grade, enterprise Clean Architecture implementation of the **Accounting Technical Test Application** built with **ASP.NET Core 8**, **Entity Framework Core**, and **SQL Server**.

---

## 1. Clean Architecture Structure

```
backend/
├── AccountingApp.sln
└── src/
    ├── Accounting.Domain/              # Enterprise Domain Entities & Business Rules
    │   ├── Common/BaseEntity.cs
    │   ├── Entities/
    │   │   ├── Account.cs              # 4-Level Chart of Accounts model
    │   │   ├── AccountType.cs          # Asset, Liability, Equity, Revenue, Expense
    │   │   ├── Voucher.cs              # Cash Payment (CP) Header
    │   │   ├── VoucherLine.cs          # Debit/Credit line items
    │   │   └── LedgerTransaction.cs    # General Ledger posted transactions
    │   ├── Exceptions/                 # Domain-specific validation exceptions
    │   └── Interfaces/                 # Repository abstractions
    │
    ├── Accounting.Application/         # Use Cases, DTOs & Business Services
    │   ├── DTOs/                       # Strongly-typed input/output models
    │   ├── Interfaces/                 # Service contracts
    │   └── Services/
    │       ├── AccountService.cs       # 4-level hierarchy integrity rules
    │       ├── VoucherService.cs       # CP voucher validation (Debit == Credit)
    │       ├── PostingService.cs       # Atomic General Ledger posting engine
    │       └── ReportService.cs        # Ledger & Trial Balance reconciliation
    │
    ├── Accounting.Infrastructure/      # Database & Persistence (EF Core)
    │   ├── Data/
    │   │   ├── AccountingDbContext.cs  # DbContext with decimal(18,2) precision
    │   │   ├── DatabaseSeeder.cs       # Standard 4-level COA & seed CP voucher
    │   │   └── Configurations/         # Fluent API entity configurations
    │   └── Repositories/               # EF Core repository implementations
    │
    └── Accounting.Api/                 # REST API & Presentation Layer
        ├── Controllers/
        │   ├── AccountsController.cs   # /api/accounts
        │   ├── VouchersController.cs   # /api/vouchers
        │   ├── LedgerController.cs     # /api/ledger
        │   ├── ReportsController.cs    # /api/reports/trial-balance
        │   └── DashboardController.cs  # /api/dashboard/summary
        ├── Program.cs                  # DI, CORS, Swagger OpenAPI configuration
        └── appsettings.json            # SQL Server Connection Strings
```

---

## 2. Core Accounting Rules Enforced

1. **Four-Level Chart of Accounts**:
   - **Level 1 (Major Head)**: Assets (1000), Liabilities (2000), Equity (3000), Revenue (4000), Expenses (5000). Cannot have a parent.
   - **Level 2 (Sub-head)**: Must have a Level 1 parent.
   - **Level 3 (Control Group)**: Must have a Level 2 parent.
   - **Level 4 (Posting Account)**: Must have a Level 3 parent. Only Level 4 accounts can receive journal entries.
   - **Hierarchy Integrity**: Rejects duplicate codes, skipping parent levels, or circular dependencies.

2. **Cash Payment (CP) Voucher**:
   - Auto-sequences identifiers (`CP-000001`, `CP-000002`).
   - Line-level validation: Prevents negative amounts, simultaneous debit/credit on one line, and zero amounts.
   - Header-level validation: Enforces strict double-entry equality ($\sum \text{Debits} = \sum \text{Credits}$).

3. **Atomic Posting Engine**:
   - Executes inside a transaction scope.
   - Validates voucher state and balance.
   - Inserts balanced debit and credit entries into `LedgerTransactions`.
   - Transitions `IsPosted = true` and records `PostedAtUtc`.
   - Prevents duplicate posting or modification of posted vouchers.

4. **Trial Balance Reconciliation**:
   - Sums all posted ledger debits and credits per posting account.
   - Verifies that Total Debits equal Total Credits.

---

## 3. How to Run Locally

### Prerequisites
- [.NET 8 SDK](https://dotnet.microsoft.com/download/dotnet/8.0)
- [Microsoft SQL Server](https://www.microsoft.com/sql-server/) (or SQL Server Express / LocalDB)

### Steps
1. Navigate to the API project folder:
   ```bash
   cd backend/src/Accounting.Api
   ```

2. Verify your SQL Server connection string in `appsettings.json`:
   ```json
   "ConnectionStrings": {
     "DefaultConnection": "Server=localhost;Database=AccountingDb;Trusted_Connection=True;MultipleActiveResultSets=true;TrustServerCertificate=True"
   }
   ```

3. Restore dependencies and run the Web API:
   ```bash
   dotnet restore
   dotnet run
   ```

4. Open Swagger UI in your browser:
   - `https://localhost:7001` or `http://localhost:5000`
   - Test endpoints interactively.

---

## 4. Connecting with React Frontend

The API has CORS configured for `http://localhost:5173` (Vite) and `http://localhost:3000` (Create React App).

In your React app:
```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'https://localhost:7001/api'
});

// Accounts
export const getAccounts = () => api.get('/accounts');
export const getPostingAccounts = () => api.get('/accounts/posting');
export const createAccount = (data) => api.post('/accounts', data);

// Vouchers
export const getVouchers = () => api.get('/vouchers');
export const createVoucher = (data) => api.post('/vouchers', data);
export const postVoucher = (id) => api.post(`/vouchers/${id}/post`);

// Reports & Dashboard
export const getTrialBalance = () => api.get('/reports/trial-balance');
export const getLedger = (accountId) => api.get(`/ledger/${accountId}`);
export const getTransactionRegister = (search, accountId) => 
  api.get('/ledger/transactions', { params: { search, accountId } });
export const getDashboardSummary = () => api.get('/dashboard/summary');
```
