using Accounting.Domain.Entities;

namespace Accounting.Domain.Interfaces;

public interface IAccountRepository
{
    Task<IReadOnlyList<Account>> GetAllAsync(CancellationToken cancellationToken = default);
    Task<IReadOnlyList<Account>> GetPostingAccountsAsync(CancellationToken cancellationToken = default);
    Task<Account?> GetByIdAsync(long id, CancellationToken cancellationToken = default);
    Task<Account?> GetByCodeAsync(string code, CancellationToken cancellationToken = default);
    Task<bool> ExistsByCodeAsync(string code, long? excludeId = null, CancellationToken cancellationToken = default);
    Task<Account> AddAsync(Account account, CancellationToken cancellationToken = default);
    Task UpdateAsync(Account account, CancellationToken cancellationToken = default);
    Task<bool> HasChildrenAsync(long id, CancellationToken cancellationToken = default);
    Task<bool> HasTransactionsAsync(long id, CancellationToken cancellationToken = default);
}
