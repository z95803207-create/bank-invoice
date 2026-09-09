package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.seed.DatabaseSeeder
import com.example.domain.model.Account
import com.example.domain.model.AccountType
import com.example.domain.model.CurrencyUtils
import com.example.domain.model.DashboardSummary
import com.example.domain.model.LedgerTransaction
import com.example.domain.model.TrialBalanceReport
import com.example.domain.model.Voucher
import com.example.domain.model.VoucherLine
import com.example.repository.AccountingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppScreen(val title: String) {
    DASHBOARD("Dashboard"),
    CHART_OF_ACCOUNTS("Chart of Accounts"),
    CP_VOUCHERS("CP Vouchers"),
    CREATE_VOUCHER("New CP Voucher"),
    LEDGER("Account Ledger"),
    TRANSACTIONS("Transaction Register"),
    TRIAL_BALANCE("Trial Balance")
}

data class CreateVoucherLineState(
    val accountId: Long = 0,
    val description: String = "",
    val debitInput: String = "",
    val creditInput: String = ""
) {
    val debitCents: Long get() = CurrencyUtils.parseToCents(debitInput)
    val creditCents: Long get() = CurrencyUtils.parseToCents(creditInput)
}

class AccountingViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    val repository = AccountingRepository(database)

    // Current Screen
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Screen Backstack for native Android back handling
    private val _screenBackStack = mutableListOf(AppScreen.DASHBOARD)

    // User Feedback Messages
    private val _notification = MutableStateFlow<String?>(null)
    val notification: StateFlow<String?> = _notification.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // All Accounts
    val allAccounts: StateFlow<List<Account>> = repository.getAccountsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val postingAccounts: StateFlow<List<Account>> = repository.getPostingAccountsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Chart of Accounts
    private val _accountSearch = MutableStateFlow("")
    val accountSearch: StateFlow<String> = _accountSearch.asStateFlow()

    private val _accountLevelFilter = MutableStateFlow<Int?>(null)
    val accountLevelFilter: StateFlow<Int?> = _accountLevelFilter.asStateFlow()

    val filteredAccounts: StateFlow<List<Account>> = combine(
        allAccounts,
        _accountSearch,
        _accountLevelFilter
    ) { accounts, query, level ->
        accounts.filter { acc ->
            val matchesLevel = level == null || acc.level == level
            val matchesQuery = query.isBlank() ||
                    acc.code.contains(query.trim(), ignoreCase = true) ||
                    acc.name.contains(query.trim(), ignoreCase = true) ||
                    acc.accountType.displayName.contains(query.trim(), ignoreCase = true)
            matchesLevel && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Vouchers
    val allVouchers: StateFlow<List<Voucher>> = repository.getVouchersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _voucherSearch = MutableStateFlow("")
    val voucherSearch: StateFlow<String> = _voucherSearch.asStateFlow()

    private val _voucherStatusFilter = MutableStateFlow("ALL") // ALL, DRAFT, POSTED
    val voucherStatusFilter: StateFlow<String> = _voucherStatusFilter.asStateFlow()

    val filteredVouchers: StateFlow<List<Voucher>> = combine(
        allVouchers,
        _voucherSearch,
        _voucherStatusFilter
    ) { vouchers, query, status ->
        vouchers.filter { v ->
            val matchesStatus = when (status) {
                "DRAFT" -> !v.isPosted
                "POSTED" -> v.isPosted
                else -> true
            }
            val matchesQuery = query.isBlank() ||
                    v.voucherNumber.contains(query.trim(), ignoreCase = true) ||
                    v.reference.contains(query.trim(), ignoreCase = true) ||
                    v.narration.contains(query.trim(), ignoreCase = true)
            matchesStatus && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected Voucher for Modal Detail
    private val _selectedVoucher = MutableStateFlow<Voucher?>(null)
    val selectedVoucher: StateFlow<Voucher?> = _selectedVoucher.asStateFlow()

    // Next Voucher Number
    private val _nextVoucherNumber = MutableStateFlow("CP-000001")
    val nextVoucherNumber: StateFlow<String> = _nextVoucherNumber.asStateFlow()

    // Dashboard State
    private val _dashboardSummary = MutableStateFlow<DashboardSummary?>(null)
    val dashboardSummary: StateFlow<DashboardSummary?> = _dashboardSummary.asStateFlow()

    // Ledger State
    private val _selectedLedgerAccount = MutableStateFlow<Account?>(null)
    val selectedLedgerAccount: StateFlow<Account?> = _selectedLedgerAccount.asStateFlow()

    private val _ledgerTransactions = MutableStateFlow<List<LedgerTransaction>>(emptyList())
    val ledgerTransactions: StateFlow<List<LedgerTransaction>> = _ledgerTransactions.asStateFlow()

    // Transaction Register State
    private val _txRegisterList = MutableStateFlow<List<LedgerTransaction>>(emptyList())
    val txRegisterList: StateFlow<List<LedgerTransaction>> = _txRegisterList.asStateFlow()

    private val _txSearch = MutableStateFlow("")
    val txSearch: StateFlow<String> = _txSearch.asStateFlow()

    private val _txAccountFilter = MutableStateFlow<Long?>(null)
    val txAccountFilter: StateFlow<Long?> = _txAccountFilter.asStateFlow()

    private val _txVoucherFilter = MutableStateFlow("")
    val txVoucherFilter: StateFlow<String> = _txVoucherFilter.asStateFlow()

    // Trial Balance State
    private val _trialBalance = MutableStateFlow<TrialBalanceReport?>(null)
    val trialBalance: StateFlow<TrialBalanceReport?> = _trialBalance.asStateFlow()

    init {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                DatabaseSeeder.seedInitialData(repository)
                refreshAll()
            } catch (e: Exception) {
                _errorMessage.value = "Initialization error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        if (_currentScreen.value != screen) {
            _screenBackStack.add(screen)
            _currentScreen.value = screen
            onScreenEntered(screen)
        }
    }

    fun navigateBack(): Boolean {
        if (_screenBackStack.size > 1) {
            _screenBackStack.removeAt(_screenBackStack.size - 1)
            val prev = _screenBackStack.last()
            _currentScreen.value = prev
            onScreenEntered(prev)
            return true
        }
        return false
    }

    private fun onScreenEntered(screen: AppScreen) {
        when (screen) {
            AppScreen.DASHBOARD -> refreshDashboard()
            AppScreen.CREATE_VOUCHER -> prepareNewVoucher()
            AppScreen.LEDGER -> refreshLedger()
            AppScreen.TRANSACTIONS -> refreshTransactions()
            AppScreen.TRIAL_BALANCE -> refreshTrialBalance()
            else -> {}
        }
    }

    fun clearNotification() {
        _notification.value = null
    }

    fun clearErrorMessage() {
        _errorMessage.value = null
    }

    fun refreshAll() {
        refreshDashboard()
        refreshLedger()
        refreshTransactions()
        refreshTrialBalance()
        updateNextVoucherNumber()
    }

    fun refreshDashboard() {
        viewModelScope.launch {
            try {
                _dashboardSummary.value = repository.getDashboardSummary()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load dashboard: ${e.message}"
            }
        }
    }

    fun refreshTrialBalance() {
        viewModelScope.launch {
            try {
                _trialBalance.value = repository.getTrialBalance()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load trial balance: ${e.message}"
            }
        }
    }

    fun refreshTransactions() {
        viewModelScope.launch {
            try {
                _txRegisterList.value = repository.getFilteredTransactions(
                    query = _txSearch.value,
                    accountId = _txAccountFilter.value,
                    voucherNumber = _txVoucherFilter.value.ifBlank { null }
                )
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load transactions: ${e.message}"
            }
        }
    }

    fun refreshLedger() {
        viewModelScope.launch {
            val acc = _selectedLedgerAccount.value ?: postingAccounts.value.firstOrNull()
            if (acc != null) {
                _selectedLedgerAccount.value = acc
                _ledgerTransactions.value = repository.getAccountLedger(acc.id)
            } else {
                _ledgerTransactions.value = emptyList()
            }
        }
    }

    fun selectLedgerAccount(account: Account) {
        _selectedLedgerAccount.value = account
        viewModelScope.launch {
            _ledgerTransactions.value = repository.getAccountLedger(account.id)
        }
    }

    fun setAccountSearch(query: String) {
        _accountSearch.value = query
    }

    fun setAccountLevelFilter(level: Int?) {
        _accountLevelFilter.value = level
    }

    fun setVoucherSearch(query: String) {
        _voucherSearch.value = query
    }

    fun setVoucherStatusFilter(status: String) {
        _voucherStatusFilter.value = status
    }

    fun selectVoucherForDetail(voucher: Voucher?) {
        _selectedVoucher.value = voucher
    }

    fun updateNextVoucherNumber() {
        viewModelScope.launch {
            _nextVoucherNumber.value = repository.generateNextVoucherNumber()
        }
    }

    fun prepareNewVoucher() {
        updateNextVoucherNumber()
    }

    fun createAccount(
        code: String,
        name: String,
        level: Int,
        parentId: Long?,
        accountType: AccountType,
        description: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val newAccount = Account(
                code = code.trim(),
                name = name.trim(),
                parentId = parentId,
                level = level,
                accountType = accountType,
                description = description.trim(),
                isActive = true
            )
            val result = repository.createAccount(newAccount)
            _isLoading.value = false
            result.onSuccess {
                _notification.value = "Account '${it.code} - ${it.name}' created successfully."
                refreshDashboard()
                onSuccess()
            }.onFailure { err ->
                _errorMessage.value = err.message ?: "Failed to create account."
            }
        }
    }

    fun updateAccount(
        id: Long,
        code: String,
        name: String,
        level: Int,
        parentId: Long?,
        accountType: AccountType,
        description: String,
        isActive: Boolean,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val updated = Account(
                id = id,
                code = code.trim(),
                name = name.trim(),
                parentId = parentId,
                level = level,
                accountType = accountType,
                description = description.trim(),
                isActive = isActive
            )
            val result = repository.updateAccount(updated)
            _isLoading.value = false
            result.onSuccess {
                _notification.value = "Account '${it.code}' updated successfully."
                refreshDashboard()
                onSuccess()
            }.onFailure { err ->
                _errorMessage.value = err.message ?: "Failed to update account."
            }
        }
    }

    fun saveVoucher(
        voucherNumber: String,
        voucherDate: Long,
        reference: String,
        narration: String,
        lines: List<CreateVoucherLineState>,
        postImmediately: Boolean,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            val domainLines = lines.map { l ->
                VoucherLine(
                    accountId = l.accountId,
                    description = l.description.trim(),
                    debitCents = l.debitCents,
                    creditCents = l.creditCents
                )
            }

            val voucher = Voucher(
                voucherNumber = voucherNumber.ifBlank { _nextVoucherNumber.value },
                voucherType = "CP",
                voucherDate = voucherDate,
                reference = reference.trim(),
                narration = narration.trim(),
                lines = domainLines
            )

            val createResult = repository.createVoucher(voucher)
            if (createResult.isFailure) {
                _isLoading.value = false
                _errorMessage.value = createResult.exceptionOrNull()?.message ?: "Failed to save voucher."
                return@launch
            }

            val saved = createResult.getOrThrow()

            if (postImmediately) {
                val postResult = repository.postVoucher(saved.id)
                _isLoading.value = false
                postResult.onSuccess { posted ->
                    _notification.value = "Voucher ${posted.voucherNumber} created and posted successfully!"
                    refreshAll()
                    onSuccess()
                }.onFailure { err ->
                    _errorMessage.value = "Voucher saved as draft, but posting failed: ${err.message}"
                    refreshAll()
                    onSuccess()
                }
            } else {
                _isLoading.value = false
                _notification.value = "Voucher ${saved.voucherNumber} saved as Draft."
                refreshAll()
                onSuccess()
            }
        }
    }

    fun postExistingVoucher(voucherId: Long, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.postVoucher(voucherId)
            _isLoading.value = false
            result.onSuccess { posted ->
                _notification.value = "Voucher ${posted.voucherNumber} posted successfully! Ledger updated."
                _selectedVoucher.value = posted
                refreshAll()
                onDone()
            }.onFailure { err ->
                _errorMessage.value = "Posting failed: ${err.message}"
            }
        }
    }

    fun setTxSearch(query: String) {
        _txSearch.value = query
        refreshTransactions()
    }

    fun setTxAccountFilter(accountId: Long?) {
        _txAccountFilter.value = accountId
        refreshTransactions()
    }

    fun setTxVoucherFilter(voucherNumber: String) {
        _txVoucherFilter.value = voucherNumber
        refreshTransactions()
    }
}
