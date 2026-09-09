namespace Accounting.Domain.Exceptions;

public class HierarchyValidationException : Exception
{
    public HierarchyValidationException(string message) : base(message) { }
}
