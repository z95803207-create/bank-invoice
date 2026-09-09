package com.example.ui.screens.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.domain.model.AccountType
import com.example.ui.AccountingViewModel
import com.example.ui.AppScreen
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldCredit
import com.example.ui.theme.RubyContainer
import com.example.ui.theme.RubyDebit

@Composable
fun ChartOfAccountsScreen(
    viewModel: AccountingViewModel,
    modifier: Modifier = Modifier
) {
    val accounts by viewModel.filteredAccounts.collectAsState()
    val allAccounts by viewModel.allAccounts.collectAsState()
    val searchQuery by viewModel.accountSearch.collectAsState()
    val levelFilter by viewModel.accountLevelFilter.collectAsState()

    var showCreateDialog by remember { mutableStateOf(false) }
    var accountToEdit by remember { mutableStateOf<Account?>(null) }

    Scaffold(
        modifier = modifier.testTag("chart_of_accounts_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showCreateDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_account")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Account")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setAccountSearch(it) },
                placeholder = { Text("Search by code, name, type...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setAccountSearch("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("accounts_search_input"),
                shape = RoundedCornerShape(12.dp)
            )

            // Level Filter Chips
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FilterChip(
                        selected = levelFilter == null,
                        onClick = { viewModel.setAccountLevelFilter(null) },
                        label = { Text("All Levels (${allAccounts.size})") },
                        modifier = Modifier.testTag("filter_all_levels")
                    )
                }
                (1..4).forEach { lvl ->
                    val count = allAccounts.count { it.level == lvl }
                    item {
                        FilterChip(
                            selected = levelFilter == lvl,
                            onClick = { viewModel.setAccountLevelFilter(if (levelFilter == lvl) null else lvl) },
                            label = { Text("Level $lvl ($count)") },
                            modifier = Modifier.testTag("filter_level_$lvl")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Account Tree List
            if (accounts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No accounts found matching filter.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(accounts, key = { it.id }) { account ->
                        AccountTreeRow(
                            account = account,
                            onEdit = { accountToEdit = account },
                            onOpenLedger = {
                                if (account.isPostingAccount) {
                                    viewModel.selectLedgerAccount(account)
                                    viewModel.navigateTo(AppScreen.LEDGER)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showCreateDialog) {
        CreateAccountDialog(
            allAccounts = allAccounts,
            onDismiss = { showCreateDialog = false },
            onSave = { code, name, level, parentId, accountType, description ->
                viewModel.createAccount(
                    code = code,
                    name = name,
                    level = level,
                    parentId = parentId,
                    accountType = accountType,
                    description = description,
                    onSuccess = { showCreateDialog = false }
                )
            }
        )
    }

    accountToEdit?.let { acc ->
        EditAccountDialog(
            account = acc,
            allAccounts = allAccounts,
            onDismiss = { accountToEdit = null },
            onSave = { name, desc, isActive ->
                viewModel.updateAccount(
                    id = acc.id,
                    code = acc.code,
                    name = name,
                    level = acc.level,
                    parentId = acc.parentId,
                    accountType = acc.accountType,
                    description = desc,
                    isActive = isActive,
                    onSuccess = { accountToEdit = null }
                )
            }
        )
    }
}

@Composable
fun AccountTreeRow(
    account: Account,
    onEdit: () -> Unit,
    onOpenLedger: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Calculate visual indentation based on account level (Level 1..4)
    val indentPadding = ((account.level - 1) * 12).dp

    val (levelColor, levelBg) = when (account.level) {
        1 -> Pair(Color(0xFF1E3A8A), Color(0xFFDBEAFE))
        2 -> Pair(Color(0xFF0369A1), Color(0xFFE0F2FE))
        3 -> Pair(Color(0xFF0F766E), Color(0xFFCCFBF1))
        4 -> Pair(Color(0xFF047857), Color(0xFFD1FAE5))
        else -> Pair(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = indentPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onEdit() },
            shape = RoundedCornerShape(10.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (account.level == 1) 2.dp else 1.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Level Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(levelBg)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "L${account.level}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = levelColor,
                                fontSize = 10.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Account Code
                        Text(
                            text = account.code,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        // Type Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = account.accountType.displayName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (account.isPostingAccount) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(EmeraldContainer)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Posting",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldCredit,
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = onOpenLedger,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = "View in Ledger",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onEdit,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Account",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = account.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (account.level <= 2) FontWeight.Bold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (account.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = account.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
