namespace Accounting.Domain.Exceptions;

public class PostingException : Exception
{
    public PostingException(string message) : base(message) { }
}
