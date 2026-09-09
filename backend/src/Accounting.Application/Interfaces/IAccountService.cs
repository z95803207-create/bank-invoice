using Accounting.Application.DTOs;

namespace Accounting.Application.Interfaces;

public interface IAccountService
{
    Task<IReadOnlyList<AccountDto>> GetAllAccountsAsync(CancellationToken cancellationToken = default);
    Task<IReadOnlyList<AccountDto>> GetPostingAccountsAsync(CancellationToken cancellationToken = default);
    Task<AccountDto> GetByIdAsync(long id, CancellationToken cancellationToken = default);
    Task<AccountDto> CreateAccountAsync(CreateAccountDto dto, CancellationToken cancellationToken = default);
    Task<AccountDto> UpdateAccountAsync(long id, UpdateAccountDto dto, CancellationToken cancellationToken = default);
}

public interface IVoucherService
{
    Task<IReadOnlyList<VoucherDto>> GetAllVouchersAsync(CancellationToken cancellationToken = default);
    Task<VoucherDto> GetByIdAsync(long id, CancellationToken cancellationToken = default);
    Task<VoucherDto> CreateVoucherAsync(CreateVoucherDto dto, CancellationToken cancellationToken = default);
}

public interface IPostingService
{
    Task<VoucherDto> PostVoucherAsync(long voucherId, CancellationToken cancellationToken = default);
}

public interface IReportService
{
    Task<IReadOnlyList<LedgerTransactionDto>> GetAccountLedgerAsync(long accountId, CancellationToken cancellationToken = default);
    Task<IReadOnlyList<LedgerTransactionDto>> GetTransactionRegisterAsync(string? search = null, long? accountId = null, CancellationToken cancellationToken = default);
    Task<TrialBalanceDto> GetTrialBalanceAsync(CancellationToken cancellationToken = default);
    Task<DashboardSummaryDto> GetDashboardSummaryAsync(CancellationToken cancellationToken = default);
}
