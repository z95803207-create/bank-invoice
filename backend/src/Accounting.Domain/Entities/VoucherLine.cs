using Accounting.Domain.Common;

namespace Accounting.Domain.Entities;

public class VoucherLine : BaseEntity
{
    public long VoucherId { get; set; }
    public long AccountId { get; set; }
    public string? Description { get; set; }

    public decimal Debit { get; set; }
    public decimal Credit { get; set; }

    // Navigation Properties
    public virtual Voucher Voucher { get; set; } = null!;
    public virtual Account Account { get; set; } = null!;
}
