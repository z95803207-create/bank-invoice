using Accounting.Domain.Entities;

namespace Accounting.Application.DTOs;

public class AccountDto
{
    public long Id { get; set; }
    public string Code { get; set; } = string.Empty;
    public string Name { get; set; } = string.Empty;
    public int Level { get; set; }
    public long? ParentId { get; set; }
    public string? ParentName { get; set; }
    public AccountType AccountType { get; set; }
    public string AccountTypeName => AccountType.ToString();
    public string? Description { get; set; }
    public bool IsActive { get; set; }
    public bool IsPostingAccount => Level == 4;
}

public class CreateAccountDto
{
    public string Code { get; set; } = string.Empty;
    public string Name { get; set; } = string.Empty;
    public int Level { get; set; }
    public long? ParentId { get; set; }
    public AccountType AccountType { get; set; }
    public string? Description { get; set; }
}

public class UpdateAccountDto
{
    public string Name { get; set; } = string.Empty;
    public string? Description { get; set; }
    public bool IsActive { get; set; } = true;
}
