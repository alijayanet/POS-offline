package com.example.ui.screens.expenses

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Expense
import com.example.data.repository.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExpensesUiState(
    val expenses: List<Expense> = emptyList(),
    val totalExpenses: Double = 0.0,
    val selectedCategory: String = "Semua",
    val message: String? = null
)

class ExpensesViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: StoreRepository

    private val _selectedCategory = MutableStateFlow("Semua")
    private val _message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ExpensesUiState>

    init {
        val db = AppDatabase.getInstance(application)
        repository = StoreRepository(db)

        uiState = combine(
            repository.allExpenses,
            repository.totalExpenses,
            _selectedCategory,
            _message
        ) { args ->
            @Suppress("UNCHECKED_CAST")
            val expenses = args[0] as List<Expense>
            val totalExp = (args[1] as? Double) ?: 0.0
            val category = args[2] as String
            val msg = args[3] as? String

            val filtered = if (category == "Semua") {
                expenses
            } else {
                expenses.filter { it.category.equals(category, ignoreCase = true) }
            }
            ExpensesUiState(
                expenses = filtered,
                totalExpenses = totalExp,
                selectedCategory = category,
                message = msg
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ExpensesUiState()
        )
    }

    fun setCategoryFilter(category: String) {
        _selectedCategory.value = category
    }

    fun addExpense(category: String, amount: Double, notes: String) {
        viewModelScope.launch {
            repository.saveExpense(
                Expense(
                    category = category,
                    amount = amount,
                    notes = notes,
                    timestamp = System.currentTimeMillis()
                )
            )
            _message.value = "Biaya pengeluaran berhasil dicatat"
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            _message.value = "Pengeluaran telah dihapus"
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
