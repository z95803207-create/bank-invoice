package com.example.ui.screens.vouchers

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Account
import com.example.domain.model.CurrencyUtils
import com.example.ui.AccountingViewModel
import com.example.ui.AppScreen
import com.example.ui.CreateVoucherLineState
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldCredit
import com.example.ui.theme.RubyContainer
import com.example.ui.theme.RubyDebit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CreateVoucherScreen(
    viewModel: AccountingViewModel,
    modifier: Modifier = Modifier
) {
    val nextVoucherNumber by viewModel.nextVoucherNumber.collectAsState()
    val postingAccounts by viewModel.postingAccounts.collectAsState()

    var reference by remember { mutableStateOf("") }
    var narration by remember { mutableStateOf("") }
    val voucherDate by remember { mutableStateOf(System.currentTimeMillis()) }

    // Initial 2 lines: typically 1 debit and 1 credit
    val lines = remember {
        mutableStateListOf(
            CreateVoucherLineState(accountId = 0L, description = "", debitInput = "", creditInput = ""),
            CreateVoucherLineState(accountId = 0L, description = "", debitInput = "", creditInput = "")
        )
    }

    // Recalculate dynamic totals in real-time
    val totalDebitCents = lines.sumOf { it.debitCents }
    val totalCreditCents = lines.sumOf { it.creditCents }
    val differenceCents = totalDebitCents - totalCreditCents
    val isBalanced = totalDebitCents > 0L && totalDebitCents == totalCreditCents
    val isValidStructure = lines.size >= 2 && lines.all { it.accountId > 0L && ((it.debitCents > 0L && it.creditCents == 0L) || (it.creditCents > 0L && it.debitCents == 0L)) }

    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    val formattedDate = dateFormat.format(Date(voucherDate))

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("create_voucher_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header Card with Voucher Number and Date
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Voucher Number",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = nextVoucherNumber,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Voucher Date",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = formattedDate,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    OutlinedTextField(
                        value = reference,
                        onValueChange = { reference = it },
                        label = { Text("Reference (e.g., Check #, Receipt #)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("voucher_reference_input")
                    )

                    OutlinedTextField(
                        value = narration,
                        onValueChange = { narration = it },
                        label = { Text("Narration (General Payment Memo)") },
                        placeholder = { Text("e.g., Office Supplies Purchase") },
                        maxLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("voucher_narration_input")
                    )
                }
            }
        }

        // Section Title: Dynamic Voucher Lines
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Voucher Lines (${lines.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                OutlinedButton(
                    onClick = {
                        lines.add(CreateVoucherLineState(accountId = 0L, description = "", debitInput = "", creditInput = ""))
                    },
                    modifier = Modifier.testTag("btn_add_line")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Line")
                }
            }
        }

        // Dynamic Line Cards
        itemsIndexed(lines) { index, lineState ->
            VoucherLineCard(
                lineNumber = index + 1,
                line = lineState,
                postingAccounts = postingAccounts,
                canDelete = lines.size > 2,
                onUpdate = { updated -> lines[index] = updated },
                onDelete = { lines.removeAt(index) }
            )
        }

        // Summary Card: Total Debit, Total Credit, Difference
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("voucher_summary_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isBalanced) EmeraldContainer else RubyContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isBalanced) Icons.Default.CheckCircle else Icons.Default.Error,
                                contentDescription = null,
                                tint = if (isBalanced) EmeraldCredit else RubyDebit,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isBalanced) "Voucher is Balanced!" else "Voucher is Unbalanced",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isBalanced) EmeraldCredit else RubyDebit
                            )
                        }

                        Text(
                            text = if (!isValidStructure) "Enter accounts & amounts" else if (isBalanced) "Ready to Post" else "Debit must equal Credit",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isBalanced) EmeraldCredit else RubyDebit,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider(color = Color.Black.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total Debit:", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = CurrencyUtils.format(totalDebitCents, includeSymbol = true),
                            fontWeight = FontWeight.Bold,
                            color = RubyDebit,
                            modifier = Modifier.testTag("total_debit_text")
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total Credit:", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = CurrencyUtils.format(totalCreditCents, includeSymbol = true),
                            fontWeight = FontWeight.Bold,
                            color = EmeraldCredit,
                            modifier = Modifier.testTag("total_credit_text")
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Difference:", fontWeight = FontWeight.Bold)
                        Text(
                            text = CurrencyUtils.format(differenceCents, includeSymbol = true),
                            fontWeight = FontWeight.Bold,
                            color = if (isBalanced) EmeraldCredit else RubyDebit,
                            modifier = Modifier.testTag("difference_text")
                        )
                    }
                }
            }
        }

        // Action Buttons: Save as Draft & Post Voucher
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        viewModel.saveVoucher(
                            voucherNumber = nextVoucherNumber,
                            voucherDate = voucherDate,
                            reference = reference,
                            narration = narration,
                            lines = lines.toList(),
                            postImmediately = false,
                            onSuccess = { viewModel.navigateTo(AppScreen.CP_VOUCHERS) }
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_save_draft"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save Draft")
                }

                Button(
                    onClick = {
                        viewModel.saveVoucher(
                            voucherNumber = nextVoucherNumber,
                            voucherDate = voucherDate,
                            reference = reference,
                            narration = narration,
                            lines = lines.toList(),
                            postImmediately = true,
                            onSuccess = { viewModel.navigateTo(AppScreen.CP_VOUCHERS) }
                        )
                    },
                    enabled = isBalanced && isValidStructure,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("btn_post_voucher"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldCredit)
                ) {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Post Voucher")
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun VoucherLineCard(
    lineNumber: Int,
    line: CreateVoucherLineState,
    postingAccounts: List<Account>,
    canDelete: Boolean,
    onUpdate: (CreateVoucherLineState) -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var accountDropdownExpanded by remember { mutableStateOf(false) }
    val selectedAccount = postingAccounts.firstOrNull { it.id == line.accountId }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("voucher_line_$lineNumber"),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Line #$lineNumber",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                if (canDelete) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp).testTag("btn_delete_line_$lineNumber")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Line",
                            tint = RubyDebit,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Account Dropdown
            Column {
                OutlinedTextField(
                    value = selectedAccount?.let { "${it.code} - ${it.name}" } ?: "Select Posting Account (Level 4)",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Account *") },
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            modifier = Modifier.clickable { accountDropdownExpanded = true }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { accountDropdownExpanded = true }
                        .testTag("line_account_select_$lineNumber")
                )

                DropdownMenu(
                    expanded = accountDropdownExpanded,
                    onDismissRequest = { accountDropdownExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    if (postingAccounts.isEmpty()) {
                        DropdownMenuItem(
                            text = { Text("No active Level 4 posting accounts available") },
                            onClick = { accountDropdownExpanded = false }
                        )
                    } else {
                        postingAccounts.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text("${acc.code} - ${acc.name} (${acc.accountType.displayName})") },
                                onClick = {
                                    onUpdate(line.copy(accountId = acc.id))
                                    accountDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Description
            OutlinedTextField(
                value = line.description,
                onValueChange = { onUpdate(line.copy(description = it)) },
                label = { Text("Line Description") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Debit & Credit Inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = line.debitInput,
                    onValueChange = { newVal ->
                        // Disallow credit if debit is entered
                        val clean = newVal.filter { it.isDigit() || it == '.' }
                        if (clean.isNotBlank()) {
                            onUpdate(line.copy(debitInput = clean, creditInput = ""))
                        } else {
                            onUpdate(line.copy(debitInput = clean))
                        }
                    },
                    label = { Text("Debit") },
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("line_debit_input_$lineNumber")
                )

                OutlinedTextField(
                    value = line.creditInput,
                    onValueChange = { newVal ->
                        // Disallow debit if credit is entered
                        val clean = newVal.filter { it.isDigit() || it == '.' }
                        if (clean.isNotBlank()) {
                            onUpdate(line.copy(creditInput = clean, debitInput = ""))
                        } else {
                            onUpdate(line.copy(creditInput = clean))
                        }
                    },
                    label = { Text("Credit") },
                    placeholder = { Text("0.00") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("line_credit_input_$lineNumber")
                )
            }
        }
    }
}
