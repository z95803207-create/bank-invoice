package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AccountingViewModel
import com.example.ui.AppScreen
import com.example.ui.components.AccountingBottomNav
import com.example.ui.components.AccountingTopBar
import com.example.ui.screens.accounts.ChartOfAccountsScreen
import com.example.ui.screens.dashboard.DashboardScreen
import com.example.ui.screens.ledger.LedgerScreen
import com.example.ui.screens.reports.TrialBalanceScreen
import com.example.ui.screens.transactions.TransactionRegisterScreen
import com.example.ui.screens.vouchers.CreateVoucherScreen
import com.example.ui.screens.vouchers.VoucherListScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: AccountingViewModel = viewModel()
                AccountingAppContent(viewModel = viewModel, onFinish = { finish() })
            }
        }
    }
}

@Composable
fun AccountingAppContent(
    viewModel: AccountingViewModel,
    onFinish: () -> Unit
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val notification by viewModel.notification.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Handle system back navigation
    BackHandler {
        if (!viewModel.navigateBack()) {
            onFinish()
        }
    }

    // Handle user notifications
    LaunchedEffect(notification) {
        notification?.let { msg ->
            snackbarHostState.showSnackbar(
                message = msg,
                duration = SnackbarDuration.Short
            )
            viewModel.clearNotification()
        }
    }

    // Handle error messages
    LaunchedEffect(errorMessage) {
        errorMessage?.let { err ->
            snackbarHostState.showSnackbar(
                message = "Error: $err",
                duration = SnackbarDuration.Long
            )
            viewModel.clearErrorMessage()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            AccountingTopBar(
                currentScreen = currentScreen,
                canNavigateBack = currentScreen != AppScreen.DASHBOARD,
                onNavigateBack = { viewModel.navigateBack() },
                onRefresh = { viewModel.refreshAll() },
                actions = {
                    if (currentScreen != AppScreen.CREATE_VOUCHER) {
                        IconButton(
                            onClick = { viewModel.navigateTo(AppScreen.CREATE_VOUCHER) },
                            modifier = Modifier.testTag("top_bar_create_voucher")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create Voucher"
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            AccountingBottomNav(
                currentScreen = currentScreen,
                onNavigate = { screen -> viewModel.navigateTo(screen) }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AppScreen.DASHBOARD -> DashboardScreen(viewModel = viewModel)
                AppScreen.CHART_OF_ACCOUNTS -> ChartOfAccountsScreen(viewModel = viewModel)
                AppScreen.CP_VOUCHERS -> VoucherListScreen(viewModel = viewModel)
                AppScreen.CREATE_VOUCHER -> CreateVoucherScreen(viewModel = viewModel)
                AppScreen.LEDGER -> LedgerScreen(viewModel = viewModel)
                AppScreen.TRANSACTIONS -> TransactionRegisterScreen(viewModel = viewModel)
                AppScreen.TRIAL_BALANCE -> TrialBalanceScreen(viewModel = viewModel)
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
