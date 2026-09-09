using Accounting.Domain.Common;

namespace Accounting.Domain.Entities;

public class Account : BaseEntity
{
    public string Code { get; set; } = string.Empty;
    public string Name { get; set; } = string.Empty;
    public int Level { get; set; } // 1, 2, 3, 4
    public long? ParentId { get; set; }
    public AccountType AccountType { get; set; }
    public string? Description { get; set; }
    public bool IsActive { get; set; } = true;

    // Computed rule: Only Level 4 accounts can receive journal/voucher entries
    public bool IsPostingAccount => Level == 4;

    // Navigation Properties
    public virtual Account? ParentAccount { get; set; }
    public virtual ICollection<Account> Children { get; set; } = new List<Account>();
    public virtual ICollection<VoucherLine> VoucherLines { get; set; } = new List<VoucherLine>();
    public virtual ICollection<LedgerTransaction> LedgerTransactions { get; set; } = new List<LedgerTransaction>();
}
