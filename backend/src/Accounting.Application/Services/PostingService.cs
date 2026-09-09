using Accounting.Application.DTOs;
using Accounting.Application.Interfaces;
using Accounting.Domain.Entities;
using Accounting.Domain.Exceptions;
using Accounting.Domain.Interfaces;

namespace Accounting.Application.Services;

public class PostingService : IPostingService
{
    private readonly IVoucherRepository _voucherRepository;
    private readonly ILedgerRepository _ledgerRepository;

    public PostingService(
        IVoucherRepository voucherRepository,
        ILedgerRepository ledgerRepository)
    {
        _voucherRepository = voucherRepository;
        _ledgerRepository = ledgerRepository;
    }

    public async Task<VoucherDto> PostVoucherAsync(long voucherId, CancellationToken cancellationToken = default)
    {
        var voucher = await _voucherRepository.GetByIdAsync(voucherId, cancellationToken);
        if (voucher == null)
            throw new KeyNotFoundException($"Voucher with ID {voucherId} was not found.");

        if (voucher.IsPosted)
            throw new PostingException($"Voucher '{voucher.VoucherNumber}' is already posted and cannot be posted again.");

        if (voucher.TotalDebit != voucher.TotalCredit || voucher.TotalDebit <= 0m)
            throw new PostingException($"Cannot post unbalanced voucher '{voucher.VoucherNumber}'. Debit: {voucher.TotalDebit:N2}, Credit: {voucher.TotalCredit:N2}.");

        var ledgerTransactions = new List<LedgerTransaction>();

        foreach (var line in voucher.Lines)
        {
            var tx = new LedgerTransaction
            {
                Date = voucher.VoucherDate,
                VoucherId = voucher.Id,
                VoucherNumber = voucher.VoucherNumber,
                VoucherType = voucher.VoucherType,
                AccountId = line.AccountId,
                Description = string.IsNullOrWhiteSpace(line.Description) ? voucher.Narration : line.Description,
                Debit = line.Debit,
                Credit = line.Credit
            };
            ledgerTransactions.Add(tx);
        }

        await _ledgerRepository.AddRangeAsync(ledgerTransactions, cancellationToken);

        voucher.IsPosted = true;
        voucher.PostedAtUtc = DateTime.UtcNow;

        await _voucherRepository.UpdateAsync(voucher, cancellationToken);

        return new VoucherDto
        {
            Id = voucher.Id,
            VoucherNumber = voucher.VoucherNumber,
            VoucherType = voucher.VoucherType,
            VoucherDate = voucher.VoucherDate,
            Reference = voucher.Reference,
            Narration = voucher.Narration,
            TotalDebit = voucher.TotalDebit,
            TotalCredit = voucher.TotalCredit,
            IsPosted = voucher.IsPosted,
            PostedAtUtc = voucher.PostedAtUtc,
            Lines = voucher.Lines.Select(l => new VoucherLineDto
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
}
