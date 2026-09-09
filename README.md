# Enterprise Double-Entry Accounting System

A production-ready financial accounting and voucher management platform featuring a **Clean Architecture ASP.NET Core 8 Web API**, a modern **React 18 + TypeScript Web Client**, and a native **Android Application (Kotlin + Jetpack Compose)**.

---

## 📁 Repository Structure Overview

When opening this project, the codebase is organized into three distinct tiers:

```
├── backend/                      # ASP.NET Core 8 Clean Architecture Web API
│   ├── AccountingApp.sln         # Visual Studio Solution file (Double-click to open)
│   ├── README.md                 # Backend detailed documentation & architecture notes
│   └── src/
│       ├── Accounting.Domain/    # Domain Entities (Account, Voucher, Ledger) & Rules
│       ├── Accounting.Application/# Services (Posting Engine, Report Engine) & DTOs
│       ├── Accounting.Infrastructure/# EF Core DbContext, Configurations & Seeder
│       └── Accounting.Api/       # Controllers, Swagger UI, CORS & Program.cs
│
├── frontend/                     # React 18 + TypeScript + Tailwind CSS Web Client
│   ├── package.json
│   ├── vite.config.ts            # Configured proxy to ASP.NET Core API (:5000)
│   ├── README.md                 # Frontend documentation
│   └── src/                      # Dashboard, Chart of Accounts, Vouchers & Ledger
│
└── app/                          # Native Android Mobile Application
    └── src/main/java/com/example/# Kotlin + Jetpack Compose + Room Local DB
```

---

## 🚀 Quick Start Guide for Clients & Evaluators

### 1. Running the ASP.NET Core 8 Backend

#### Option A: Using Visual Studio (Recommended & Easiest)
1. Navigate to the `backend/` folder.
2. Double-click **`AccountingApp.sln`** to open in Visual Studio 2022.
3. In Solution Explorer, right-click **`Accounting.Api`** and select **Set as Startup Project**.
4. Press **`F5`** (or click the green **Run** button).
5. The Swagger API documentation UI will automatically open in your default browser at:
   - **`https://localhost:7001/swagger`** or **`http://localhost:5000/swagger`**

#### Option B: Using .NET CLI / VS Code / Terminal
```bash
# 1. Navigate to the API directory
cd backend/src/Accounting.Api

# 2. Restore NuGet dependencies
dotnet restore

# 3. Launch the API
dotnet run
```
*Note: On first startup, the application automatically creates the SQL database and seeds standard 4-Level Chart of Accounts with sample transactions (`DatabaseSeeder.cs`).*

---

### 2. Running the React Web Client

```bash
# 1. Open a new terminal in the frontend directory
cd frontend

# 2. Install dependencies
npm install

# 3. Start the development server
npm run dev
```
- Open **`http://localhost:3000`** in your browser.
- The web app automatically detects the ASP.NET Core backend and reflects real-time status:
  - **`ASP.NET API Online`**: Connected to your live C# Web API.
  - **`Standalone / Ready`**: Includes an offline-ready in-memory mock engine so all features can be explored even before setting up SQL Server.

---

### 3. Running the Android Application

1. Open Android Studio and choose **Open Project**.
2. Select the root folder.
3. Gradle will sync automatically.
4. Click **Run** (`Shift + F10`) to launch on your Android Emulator or physical device.

---

## 📊 Core Accounting Business Logic Implemented

### 1. 4-Level Chart of Accounts (COA) Hierarchy
Enforces strict accounting hierarchy rules:
- **Level 1 (Major Head)**: Assets (1000), Liabilities (2000), Equity (3000), Revenue (4000), Expenses (5000).
- **Level 2 (Sub-Head)**: e.g. Current Assets (1100), Current Liabilities (2100).
- **Level 3 (Control Group)**: e.g. Cash and Bank Balances (1110), Accounts Payable (2110).
- **Level 4 (Posting Account)**: Only Level 4 accounts can receive journal entries (e.g., Pettty Cash 1111, Main Bank Account 1112).
- Validates parent-child relationships and prevents circular or invalid parent links.

### 2. Cash Payment (CP) Voucher & Double-Entry Balance
- Auto-generates sequential voucher codes (`CP-000001`, `CP-000002`).
- Real-time client & server validation ensuring Total Debits strictly equal Total Credits ($\sum \text{Debit} = \sum \text{Credit}$).
- Validates that accounts belong strictly to Level 4.

### 3. Atomic General Ledger Posting Engine
- Executes in an ACID database transaction.
- Prevents tampering or duplicate posting once a voucher is committed.
- Produces balanced journal ledger entries.

### 4. Financial Statements & Reports
- **General Ledger Account Statement**: Filter by any posting account and date range with progressive running balance (Dr / Cr).
- **Transaction Register**: Chronological audit trail of all posted transactions.
- **Trial Balance Report**: Full reconciliation statement verifying equality of all debit and credit balances across the enterprise.

---

## 🛠️ Technology Stack Summary

| Layer | Technologies Used |
|---|---|
| **Backend API** | ASP.NET Core 8, Entity Framework Core 8, C#, SQL Server / LocalDB, Swagger / OpenAPI, Clean Architecture |
| **Frontend Web** | React 18, TypeScript, Tailwind CSS, Vite, Lucide Icons, Axios |
| **Mobile App** | Kotlin, Jetpack Compose, Material 3, Android Room Database, StateFlow |

---

## 📬 API Endpoints Reference

| HTTP Method | Route | Description |
|---|---|---|
| `GET` | `/api/accounts` | Retrieve full 4-level Chart of Accounts hierarchy |
| `GET` | `/api/accounts/posting` | List only Level 4 accounts permitted for vouchers |
| `POST` | `/api/accounts` | Create new account validating level hierarchy |
| `GET` | `/api/vouchers` | List Cash Payment vouchers (Posted & Draft) |
| `POST` | `/api/vouchers` | Create new voucher with double-entry lines |
| `POST` | `/api/vouchers/{id}/post` | Atomically commit voucher to General Ledger |
| `GET` | `/api/ledger/{accountId}` | Get account ledger statement with running balance |
| `GET` | `/api/ledger/transactions` | Full chronological transaction audit register |
| `GET` | `/api/reports/trial-balance`| Reconciled Trial Balance statement |
| `GET` | `/api/dashboard/summary` | Financial KPIs and double-entry health status |
