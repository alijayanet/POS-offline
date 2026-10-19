package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.BestSellingProduct
import com.example.data.model.CartItem
import com.example.data.model.Category
import com.example.data.model.Customer
import com.example.data.model.DebtRecord
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.data.model.StoreProfile
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StoreRepository(private val db: AppDatabase) {

    // --- Product operations ---
    val allProducts: Flow<List<Product>> = db.productDao().getAllProducts()
    val lowStockProducts: Flow<List<Product>> = db.productDao().getLowStockProducts()
    val lowStockCount: Flow<Int> = db.productDao().getLowStockCount()
    val totalProductCount: Flow<Int> = db.productDao().getTotalProductCount()
    val totalInventoryCostValue: Flow<Double?> = db.productDao().getTotalInventoryCostValue()
    val totalInventoryRetailValue: Flow<Double?> = db.productDao().getTotalInventoryRetailValue()

    fun searchProducts(query: String): Flow<List<Product>> = db.productDao().searchProducts(query)
    fun getProductsByCategory(category: String): Flow<List<Product>> = db.productDao().getProductsByCategory(category)

    suspend fun getProductByBarcode(barcode: String): Product? = withContext(Dispatchers.IO) {
        db.productDao().getProductByBarcode(barcode)
    }

    suspend fun saveProduct(product: Product): Long = withContext(Dispatchers.IO) {
        if (product.id == 0L) {
            db.productDao().insertProduct(product)
        } else {
            db.productDao().updateProduct(product.copy(updatedAt = System.currentTimeMillis()))
            product.id
        }
    }

    suspend fun adjustStock(productId: Long, amountToAdd: Double) = withContext(Dispatchers.IO) {
        if (amountToAdd >= 0) {
            db.productDao().addStock(productId, amountToAdd)
        } else {
            db.productDao().reduceStock(productId, -amountToAdd)
        }
    }

    suspend fun deleteProduct(productId: Long) = withContext(Dispatchers.IO) {
        db.productDao().softDeleteProduct(productId)
    }

    // --- Category operations ---
    val allCategories: Flow<List<Category>> = db.categoryDao().getAllCategories()

    suspend fun saveCategory(category: Category): Long = withContext(Dispatchers.IO) {
        if (category.id == 0L) {
            db.categoryDao().insertCategory(category)
        } else {
            db.categoryDao().updateCategory(category)
            category.id
        }
    }

    suspend fun deleteCategory(category: Category) = withContext(Dispatchers.IO) {
        db.categoryDao().deleteCategory(category)
    }

    // --- Customer & Debt operations ---
    val allCustomers: Flow<List<Customer>> = db.customerDao().getAllCustomers()
    val customersWithDebt: Flow<List<Customer>> = db.customerDao().getCustomersWithDebt()
    val totalCustomerDebt: Flow<Double?> = db.customerDao().getTotalCustomerDebt()
    val allDebts: Flow<List<DebtRecord>> = db.debtDao().getAllDebts()
    val unsettledDebts: Flow<List<DebtRecord>> = db.debtDao().getUnsettledDebts()

    fun searchCustomers(query: String): Flow<List<Customer>> = db.customerDao().searchCustomers(query)

    suspend fun saveCustomer(customer: Customer): Long = withContext(Dispatchers.IO) {
        if (customer.id == 0L) {
            db.customerDao().insertCustomer(customer)
        } else {
            db.customerDao().updateCustomer(customer)
            customer.id
        }
    }

    suspend fun deleteCustomer(customer: Customer) = withContext(Dispatchers.IO) {
        db.customerDao().deleteCustomer(customer)
    }

    suspend fun payDebt(debtId: Long, customerId: Long, paymentAmount: Double) = withContext(Dispatchers.IO) {
        db.debtDao().payDebt(debtId, paymentAmount)
        db.customerDao().reduceDebt(customerId, paymentAmount)
    }

    // --- Transaction / POS operations ---
    val allTransactions: Flow<List<TransactionOrder>> = db.transactionDao().getAllOrders()
    val bestSellingProducts: Flow<List<BestSellingProduct>> = db.transactionDao().getBestSellingProducts(6)

    fun getTransactionsByDateRange(startTime: Long, endTime: Long): Flow<List<TransactionOrder>> =
        db.transactionDao().getOrdersByDateRange(startTime, endTime)

    fun getItemsForOrder(orderId: Long): Flow<List<TransactionItem>> =
        db.transactionDao().getItemsForOrder(orderId)

    suspend fun getItemsForOrderSync(orderId: Long): List<TransactionItem> = withContext(Dispatchers.IO) {
        db.transactionDao().getItemsForOrderSync(orderId)
    }

    fun getTodayStats(): Triple<Flow<Int>, Flow<Double?>, Flow<Double?>> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis
        val endOfDay = startOfDay + 86400000L - 1

        val countFlow = db.transactionDao().getOrderCount(startOfDay, endOfDay)
        val revenueFlow = db.transactionDao().getTotalRevenue(startOfDay, endOfDay)
        val profitFlow = db.transactionDao().getTotalProfit(startOfDay, endOfDay)
        return Triple(countFlow, revenueFlow, profitFlow)
    }

    fun getMonthlyStats(): Triple<Flow<Int>, Flow<Double?>, Flow<Double?>> {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfMonth = calendar.timeInMillis
        val endOfMonth = System.currentTimeMillis() + 86400000L

        val countFlow = db.transactionDao().getOrderCount(startOfMonth, endOfMonth)
        val revenueFlow = db.transactionDao().getTotalRevenue(startOfMonth, endOfMonth)
        val profitFlow = db.transactionDao().getTotalProfit(startOfMonth, endOfMonth)
        return Triple(countFlow, revenueFlow, profitFlow)
    }

    /**
     * Executes a complete POS Checkout:
     * 1. Generates invoice number
     * 2. Reduces stock of purchased products
     * 3. Inserts transaction order & items
     * 4. If payment is DEBT/KASBON, records customer debt
     */
    suspend fun processCheckout(
        cartItems: List<CartItem>,
        customer: Customer?,
        subtotal: Double,
        discountAmount: Double,
        taxAmount: Double,
        grandTotal: Double,
        paidAmount: Double,
        changeAmount: Double,
        paymentMethod: PaymentMethod,
        notes: String = ""
    ): TransactionOrder = withContext(Dispatchers.IO) {
        val invoiceNumber = generateInvoiceNumber()
        val totalProfit = cartItems.sumOf { it.totalProfit } - discountAmount

        val order = TransactionOrder(
            invoiceNumber = invoiceNumber,
            customerId = customer?.id,
            customerName = customer?.name ?: "Pelanggan Umum",
            subtotal = subtotal,
            discountAmount = discountAmount,
            taxAmount = taxAmount,
            grandTotal = grandTotal,
            paidAmount = paidAmount,
            changeAmount = changeAmount,
            paymentMethod = paymentMethod.name,
            totalProfit = if (totalProfit < 0) 0.0 else totalProfit,
            notes = notes,
            timestamp = System.currentTimeMillis()
        )

        val orderId = db.transactionDao().insertOrder(order)
        val orderWithId = order.copy(id = orderId)

        // Insert items and reduce stock
        val items = cartItems.map { cartItem ->
            // Reduce stock
            db.productDao().reduceStock(cartItem.product.id, cartItem.quantity)

            TransactionItem(
                orderId = orderId,
                productId = cartItem.product.id,
                productName = cartItem.product.name,
                category = cartItem.product.category,
                buyPrice = cartItem.product.buyPrice,
                sellPrice = cartItem.unitPrice,
                quantity = cartItem.quantity,
                unit = cartItem.product.unit,
                discount = cartItem.discount,
                subtotal = cartItem.subtotal,
                profit = cartItem.totalProfit
            )
        }
        db.transactionDao().insertItems(items)

        // If payment method is DEBT / KASBON
        if (paymentMethod == PaymentMethod.DEBT && customer != null) {
            val remaining = grandTotal - paidAmount
            if (remaining > 0) {
                db.customerDao().addDebt(customer.id, remaining)
                db.debtDao().insertDebt(
                    DebtRecord(
                        customerId = customer.id,
                        customerName = customer.name,
                        orderId = orderId,
                        invoiceNumber = invoiceNumber,
                        totalDebt = remaining,
                        amountPaid = 0.0,
                        remainingDebt = remaining,
                        isSettled = false,
                        notes = notes.ifBlank { "Kasbon belanja $invoiceNumber" },
                        createdAt = System.currentTimeMillis()
                    )
                )
            }
        }

        orderWithId
    }

    suspend fun saveManualInvoice(
        order: TransactionOrder,
        items: List<TransactionItem>
    ): Long = withContext(Dispatchers.IO) {
        val orderId = db.transactionDao().insertOrder(order)
        val itemsWithId = items.map { it.copy(orderId = orderId) }
        db.transactionDao().insertItems(itemsWithId)
        orderId
    }

    suspend fun cancelTransaction(order: TransactionOrder) = withContext(Dispatchers.IO) {
        // Restock products
        val items = db.transactionDao().getItemsForOrderSync(order.id)
        for (item in items) {
            db.productDao().addStock(item.productId, item.quantity)
        }

        // If this order had debt, reduce customer debt
        if (order.paymentMethod == PaymentMethod.DEBT.name && order.customerId != null) {
            val remaining = order.grandTotal - order.paidAmount
            if (remaining > 0) {
                db.customerDao().reduceDebt(order.customerId, remaining)
            }
        }

        db.transactionDao().deleteItemsForOrder(order.id)
        db.transactionDao().deleteOrder(order)
    }

    // --- Store Profile operations ---
    val storeProfile: Flow<StoreProfile?> = db.storeProfileDao().getStoreProfile()

    suspend fun saveStoreProfile(profile: StoreProfile) = withContext(Dispatchers.IO) {
        db.storeProfileDao().insertOrUpdate(profile)
    }

    suspend fun getStoreProfileSync(): StoreProfile? = withContext(Dispatchers.IO) {
        db.storeProfileDao().getStoreProfileSync()
    }

    // --- Held Carts (Park Cart) operations ---
    val allHeldCarts: Flow<List<com.example.data.model.HeldCart>> = db.heldCartDao().getAllHeldCarts()

    suspend fun saveHeldCart(heldCart: com.example.data.model.HeldCart): Long = withContext(Dispatchers.IO) {
        db.heldCartDao().insertHeldCart(heldCart)
    }

    suspend fun deleteHeldCart(heldCart: com.example.data.model.HeldCart) = withContext(Dispatchers.IO) {
        db.heldCartDao().deleteHeldCart(heldCart)
    }

    suspend fun deleteHeldCartById(id: Long) = withContext(Dispatchers.IO) {
        db.heldCartDao().deleteHeldCartById(id)
    }

    // --- Supplier operations ---
    val allSuppliers: Flow<List<com.example.data.model.Supplier>> = db.supplierDao().getAllSuppliers()

    suspend fun saveSupplier(supplier: com.example.data.model.Supplier): Long = withContext(Dispatchers.IO) {
        if (supplier.id == 0L) {
            db.supplierDao().insertSupplier(supplier)
        } else {
            db.supplierDao().updateSupplier(supplier)
            supplier.id
        }
    }

    suspend fun deleteSupplier(supplier: com.example.data.model.Supplier) = withContext(Dispatchers.IO) {
        db.supplierDao().deleteSupplier(supplier)
    }

    // --- Purchase operations ---
    val allPurchases: Flow<List<com.example.data.model.Purchase>> = db.purchaseDao().getAllPurchases()

    suspend fun savePurchase(purchase: com.example.data.model.Purchase): Long = withContext(Dispatchers.IO) {
        db.purchaseDao().insertPurchase(purchase)
    }

    // --- Expense operations ---
    val allExpenses: Flow<List<com.example.data.model.Expense>> = db.expenseDao().getAllExpenses()
    val totalExpenses: Flow<Double?> = db.expenseDao().getTotalExpenses()

    fun getExpensesByDateRange(startTime: Long, endTime: Long): Flow<List<com.example.data.model.Expense>> =
        db.expenseDao().getExpensesByDateRange(startTime, endTime)

    fun getTotalExpensesByDateRange(startTime: Long, endTime: Long): Flow<Double?> =
        db.expenseDao().getTotalExpensesByDateRange(startTime, endTime)

    suspend fun saveExpense(expense: com.example.data.model.Expense): Long = withContext(Dispatchers.IO) {
        if (expense.id == 0L) {
            db.expenseDao().insertExpense(expense)
        } else {
            db.expenseDao().updateExpense(expense)
            expense.id
        }
    }

    suspend fun deleteExpense(expense: com.example.data.model.Expense) = withContext(Dispatchers.IO) {
        db.expenseDao().deleteExpense(expense)
    }

    // --- Batch Products ---
    suspend fun importProducts(products: List<Product>) = withContext(Dispatchers.IO) {
        db.productDao().insertProducts(products)
    }

    // --- Subscription Packages ---
    val allSubscriptionPackages: Flow<List<com.example.data.model.SubscriptionPackage>> =
        db.subscriptionDao().getAllPackages()

    suspend fun saveSubscriptionPackage(pkg: com.example.data.model.SubscriptionPackage): Long = withContext(Dispatchers.IO) {
        if (pkg.id == 0L) {
            db.subscriptionDao().insertPackage(pkg)
        } else {
            db.subscriptionDao().updatePackage(pkg)
            pkg.id
        }
    }

    suspend fun deleteSubscriptionPackage(pkg: com.example.data.model.SubscriptionPackage) = withContext(Dispatchers.IO) {
        db.subscriptionDao().deletePackage(pkg)
    }

    // --- Subscription Customers ---
    val allSubscriptionCustomers: Flow<List<com.example.data.model.SubscriptionCustomer>> =
        db.subscriptionDao().getAllSubscriptionCustomers()

    fun searchSubscriptionCustomers(query: String): Flow<List<com.example.data.model.SubscriptionCustomer>> =
        db.subscriptionDao().searchSubscriptionCustomers(query)

    suspend fun saveSubscriptionCustomer(customer: com.example.data.model.SubscriptionCustomer): Long = withContext(Dispatchers.IO) {
        if (customer.id == 0L) {
            db.subscriptionDao().insertSubscriptionCustomer(customer)
        } else {
            db.subscriptionDao().updateSubscriptionCustomer(customer.copy(updatedAt = System.currentTimeMillis()))
            customer.id
        }
    }

    suspend fun deleteSubscriptionCustomer(customer: com.example.data.model.SubscriptionCustomer) = withContext(Dispatchers.IO) {
        db.subscriptionDao().deleteSubscriptionCustomer(customer)
    }

    suspend fun importCustomers(customers: List<com.example.data.model.Customer>) = withContext(Dispatchers.IO) {
        db.customerDao().insertCustomers(customers)
    }

    suspend fun importSubscriptionCustomers(customers: List<com.example.data.model.SubscriptionCustomer>) = withContext(Dispatchers.IO) {
        db.subscriptionDao().insertSubscriptionCustomers(customers)
    }

    // --- Subscription Payments ---
    val allSubscriptionPayments: Flow<List<com.example.data.model.SubscriptionPayment>> =
        db.subscriptionDao().getAllSubscriptionPayments()

    suspend fun recordSubscriptionPayment(
        payment: com.example.data.model.SubscriptionPayment,
        newPaidUntilTimestamp: Long
    ): Long = withContext(Dispatchers.IO) {
        val paymentId = db.subscriptionDao().insertSubscriptionPayment(payment)
        db.subscriptionDao().updateCustomerPaidUntil(
            customerId = payment.customerId,
            paidUntilTimestamp = newPaidUntilTimestamp,
            updatedAt = System.currentTimeMillis()
        )
        paymentId
    }

    // --- Collector Operations ---
    val allCollectors: Flow<List<com.example.data.model.Collector>> = db.collectorDao().getAllCollectors()
    val activeCollectors: Flow<List<com.example.data.model.Collector>> = db.collectorDao().getActiveCollectors()
    val allDepositLogs: Flow<List<com.example.data.model.CollectorDepositLog>> = db.collectorDao().getAllDepositLogs()

    suspend fun getCollectorById(id: String): com.example.data.model.Collector? = withContext(Dispatchers.IO) {
        db.collectorDao().getCollectorById(id)
    }

    suspend fun saveCollector(collector: com.example.data.model.Collector) = withContext(Dispatchers.IO) {
        db.collectorDao().insertCollector(collector)
    }

    suspend fun deleteCollector(collector: com.example.data.model.Collector) = withContext(Dispatchers.IO) {
        db.collectorDao().deleteCollector(collector)
    }

    suspend fun deleteCollectorById(id: String) = withContext(Dispatchers.IO) {
        db.collectorDao().deleteCollectorById(id)
    }

    suspend fun recordCollectorPayment(
        depositLog: com.example.data.model.CollectorDepositLog,
        payment: com.example.data.model.SubscriptionPayment,
        newPaidUntilTimestamp: Long
    ) = withContext(Dispatchers.IO) {
        db.collectorDao().insertDepositLog(depositLog)
        recordSubscriptionPayment(payment, newPaidUntilTimestamp)
    }

    fun getLogsByCollector(collectorId: String): Flow<List<com.example.data.model.CollectorDepositLog>> =
        db.collectorDao().getLogsByCollector(collectorId)

    fun getUndepositedLogsByCollector(collectorId: String): Flow<List<com.example.data.model.CollectorDepositLog>> =
        db.collectorDao().getUndepositedLogsByCollector(collectorId)

    fun getUndepositedTotalByCollector(collectorId: String): Flow<Double?> =
        db.collectorDao().getUndepositedTotalByCollector(collectorId)

    suspend fun markAllDepositedForCollector(collectorId: String) = withContext(Dispatchers.IO) {
        db.collectorDao().markAllDepositedForCollector(collectorId)
    }

    private fun generateInvoiceNumber(): String {
        val dateFormat = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault())
        return "INV-" + dateFormat.format(Date())
    }
}
