using Accounting.Domain.Entities;

namespace Accounting.Application.DTOs;

public class TrialBalanceDto
{
    public DateTime AsOfDate { get; set; } = DateTime.UtcNow;
    public decimal TotalDebit { get; set; }
    public decimal TotalCredit { get; set; }
    public decimal Difference => Math.Abs(TotalDebit - TotalCredit);
    public bool IsBalanced => TotalDebit == TotalCredit;
    public List<TrialBalanceRowDto> Rows { get; set; } = new();
}

public class TrialBalanceRowDto
{
    public long AccountId { get; set; }
    public string AccountCode { get; set; } = string.Empty;
    public string AccountName { get; set; } = string.Empty;
    public AccountType AccountType { get; set; }
    public decimal TotalDebit { get; set; }
    public decimal TotalCredit { get; set; }
    public decimal NetDebitBalance { get; set; }
    public decimal NetCreditBalance { get; set; }
}

public class DashboardSummaryDto
{
    public int TotalAccounts { get; set; }
    public int TotalPostingAccounts { get; set; }
    public int TotalVouchers { get; set; }
    public int PostedVouchers { get; set; }
    public int DraftVouchers { get; set; }
    public decimal TotalDebits { get; set; }
    public decimal TotalCredits { get; set; }
    public bool IsTrialBalanceBalanced { get; set; }
    public List<VoucherDto> RecentVouchers { get; set; } = new();
}
