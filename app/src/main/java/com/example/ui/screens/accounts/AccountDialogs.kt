package com.example.ui.screens.accounts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.Account
import com.example.domain.model.AccountType

@Composable
fun CreateAccountDialog(
    allAccounts: List<Account>,
    onDismiss: () -> Unit,
    onSave: (code: String, name: String, level: Int, parentId: Long?, accountType: AccountType, description: String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var level by remember { mutableIntStateOf(4) }
    var parentId by remember { mutableStateOf<Long?>(null) }
    var accountType by remember { mutableStateOf(AccountType.ASSET) }
    var description by remember { mutableStateOf("") }

    // Filter valid potential parents based on selected level
    val validParents = remember(level, allAccounts) {
        if (level == 1) emptyList()
        else allAccounts.filter { it.level == level - 1 }
    }

    var parentDropdownExpanded by remember { mutableStateOf(false) }
    val selectedParent = allAccounts.firstOrNull { it.id == parentId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Create New Account", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Level Selector
                Text(
                    text = "Account Level",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    (1..4).forEach { lvl ->
                        FilterChip(
                            selected = level == lvl,
                            onClick = {
                                level = lvl
                                parentId = null // reset parent when level changes
                            },
                            label = { Text("Level $lvl") },
                            modifier = Modifier.weight(1f).testTag("chip_level_$lvl")
                        )
                    }
                }

                // Parent Account Dropdown (for levels 2, 3, 4)
                if (level > 1) {
                    Column {
                        Text(
                            text = "Parent Account (Must be Level ${level - 1}) *",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(
                            value = selectedParent?.let { "${it.code} - ${it.name}" } ?: "Select Parent Account",
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    modifier = Modifier.clickable { parentDropdownExpanded = true }
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { parentDropdownExpanded = true }
                                .testTag("account_parent_dropdown")
                        )

                        DropdownMenu(
                            expanded = parentDropdownExpanded,
                            onDismissRequest = { parentDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            if (validParents.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No Level ${level - 1} accounts found") },
                                    onClick = { parentDropdownExpanded = false }
                                )
                            } else {
                                validParents.forEach { parent ->
                                    DropdownMenuItem(
                                        text = { Text("${parent.code} - ${parent.name} (${parent.accountType.displayName})") },
                                        onClick = {
                                            parentId = parent.id
                                            accountType = parent.accountType // inherit account type
                                            parentDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Text(
                        text = "Level 1 is a Major Head and has no parent account.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }

                // Account Code
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Account Code *") },
                    placeholder = { Text("e.g., 512004") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_code_input")
                )

                // Account Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name *") },
                    placeholder = { Text("e.g., Water & Sewage Expense") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("account_name_input")
                )

                // Account Type Selector
                Text(
                    text = "Account Type *",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    AccountType.entries.forEach { type ->
                        FilterChip(
                            selected = accountType == type,
                            onClick = { accountType = type },
                            label = { Text(type.displayName, fontSize = 11.sp) },
                            modifier = Modifier.testTag("account_type_${type.name}")
                        )
                    }
                }

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (Optional)") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(code, name, level, parentId, accountType, description)
                },
                enabled = code.isNotBlank() && name.isNotBlank() && (level == 1 || parentId != null),
                modifier = Modifier.testTag("btn_save_account")
            ) {
                Text("Create Account")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun EditAccountDialog(
    account: Account,
    allAccounts: List<Account>,
    onDismiss: () -> Unit,
    onSave: (name: String, description: String, isActive: Boolean) -> Unit
) {
    var name by remember { mutableStateOf(account.name) }
    var description by remember { mutableStateOf(account.description) }
    var isActive by remember { mutableStateOf(account.isActive) }

    val parent = allAccounts.firstOrNull { it.id == account.parentId }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Edit Account: ${account.code}", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Level: ${account.levelDescription}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                if (parent != null) {
                    Text(
                        text = "Parent: ${parent.code} - ${parent.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "Type: ${account.accountType.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Account Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    maxLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Active Status", fontWeight = FontWeight.Medium)
                        Text(
                            text = if (isActive) "Enabled for posting" else "Disabled",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, description, isActive) },
                enabled = name.isNotBlank()
            ) {
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
