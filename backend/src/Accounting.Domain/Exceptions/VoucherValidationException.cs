namespace Accounting.Domain.Exceptions;

public class VoucherValidationException : Exception
{
    public VoucherValidationException(string message) : base(message) { }
}
