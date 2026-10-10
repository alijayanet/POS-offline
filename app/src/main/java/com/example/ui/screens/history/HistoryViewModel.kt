package com.example.ui.screens.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.PaymentMethod
import com.example.data.model.StoreProfile
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionOrder
import com.example.data.repository.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

enum class HistoryDateFilter(val displayName: String) {
    TODAY("Hari Ini"),
    LAST_7_DAYS("7 Hari"),
    THIS_MONTH("Bulan Ini"),
    ALL("Semua")
}

data class HistoryUiState(
    val orders: List<TransactionOrder> = emptyList(),
    val storeProfile: StoreProfile? = null,
    val dateFilter: HistoryDateFilter = HistoryDateFilter.ALL,
    val paymentFilter: String = "Semua",
    val searchQuery: String = "",
    val totalRevenue: Double = 0.0,
    val totalProfit: Double = 0.0,
    val totalOrdersCount: Int = 0,
    val selectedOrderForReceipt: TransactionOrder? = null,
    val selectedOrderItems: List<TransactionItem> = emptyList(),
    val message: String? = null
)

class HistoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: StoreRepository

    private val _dateFilter = MutableStateFlow(HistoryDateFilter.ALL)
    private val _paymentFilter = MutableStateFlow("Semua")
    private val _searchQuery = MutableStateFlow("")
    private val _selectedOrder = MutableStateFlow<TransactionOrder?>(null)
    private val _selectedOrderItems = MutableStateFlow<List<TransactionItem>>(emptyList())
    private val _message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<HistoryUiState>

    init {
        val db = AppDatabase.getInstance(application)
        repository = StoreRepository(db)

        uiState = combine(
            repository.allTransactions,
            repository.storeProfile,
            _dateFilter,
            _paymentFilter,
            _searchQuery,
            _selectedOrder,
            _selectedOrderItems
        ) { args ->
            @Suppress("UNCHECKED_CAST")
            val allOrders = args[0] as List<TransactionOrder>
            val profile = args[1] as? StoreProfile
            val dateF = args[2] as HistoryDateFilter
            val payF = args[3] as String
            val query = args[4] as String
            val selOrder = args[5] as? TransactionOrder
            @Suppress("UNCHECKED_CAST")
            val selItems = args[6] as List<TransactionItem>

            val now = System.currentTimeMillis()
            val filteredByDate = allOrders.filter { order ->
                when (dateF) {
                    HistoryDateFilter.ALL -> true
                    HistoryDateFilter.TODAY -> {
                        val cal = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        order.timestamp >= cal.timeInMillis
                    }
                    HistoryDateFilter.LAST_7_DAYS -> {
                        val sevenDaysAgo = now - (7L * 86400000L)
                        order.timestamp >= sevenDaysAgo
                    }
                    HistoryDateFilter.THIS_MONTH -> {
                        val cal = Calendar.getInstance().apply {
                            set(Calendar.DAY_OF_MONTH, 1)
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        order.timestamp >= cal.timeInMillis
                    }
                }
            }

            val filtered = filteredByDate.filter { order ->
                val matchesPayment = (payF == "Semua" || order.paymentMethod.equals(payF, ignoreCase = true))
                val matchesQuery = query.isBlank() ||
                        order.invoiceNumber.contains(query, ignoreCase = true) ||
                        order.customerName.contains(query, ignoreCase = true)
                matchesPayment && matchesQuery
            }

            HistoryUiState(
                orders = filtered,
                storeProfile = profile,
                dateFilter = dateF,
                paymentFilter = payF,
                searchQuery = query,
                totalRevenue = filtered.sumOf { it.grandTotal },
                totalProfit = filtered.sumOf { it.totalProfit },
                totalOrdersCount = filtered.size,
                selectedOrderForReceipt = selOrder,
                selectedOrderItems = selItems
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HistoryUiState()
        )
    }

    fun setDateFilter(filter: HistoryDateFilter) {
        _dateFilter.value = filter
    }

    fun setPaymentFilter(filter: String) {
        _paymentFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun viewReceipt(order: TransactionOrder) {
        viewModelScope.launch {
            val items = repository.getItemsForOrderSync(order.id)
            _selectedOrder.value = order
            _selectedOrderItems.value = items
        }
    }

    fun dismissReceipt() {
        _selectedOrder.value = null
        _selectedOrderItems.value = emptyList()
    }

    fun cancelTransaction(order: TransactionOrder) {
        viewModelScope.launch {
            repository.cancelTransaction(order)
            _message.value = "Transaksi ${order.invoiceNumber} telah dibatalkan & stok dikembalikan"
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
