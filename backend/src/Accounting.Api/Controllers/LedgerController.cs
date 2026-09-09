using Accounting.Application.DTOs;
using Accounting.Application.Interfaces;
using Microsoft.AspNetCore.Mvc;

namespace Accounting.Api.Controllers;

[ApiController]
[Route("api/[controller]")]
[Produces("application/json")]
public class LedgerController : ControllerBase
{
    private readonly IReportService _reportService;

    public LedgerController(IReportService reportService)
    {
        _reportService = reportService;
    }

    /// <summary>
    /// Gets the account ledger for a specific posting account with running balances.
    /// </summary>
    [HttpGet("{accountId:long}")]
    [ProducesResponseType(typeof(IReadOnlyList<LedgerTransactionDto>), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> GetAccountLedger(long accountId, CancellationToken cancellationToken)
    {
        try
        {
            var entries = await _reportService.GetAccountLedgerAsync(accountId, cancellationToken);
            return Ok(entries);
        }
        catch (KeyNotFoundException ex)
        {
            return NotFound(new { message = ex.Message });
        }
    }

    /// <summary>
    /// Transaction Register: Search and filter across all general ledger transactions.
    /// </summary>
    [HttpGet("transactions")]
    [ProducesResponseType(typeof(IReadOnlyList<LedgerTransactionDto>), StatusCodes.Status200OK)]
    public async Task<IActionResult> GetTransactionRegister(
        [FromQuery] string? search,
        [FromQuery] long? accountId,
        CancellationToken cancellationToken)
    {
        var entries = await _reportService.GetTransactionRegisterAsync(search, accountId, cancellationToken);
        return Ok(entries);
    }
}
