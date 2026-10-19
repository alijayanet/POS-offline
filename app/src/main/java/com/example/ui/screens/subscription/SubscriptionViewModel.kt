package com.example.ui.screens.subscription

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.StoreProfile
import com.example.data.model.SubscriptionCustomer
import com.example.data.model.SubscriptionPackage
import com.example.data.model.SubscriptionPayment
import com.example.data.repository.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import com.example.util.DynamicFirebaseManager

enum class BillingFilter(val displayName: String) {
    ALL("Semua Tagihan"),
    OVERDUE("Menunggak / Jatuh Tempo"),
    PAID("Lunas Bulan Ini")
}

enum class BillingStatus(val displayName: String) {
    PAID("Lunas"),
    DUE_TODAY("Jatuh Tempo Hari Ini"),
    UPCOMING("Menjelang Tagihan"),
    OVERDUE("Menunggak")
}

data class CustomerBillingItem(
    val customer: SubscriptionCustomer,
    val status: BillingStatus,
    val monthsDue: Int,
    val totalAmountDue: Double,
    val nextDueDateTimestamp: Long
)

data class SubscriptionUiState(
    val billingItems: List<CustomerBillingItem> = emptyList(),
    val packages: List<SubscriptionPackage> = emptyList(),
    val customers: List<SubscriptionCustomer> = emptyList(),
    val payments: List<SubscriptionPayment> = emptyList(),
    val collectors: List<com.example.data.model.Collector> = emptyList(),
    val depositLogs: List<com.example.data.model.CollectorDepositLog> = emptyList(),
    val storeProfile: StoreProfile? = null,
    val selectedFilter: BillingFilter = BillingFilter.ALL,
    val searchQuery: String = "",
    val totalOverdueCustomers: Int = 0,
    val totalOverdueAmount: Double = 0.0,
    val totalActiveCustomers: Int = 0,
    val totalMonthlyRevenuePotential: Double = 0.0,
    val isCloudSyncEnabled: Boolean = false,
    val activeRole: String = "ADMIN", // "ADMIN" or "COLLECTOR"
    val activeCollectorName: String = "",
    val activeCollectorId: String = "",
    val message: String? = null
)

class SubscriptionViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: StoreRepository

    private val _selectedFilter = MutableStateFlow(BillingFilter.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SubscriptionUiState>

    init {
        val db = AppDatabase.getInstance(application)
        repository = StoreRepository(db)

        val roleInfo = DynamicFirebaseManager.getRoleInfo(application)
        val syncConfig = DynamicFirebaseManager.getSyncConfig(application)

        uiState = combine(
            repository.allSubscriptionCustomers,
            repository.allSubscriptionPackages,
            repository.allSubscriptionPayments,
            repository.allCollectors,
            repository.allDepositLogs,
            repository.storeProfile,
            _selectedFilter,
            _searchQuery,
            _message
        ) { args ->
            @Suppress("UNCHECKED_CAST")
            val customers = args[0] as List<SubscriptionCustomer>
            @Suppress("UNCHECKED_CAST")
            val packages = args[1] as List<SubscriptionPackage>
            @Suppress("UNCHECKED_CAST")
            val payments = args[2] as List<SubscriptionPayment>
            @Suppress("UNCHECKED_CAST")
            val collectors = args[3] as List<com.example.data.model.Collector>
            @Suppress("UNCHECKED_CAST")
            val depositLogs = args[4] as List<com.example.data.model.CollectorDepositLog>
            val profile = args[5] as? StoreProfile
            val filter = args[6] as BillingFilter
            val query = args[7] as String
            val msg = args[8] as? String

            val currentTime = System.currentTimeMillis()

            // Map customers to CustomerBillingItem
            val allBillingItems = customers.map { cust ->
                calculateCustomerBilling(cust, currentTime)
            }

            // Stats
            val overdueItems = allBillingItems.filter { it.status == BillingStatus.OVERDUE || it.status == BillingStatus.DUE_TODAY }
            val totalOverdueCount = overdueItems.size
            val totalOverdueSum = overdueItems.sumOf { it.totalAmountDue }
            val activeCustCount = customers.count { it.status == "ACTIVE" }
            val monthlyPotential = customers.filter { it.status == "ACTIVE" }.sumOf { it.monthlyPrice }

            // Filter
            val filteredBilling = allBillingItems.filter { item ->
                val matchesFilter = when (filter) {
                    BillingFilter.ALL -> true
                    BillingFilter.OVERDUE -> item.status == BillingStatus.OVERDUE || item.status == BillingStatus.DUE_TODAY
                    BillingFilter.PAID -> item.status == BillingStatus.PAID
                }
                val matchesQuery = query.isBlank() ||
                        item.customer.name.contains(query, ignoreCase = true) ||
                        item.customer.customerCode.contains(query, ignoreCase = true) ||
                        item.customer.phone.contains(query, ignoreCase = true) ||
                        item.customer.packageName.contains(query, ignoreCase = true)

                matchesFilter && matchesQuery
            }

            SubscriptionUiState(
                billingItems = filteredBilling,
                packages = packages,
                customers = customers,
                payments = payments,
                collectors = collectors,
                depositLogs = depositLogs,
                storeProfile = profile,
                selectedFilter = filter,
                searchQuery = query,
                totalOverdueCustomers = totalOverdueCount,
                totalOverdueAmount = totalOverdueSum,
                totalActiveCustomers = activeCustCount,
                totalMonthlyRevenuePotential = monthlyPotential,
                isCloudSyncEnabled = syncConfig.isEnabled,
                activeRole = roleInfo.first,
                activeCollectorId = roleInfo.second.first,
                activeCollectorName = roleInfo.second.second,
                message = msg
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SubscriptionUiState()
        )

        // Start listening to real-time Cloud Firestore updates if enabled
        if (syncConfig.isEnabled) {
            DynamicFirebaseManager.startListeningCustomerUpdates(application) { remoteCustomer ->
                viewModelScope.launch {
                    repository.saveSubscriptionCustomer(remoteCustomer)
                }
            }
            DynamicFirebaseManager.startListeningDepositLogUpdates(application) { remoteLog ->
                viewModelScope.launch {
                    repository.saveDepositLog(remoteLog)
                }
            }
        }
    }

    fun setFilter(filter: BillingFilter) {
        _selectedFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun savePackage(pkg: SubscriptionPackage) {
        viewModelScope.launch {
            repository.saveSubscriptionPackage(pkg)
            _message.value = "Paket langganan berhasil disimpan"
        }
    }

    fun deletePackage(pkg: SubscriptionPackage) {
        viewModelScope.launch {
            repository.deleteSubscriptionPackage(pkg)
            _message.value = "Paket langganan telah dihapus"
        }
    }

    fun saveCustomer(customer: SubscriptionCustomer) {
        viewModelScope.launch {
            val customerId = repository.saveSubscriptionCustomer(customer)
            val updated = if (customer.id == 0L) customer.copy(id = customerId) else customer
            // Sync to Firestore if cloud sync is enabled
            DynamicFirebaseManager.syncCustomerToCloud(getApplication(), updated)
            _message.value = "Data pelanggan langganan berhasil disimpan"
        }
    }

    fun deleteCustomer(customer: SubscriptionCustomer) {
        viewModelScope.launch {
            repository.deleteSubscriptionCustomer(customer)
            DynamicFirebaseManager.deleteCustomerFromCloud(getApplication(), customer.id)
            _message.value = "Pelanggan telah dihapus"
        }
    }

    // Collector CRUD
    fun saveCollector(collector: com.example.data.model.Collector) {
        viewModelScope.launch {
            repository.saveCollector(collector)
            DynamicFirebaseManager.syncCollectorToCloud(getApplication(), collector)
            _message.value = "Kolektor ${collector.name} berhasil disimpan"
        }
    }

    fun deleteCollector(collector: com.example.data.model.Collector) {
        viewModelScope.launch {
            repository.deleteCollector(collector)
            _message.value = "Kolektor telah dihapus"
        }
    }

    fun markCollectorDeposited(collectorId: String) {
        viewModelScope.launch {
            repository.markAllDepositedForCollector(collectorId)
            val config = DynamicFirebaseManager.getSyncConfig(getApplication())
            if (config.isEnabled) {
                val undeposited = uiState.value.depositLogs.filter { it.collectorId == collectorId && !it.isDepositedToAdmin }
                for (log in undeposited) {
                    DynamicFirebaseManager.markDepositLogOnCloud(getApplication(), log.id)
                }
            }
            _message.value = "Semua setoran kolektor berhasil ditandai telah disetor"
        }
    }

    fun markLogDeposited(logId: String) {
        viewModelScope.launch {
            repository.markLogDeposited(logId)
            val config = DynamicFirebaseManager.getSyncConfig(getApplication())
            if (config.isEnabled) {
                DynamicFirebaseManager.markDepositLogOnCloud(getApplication(), logId)
            }
            _message.value = "Setoran berhasil diterima dan ditandai lunas disetor"
        }
    }

    fun markAllLogsDeposited() {
        viewModelScope.launch {
            repository.markAllLogsDeposited()
            val config = DynamicFirebaseManager.getSyncConfig(getApplication())
            if (config.isEnabled) {
                val undeposited = uiState.value.depositLogs.filter { !it.isDepositedToAdmin }
                for (log in undeposited) {
                    DynamicFirebaseManager.markDepositLogOnCloud(getApplication(), log.id)
                }
            }
            _message.value = "Semua setoran uang kolektor berhasil diterima kasir"
        }
    }

    fun exportCollectorLogsToUri(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch {
            val logs = uiState.value.depositLogs
            val csv = com.example.util.CsvSpreadsheetUtils.exportCollectorDepositLogsToCsv(logs)
            val result = com.example.util.CsvSpreadsheetUtils.writeCsvToUri(context, uri, csv)
            if (result.isSuccess) {
                _message.value = "Berhasil mengekspor ${logs.size} transaksi kolektor ke Excel / CSV"
            } else {
                _message.value = "Gagal mengekspor: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun shareCollectorLogs(context: android.content.Context) {
        viewModelScope.launch {
            val logs = uiState.value.depositLogs
            val csv = com.example.util.CsvSpreadsheetUtils.exportCollectorDepositLogsToCsv(logs)
            val fileName = "Laporan_Kolektor_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.csv"
            com.example.util.CsvSpreadsheetUtils.shareCsvFile(
                context = context,
                csvString = csv,
                fileName = fileName,
                subject = "Laporan Transaksi & Setoran Kolektor TokoPOS"
            )
        }
    }

    fun processPayment(
        customer: SubscriptionCustomer,
        monthsCount: Int,
        paymentMethod: String,
        notes: String,
        onSuccess: ((SubscriptionPayment, Long) -> Unit)? = null
    ) {
        viewModelScope.launch {
            val totalAmount = customer.monthlyPrice * monthsCount
            val invoiceNumber = generatePaymentInvoice()

            // Calculate new paidUntilTimestamp
            val cal = Calendar.getInstance()
            if (customer.paidUntilTimestamp > System.currentTimeMillis()) {
                cal.timeInMillis = customer.paidUntilTimestamp
            } else {
                cal.timeInMillis = customer.paidUntilTimestamp
                if (cal.get(Calendar.YEAR) < 2020) {
                    cal.timeInMillis = System.currentTimeMillis()
                }
            }
            cal.set(Calendar.DAY_OF_MONTH, customer.dueDay.coerceIn(1, 28))
            cal.add(Calendar.MONTH, monthsCount)
            val newPaidUntil = cal.timeInMillis

            val periodStart = customer.paidUntilTimestamp
            val periodEnd = newPaidUntil

            val currentState = uiState.value
            val isCollectorRole = currentState.activeRole == "COLLECTOR"
            val collectorName = if (isCollectorRole) currentState.activeCollectorName else "Kasir Admin"
            val collectorId = if (isCollectorRole) currentState.activeCollectorId else "ADMIN"

            val payment = SubscriptionPayment(
                customerId = customer.id,
                customerCode = customer.customerCode,
                customerName = customer.name,
                invoiceNumber = invoiceNumber,
                packageName = customer.packageName,
                monthlyPrice = customer.monthlyPrice,
                monthsCount = monthsCount,
                periodStartTimestamp = periodStart,
                periodEndTimestamp = periodEnd,
                totalAmount = totalAmount,
                paymentMethod = paymentMethod,
                timestamp = System.currentTimeMillis(),
                notes = if (isCollectorRole) "Diterima Kolektor: $collectorName. $notes" else notes
            )

            // If collector, create deposit log
            if (isCollectorRole) {
                val depositLog = com.example.data.model.CollectorDepositLog(
                    id = invoiceNumber,
                    collectorId = collectorId,
                    collectorName = collectorName,
                    customerId = customer.id,
                    customerCode = customer.customerCode,
                    customerName = customer.name,
                    amountPaid = totalAmount,
                    paidAt = System.currentTimeMillis(),
                    isDepositedToAdmin = false
                )
                repository.recordCollectorPayment(depositLog, payment, newPaidUntil)
                // Realtime sync to Cloud
                DynamicFirebaseManager.syncDepositLogToCloud(getApplication(), depositLog, payment, newPaidUntil)
            } else {
                repository.recordSubscriptionPayment(payment, newPaidUntil)
                // Update cloud customer
                val updatedCust = customer.copy(paidUntilTimestamp = newPaidUntil)
                DynamicFirebaseManager.syncCustomerToCloud(getApplication(), updatedCust)
            }

            _message.value = "Pembayaran ${customer.name} ($monthsCount Bulan) berhasil dicatat!"
            onSuccess?.invoke(payment, newPaidUntil)
        }
    }

    fun exportToCsvUri(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch {
            val csv = com.example.util.CsvSpreadsheetUtils.exportSubscriptionCustomersToCsv(uiState.value.customers)
            val result = com.example.util.CsvSpreadsheetUtils.writeCsvToUri(context, uri, csv)
            if (result.isSuccess) {
                _message.value = "Berhasil mengekspor ${uiState.value.customers.size} pelanggan langganan ke Excel"
            } else {
                _message.value = "Gagal mengekspor: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun exportTemplateUri(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch {
            val csv = com.example.util.CsvSpreadsheetUtils.generateSubscriptionCustomerTemplateCsv(uiState.value.packages)
            val result = com.example.util.CsvSpreadsheetUtils.writeCsvToUri(context, uri, csv)
            if (result.isSuccess) {
                _message.value = "Template format pelanggan langganan berhasil diunduh"
            } else {
                _message.value = "Gagal mengunduh template: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun importCustomersFromUri(
        context: android.content.Context,
        uri: android.net.Uri,
        onParsed: (List<SubscriptionCustomer>) -> Unit
    ) {
        viewModelScope.launch {
            val readResult = com.example.util.CsvSpreadsheetUtils.readCsvFromUri(context, uri)
            if (readResult.isFailure) {
                _message.value = "Gagal membaca file: ${readResult.exceptionOrNull()?.message}"
                return@launch
            }
            val parsedCustomers = com.example.util.CsvSpreadsheetUtils.parseSubscriptionCustomersFromCsv(
                readResult.getOrThrow(),
                uiState.value.packages
            )
            if (parsedCustomers.isEmpty()) {
                _message.value = "Tidak ada data pelanggan langganan yang valid dalam file Excel/CSV ini"
                return@launch
            }
            onParsed(parsedCustomers)
        }
    }

    fun executeImport(customers: List<SubscriptionCustomer>) {
        viewModelScope.launch {
            repository.importSubscriptionCustomers(customers)
            _message.value = "Berhasil mengimpor ${customers.size} pelanggan langganan!"
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    private fun generatePaymentInvoice(): String {
        val sdf = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault())
        return "KW-" + sdf.format(Date())
    }

    companion object {
        fun calculateCustomerBilling(customer: SubscriptionCustomer, currentTime: Long): CustomerBillingItem {
            val nowCal = Calendar.getInstance().apply { timeInMillis = currentTime }
            val paidCal = Calendar.getInstance().apply { timeInMillis = customer.paidUntilTimestamp }

            // Target due date for current month
            val currentMonthDueCal = Calendar.getInstance().apply {
                timeInMillis = currentTime
                set(Calendar.DAY_OF_MONTH, customer.dueDay.coerceIn(1, 28))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
            }

            if (customer.paidUntilTimestamp >= currentMonthDueCal.timeInMillis) {
                // Already paid for this month or beyond
                return CustomerBillingItem(
                    customer = customer,
                    status = BillingStatus.PAID,
                    monthsDue = 0,
                    totalAmountDue = 0.0,
                    nextDueDateTimestamp = customer.paidUntilTimestamp
                )
            }

            // Calculate months difference
            var monthsDiff = (nowCal.get(Calendar.YEAR) - paidCal.get(Calendar.YEAR)) * 12 +
                    (nowCal.get(Calendar.MONTH) - paidCal.get(Calendar.MONTH))

            if (monthsDiff < 1) monthsDiff = 1

            val totalAmount = customer.monthlyPrice * monthsDiff
            val todayDay = nowCal.get(Calendar.DAY_OF_MONTH)

            val status = when {
                monthsDiff > 1 -> BillingStatus.OVERDUE
                todayDay == customer.dueDay -> BillingStatus.DUE_TODAY
                todayDay > customer.dueDay -> BillingStatus.OVERDUE
                todayDay >= customer.dueDay - 3 -> BillingStatus.UPCOMING
                else -> BillingStatus.UPCOMING
            }

            return CustomerBillingItem(
                customer = customer,
                status = status,
                monthsDue = monthsDiff,
                totalAmountDue = totalAmount,
                nextDueDateTimestamp = currentMonthDueCal.timeInMillis
            )
        }
    }
}
