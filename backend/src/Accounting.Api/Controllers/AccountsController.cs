using Accounting.Application.DTOs;
using Accounting.Application.Interfaces;
using Accounting.Domain.Exceptions;
using Microsoft.AspNetCore.Mvc;

namespace Accounting.Api.Controllers;

[ApiController]
[Route("api/[controller]")]
[Produces("application/json")]
public class AccountsController : ControllerBase
{
    private readonly IAccountService _accountService;
    private readonly ILogger<AccountsController> _logger;

    public AccountsController(IAccountService accountService, ILogger<AccountsController> logger)
    {
        _accountService = accountService;
        _logger = logger;
    }

    /// <summary>
    /// Gets all accounts in the 4-level hierarchy.
    /// </summary>
    [HttpGet]
    [ProducesResponseType(typeof(IReadOnlyList<AccountDto>), StatusCodes.Status200OK)]
    public async Task<IActionResult> GetAll(CancellationToken cancellationToken)
    {
        var accounts = await _accountService.GetAllAccountsAsync(cancellationToken);
        return Ok(accounts);
    }

    /// <summary>
    /// Gets all active Level 4 posting accounts.
    /// </summary>
    [HttpGet("posting")]
    [ProducesResponseType(typeof(IReadOnlyList<AccountDto>), StatusCodes.Status200OK)]
    public async Task<IActionResult> GetPostingAccounts(CancellationToken cancellationToken)
    {
        var accounts = await _accountService.GetPostingAccountsAsync(cancellationToken);
        return Ok(accounts);
    }

    /// <summary>
    /// Gets a single account by ID.
    /// </summary>
    [HttpGet("{id:long}")]
    [ProducesResponseType(typeof(AccountDto), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> GetById(long id, CancellationToken cancellationToken)
    {
        try
        {
            var account = await _accountService.GetByIdAsync(id, cancellationToken);
            return Ok(account);
        }
        catch (KeyNotFoundException ex)
        {
            return NotFound(new { message = ex.Message });
        }
    }

    /// <summary>
    /// Creates a new account in the Chart of Accounts with strict 4-level hierarchy validation.
    /// </summary>
    [HttpPost]
    [ProducesResponseType(typeof(AccountDto), StatusCodes.Status201Created)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> Create([FromBody] CreateAccountDto dto, CancellationToken cancellationToken)
    {
        try
        {
            var created = await _accountService.CreateAccountAsync(dto, cancellationToken);
            return CreatedAtAction(nameof(GetById), new { id = created.Id }, created);
        }
        catch (HierarchyValidationException ex)
        {
            _logger.LogWarning("Hierarchy validation error: {Message}", ex.Message);
            return BadRequest(new { title = "Hierarchy Validation Error", message = ex.Message });
        }
    }

    /// <summary>
    /// Updates an existing account's name, description, or active status.
    /// </summary>
    [HttpPut("{id:long}")]
    [ProducesResponseType(typeof(AccountDto), StatusCodes.Status200OK)]
    [ProducesResponseType(StatusCodes.Status400BadRequest)]
    [ProducesResponseType(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> Update(long id, [FromBody] UpdateAccountDto dto, CancellationToken cancellationToken)
    {
        try
        {
            var updated = await _accountService.UpdateAccountAsync(id, dto, cancellationToken);
            return Ok(updated);
        }
        catch (KeyNotFoundException ex)
        {
            return NotFound(new { message = ex.Message });
        }
        catch (HierarchyValidationException ex)
        {
            return BadRequest(new { message = ex.Message });
        }
    }
}
