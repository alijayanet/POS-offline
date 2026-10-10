package com.example.ui.screens.debts

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Customer
import com.example.data.model.DebtRecord
import com.example.data.repository.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import com.example.data.model.StoreProfile

data class DebtsUiState(
    val customers: List<Customer> = emptyList(),
    val debtRecords: List<DebtRecord> = emptyList(),
    val totalCustomerDebt: Double = 0.0,
    val storeProfile: StoreProfile? = null,
    val searchQuery: String = "",
    val message: String? = null
)

class DebtsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: StoreRepository

    private val _searchQuery = MutableStateFlow("")
    private val _message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<DebtsUiState>

    init {
        val db = AppDatabase.getInstance(application)
        repository = StoreRepository(db)

        uiState = combine(
            repository.allCustomers,
            repository.unsettledDebts,
            repository.totalCustomerDebt,
            repository.storeProfile,
            _searchQuery
        ) { args ->
            @Suppress("UNCHECKED_CAST")
            val allCusts = args[0] as List<Customer>
            @Suppress("UNCHECKED_CAST")
            val debts = args[1] as List<DebtRecord>
            val totalDebt = (args[2] as? Double) ?: 0.0
            val profile = args[3] as? StoreProfile
            val query = args[4] as String

            val filteredCustomers = allCusts.filter { cust ->
                query.isBlank() ||
                cust.name.contains(query, ignoreCase = true) ||
                cust.phone.contains(query, ignoreCase = true)
            }

            DebtsUiState(
                customers = filteredCustomers,
                debtRecords = debts,
                totalCustomerDebt = totalDebt,
                storeProfile = profile,
                searchQuery = query
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = DebtsUiState()
        )
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun saveCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.saveCustomer(customer)
            _message.value = "Data pelanggan berhasil disimpan"
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
            _message.value = "Pelanggan dihapus"
        }
    }

    fun payDebt(debt: DebtRecord, amount: Double) {
        viewModelScope.launch {
            repository.payDebt(debt.id, debt.customerId, amount)
            _message.value = "Pembayaran hutang berhasil dicatat"
        }
    }

    fun exportToCsvUri(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch {
            val csv = com.example.util.CsvSpreadsheetUtils.exportCustomersToCsv(uiState.value.customers)
            val result = com.example.util.CsvSpreadsheetUtils.writeCsvToUri(context, uri, csv)
            if (result.isSuccess) {
                _message.value = "Berhasil mengekspor ${uiState.value.customers.size} data pelanggan ke Excel"
            } else {
                _message.value = "Gagal mengekspor: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun exportTemplateUri(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch {
            val csv = com.example.util.CsvSpreadsheetUtils.generateCustomerTemplateCsv()
            val result = com.example.util.CsvSpreadsheetUtils.writeCsvToUri(context, uri, csv)
            if (result.isSuccess) {
                _message.value = "Template format data pelanggan berhasil diunduh"
            } else {
                _message.value = "Gagal mengunduh template: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun importCustomersFromUri(
        context: android.content.Context,
        uri: android.net.Uri,
        onParsed: (List<Customer>) -> Unit
    ) {
        viewModelScope.launch {
            val readResult = com.example.util.CsvSpreadsheetUtils.readCsvFromUri(context, uri)
            if (readResult.isFailure) {
                _message.value = "Gagal membaca file: ${readResult.exceptionOrNull()?.message}"
                return@launch
            }
            val parsedCustomers = com.example.util.CsvSpreadsheetUtils.parseCustomersFromCsv(readResult.getOrThrow())
            if (parsedCustomers.isEmpty()) {
                _message.value = "Tidak ada data pelanggan yang valid dalam file Excel/CSV ini"
                return@launch
            }
            onParsed(parsedCustomers)
        }
    }

    fun executeImport(customers: List<Customer>) {
        viewModelScope.launch {
            repository.importCustomers(customers)
            _message.value = "Berhasil mengimpor ${customers.size} data pelanggan!"
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
