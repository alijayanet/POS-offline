package com.example.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.Category
import com.example.data.model.StoreProfile
import com.example.data.repository.StoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val storeProfile: StoreProfile? = null,
    val categories: List<Category> = emptyList(),
    val message: String? = null
)

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: StoreRepository
    private val appDatabase: AppDatabase

    private val _message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SettingsUiState>

    init {
        appDatabase = AppDatabase.getInstance(application)
        repository = StoreRepository(appDatabase)

        uiState = combine(
            repository.storeProfile,
            repository.allCategories,
            _message
        ) { profile, categories, msg ->
            SettingsUiState(
                storeProfile = profile,
                categories = categories,
                message = msg
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUiState()
        )
    }

    fun saveStoreProfile(profile: StoreProfile) {
        viewModelScope.launch {
            repository.saveStoreProfile(profile)
            _message.value = "Profil toko berhasil disimpan"
        }
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            repository.saveCategory(Category(name = name))
            _message.value = "Kategori $name ditambahkan"
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
            _message.value = "Kategori ${category.name} dihapus"
        }
    }

    fun resetDemoData() {
        viewModelScope.launch {
            appDatabase.seedInitialData()
            _message.value = "Data demo toko berhasil dimuat ulang"
        }
    }

    fun scanQrisFromImage(uri: android.net.Uri) {
        viewModelScope.launch {
            val payload = com.example.util.QrCodeGenerator.decodeFromUri(getApplication(), uri)
            if (!payload.isNullOrBlank()) {
                val current = uiState.value.storeProfile ?: StoreProfile()
                repository.saveStoreProfile(current.copy(qrisStaticPayload = payload))
                val merchant = com.example.util.QrisEngine.getMerchantNameFromPayload(payload)
                _message.value = "QRIS Terdeteksi! Merchant: $merchant"
            } else {
                _message.value = "Gagal membaca QR Code dari gambar yang dipilih."
            }
        }
    }

    fun testPrint(deviceAddress: String, paperSize: String) {
        viewModelScope.launch {
            val profile = uiState.value.storeProfile
            val dummyOrder = com.example.data.model.TransactionOrder(
                invoiceNumber = "TEST-PRINT-001",
                customerName = "Tes Cetak Printer",
                subtotal = 50000.0,
                grandTotal = 50000.0,
                paidAmount = 50000.0,
                paymentMethod = "CASH"
            )
            val dummyItems = listOf(
                com.example.data.model.TransactionItem(
                    orderId = 0,
                    productId = 1,
                    productName = "Tes Cetak Struk POS",
                    category = "Umum",
                    buyPrice = 10000.0,
                    sellPrice = 50000.0,
                    quantity = 1.0,
                    unit = "Pcs",
                    subtotal = 50000.0,
                    profit = 40000.0
                )
            )

            val res = com.example.util.ThermalPrinterManager.printBluetoothReceipt(
                getApplication(), deviceAddress, dummyOrder, dummyItems, profile, paperSize
            )
            if (res.isSuccess) {
                _message.value = "Tes cetak struk berhasil terkirim!"
            } else {
                _message.value = "Gagal tes cetak: ${res.exceptionOrNull()?.message}"
            }
        }
    }

    fun shareBackup(context: android.content.Context) {
        viewModelScope.launch {
            try {
                val json = com.example.util.BackupManager.createBackupJson(appDatabase)
                com.example.util.BackupManager.shareBackupJson(context, json)
            } catch (e: Exception) {
                _message.value = "Gagal membuat file backup: ${e.message}"
            }
        }
    }

    fun saveBackupToUri(context: android.content.Context, uri: android.net.Uri) {
        viewModelScope.launch {
            try {
                val json = com.example.util.BackupManager.createBackupJson(appDatabase)
                val res = com.example.util.BackupManager.writeJsonToUri(context, uri, json)
                if (res.isSuccess) {
                    _message.value = "Backup data berhasil disimpan ke perangkat!"
                } else {
                    _message.value = "Gagal menyimpan file backup: ${res.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _message.value = "Gagal memproses backup: ${e.message}"
            }
        }
    }

    fun restoreFromUri(
        context: android.content.Context,
        uri: android.net.Uri,
        replaceExisting: Boolean,
        onSuccess: (com.example.util.BackupSummary) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val readRes = com.example.util.BackupManager.readJsonFromUri(context, uri)
                if (readRes.isFailure) {
                    _message.value = "Gagal membaca file backup: ${readRes.exceptionOrNull()?.message}"
                    return@launch
                }
                val jsonString = readRes.getOrNull() ?: ""
                val res = com.example.util.BackupManager.restoreFromJson(appDatabase, jsonString, replaceExisting)
                if (res.isSuccess) {
                    val summary = res.getOrThrow()
                    onSuccess(summary)
                    _message.value = "Restore berhasil! ${summary.totalProducts} produk dan ${summary.totalOrders} transaksi berhasil dipulihkan."
                } else {
                    _message.value = "Gagal restore database: ${res.exceptionOrNull()?.message}"
                }
            } catch (e: Exception) {
                _message.value = "Gagal restore: ${e.message}"
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }
}
