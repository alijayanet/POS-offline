package com.example.ui.screens.inventory

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Category
import com.example.data.model.Product
import com.example.data.repository.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class StockFilterOption(val displayName: String) {
    ALL("Semua"),
    LOW_STOCK("Stok Menipis"),
    OUT_OF_STOCK("Stok Habis"),
    SAFE("Stok Aman")
}

data class InventoryUiState(
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategory: String = "Semua",
    val stockFilter: StockFilterOption = StockFilterOption.ALL,
    val searchQuery: String = "",
    val totalProductCount: Int = 0,
    val lowStockCount: Int = 0,
    val totalInventoryCostValue: Double = 0.0,
    val totalInventoryRetailValue: Double = 0.0,
    val message: String? = null
)

class InventoryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: StoreRepository

    private val _selectedCategory = MutableStateFlow("Semua")
    private val _stockFilter = MutableStateFlow(StockFilterOption.ALL)
    private val _searchQuery = MutableStateFlow("")
    private val _message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<InventoryUiState>

    init {
        val db = AppDatabase.getInstance(application)
        repository = StoreRepository(db)

        uiState = combine(
            repository.allProducts,
            repository.allCategories,
            repository.lowStockCount,
            repository.totalProductCount,
            repository.totalInventoryCostValue,
            repository.totalInventoryRetailValue,
            _selectedCategory,
            _stockFilter,
            _searchQuery
        ) { args ->
            @Suppress("UNCHECKED_CAST")
            val allProds = args[0] as List<Product>
            @Suppress("UNCHECKED_CAST")
            val allCats = args[1] as List<Category>
            val lowCount = (args[2] as? Int) ?: 0
            val totalProd = (args[3] as? Int) ?: 0
            val costVal = (args[4] as? Double) ?: 0.0
            val retailVal = (args[5] as? Double) ?: 0.0
            val cat = args[6] as String
            val filter = args[7] as StockFilterOption
            val query = args[8] as String

            val filtered = allProds.filter { prod ->
                val matchesCategory = (cat == "Semua" || prod.category.equals(cat, ignoreCase = true))
                val matchesStock = when (filter) {
                    StockFilterOption.ALL -> true
                    StockFilterOption.LOW_STOCK -> prod.stock <= prod.minStockAlert && prod.stock > 0
                    StockFilterOption.OUT_OF_STOCK -> prod.stock <= 0
                    StockFilterOption.SAFE -> prod.stock > prod.minStockAlert
                }
                val matchesQuery = query.isBlank() ||
                        prod.name.contains(query, ignoreCase = true) ||
                        prod.barcode.contains(query, ignoreCase = true) ||
                        prod.supplier.contains(query, ignoreCase = true)

                matchesCategory && matchesStock && matchesQuery
            }

            InventoryUiState(
                products = filtered,
                categories = allCats,
                selectedCategory = cat,
                stockFilter = filter,
                searchQuery = query,
                totalProductCount = totalProd,
                lowStockCount = lowCount,
                totalInventoryCostValue = costVal,
                totalInventoryRetailValue = retailVal
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = InventoryUiState()
        )
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setStockFilter(filter: StockFilterOption) {
        _stockFilter.value = filter
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun saveProduct(product: Product) {
        viewModelScope.launch {
            repository.saveProduct(product)
            _message.value = "Produk berhasil disimpan"
        }
    }

    fun adjustStock(productId: Long, amountToAdd: Double) {
        viewModelScope.launch {
            repository.adjustStock(productId, amountToAdd)
            _message.value = "Stok berhasil diperbarui"
        }
    }

    fun deleteProduct(productId: Long) {
        viewModelScope.launch {
            repository.deleteProduct(productId)
            _message.value = "Produk telah dihapus"
        }
    }

    fun printBarcodeLabel(product: Product, copies: Int = 1) {
        viewModelScope.launch {
            val profile = repository.getStoreProfileSync()
            val mac = profile?.bluetoothPrinterMac ?: ""
            if (mac.isBlank()) {
                _message.value = "Belum ada printer Bluetooth yang tersimpan. Atur di menu Pengaturan."
                return@launch
            }
            val res = com.example.util.ThermalPrinterManager.printProductBarcodeLabel(
                context = getApplication(),
                deviceAddress = mac,
                product = product,
                copies = copies,
                storeName = profile?.storeName ?: "TOKO KASIR"
            )
            if (res.isSuccess) {
                _message.value = "Label barcode berhasil dikirim ke printer ($copies label)"
            } else {
                _message.value = "Gagal mencetak barcode: ${res.exceptionOrNull()?.message}"
            }
        }
    }

    fun exportToCsvUri(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                val prods = uiState.value.products
                val csv = com.example.util.CsvSpreadsheetUtils.exportProductsToCsv(prods)
                val res = com.example.util.CsvSpreadsheetUtils.writeCsvToUri(context, uri, csv)
                if (res.isSuccess) {
                    _message.value = "Data stok (${prods.size} produk) berhasil diekspor ke Excel/CSV!"
                } else {
                    _message.value = "Gagal menyimpan file: ${res.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _message.value = "Gagal ekspor: ${e.message}"
            }
        }
    }

    fun shareProductsCsv(context: android.content.Context) {
        viewModelScope.launch {
            try {
                val prods = uiState.value.products
                val csv = com.example.util.CsvSpreadsheetUtils.exportProductsToCsv(prods)
                val sdf = java.text.SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault())
                val fileName = "Stok_Produk_TokoPOS_${sdf.format(java.util.Date())}.csv"
                com.example.util.CsvSpreadsheetUtils.shareCsvFile(context, csv, fileName, "Data Stok Produk TokoPOS")
            } catch (e: Exception) {
                _message.value = "Gagal membagikan CSV: ${e.message}"
            }
        }
    }

    fun exportTemplateUri(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                val csv = com.example.util.CsvSpreadsheetUtils.generateProductTemplateCsv()
                val res = com.example.util.CsvSpreadsheetUtils.writeCsvToUri(context, uri, csv)
                if (res.isSuccess) {
                    _message.value = "Template Excel/CSV produk berhasil disimpan!"
                } else {
                    _message.value = "Gagal menyimpan template: ${res.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _message.value = "Gagal membuat template: ${e.message}"
            }
        }
    }

    fun shareTemplateCsv(context: android.content.Context) {
        viewModelScope.launch {
            try {
                val csv = com.example.util.CsvSpreadsheetUtils.generateProductTemplateCsv()
                val fileName = "Template_Impor_Produk_TokoPOS.csv"
                com.example.util.CsvSpreadsheetUtils.shareCsvFile(context, csv, fileName, "Template Impor Produk TokoPOS")
            } catch (e: Exception) {
                _message.value = "Gagal membagikan template: ${e.message}"
            }
        }
    }

    fun importProductsFromUri(
        context: android.content.Context,
        uri: android.net.Uri,
        onPreview: (List<Product>) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val readRes = com.example.util.CsvSpreadsheetUtils.readCsvFromUri(context, uri)
                if (readRes.isFailure) {
                    _message.value = "Gagal membaca file: ${readRes.exceptionOrNull()?.message}"
                    return@launch
                }
                val csvString = readRes.getOrNull() ?: ""
                val products = com.example.util.CsvSpreadsheetUtils.parseProductsFromCsv(csvString)
                if (products.isEmpty()) {
                    _message.value = "Tidak ada data produk valid yang ditemukan dalam file CSV."
                } else {
                    onPreview(products)
                }
            } catch (e: Exception) {
                _message.value = "Gagal memproses file impor: ${e.message}"
            }
        }
    }

    fun executeImport(products: List<Product>) {
        viewModelScope.launch {
            try {
                repository.importProducts(products)
                _message.value = "Berhasil mengimpor ${products.size} produk ke katalog stok!"
            } catch (e: Exception) {
                _message.value = "Gagal menyimpan produk impor: ${e.message}"
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
