package com.example.ui.screens.vouchers

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.CurrencyUtils
import com.example.domain.model.Voucher
import com.example.ui.AccountingViewModel
import com.example.ui.AppScreen
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldCredit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VoucherListScreen(
    viewModel: AccountingViewModel,
    modifier: Modifier = Modifier
) {
    val vouchers by viewModel.filteredVouchers.collectAsState()
    val allVouchers by viewModel.allVouchers.collectAsState()
    val searchQuery by viewModel.voucherSearch.collectAsState()
    val statusFilter by viewModel.voucherStatusFilter.collectAsState()
    val selectedVoucher by viewModel.selectedVoucher.collectAsState()

    val tabs = listOf("ALL" to "All (${allVouchers.size})", "DRAFT" to "Draft (${allVouchers.count { !it.isPosted }})", "POSTED" to "Posted (${allVouchers.count { it.isPosted }})")
    val selectedTabIndex = tabs.indexOfFirst { it.first == statusFilter }.coerceAtLeast(0)

    Scaffold(
        modifier = modifier.testTag("voucher_list_screen"),
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.navigateTo(AppScreen.CREATE_VOUCHER) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_create_voucher")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Create Voucher")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.setVoucherSearch(it) },
                placeholder = { Text("Search vouchers (number, reference, narration)...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setVoucherSearch("") }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("voucher_search_input"),
                shape = RoundedCornerShape(12.dp)
            )

            // Status Tabs
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                tabs.forEachIndexed { index, (key, label) ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { viewModel.setVoucherStatusFilter(key) },
                        text = { Text(text = label, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) },
                        modifier = Modifier.testTag("tab_voucher_$key")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Vouchers List
            if (vouchers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No vouchers found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(vouchers, key = { it.id }) { voucher ->
                        VoucherCardRow(
                            voucher = voucher,
                            onClick = { viewModel.selectVoucherForDetail(voucher) },
                            onPostClick = { viewModel.postExistingVoucher(voucher.id) }
                        )
                    }
                }
            }
        }
    }

    selectedVoucher?.let { voucher ->
        VoucherDetailDialog(
            voucher = voucher,
            onDismiss = { viewModel.selectVoucherForDetail(null) },
            onPostVoucher = { id ->
                viewModel.postExistingVoucher(id) {
                    viewModel.selectVoucherForDetail(null)
                }
            }
        )
    }
}

@Composable
fun VoucherCardRow(
    voucher: Voucher,
    onClick: () -> Unit,
    onPostClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    val formattedDate = dateFormat.format(Date(voucher.voucherDate))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("voucher_card_${voucher.voucherNumber}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                if (voucher.isPosted) EmeraldContainer else Color(0xFFFEF3C7),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (voucher.isPosted) Icons.Default.CheckCircle else Icons.Default.HourglassEmpty,
                            contentDescription = null,
                            tint = if (voucher.isPosted) EmeraldCredit else Color(0xFFD97706),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = voucher.voucherNumber,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = formattedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = CurrencyUtils.format(voucher.totalDebitCents, includeSymbol = true),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (voucher.isPosted) EmeraldContainer else Color(0xFFFEF3C7))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (voucher.isPosted) "POSTED" else "DRAFT",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (voucher.isPosted) EmeraldCredit else Color(0xFFD97706),
                            fontSize = 10.sp
                        )
                    }
                }
            }

            if (voucher.narration.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = voucher.narration,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }

            if (voucher.reference.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Ref: ${voucher.reference}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${voucher.lines.size} Lines",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!voucher.isPosted) {
                    Text(
                        text = "Post Now →",
                        style = MaterialTheme.typography.labelMedium,
                        color = EmeraldCredit,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onPostClick() }
                    )
                }
            }
        }
    }
}
