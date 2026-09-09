namespace Accounting.Application.DTOs;

public class VoucherDto
{
    public long Id { get; set; }
    public string VoucherNumber { get; set; } = string.Empty;
    public string VoucherType { get; set; } = "CP";
    public DateTime VoucherDate { get; set; }
    public string? Reference { get; set; }
    public string Narration { get; set; } = string.Empty;
    public decimal TotalDebit { get; set; }
    public decimal TotalCredit { get; set; }
    public decimal Difference => TotalDebit - TotalCredit;
    public bool IsBalanced => TotalDebit > 0 && TotalDebit == TotalCredit;
    public bool IsPosted { get; set; }
    public DateTime? PostedAtUtc { get; set; }
    public List<VoucherLineDto> Lines { get; set; } = new();
}

public class VoucherLineDto
{
    public long Id { get; set; }
    public long AccountId { get; set; }
    public string AccountCode { get; set; } = string.Empty;
    public string AccountName { get; set; } = string.Empty;
    public string? Description { get; set; }
    public decimal Debit { get; set; }
    public decimal Credit { get; set; }
}

public class CreateVoucherDto
{
    public DateTime VoucherDate { get; set; } = DateTime.UtcNow;
    public string? Reference { get; set; }
    public string Narration { get; set; } = string.Empty;
    public bool PostImmediately { get; set; } = false;
    public List<CreateVoucherLineDto> Lines { get; set; } = new();
}

public class CreateVoucherLineDto
{
    public long AccountId { get; set; }
    public string? Description { get; set; }
    public decimal Debit { get; set; }
    public decimal Credit { get; set; }
}
