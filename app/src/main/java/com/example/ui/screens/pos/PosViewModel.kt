package com.example.ui.screens.pos

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.CartItem
import com.example.data.model.Category
import com.example.data.model.Customer
import com.example.data.model.HeldCart
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.data.model.StoreProfile
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionOrder
import com.example.data.repository.StoreRepository
import com.example.util.QrisEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

data class PosUiState(
    val products: List<Product> = emptyList(),
    val allRawProducts: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val customers: List<Customer> = emptyList(),
    val storeProfile: StoreProfile? = null,
    val heldCarts: List<HeldCart> = emptyList(),
    val selectedCategory: String = "Semua",
    val searchQuery: String = "",
    val cartItems: List<CartItem> = emptyList(),
    val selectedCustomer: Customer? = null,
    val totalDiscount: Double = 0.0,
    val lastCompletedOrder: TransactionOrder? = null,
    val lastCompletedItems: List<TransactionItem> = emptyList(),
    val isCheckingOut: Boolean = false,
    val isCameraScanning: Boolean = false,
    val message: String? = null
) {
    val subtotal: Double
        get() = cartItems.sumOf { it.subtotal }

    val taxAmount: Double
        get() {
            val taxRate = if (storeProfile?.isTaxEnabled == true) (storeProfile.taxRatePercent / 100.0) else 0.0
            return (subtotal - totalDiscount).coerceAtLeast(0.0) * taxRate
        }

    val grandTotal: Double
        get() = ((subtotal - totalDiscount).coerceAtLeast(0.0) + taxAmount)

    val totalItemsCount: String
        get() = com.example.ui.components.CurrencyUtils.formatQty(cartItems.sumOf { it.quantity })
}

class PosViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: StoreRepository

    private val _selectedCategory = MutableStateFlow("Semua")
    private val _searchQuery = MutableStateFlow("")
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    private val _totalDiscount = MutableStateFlow(0.0)
    private val _lastCompletedOrder = MutableStateFlow<TransactionOrder?>(null)
    private val _lastCompletedItems = MutableStateFlow<List<TransactionItem>>(emptyList())
    private val _isCheckingOut = MutableStateFlow(false)
    private val _isCameraScanning = MutableStateFlow(false)
    private val _message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<PosUiState>

    init {
        val db = AppDatabase.getInstance(application)
        repository = StoreRepository(db)

        uiState = combine(
            repository.allProducts,
            repository.allCategories,
            repository.allCustomers,
            repository.storeProfile,
            repository.allHeldCarts,
            _selectedCategory,
            _searchQuery,
            _cartItems,
            _selectedCustomer,
            _totalDiscount,
            _lastCompletedOrder,
            _lastCompletedItems,
            _isCheckingOut,
            _isCameraScanning
        ) { args ->
            @Suppress("UNCHECKED_CAST")
            val allProds = args[0] as List<Product>
            @Suppress("UNCHECKED_CAST")
            val allCats = args[1] as List<Category>
            @Suppress("UNCHECKED_CAST")
            val allCusts = args[2] as List<Customer>
            val profile = args[3] as? StoreProfile
            @Suppress("UNCHECKED_CAST")
            val heldCartsList = args[4] as List<HeldCart>
            val cat = args[5] as String
            val query = args[6] as String
            @Suppress("UNCHECKED_CAST")
            val cart = args[7] as List<CartItem>
            val cust = args[8] as? Customer
            val discount = args[9] as Double
            val completedOrder = args[10] as? TransactionOrder
            @Suppress("UNCHECKED_CAST")
            val completedItems = args[11] as List<TransactionItem>
            val checkingOut = args[12] as Boolean
            val cameraScanning = args[13] as Boolean

            val filteredProducts = allProds.filter { prod ->
                val matchesCategory = (cat == "Semua" || prod.category.equals(cat, ignoreCase = true))
                val matchesQuery = query.isBlank() ||
                        prod.name.contains(query, ignoreCase = true) ||
                        prod.barcode.contains(query, ignoreCase = true) ||
                        prod.sku.contains(query, ignoreCase = true)
                matchesCategory && matchesQuery
            }

            PosUiState(
                products = filteredProducts,
                allRawProducts = allProds,
                categories = allCats,
                customers = allCusts,
                storeProfile = profile,
                heldCarts = heldCartsList,
                selectedCategory = cat,
                searchQuery = query,
                cartItems = cart,
                selectedCustomer = cust,
                totalDiscount = discount,
                lastCompletedOrder = completedOrder,
                lastCompletedItems = completedItems,
                isCheckingOut = checkingOut,
                isCameraScanning = cameraScanning
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PosUiState()
        )
    }

    fun setCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openCameraScanner() {
        _isCameraScanning.value = true
    }

    fun closeCameraScanner() {
        _isCameraScanning.value = false
    }

    fun scanBarcode(barcode: String) {
        viewModelScope.launch {
            val cleanBarcode = barcode.trim()

            // Check if scanned barcode is a customer ID Card
            val customerMatched = uiState.value.customers.firstOrNull {
                "CUST-${it.id}".equals(cleanBarcode, ignoreCase = true) ||
                it.phone.equals(cleanBarcode, ignoreCase = true) ||
                it.name.equals(cleanBarcode, ignoreCase = true)
            }
            if (customerMatched != null) {
                setSelectedCustomer(customerMatched)
                _message.value = "Pelanggan Terpilih: ${customerMatched.name}"
                closeCameraScanner()
                return@launch
            }

            val product = repository.getProductByBarcode(cleanBarcode)
            if (product != null) {
                addToCart(product, 1.0)
                _message.value = "Ditambahkan: ${product.name}"
                closeCameraScanner()
            } else {
                // Try searching by name/sku match if not barcode
                val matched = uiState.value.allRawProducts.firstOrNull {
                    it.barcode.equals(cleanBarcode, ignoreCase = true) ||
                    it.sku.equals(cleanBarcode, ignoreCase = true) ||
                    it.name.contains(cleanBarcode, ignoreCase = true)
                }
                if (matched != null) {
                    addToCart(matched, 1.0)
                    _message.value = "Ditambahkan: ${matched.name}"
                    closeCameraScanner()
                } else {
                    _message.value = "Barcode '$cleanBarcode' tidak ditemukan!"
                }
            }
        }
    }

    fun addToCart(product: Product, quantity: Double = 1.0) {
        if (product.stock <= 0) {
            _message.value = "Stok ${product.name} telah habis!"
            return
        }

        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == product.id }

        if (index != -1) {
            val existing = currentList[index]
            val newQty = existing.quantity + quantity
            if (newQty > product.stock) {
                _message.value = "Hanya tersisa ${com.example.ui.components.CurrencyUtils.formatQty(product.stock)} ${product.unit} di stok"
                return
            }
            currentList[index] = existing.copy(quantity = newQty)
        } else {
            currentList.add(CartItem(product = product, quantity = quantity.coerceAtMost(product.stock)))
        }
        _cartItems.value = currentList
    }

    fun updateCartQuantity(productId: Long, newQuantity: Double) {
        if (newQuantity <= 0) {
            removeFromCart(productId)
            return
        }
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index != -1) {
            val item = currentList[index]
            if (newQuantity > item.product.stock) {
                _message.value = "Maksimal stok tersedia: ${com.example.ui.components.CurrencyUtils.formatQty(item.product.stock)} ${item.product.unit}"
                return
            }
            currentList[index] = item.copy(quantity = newQuantity)
            _cartItems.value = currentList
        }
    }

    fun updateCartUnit(productId: Long, unitName: String, conversionFactor: Double) {
        val currentList = _cartItems.value.toMutableList()
        val index = currentList.indexOfFirst { it.product.id == productId }
        if (index != -1) {
            val item = currentList[index]
            currentList[index] = item.copy(
                selectedUnit = unitName,
                conversionFactor = conversionFactor
            )
            _cartItems.value = currentList
        }
    }

    fun removeFromCart(productId: Long) {
        _cartItems.update { list -> list.filterNot { it.product.id == productId } }
    }

    fun clearCart() {
        _cartItems.value = emptyList()
        _totalDiscount.value = 0.0
        _selectedCustomer.value = null
    }

    fun setSelectedCustomer(customer: Customer?) {
        _selectedCustomer.value = customer
    }

    fun setTotalDiscount(discount: Double) {
        _totalDiscount.value = discount.coerceAtLeast(0.0)
    }

    // --- HOLD & PARK CART FEATURE ---
    fun holdCurrentCart(note: String = "Antrean Pembeli") {
        val currentCart = _cartItems.value
        if (currentCart.isEmpty()) {
            _message.value = "Keranjang masih kosong"
            return
        }

        viewModelScope.launch {
            try {
                val jsonArray = JSONArray()
                for (item in currentCart) {
                    val obj = JSONObject().apply {
                        put("productId", item.product.id)
                        put("quantity", item.quantity)
                        put("selectedUnit", item.selectedUnit)
                        put("conversionFactor", item.conversionFactor)
                        put("discount", item.discount)
                    }
                    jsonArray.put(obj)
                }

                val held = HeldCart(
                    note = note.ifBlank { "Antrean #${uiState.value.heldCarts.size + 1}" },
                    customerName = _selectedCustomer.value?.name ?: "Pelanggan Umum",
                    totalAmount = uiState.value.grandTotal,
                    itemsCount = _cartItems.value.size,
                    itemsJson = jsonArray.toString(),
                    timestamp = System.currentTimeMillis()
                )

                repository.saveHeldCart(held)
                clearCart()
                _message.value = "Keranjang berhasil ditahan (Park Cart)"
            } catch (e: Exception) {
                _message.value = "Gagal menahan keranjang: ${e.message}"
            }
        }
    }

    fun resumeHeldCart(held: HeldCart) {
        viewModelScope.launch {
            try {
                val allProds = repository.allProducts.first()
                val jsonArray = JSONArray(held.itemsJson)
                val restoredItems = mutableListOf<CartItem>()

                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    val prodId = obj.getLong("productId")
                    val qty = obj.optDouble("quantity", 1.0)
                    val unit = obj.optString("selectedUnit", "Pcs")
                    val conv = obj.optDouble("conversionFactor", 1.0)
                    val disc = obj.optDouble("discount", 0.0)

                    val prod = allProds.find { it.id == prodId }
                    if (prod != null) {
                        restoredItems.add(
                            CartItem(
                                product = prod,
                                quantity = qty,
                                selectedUnit = unit,
                                conversionFactor = conv,
                                discount = disc
                            )
                        )
                    }
                }

                _cartItems.value = restoredItems
                repository.deleteHeldCartById(held.id)
                _message.value = "Keranjang '${held.note}' dipanggil kembali"
            } catch (e: Exception) {
                _message.value = "Gagal memanggil keranjang: ${e.message}"
            }
        }
    }

    fun deleteHeldCart(held: HeldCart) {
        viewModelScope.launch {
            repository.deleteHeldCart(held)
            _message.value = "Keranjang tertahan dihapus"
        }
    }

    fun generateDynamicQrisPayload(): Pair<String, String>? {
        val profile = uiState.value.storeProfile
        val staticPayload = profile?.qrisStaticPayload ?: ""
        if (staticPayload.isBlank()) return null

        return try {
            val amount = uiState.value.grandTotal
            val dynamicPayload = QrisEngine.convertStaticQrisToDynamic(staticPayload, amount)
            val merchantName = QrisEngine.getMerchantNameFromPayload(staticPayload)
            Pair(dynamicPayload, merchantName)
        } catch (e: Exception) {
            null
        }
    }

    fun processPayment(
        paymentMethod: PaymentMethod,
        paidAmount: Double,
        notes: String = ""
    ) {
        val state = uiState.value
        if (state.cartItems.isEmpty()) return

        val change = if (paymentMethod == PaymentMethod.CASH) {
            (paidAmount - state.grandTotal).coerceAtLeast(0.0)
        } else {
            0.0
        }

        viewModelScope.launch {
            _isCheckingOut.value = true
            try {
                val order = repository.processCheckout(
                    cartItems = state.cartItems,
                    customer = state.selectedCustomer,
                    subtotal = state.subtotal,
                    discountAmount = state.totalDiscount,
                    taxAmount = state.taxAmount,
                    grandTotal = state.grandTotal,
                    paidAmount = paidAmount,
                    changeAmount = change,
                    paymentMethod = paymentMethod,
                    notes = notes
                )

                val items = repository.getItemsForOrderSync(order.id)
                _lastCompletedOrder.value = order
                _lastCompletedItems.value = items
                clearCart()
            } catch (e: Exception) {
                _message.value = "Gagal memproses transaksi: ${e.localizedMessage}"
            } finally {
                _isCheckingOut.value = false
            }
        }
    }

    fun dismissReceipt() {
        _lastCompletedOrder.value = null
        _lastCompletedItems.value = emptyList()
    }

    fun clearMessage() {
        _message.value = null
    }
}
