using Accounting.Domain.Entities;
using Accounting.Domain.Interfaces;
using Accounting.Infrastructure.Data;
using Microsoft.EntityFrameworkCore;

namespace Accounting.Infrastructure.Repositories;

public class AccountRepository : IAccountRepository
{
    private readonly AccountingDbContext _context;

    public AccountRepository(AccountingDbContext context)
    {
        _context = context;
    }

    public async Task<IReadOnlyList<Account>> GetAllAsync(CancellationToken cancellationToken = default)
    {
        return await _context.Accounts
            .Include(a => a.ParentAccount)
            .OrderBy(a => a.Code)
            .ToListAsync(cancellationToken);
    }

    public async Task<IReadOnlyList<Account>> GetPostingAccountsAsync(CancellationToken cancellationToken = default)
    {
        return await _context.Accounts
            .Include(a => a.ParentAccount)
            .Where(a => a.Level == 4 && a.IsActive)
            .OrderBy(a => a.Code)
            .ToListAsync(cancellationToken);
    }

    public async Task<Account?> GetByIdAsync(long id, CancellationToken cancellationToken = default)
    {
        return await _context.Accounts
            .Include(a => a.ParentAccount)
            .FirstOrDefaultAsync(a => a.Id == id, cancellationToken);
    }

    public async Task<Account?> GetByCodeAsync(string code, CancellationToken cancellationToken = default)
    {
        return await _context.Accounts
            .Include(a => a.ParentAccount)
            .FirstOrDefaultAsync(a => a.Code == code, cancellationToken);
    }

    public async Task<bool> ExistsByCodeAsync(string code, long? excludeId = null, CancellationToken cancellationToken = default)
    {
        return await _context.Accounts
            .AnyAsync(a => a.Code == code && (!excludeId.HasValue || a.Id != excludeId.Value), cancellationToken);
    }

    public async Task<Account> AddAsync(Account account, CancellationToken cancellationToken = default)
    {
        await _context.Accounts.AddAsync(account, cancellationToken);
        await _context.SaveChangesAsync(cancellationToken);
        return account;
    }

    public async Task UpdateAsync(Account account, CancellationToken cancellationToken = default)
    {
        _context.Accounts.Update(account);
        await _context.SaveChangesAsync(cancellationToken);
    }

    public async Task<bool> HasChildrenAsync(long id, CancellationToken cancellationToken = default)
    {
        return await _context.Accounts.AnyAsync(a => a.ParentId == id, cancellationToken);
    }

    public async Task<bool> HasTransactionsAsync(long id, CancellationToken cancellationToken = default)
    {
        return await _context.LedgerTransactions.AnyAsync(lt => lt.AccountId == id, cancellationToken);
    }
}
