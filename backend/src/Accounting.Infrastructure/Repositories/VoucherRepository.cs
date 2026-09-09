using Accounting.Domain.Entities;
using Accounting.Domain.Interfaces;
using Accounting.Infrastructure.Data;
using Microsoft.EntityFrameworkCore;

namespace Accounting.Infrastructure.Repositories;

public class VoucherRepository : IVoucherRepository
{
    private readonly AccountingDbContext _context;

    public VoucherRepository(AccountingDbContext context)
    {
        _context = context;
    }

    public async Task<IReadOnlyList<Voucher>> GetAllAsync(CancellationToken cancellationToken = default)
    {
        return await _context.Vouchers
            .Include(v => v.Lines)
                .ThenInclude(l => l.Account)
            .OrderByDescending(v => v.VoucherDate)
            .ThenByDescending(v => v.Id)
            .ToListAsync(cancellationToken);
    }

    public async Task<Voucher?> GetByIdAsync(long id, CancellationToken cancellationToken = default)
    {
        return await _context.Vouchers
            .Include(v => v.Lines)
                .ThenInclude(l => l.Account)
            .FirstOrDefaultAsync(v => v.Id == id, cancellationToken);
    }

    public async Task<Voucher?> GetByNumberAsync(string voucherNumber, CancellationToken cancellationToken = default)
    {
        return await _context.Vouchers
            .Include(v => v.Lines)
                .ThenInclude(l => l.Account)
            .FirstOrDefaultAsync(v => v.VoucherNumber == voucherNumber, cancellationToken);
    }

    public async Task<string> GenerateNextVoucherNumberAsync(string prefix = "CP", CancellationToken cancellationToken = default)
    {
        var lastVoucher = await _context.Vouchers
            .Where(v => v.VoucherNumber.StartsWith($"{prefix}-"))
            .OrderByDescending(v => v.Id)
            .Select(v => v.VoucherNumber)
            .FirstOrDefaultAsync(cancellationToken);

        int nextSequence = 1;
        if (!string.IsNullOrEmpty(lastVoucher))
        {
            var parts = lastVoucher.Split('-');
            if (parts.Length == 2 && int.TryParse(parts[1], out int current))
            {
                nextSequence = current + 1;
            }
        }

        return $"{prefix}-{nextSequence:D6}";
    }

    public async Task<Voucher> AddAsync(Voucher voucher, CancellationToken cancellationToken = default)
    {
        await _context.Vouchers.AddAsync(voucher, cancellationToken);
        await _context.SaveChangesAsync(cancellationToken);
        return voucher;
    }

    public async Task UpdateAsync(Voucher voucher, CancellationToken cancellationToken = default)
    {
        _context.Vouchers.Update(voucher);
        await _context.SaveChangesAsync(cancellationToken);
    }
}
