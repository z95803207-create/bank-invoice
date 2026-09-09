using Accounting.Domain.Common;

namespace Accounting.Domain.Entities;

public class Voucher : BaseEntity
{
    public string VoucherNumber { get; set; } = string.Empty;
    public string VoucherType { get; set; } = "CP"; // Cash Payment
    public DateTime VoucherDate { get; set; } = DateTime.UtcNow;
    public string? Reference { get; set; }
    public string Narration { get; set; } = string.Empty;

    public decimal TotalDebit { get; set; }
    public decimal TotalCredit { get; set; }

    public bool IsPosted { get; set; } = false;
    public DateTime? PostedAtUtc { get; set; }

    // Navigation Properties
    public virtual ICollection<VoucherLine> Lines { get; set; } = new List<VoucherLine>();
    public virtual ICollection<LedgerTransaction> LedgerTransactions { get; set; } = new List<LedgerTransaction>();
}
