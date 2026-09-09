namespace Accounting.Domain.Entities;

public enum AccountType
{
    Asset = 1,
    Liability = 2,
    Equity = 3,
    Revenue = 4,
    Expense = 5
}

public enum NormalBalance
{
    Debit = 1,
    Credit = 2
}

public static class AccountTypeExtensions
{
    public static NormalBalance GetNormalBalance(this AccountType type) => type switch
    {
        AccountType.Asset => NormalBalance.Debit,
        AccountType.Expense => NormalBalance.Debit,
        AccountType.Liability => NormalBalance.Credit,
        AccountType.Equity => NormalBalance.Credit,
        AccountType.Revenue => NormalBalance.Credit,
        _ => NormalBalance.Debit
    };
}
