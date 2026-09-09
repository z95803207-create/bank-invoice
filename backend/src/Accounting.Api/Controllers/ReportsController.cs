using Accounting.Application.DTOs;
using Accounting.Application.Interfaces;
using Microsoft.AspNetCore.Mvc;

namespace Accounting.Api.Controllers;

[ApiController]
[Route("api/[controller]")]
[Produces("application/json")]
public class ReportsController : ControllerBase
{
    private readonly IReportService _reportService;

    public ReportsController(IReportService reportService)
    {
        _reportService = reportService;
    }

    /// <summary>
    /// Gets the reconciled Trial Balance report verifying that Debits equal Credits.
    /// </summary>
    [HttpGet("trial-balance")]
    [ProducesResponseType(typeof(TrialBalanceDto), StatusCodes.Status200OK)]
    public async Task<IActionResult> GetTrialBalance(CancellationToken cancellationToken)
    {
        var report = await _reportService.GetTrialBalanceAsync(cancellationToken);
        return Ok(report);
    }
}
