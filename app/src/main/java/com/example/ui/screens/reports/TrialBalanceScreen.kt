package com.example.ui.screens.reports

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.example.domain.model.TrialBalanceRow
import com.example.ui.AccountingViewModel
import com.example.ui.theme.EmeraldContainer
import com.example.ui.theme.EmeraldCredit
import com.example.ui.theme.RubyContainer
import com.example.ui.theme.RubyDebit
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TrialBalanceScreen(
    viewModel: AccountingViewModel,
    modifier: Modifier = Modifier
) {
    val report by viewModel.trialBalance.collectAsState()

    val dateFormat = SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.US)
    val asOfText = report?.let { dateFormat.format(Date(it.asOfDate)) } ?: "—"
    val isBalanced = report?.isBalanced ?: true

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("trial_balance_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Status & Balance Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("trial_balance_status_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isBalanced) EmeraldContainer else RubyContainer
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                if (isBalanced) EmeraldCredit else RubyDebit,
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isBalanced) Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBalanced) "Trial Balance is Balanced!" else "Out of Balance!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isBalanced) EmeraldCredit else RubyDebit
                        )
                        Text(
                            text = "As of $asOfText",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.White.copy(alpha = 0.8f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Diff: ${CurrencyUtils.format(report?.differenceCents ?: 0L, includeSymbol = true)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isBalanced) EmeraldCredit else RubyDebit
                        )
                    }
                }
            }
        }

        // Table Header
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Account (Code & Name)",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1.8f)
                    )
                    Text(
                        text = "Debit",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                    Text(
                        text = "Credit",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                    )
                }
            }
        }

        // Rows
        val rows = report?.rows ?: emptyList()
        if (rows.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No posted transactions in trial balance yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(rows, key = { it.accountId }) { row ->
                TrialBalanceRowCard(row = row)
            }
        }

        // Summary Reconciliation Row Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("trial_balance_summary_row"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Trial Balance Reconciliation Summary",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total Debit Balance:", fontWeight = FontWeight.Bold)
                        Text(
                            text = CurrencyUtils.format(report?.totalDebitCents ?: 0L, includeSymbol = true),
                            fontWeight = FontWeight.Bold,
                            color = RubyDebit,
                            modifier = Modifier.testTag("tb_total_debit")
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total Credit Balance:", fontWeight = FontWeight.Bold)
                        Text(
                            text = CurrencyUtils.format(report?.totalCreditCents ?: 0L, includeSymbol = true),
                            fontWeight = FontWeight.Bold,
                            color = EmeraldCredit,
                            modifier = Modifier.testTag("tb_total_credit")
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Variance / Difference:", fontWeight = FontWeight.Bold)
                        Text(
                            text = CurrencyUtils.format(report?.differenceCents ?: 0L, includeSymbol = true),
                            fontWeight = FontWeight.Bold,
                            color = if (isBalanced) EmeraldCredit else RubyDebit,
                            modifier = Modifier.testTag("tb_difference")
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun TrialBalanceRowCard(
    row: TrialBalanceRow,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1.8f)) {
                Text(
                    text = "${row.accountCode} · ${row.accountName}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = row.accountType.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            }

            Text(
                text = if (row.netDebitBalanceCents > 0L) CurrencyUtils.format(row.netDebitBalanceCents) else "—",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (row.netDebitBalanceCents > 0L) FontWeight.Bold else FontWeight.Normal,
                color = if (row.netDebitBalanceCents > 0L) RubyDebit else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )

            Text(
                text = if (row.netCreditBalanceCents > 0L) CurrencyUtils.format(row.netCreditBalanceCents) else "—",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = if (row.netCreditBalanceCents > 0L) FontWeight.Bold else FontWeight.Normal,
                color = if (row.netCreditBalanceCents > 0L) EmeraldCredit else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.End
            )
        }
    }
}
