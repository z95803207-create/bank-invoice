using Accounting.Application.DTOs;
using Accounting.Application.Interfaces;
using Accounting.Domain.Entities;
using Accounting.Domain.Exceptions;
using Accounting.Domain.Interfaces;

namespace Accounting.Application.Services;

public class VoucherService : IVoucherService
{
    private readonly IVoucherRepository _voucherRepository;
    private readonly IAccountRepository _accountRepository;
    private readonly IPostingService _postingService;

    public VoucherService(
        IVoucherRepository voucherRepository,
        IAccountRepository accountRepository,
        IPostingService postingService)
    {
        _voucherRepository = voucherRepository;
        _accountRepository = accountRepository;
        _postingService = postingService;
    }

    public async Task<IReadOnlyList<VoucherDto>> GetAllVouchersAsync(CancellationToken cancellationToken = default)
    {
        var vouchers = await _voucherRepository.GetAllAsync(cancellationToken);
        return vouchers.Select(MapToDto).ToList();
    }

    public async Task<VoucherDto> GetByIdAsync(long id, CancellationToken cancellationToken = default)
    {
        var voucher = await _voucherRepository.GetByIdAsync(id, cancellationToken);
        if (voucher == null)
            throw new KeyNotFoundException($"Voucher with ID {id} was not found.");
        return MapToDto(voucher);
    }

    public async Task<VoucherDto> CreateVoucherAsync(CreateVoucherDto dto, CancellationToken cancellationToken = default)
    {
        if (dto.Lines == null || dto.Lines.Count < 2)
            throw new VoucherValidationException("A voucher must contain at least two line items.");

        if (string.IsNullOrWhiteSpace(dto.Narration))
            throw new VoucherValidationException("Voucher narration is required.");

        decimal totalDebit = 0m;
        decimal totalCredit = 0m;

        var voucherLines = new List<VoucherLine>();

        foreach (var (line, index) in dto.Lines.Select((l, i) => (l, i + 1)))
        {
            if (line.Debit < 0m || line.Credit < 0m)
                throw new VoucherValidationException($"Line {index}: Debit and Credit amounts cannot be negative.");

            if (line.Debit > 0m && line.Credit > 0m)
                throw new VoucherValidationException($"Line {index}: A line item cannot have both Debit and Credit amounts.");

            if (line.Debit == 0m && line.Credit == 0m)
                throw new VoucherValidationException($"Line {index}: Line item must have either a Debit or a Credit amount.");

            var account = await _accountRepository.GetByIdAsync(line.AccountId, cancellationToken);
            if (account == null)
                throw new VoucherValidationException($"Line {index}: Account with ID {line.AccountId} does not exist.");

            if (!account.IsActive)
                throw new VoucherValidationException($"Line {index}: Account '{account.Code}' is inactive.");

            if (account.Level != 4)
                throw new VoucherValidationException($"Line {index}: Account '{account.Code} - {account.Name}' is a Level {account.Level} control account. Only Level 4 posting accounts are allowed.");

            totalDebit += line.Debit;
            totalCredit += line.Credit;

            voucherLines.Add(new VoucherLine
            {
                AccountId = line.AccountId,
                Description = line.Description?.Trim(),
                Debit = line.Debit,
                Credit = line.Credit
            });
        }

        if (totalDebit <= 0m)
            throw new VoucherValidationException("Voucher total amount must be greater than zero.");

        if (totalDebit != totalCredit)
            throw new VoucherValidationException($"Unbalanced voucher! Total Debits (${totalDebit:N2}) must equal Total Credits (${totalCredit:N2}). Variance: ${Math.Abs(totalDebit - totalCredit):N2}.");

        string voucherNumber = await _voucherRepository.GenerateNextVoucherNumberAsync("CP", cancellationToken);

        var voucher = new Voucher
        {
            VoucherNumber = voucherNumber,
            VoucherType = "CP",
            VoucherDate = dto.VoucherDate,
            Reference = dto.Reference?.Trim(),
            Narration = dto.Narration.Trim(),
            TotalDebit = totalDebit,
            TotalCredit = totalCredit,
            IsPosted = false,
            Lines = voucherLines
        };

        var saved = await _voucherRepository.AddAsync(voucher, cancellationToken);

        if (dto.PostImmediately)
        {
            return await _postingService.PostVoucherAsync(saved.Id, cancellationToken);
        }

        return MapToDto(saved);
    }

    private static VoucherDto MapToDto(Voucher v) => new()
    {
        Id = v.Id,
        VoucherNumber = v.VoucherNumber,
        VoucherType = v.VoucherType,
        VoucherDate = v.VoucherDate,
        Reference = v.Reference,
        Narration = v.Narration,
        TotalDebit = v.TotalDebit,
        TotalCredit = v.TotalCredit,
        IsPosted = v.IsPosted,
        PostedAtUtc = v.PostedAtUtc,
        Lines = v.Lines.Select(l => new VoucherLineDto
        {
            Id = l.Id,
            AccountId = l.AccountId,
            AccountCode = l.Account?.Code ?? string.Empty,
            AccountName = l.Account?.Name ?? string.Empty,
            Description = l.Description,
            Debit = l.Debit,
            Credit = l.Credit
        }).ToList()
    };
}
