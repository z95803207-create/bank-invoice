using Accounting.Domain.Entities;

namespace Accounting.Domain.Interfaces;

public interface IVoucherRepository
{
    Task<IReadOnlyList<Voucher>> GetAllAsync(CancellationToken cancellationToken = default);
    Task<Voucher?> GetByIdAsync(long id, CancellationToken cancellationToken = default);
    Task<Voucher?> GetByNumberAsync(string voucherNumber, CancellationToken cancellationToken = default);
    Task<string> GenerateNextVoucherNumberAsync(string prefix = "CP", CancellationToken cancellationToken = default);
    Task<Voucher> AddAsync(Voucher voucher, CancellationToken cancellationToken = default);
    Task UpdateAsync(Voucher voucher, CancellationToken cancellationToken = default);
}
