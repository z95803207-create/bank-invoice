using Accounting.Domain.Entities;
using Accounting.Domain.Interfaces;
using Accounting.Infrastructure.Data;
using Microsoft.EntityFrameworkCore;

namespace Accounting.Infrastructure.Repositories;

public class LedgerRepository : ILedgerRepository
{
    private readonly AccountingDbContext _context;

    public LedgerRepository(AccountingDbContext context)
    {
        _context = context;
    }

    public async Task<IReadOnlyList<LedgerTransaction>> GetAllAsync(string? searchQuery = null, long? accountId = null, CancellationToken cancellationToken = default)
    {
        var query = _context.LedgerTransactions
            .Include(lt => lt.Account)
            .Include(lt => lt.Voucher)
            .AsQueryable();

        if (accountId.HasValue)
        {
            query = query.Where(lt => lt.AccountId == accountId.Value);
        }

        if (!string.IsNullOrWhiteSpace(searchQuery))
        {
            var term = searchQuery.Trim().ToLower();
            query = query.Where(lt =>
                lt.VoucherNumber.ToLower().Contains(term) ||
                lt.Account.Code.ToLower().Contains(term) ||
                lt.Account.Name.ToLower().Contains(term) ||
                lt.Description.ToLower().Contains(term));
        }

        return await query
            .OrderByDescending(lt => lt.Date)
            .ThenByDescending(lt => lt.Id)
            .ToListAsync(cancellationToken);
    }

    public async Task<IReadOnlyList<LedgerTransaction>> GetByAccountIdAsync(long accountId, CancellationToken cancellationToken = default)
    {
        return await _context.LedgerTransactions
            .Include(lt => lt.Account)
            .Include(lt => lt.Voucher)
            .Where(lt => lt.AccountId == accountId)
            .OrderBy(lt => lt.Date)
            .ThenBy(lt => lt.Id)
            .ToListAsync(cancellationToken);
    }

    public async Task AddRangeAsync(IEnumerable<LedgerTransaction> transactions, CancellationToken cancellationToken = default)
    {
        await _context.LedgerTransactions.AddRangeAsync(transactions, cancellationToken);
        await _context.SaveChangesAsync(cancellationToken);
    }
}
