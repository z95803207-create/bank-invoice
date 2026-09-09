using Accounting.Domain.Entities;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;

namespace Accounting.Infrastructure.Data.Configurations;

public class LedgerTransactionConfiguration : IEntityTypeConfiguration<LedgerTransaction>
{
    public void Configure(EntityTypeBuilder<LedgerTransaction> builder)
    {
        builder.ToTable("LedgerTransactions");

        builder.HasKey(lt => lt.Id);

        builder.Property(lt => lt.Date)
            .IsRequired();

        builder.HasIndex(lt => lt.Date);

        builder.Property(lt => lt.VoucherNumber)
            .IsRequired()
            .HasMaxLength(50);

        builder.Property(lt => lt.VoucherType)
            .IsRequired()
            .HasMaxLength(10);

        builder.Property(lt => lt.Description)
            .HasMaxLength(500);

        builder.Property(lt => lt.Debit)
            .HasPrecision(18, 2);

        builder.Property(lt => lt.Credit)
            .HasPrecision(18, 2);

        builder.HasOne(lt => lt.Voucher)
            .WithMany(v => v.LedgerTransactions)
            .HasForeignKey(lt => lt.VoucherId)
            .OnDelete(DeleteBehavior.Restrict);

        builder.HasOne(lt => lt.Account)
            .WithMany(a => a.LedgerTransactions)
            .HasForeignKey(lt => lt.AccountId)
            .OnDelete(DeleteBehavior.Restrict);
    }
}
