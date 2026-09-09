using Accounting.Application.DTOs;
using Accounting.Application.Interfaces;
using Accounting.Domain.Exceptions;
using Microsoft.AspNetCore.Mvc;

namespace Accounting.Api.Controllers;

[ApiController]
[Route("api/[controller]")]
[Produces("application/json")]
public class VouchersController : ControllerBase
{
    private readonly IVoucherService _voucherService;
    private readonly IPostingService _postingService;
    private readonly ILogger<VouchersController> _logger;

    public VouchersController(
        IVoucherService voucherService,
        IPostingService postingService,
        ILogger<VouchersController> logger)
    {
        _voucherService = voucherService;
        _postingService = postingService;
        _logger = logger;
    }

    /// <summary>
    /// Gets all Cash Payment (CP) vouchers.
    /// </summary>
    [HttpGet]
    [ProducesResponseType(typeof(IReadOnlyList<VoucherDto>), StatusCodes.Status200OK)]
    public async Task<IActionResult> GetAll(CancellationToken cancellationToken)
    {
        var vouchers = await _voucherService.GetAllVouchersAsync(cancellationToken);
        return Ok(vouchers);
    }

    /// <summary>
    /// Gets a single voucher with its lines by ID.
    /// </summary>
    [HttpGet("{id:long}")]
    [ProducesResponseType(typeof(VoucherDto), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> GetById(long id, CancellationToken cancellationToken)
    {
        try
        {
            var voucher = await _voucherService.GetByIdAsync(id, cancellationToken);
            return Ok(voucher);
        }
        catch (KeyNotFoundException ex)
        {
            return NotFound(new { message = ex.Message });
        }
    }

    /// <summary>
    /// Creates a new Cash Payment (CP) voucher with balance validation.
    /// </summary>
    [HttpPost]
    [ProducesResponseType(typeof(VoucherDto), StatusCodes.Status201Created)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> Create([FromBody] CreateVoucherDto dto, CancellationToken cancellationToken)
    {
        try
        {
            var created = await _voucherService.CreateVoucherAsync(dto, cancellationToken);
            return CreatedAtAction(nameof(GetById), new { id = created.Id }, created);
        }
        catch (VoucherValidationException ex)
        {
            _logger.LogWarning("Voucher validation error: {Message}", ex.Message);
            return BadRequest(new { title = "Voucher Validation Error", message = ex.Message });
        }
        catch (PostingException ex)
        {
            _logger.LogWarning("Posting error: {Message}", ex.Message);
            return BadRequest(new { title = "Posting Error", message = ex.Message });
        }
    }

    /// <summary>
    /// Posts an existing voucher into the General Ledger (atomic one-time posting).
    /// </summary>
    [HttpPost("{id:long}/post")]
    [ProducesResponseType(typeof(VoucherDto), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> Post(long id, CancellationToken cancellationToken)
    {
        try
        {
            var posted = await _postingService.PostVoucherAsync(id, cancellationToken);
            return Ok(posted);
        }
        catch (KeyNotFoundException ex)
        {
            return NotFound(new { message = ex.Message });
        }
        catch (PostingException ex)
        {
            _logger.LogWarning("Posting error: {Message}", ex.Message);
            return BadRequest(new { title = "Posting Error", message = ex.Message });
        }
    }
}
