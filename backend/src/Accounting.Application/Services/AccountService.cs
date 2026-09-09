using Accounting.Application.DTOs;
using Accounting.Application.Interfaces;
using Accounting.Domain.Entities;
using Accounting.Domain.Exceptions;
using Accounting.Domain.Interfaces;

namespace Accounting.Application.Services;

public class AccountService : IAccountService
{
    private readonly IAccountRepository _accountRepository;

    public AccountService(IAccountRepository accountRepository)
    {
        _accountRepository = accountRepository;
    }

    public async Task<IReadOnlyList<AccountDto>> GetAllAccountsAsync(CancellationToken cancellationToken = default)
    {
        var accounts = await _accountRepository.GetAllAsync(cancellationToken);
        return accounts.Select(MapToDto).ToList();
    }

    public async Task<IReadOnlyList<AccountDto>> GetPostingAccountsAsync(CancellationToken cancellationToken = default)
    {
        var accounts = await _accountRepository.GetPostingAccountsAsync(cancellationToken);
        return accounts.Select(MapToDto).ToList();
    }

    public async Task<AccountDto> GetByIdAsync(long id, CancellationToken cancellationToken = default)
    {
        var account = await _accountRepository.GetByIdAsync(id, cancellationToken);
        if (account == null)
            throw new KeyNotFoundException($"Account with ID {id} was not found.");
        return MapToDto(account);
    }

    public async Task<AccountDto> CreateAccountAsync(CreateAccountDto dto, CancellationToken cancellationToken = default)
    {
        if (string.IsNullOrWhiteSpace(dto.Code))
            throw new HierarchyValidationException("Account code is required.");
        if (string.IsNullOrWhiteSpace(dto.Name))
            throw new HierarchyValidationException("Account name is required.");
        if (dto.Level < 1 || dto.Level > 4)
            throw new HierarchyValidationException("Account level must be between 1 and 4.");

        if (await _accountRepository.ExistsByCodeAsync(dto.Code.Trim(), null, cancellationToken))
            throw new HierarchyValidationException($"Account with code '{dto.Code}' already exists.");

        AccountType finalType = dto.AccountType;

        if (dto.Level == 1)
        {
            if (dto.ParentId.HasValue)
                throw new HierarchyValidationException("Level 1 (Major Head) accounts cannot have a parent account.");
        }
        else
        {
            if (!dto.ParentId.HasValue)
                throw new HierarchyValidationException($"Level {dto.Level} accounts must have a parent account.");

            var parent = await _accountRepository.GetByIdAsync(dto.ParentId.Value, cancellationToken);
            if (parent == null)
                throw new HierarchyValidationException($"Parent account with ID {dto.ParentId.Value} does not exist.");

            if (parent.Level != dto.Level - 1)
                throw new HierarchyValidationException($"Invalid parent: Level {dto.Level} account must have a Level {dto.Level - 1} parent (selected parent is Level {parent.Level}).");

            finalType = parent.AccountType; // Inherit account classification from parent hierarchy
        }

        var account = new Account
        {
            Code = dto.Code.Trim(),
            Name = dto.Name.Trim(),
            Level = dto.Level,
            ParentId = dto.ParentId,
            AccountType = finalType,
            Description = dto.Description?.Trim(),
            IsActive = true
        };

        var created = await _accountRepository.AddAsync(account, cancellationToken);
        return MapToDto(created);
    }

    public async Task<AccountDto> UpdateAccountAsync(long id, UpdateAccountDto dto, CancellationToken cancellationToken = default)
    {
        var account = await _accountRepository.GetByIdAsync(id, cancellationToken);
        if (account == null)
            throw new KeyNotFoundException($"Account with ID {id} was not found.");

        if (string.IsNullOrWhiteSpace(dto.Name))
            throw new HierarchyValidationException("Account name cannot be empty.");

        account.Name = dto.Name.Trim();
        account.Description = dto.Description?.Trim();
        account.IsActive = dto.IsActive;
        account.UpdatedAtUtc = DateTime.UtcNow;

        await _accountRepository.UpdateAsync(account, cancellationToken);
        return MapToDto(account);
    }

    private static AccountDto MapToDto(Account a) => new()
    {
        Id = a.Id,
        Code = a.Code,
        Name = a.Name,
        Level = a.Level,
        ParentId = a.ParentId,
        ParentName = a.ParentAccount?.Name,
        AccountType = a.AccountType,
        Description = a.Description,
        IsActive = a.IsActive
    };
}
