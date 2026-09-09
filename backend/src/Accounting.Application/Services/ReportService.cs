using Accounting.Application.DTOs;
using Accounting.Application.Interfaces;
using Accounting.Domain.Entities;
using Accounting.Domain.Interfaces;

namespace Accounting.Application.Services;

public class ReportService : IReportService
{
    private readonly IAccountRepository _accountRepository;
    private readonly IVoucherRepository _voucherRepository;
    private readonly ILedgerRepository _ledgerRepository;

    public ReportService(
        IAccountRepository accountRepository,
        IVoucherRepository voucherRepository,
        ILedgerRepository ledgerRepository)
    {
        _accountRepository = accountRepository;
        _voucherRepository = voucherRepository;
        _ledgerRepository = ledgerRepository;
    }

    public async Task<IReadOnlyList<LedgerTransactionDto>> GetAccountLedgerAsync(long accountId, CancellationToken cancellationToken = default)
    {
        var account = await _accountRepository.GetByIdAsync(accountId, cancellationToken);
        if (account == null)
            throw new KeyNotFoundException($"Account with ID {accountId} was not found.");

        var txs = await _ledgerRepository.GetByAccountIdAsync(accountId, cancellationToken);
        var isDebitNormal = account.AccountType.GetNormalBalance() == NormalBalance.Debit;

        decimal runningBalance = 0m;
        var result = new List<LedgerTransactionDto>();

        foreach (var tx in txs.OrderBy(t => t.Date).ThenBy(t => t.Id))
        {
            if (isDebitNormal)
            {
                runningBalance += (tx.Debit - tx.Credit);
            }
            else
            {
                runningBalance += (tx.Credit - tx.Debit);
            }

            result.Add(new LedgerTransactionDto
            {
                Id = tx.Id,
                Date = tx.Date,
                VoucherId = tx.VoucherId,
                VoucherNumber = tx.VoucherNumber,
                VoucherType = tx.VoucherType,
                AccountId = tx.AccountId,
                AccountCode = account.Code,
                AccountName = account.Name,
                Description = tx.Description,
                Debit = tx.Debit,
                Credit = tx.Credit,
                RunningBalance = runningBalance
            });
        }

        return result;
    }

    public async Task<IReadOnlyList<LedgerTransactionDto>> GetTransactionRegisterAsync(string? search = null, long? accountId = null, CancellationToken cancellationToken = default)
    {
        var txs = await _ledgerRepository.GetAllAsync(search, accountId, cancellationToken);
        return txs.Select(tx => new LedgerTransactionDto
        {
            Id = tx.Id,
            Date = tx.Date,
            VoucherId = tx.VoucherId,
            VoucherNumber = tx.VoucherNumber,
            VoucherType = tx.VoucherType,
            AccountId = tx.AccountId,
            AccountCode = tx.Account?.Code ?? string.Empty,
            AccountName = tx.Account?.Name ?? string.Empty,
            Description = tx.Description,
            Debit = tx.Debit,
            Credit = tx.Credit
        }).ToList();
    }

    public async Task<TrialBalanceDto> GetTrialBalanceAsync(CancellationToken cancellationToken = default)
    {
        var postingAccounts = await _accountRepository.GetPostingAccountsAsync(cancellationToken);
        var allTx = await _ledgerRepository.GetAllAsync(null, null, cancellationToken);

        var rows = new List<TrialBalanceRowDto>();
        decimal grandTotalDebit = 0m;
        decimal grandTotalCredit = 0m;

        foreach (var acc in postingAccounts.OrderBy(a => a.Code))
        {
            var accTxs = allTx.Where(t => t.AccountId == acc.Id).ToList();
            decimal totalDebit = accTxs.Sum(t => t.Debit);
            decimal totalCredit = accTxs.Sum(t => t.Credit);

            decimal netDebit = 0m;
            decimal netCredit = 0m;

            if (totalDebit > totalCredit)
            {
                netDebit = totalDebit - totalCredit;
            }
            else if (totalCredit > totalDebit)
            {
                netCredit = totalCredit - totalDebit;
            }

            grandTotalDebit += netDebit;
            grandTotalCredit += netCredit;

            rows.Add(new TrialBalanceRowDto
            {
                AccountId = acc.Id,
                AccountCode = acc.Code,
                AccountName = acc.Name,
                AccountType = acc.AccountType,
                TotalDebit = totalDebit,
                TotalCredit = totalCredit,
                NetDebitBalance = netDebit,
                NetCreditBalance = netCredit
            });
        }

        return new TrialBalanceDto
        {
            AsOfDate = DateTime.UtcNow,
            TotalDebit = grandTotalDebit,
            TotalCredit = grandTotalCredit,
            Rows = rows
        };
    }

    public async Task<DashboardSummaryDto> GetDashboardSummaryAsync(CancellationToken cancellationToken = default)
    {
        var accounts = await _accountRepository.GetAllAsync(cancellationToken);
        var vouchers = await _voucherRepository.GetAllAsync(cancellationToken);
        var allTx = await _ledgerRepository.GetAllAsync(null, null, cancellationToken);

        var tb = await GetTrialBalanceAsync(cancellationToken);

        return new DashboardSummaryDto
        {
            TotalAccounts = accounts.Count,
            TotalPostingAccounts = accounts.Count(a => a.Level == 4),
            TotalVouchers = vouchers.Count,
            PostedVouchers = vouchers.Count(v => v.IsPosted),
            DraftVouchers = vouchers.Count(v => !v.IsPosted),
            TotalDebits = allTx.Sum(t => t.Debit),
            TotalCredits = allTx.Sum(t => t.Credit),
            IsTrialBalanceBalanced = tb.IsBalanced,
            RecentVouchers = vouchers.OrderByDescending(v => v.VoucherDate)
                .Take(5)
                .Select(v => new VoucherDto
                {
                    Id = v.Id,
                    VoucherNumber = v.VoucherNumber,
                    VoucherType = v.VoucherType,
                    VoucherDate = v.VoucherDate,
                    Reference = v.Reference,
                    Narration = v.Narration,
                    TotalDebit = v.TotalDebit,
                    TotalCredit = v.TotalCredit,
                    IsPosted = v.IsPosted
                }).ToList()
        };
    }
}
