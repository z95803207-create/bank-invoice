using Accounting.Domain.Entities;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Metadata.Builders;

namespace Accounting.Infrastructure.Data.Configurations;

public class VoucherLineConfiguration : IEntityTypeConfiguration<VoucherLine>
{
    public void Configure(EntityTypeBuilder<VoucherLine> builder)
    {
        builder.ToTable("VoucherLines");

        builder.HasKey(vl => vl.Id);

        builder.Property(vl => vl.Description)
            .HasMaxLength(500);

        builder.Property(vl => vl.Debit)
            .HasPrecision(18, 2);

        builder.Property(vl => vl.Credit)
            .HasPrecision(18, 2);

        builder.HasOne(vl => vl.Account)
            .WithMany(a => a.VoucherLines)
            .HasForeignKey(vl => vl.AccountId)
            .OnDelete(DeleteBehavior.Restrict);
    }
}
