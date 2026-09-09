using Accounting.Domain.Entities;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;

namespace Accounting.Infrastructure.Data.Configurations;

public class VoucherConfiguration : IEntityTypeConfiguration<Voucher>
{
    public void Configure(EntityTypeBuilder<Voucher> builder)
    {
        builder.ToTable("Vouchers");

        builder.HasKey(v => v.Id);

        builder.Property(v => v.VoucherNumber)
            .IsRequired()
            .HasMaxLength(50);

        builder.HasIndex(v => v.VoucherNumber)
            .IsUnique();

        builder.Property(v => v.VoucherType)
            .IsRequired()
            .HasMaxLength(10);

        builder.Property(v => v.VoucherDate)
            .IsRequired();

        builder.Property(v => v.Reference)
            .HasMaxLength(100);

        builder.Property(v => v.Narration)
            .IsRequired()
            .HasMaxLength(1000);

        builder.Property(v => v.TotalDebit)
            .HasPrecision(18, 2);

        builder.Property(v => v.TotalCredit)
            .HasPrecision(18, 2);

        builder.Property(v => v.IsPosted)
            .HasDefaultValue(false);

        builder.HasMany(v => v.Lines)
            .WithOne(l => l.Voucher)
            .HasForeignKey(l => l.VoucherId)
            .OnDelete(DeleteBehavior.Cascade);
    }
}
