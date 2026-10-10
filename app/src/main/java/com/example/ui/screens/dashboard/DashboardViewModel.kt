package com.example.ui.screens.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.BestSellingProduct
import com.example.data.model.Product
import com.example.data.model.StoreProfile
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionOrder
import com.example.data.repository.StoreRepository
import com.example.ui.components.CurrencyUtils
import com.example.ui.components.DailySalesData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

data class DashboardUiState(
    val storeProfile: StoreProfile? = null,
    val todayRevenue: Double = 0.0,
    val todayProfit: Double = 0.0,
    val todayOrdersCount: Int = 0,
    val lowStockCount: Int = 0,
    val lowStockList: List<Product> = emptyList(),
    val totalProductCount: Int = 0,
    val totalCustomerDebt: Double = 0.0,
    val recentTransactions: List<TransactionOrder> = emptyList(),
    val bestSellingProducts: List<BestSellingProduct> = emptyList(),
    val weeklySalesChart: List<DailySalesData> = emptyList(),
    val overdueSubscriptionCount: Int = 0,
    val overdueSubscriptionTotal: Double = 0.0
)

class DashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: StoreRepository

    val uiState: StateFlow<DashboardUiState>

    init {
        val db = AppDatabase.getInstance(application)
        repository = StoreRepository(db)

        val (todayCountFlow, todayRevFlow, todayProfFlow) = repository.getTodayStats()

        uiState = combine(
            repository.storeProfile,
            todayRevFlow,
            todayProfFlow,
            todayCountFlow,
            repository.lowStockCount,
            repository.lowStockProducts,
            repository.totalProductCount,
            repository.totalCustomerDebt,
            repository.allTransactions,
            repository.bestSellingProducts,
            repository.allSubscriptionCustomers
        ) { args ->
            val profile = args[0] as? StoreProfile
            val todayRev = (args[1] as? Double) ?: 0.0
            val todayProf = (args[2] as? Double) ?: 0.0
            val todayCount = (args[3] as? Int) ?: 0
            val lowCount = (args[4] as? Int) ?: 0
            @Suppress("UNCHECKED_CAST")
            val lowList = (args[5] as? List<Product>) ?: emptyList()
            val totalProd = (args[6] as? Int) ?: 0
            val totalDebt = (args[7] as? Double) ?: 0.0
            @Suppress("UNCHECKED_CAST")
            val allOrders = (args[8] as? List<TransactionOrder>) ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val bestSelling = (args[9] as? List<BestSellingProduct>) ?: emptyList()
            @Suppress("UNCHECKED_CAST")
            val subCustomers = (args[10] as? List<com.example.data.model.SubscriptionCustomer>) ?: emptyList()

            // Calculate subscription overdues
            val currentTime = System.currentTimeMillis()
            var overdueCount = 0
            var overdueSum = 0.0
            for (c in subCustomers) {
                if (c.status == "ACTIVE" || c.status == "ISOLIR") {
                    val item = com.example.ui.screens.subscription.SubscriptionViewModel.calculateCustomerBilling(c, currentTime)
                    if (item.status == com.example.ui.screens.subscription.BillingStatus.OVERDUE || item.status == com.example.ui.screens.subscription.BillingStatus.DUE_TODAY) {
                        overdueCount++
                        overdueSum += item.totalAmountDue
                    }
                }
            }

            // Calculate 7-day sales breakdown
            val chartData = calculateWeeklyChartData(allOrders)

            DashboardUiState(
                storeProfile = profile,
                todayRevenue = todayRev,
                todayProfit = todayProf,
                todayOrdersCount = todayCount,
                lowStockCount = lowCount,
                lowStockList = lowList.take(5),
                totalProductCount = totalProd,
                totalCustomerDebt = totalDebt,
                recentTransactions = allOrders.take(5),
                bestSellingProducts = bestSelling,
                weeklySalesChart = chartData,
                overdueSubscriptionCount = overdueCount,
                overdueSubscriptionTotal = overdueSum
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DashboardUiState()
        )
    }

    private fun calculateWeeklyChartData(orders: List<TransactionOrder>): List<DailySalesData> {
        val result = mutableListOf<DailySalesData>()
        val calendar = Calendar.getInstance()

        // 7 days ending today
        for (i in 6 downTo 0) {
            val dayCal = Calendar.getInstance().apply {
                timeInMillis = calendar.timeInMillis
                add(Calendar.DAY_OF_YEAR, -i)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val startOfDay = dayCal.timeInMillis
            val endOfDay = startOfDay + 86400000L - 1

            val dayOrders = orders.filter { it.timestamp in startOfDay..endOfDay }
            val totalSales = dayOrders.sumOf { it.grandTotal }
            val count = dayOrders.size
            val label = if (i == 0) "Hari ini" else CurrencyUtils.formatDayName(startOfDay)

            result.add(DailySalesData(dayLabel = label, totalSales = totalSales, orderCount = count))
        }

        return result
    }

    fun saveManualInvoice(
        order: TransactionOrder,
        items: List<TransactionItem>,
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val orderId = repository.saveManualInvoice(order, items)
            onSuccess(orderId)
        }
    }
}
