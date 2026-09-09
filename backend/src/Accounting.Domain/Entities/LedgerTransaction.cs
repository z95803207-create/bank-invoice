using Accounting.Domain.Common;

namespace Accounting.Domain.Entities;

public class LedgerTransaction : BaseEntity
{
    public DateTime Date { get; set; }
    public long VoucherId { get; set; }
    public string VoucherNumber { get; set; } = string.Empty;
    public string VoucherType { get; set; } = "CP";

    public long AccountId { get; set; }
    public string Description { get; set; } = string.Empty;

    public decimal Debit { get; set; }
    public decimal Credit { get; set; }

    // Navigation Properties
    public virtual Voucher Voucher { get; set; } = null!;
    public virtual Account Account { get; set; } = null!;
}
