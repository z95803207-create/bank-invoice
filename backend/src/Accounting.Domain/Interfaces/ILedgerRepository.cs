using Accounting.Domain.Entities;

namespace Accounting.Domain.Interfaces;

public interface ILedgerRepository
{
    Task<IReadOnlyList<LedgerTransaction>> GetAllAsync(string? searchQuery = null, long? accountId = null, CancellationToken cancellationToken = default);
    Task<IReadOnlyList<LedgerTransaction>> GetByAccountIdAsync(long accountId, CancellationToken cancellationToken = default);
    Task AddRangeAsync(IEnumerable<LedgerTransaction> transactions, CancellationToken cancellationToken = default);
}
