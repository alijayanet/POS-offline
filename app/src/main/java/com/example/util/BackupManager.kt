package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.room.withTransaction
import com.example.data.local.AppDatabase
import com.example.data.model.Category
import com.example.data.model.Customer
import com.example.data.model.DebtRecord
import com.example.data.model.Product
import com.example.data.model.Purchase
import com.example.data.model.StoreProfile
import com.example.data.model.Supplier
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionOrder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class BackupSummary(
    val appName: String = "TokoPOS",
    val backupDate: Long = System.currentTimeMillis(),
    val totalProducts: Int = 0,
    val totalCategories: Int = 0,
    val totalCustomers: Int = 0,
    val totalOrders: Int = 0,
    val totalItems: Int = 0,
    val totalDebts: Int = 0,
    val totalSuppliers: Int = 0,
    val totalPurchases: Int = 0,
    val storeName: String = ""
)

object BackupManager {

    suspend fun createBackupJson(database: AppDatabase): String = withContext(Dispatchers.IO) {
        val root = JSONObject()

        root.put("appName", "TokoPOS")
        root.put("version", 1)
        root.put("backupDate", System.currentTimeMillis())

        // 1. Store Profile
        val profile = database.storeProfileDao().getStoreProfileSync()
        if (profile != null) {
            val pObj = JSONObject().apply {
                put("id", profile.id)
                put("storeName", profile.storeName)
                put("ownerName", profile.ownerName)
                put("phone", profile.phone)
                put("address", profile.address)
                put("receiptFooter", profile.receiptFooter)
                put("currencySymbol", profile.currencySymbol)
                put("taxRatePercent", profile.taxRatePercent)
                put("isTaxEnabled", profile.isTaxEnabled)
                put("printerType", profile.printerType)
                put("bluetoothPrinterMac", profile.bluetoothPrinterMac)
                put("paperSize", profile.paperSize)
                put("qrisStaticPayload", profile.qrisStaticPayload)
                put("bankAccountsInfo", profile.bankAccountsInfo)
            }
            root.put("storeProfile", pObj)
        }

        // 2. Categories
        val categories = database.categoryDao().getAllCategoriesSync()
        val catArray = JSONArray()
        for (c in categories) {
            catArray.put(JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("iconName", c.iconName)
                put("colorHex", c.colorHex)
            })
        }
        root.put("categories", catArray)

        // 3. Products
        val products = database.productDao().getAllProductsSync()
        val prodArray = JSONArray()
        for (p in products) {
            prodArray.put(JSONObject().apply {
                put("id", p.id)
                put("barcode", p.barcode)
                put("name", p.name)
                put("category", p.category)
                put("buyPrice", p.buyPrice)
                put("sellPrice", p.sellPrice)
                put("stock", p.stock)
                put("unit", p.unit)
                put("minStockAlert", p.minStockAlert)
                put("wholesalePrice", p.wholesalePrice)
                put("wholesaleMinQty", p.wholesaleMinQty)
                put("unitsJson", p.unitsJson)
                put("description", p.description)
                put("supplier", p.supplier)
                put("isActive", p.isActive)
                put("createdAt", p.createdAt)
                put("updatedAt", p.updatedAt)
            })
        }
        root.put("products", prodArray)

        // 4. Customers
        val customers = database.customerDao().getAllCustomersSync()
        val custArray = JSONArray()
        for (c in customers) {
            custArray.put(JSONObject().apply {
                put("id", c.id)
                put("name", c.name)
                put("phone", c.phone)
                put("address", c.address)
                put("totalDebt", c.totalDebt)
                put("notes", c.notes)
                put("createdAt", c.createdAt)
            })
        }
        root.put("customers", custArray)

        // 5. Orders & Items
        val orders = database.transactionDao().getAllOrdersSync()
        val orderArray = JSONArray()
        for (o in orders) {
            orderArray.put(JSONObject().apply {
                put("id", o.id)
                put("invoiceNumber", o.invoiceNumber)
                put("customerId", o.customerId ?: JSONObject.NULL)
                put("customerName", o.customerName)
                put("subtotal", o.subtotal)
                put("discountAmount", o.discountAmount)
                put("taxAmount", o.taxAmount)
                put("grandTotal", o.grandTotal)
                put("paidAmount", o.paidAmount)
                put("changeAmount", o.changeAmount)
                put("paymentMethod", o.paymentMethod)
                put("totalProfit", o.totalProfit)
                put("notes", o.notes)
                put("timestamp", o.timestamp)
            })
        }
        root.put("orders", orderArray)

        val items = database.transactionDao().getAllItemsSync()
        val itemArray = JSONArray()
        for (item in items) {
            itemArray.put(JSONObject().apply {
                put("id", item.id)
                put("orderId", item.orderId)
                put("productId", item.productId)
                put("productName", item.productName)
                put("category", item.category)
                put("buyPrice", item.buyPrice)
                put("sellPrice", item.sellPrice)
                put("quantity", item.quantity)
                put("unit", item.unit)
                put("discount", item.discount)
                put("subtotal", item.subtotal)
                put("profit", item.profit)
            })
        }
        root.put("items", itemArray)

        // 6. Debts
        val debts = database.debtDao().getAllDebtsSync()
        val debtArray = JSONArray()
        for (d in debts) {
            debtArray.put(JSONObject().apply {
                put("id", d.id)
                put("customerId", d.customerId)
                put("customerName", d.customerName)
                put("invoiceNumber", d.invoiceNumber)
                put("totalDebt", d.totalDebt)
                put("amountPaid", d.amountPaid)
                put("remainingDebt", d.remainingDebt)
                put("isSettled", d.isSettled)
                put("notes", d.notes)
                put("createdAt", d.createdAt)
                put("settledAt", d.settledAt ?: JSONObject.NULL)
            })
        }
        root.put("debts", debtArray)

        // 7. Suppliers
        val suppliers = database.supplierDao().getAllSuppliersSync()
        val supArray = JSONArray()
        for (s in suppliers) {
            supArray.put(JSONObject().apply {
                put("id", s.id)
                put("name", s.name)
                put("phone", s.phone)
                put("address", s.address)
                put("notes", s.notes)
                put("totalPayable", s.totalPayable)
            })
        }
        root.put("suppliers", supArray)

        // 8. Purchases
        val purchases = database.purchaseDao().getAllPurchasesSync()
        val purArray = JSONArray()
        for (p in purchases) {
            purArray.put(JSONObject().apply {
                put("id", p.id)
                put("supplierId", p.supplierId ?: JSONObject.NULL)
                put("supplierName", p.supplierName)
                put("invoiceNumber", p.invoiceNumber)
                put("totalAmount", p.totalAmount)
                put("paidAmount", p.paidAmount)
                put("debtBalance", p.debtBalance)
                put("paymentType", p.paymentType)
                put("paymentStatus", p.paymentStatus)
                put("dueDate", p.dueDate ?: JSONObject.NULL)
                put("notes", p.notes)
                put("timestamp", p.timestamp)
            })
        }
        root.put("purchases", purArray)

        // 9. Expenses
        val expenses = database.expenseDao().getAllExpensesSync()
        val expArray = JSONArray()
        for (e in expenses) {
            expArray.put(JSONObject().apply {
                put("id", e.id)
                put("category", e.category)
                put("amount", e.amount)
                put("notes", e.notes)
                put("timestamp", e.timestamp)
            })
        }
        root.put("expenses", expArray)

        // 10. Subscription Packages
        val subPackages = database.subscriptionDao().getAllPackagesSync()
        val subPkgArray = JSONArray()
        for (pkg in subPackages) {
            subPkgArray.put(JSONObject().apply {
                put("id", pkg.id)
                put("name", pkg.name)
                put("price", pkg.price)
                put("speedInfo", pkg.speedInfo)
                put("description", pkg.description)
                put("isActive", pkg.isActive)
                put("createdAt", pkg.createdAt)
            })
        }
        root.put("subscriptionPackages", subPkgArray)

        // 11. Subscription Customers
        val subCustomers = database.subscriptionDao().getAllSubscriptionCustomersSync()
        val subCustArray = JSONArray()
        for (c in subCustomers) {
            subCustArray.put(JSONObject().apply {
                put("id", c.id)
                put("customerCode", c.customerCode)
                put("name", c.name)
                put("phone", c.phone)
                put("address", c.address)
                put("packageId", c.packageId)
                put("packageName", c.packageName)
                put("monthlyPrice", c.monthlyPrice)
                put("installationDate", c.installationDate)
                put("dueDay", c.dueDay)
                put("paidUntilTimestamp", c.paidUntilTimestamp)
                put("status", c.status)
                put("notes", c.notes)
                put("createdAt", c.createdAt)
                put("updatedAt", c.updatedAt)
            })
        }
        root.put("subscriptionCustomers", subCustArray)

        // 12. Subscription Payments
        val subPayments = database.subscriptionDao().getAllSubscriptionPaymentsSync()
        val subPayArray = JSONArray()
        for (pay in subPayments) {
            subPayArray.put(JSONObject().apply {
                put("id", pay.id)
                put("customerId", pay.customerId)
                put("customerCode", pay.customerCode)
                put("customerName", pay.customerName)
                put("invoiceNumber", pay.invoiceNumber)
                put("packageName", pay.packageName)
                put("monthlyPrice", pay.monthlyPrice)
                put("monthsCount", pay.monthsCount)
                put("periodStartTimestamp", pay.periodStartTimestamp)
                put("periodEndTimestamp", pay.periodEndTimestamp)
                put("totalAmount", pay.totalAmount)
                put("paymentMethod", pay.paymentMethod)
                put("timestamp", pay.timestamp)
                put("notes", pay.notes)
            })
        }
        root.put("subscriptionPayments", subPayArray)

        root.toString(2)
    }

    fun parseBackupSummary(jsonString: String): BackupSummary? {
        return try {
            val root = JSONObject(jsonString)
            val profileObj = root.optJSONObject("storeProfile")
            val storeName = profileObj?.optString("storeName", "Toko Ali Jaya") ?: "Toko"
            val backupDate = root.optLong("backupDate", System.currentTimeMillis())

            BackupSummary(
                appName = root.optString("appName", "TokoPOS"),
                backupDate = backupDate,
                totalProducts = root.optJSONArray("products")?.length() ?: 0,
                totalCategories = root.optJSONArray("categories")?.length() ?: 0,
                totalCustomers = root.optJSONArray("customers")?.length() ?: 0,
                totalOrders = root.optJSONArray("orders")?.length() ?: 0,
                totalItems = root.optJSONArray("items")?.length() ?: 0,
                totalDebts = root.optJSONArray("debts")?.length() ?: 0,
                totalSuppliers = root.optJSONArray("suppliers")?.length() ?: 0,
                totalPurchases = root.optJSONArray("purchases")?.length() ?: 0,
                storeName = storeName
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun restoreFromJson(
        database: AppDatabase,
        jsonString: String,
        replaceExisting: Boolean
    ): Result<BackupSummary> = withContext(Dispatchers.IO) {
        try {
            val root = JSONObject(jsonString)
            val summary = parseBackupSummary(jsonString) ?: return@withContext Result.failure(Exception("Format file backup tidak valid!"))

            database.withTransaction {
                if (replaceExisting) {
                    database.productDao().clearAllProducts()
                    database.categoryDao().clearAllCategories()
                    database.customerDao().clearAllCustomers()
                    database.transactionDao().clearAllOrders()
                    database.transactionDao().clearAllItems()
                    database.debtDao().clearAllDebts()
                    database.supplierDao().clearAllSuppliers()
                    database.purchaseDao().clearAllPurchases()
                    database.expenseDao().clearAllExpenses()
                    database.subscriptionDao().clearAllPackages()
                    database.subscriptionDao().clearAllSubscriptionCustomers()
                    database.subscriptionDao().clearAllSubscriptionPayments()
                }

                // 1. Store Profile
                val pObj = root.optJSONObject("storeProfile")
                if (pObj != null) {
                    val profile = StoreProfile(
                        id = 1,
                        storeName = pObj.optString("storeName", "Toko Ali Jaya"),
                        ownerName = pObj.optString("ownerName", "Ali Jaya"),
                        phone = pObj.optString("phone", ""),
                        address = pObj.optString("address", ""),
                        receiptFooter = pObj.optString("receiptFooter", "Terima kasih atas kunjungan Anda!"),
                        currencySymbol = pObj.optString("currencySymbol", "Rp"),
                        taxRatePercent = pObj.optDouble("taxRatePercent", 0.0),
                        isTaxEnabled = pObj.optBoolean("isTaxEnabled", false),
                        printerType = pObj.optString("printerType", "BLUETOOTH"),
                        bluetoothPrinterMac = pObj.optString("bluetoothPrinterMac", ""),
                        paperSize = pObj.optString("paperSize", "58mm"),
                        qrisStaticPayload = pObj.optString("qrisStaticPayload", ""),
                        bankAccountsInfo = pObj.optString("bankAccountsInfo", "🏧 BCA : 8820 1928 334 (a.n Ali Jaya)\n🏧 BRI : 0123 0100 4567 508 (a.n Ali Jaya)\n🏧 Dana/OVO/ShopeePay : 0812 3456 7890\n📱 Kasir Toko : Tersedia Tunai / QRIS di toko")
                    )
                    database.storeProfileDao().insertOrUpdate(profile)
                }

                // 2. Categories
                val catArray = root.optJSONArray("categories")
                if (catArray != null) {
                    val catList = mutableListOf<Category>()
                    for (i in 0 until catArray.length()) {
                        val c = catArray.getJSONObject(i)
                        catList.add(
                            Category(
                                id = if (replaceExisting) c.optLong("id", 0) else 0,
                                name = c.getString("name"),
                                iconName = c.optString("iconName", "Category"),
                                colorHex = c.optString("colorHex", "#0D9488")
                            )
                        )
                    }
                    if (catList.isNotEmpty()) {
                        database.categoryDao().insertCategories(catList)
                    }
                }

                // 3. Products
                val prodArray = root.optJSONArray("products")
                if (prodArray != null) {
                    val prodList = mutableListOf<Product>()
                    for (i in 0 until prodArray.length()) {
                        val p = prodArray.getJSONObject(i)
                        prodList.add(
                            Product(
                                id = if (replaceExisting) p.optLong("id", 0) else 0,
                                barcode = p.optString("barcode", ""),
                                name = p.getString("name"),
                                category = p.optString("category", "Umum"),
                                buyPrice = p.optDouble("buyPrice", 0.0),
                                sellPrice = p.optDouble("sellPrice", 0.0),
                                stock = p.optDouble("stock", 0.0),
                                unit = p.optString("unit", "Pcs"),
                                minStockAlert = p.optDouble("minStockAlert", 5.0),
                                wholesalePrice = p.optDouble("wholesalePrice", 0.0),
                                wholesaleMinQty = p.optDouble("wholesaleMinQty", 0.0),
                                unitsJson = p.optString("unitsJson", ""),
                                description = p.optString("description", ""),
                                supplier = p.optString("supplier", ""),
                                isActive = p.optBoolean("isActive", true),
                                createdAt = p.optLong("createdAt", System.currentTimeMillis()),
                                updatedAt = p.optLong("updatedAt", System.currentTimeMillis())
                            )
                        )
                    }
                    if (prodList.isNotEmpty()) {
                        database.productDao().insertProducts(prodList)
                    }
                }

                // 4. Customers
                val custArray = root.optJSONArray("customers")
                if (custArray != null) {
                    val custList = mutableListOf<Customer>()
                    for (i in 0 until custArray.length()) {
                        val c = custArray.getJSONObject(i)
                        custList.add(
                            Customer(
                                id = if (replaceExisting) c.optLong("id", 0) else 0,
                                name = c.getString("name"),
                                phone = c.optString("phone", ""),
                                address = c.optString("address", ""),
                                totalDebt = c.optDouble("totalDebt", 0.0),
                                notes = c.optString("notes", ""),
                                createdAt = c.optLong("createdAt", System.currentTimeMillis())
                            )
                        )
                    }
                    if (custList.isNotEmpty()) {
                        database.customerDao().insertCustomers(custList)
                    }
                }

                // 5. Orders & Items
                val orderArray = root.optJSONArray("orders")
                if (orderArray != null) {
                    for (i in 0 until orderArray.length()) {
                        val o = orderArray.getJSONObject(i)
                        val order = TransactionOrder(
                            id = if (replaceExisting) o.optLong("id", 0) else 0,
                            invoiceNumber = o.optString("invoiceNumber", "INV-${System.currentTimeMillis()}"),
                            customerId = if (o.isNull("customerId")) null else o.optLong("customerId"),
                            customerName = o.optString("customerName", "Pelanggan Umum"),
                            subtotal = o.optDouble("subtotal", 0.0),
                            discountAmount = o.optDouble("discountAmount", 0.0),
                            taxAmount = o.optDouble("taxAmount", 0.0),
                            grandTotal = o.optDouble("grandTotal", 0.0),
                            paidAmount = o.optDouble("paidAmount", 0.0),
                            changeAmount = o.optDouble("changeAmount", 0.0),
                            paymentMethod = o.optString("paymentMethod", "CASH"),
                            totalProfit = o.optDouble("totalProfit", 0.0),
                            notes = o.optString("notes", ""),
                            timestamp = o.optLong("timestamp", System.currentTimeMillis())
                        )
                        database.transactionDao().insertOrder(order)
                    }
                }

                val itemArray = root.optJSONArray("items")
                if (itemArray != null) {
                    val itemList = mutableListOf<TransactionItem>()
                    for (i in 0 until itemArray.length()) {
                        val item = itemArray.getJSONObject(i)
                        itemList.add(
                            TransactionItem(
                                id = if (replaceExisting) item.optLong("id", 0) else 0,
                                orderId = item.optLong("orderId", 0),
                                productId = item.optLong("productId", 0),
                                productName = item.optString("productName", ""),
                                category = item.optString("category", "Umum"),
                                buyPrice = item.optDouble("buyPrice", 0.0),
                                sellPrice = item.optDouble("sellPrice", 0.0),
                                quantity = item.optDouble("quantity", 1.0),
                                unit = item.optString("unit", "Pcs"),
                                discount = item.optDouble("discount", 0.0),
                                subtotal = item.optDouble("subtotal", 0.0),
                                profit = item.optDouble("profit", 0.0)
                            )
                        )
                    }
                    if (itemList.isNotEmpty()) {
                        database.transactionDao().insertItems(itemList)
                    }
                }

                // 6. Debts
                val debtArray = root.optJSONArray("debts")
                if (debtArray != null) {
                    val debtList = mutableListOf<DebtRecord>()
                    for (i in 0 until debtArray.length()) {
                        val d = debtArray.getJSONObject(i)
                        debtList.add(
                            DebtRecord(
                                id = if (replaceExisting) d.optLong("id", 0) else 0,
                                customerId = d.optLong("customerId", 0),
                                customerName = d.optString("customerName", "Pelanggan"),
                                invoiceNumber = d.optString("invoiceNumber", ""),
                                totalDebt = d.optDouble("totalDebt", 0.0),
                                amountPaid = d.optDouble("amountPaid", 0.0),
                                remainingDebt = d.optDouble("remainingDebt", 0.0),
                                isSettled = d.optBoolean("isSettled", false),
                                notes = d.optString("notes", ""),
                                createdAt = d.optLong("createdAt", System.currentTimeMillis()),
                                settledAt = if (d.isNull("settledAt")) null else d.optLong("settledAt")
                            )
                        )
                    }
                    if (debtList.isNotEmpty()) {
                        database.debtDao().insertDebts(debtList)
                    }
                }

                // 7. Suppliers
                val supArray = root.optJSONArray("suppliers")
                if (supArray != null) {
                    val supList = mutableListOf<Supplier>()
                    for (i in 0 until supArray.length()) {
                        val s = supArray.getJSONObject(i)
                        supList.add(
                            Supplier(
                                id = if (replaceExisting) s.optLong("id", 0) else 0,
                                name = s.optString("name", ""),
                                phone = s.optString("phone", ""),
                                address = s.optString("address", ""),
                                notes = s.optString("notes", ""),
                                totalPayable = s.optDouble("totalPayable", 0.0)
                            )
                        )
                    }
                    if (supList.isNotEmpty()) {
                        database.supplierDao().insertSuppliers(supList)
                    }
                }

                // 8. Purchases
                val purArray = root.optJSONArray("purchases")
                if (purArray != null) {
                    val purList = mutableListOf<Purchase>()
                    for (i in 0 until purArray.length()) {
                        val p = purArray.getJSONObject(i)
                        purList.add(
                            Purchase(
                                id = if (replaceExisting) p.optLong("id", 0) else 0,
                                supplierId = if (p.isNull("supplierId")) null else p.optLong("supplierId"),
                                supplierName = p.optString("supplierName", "Supplier Umum"),
                                invoiceNumber = p.optString("invoiceNumber", ""),
                                totalAmount = p.optDouble("totalAmount", 0.0),
                                paidAmount = p.optDouble("paidAmount", 0.0),
                                debtBalance = p.optDouble("debtBalance", 0.0),
                                paymentType = p.optString("paymentType", "CASH"),
                                paymentStatus = p.optString("paymentStatus", "PAID"),
                                dueDate = if (p.isNull("dueDate")) null else p.optLong("dueDate"),
                                notes = p.optString("notes", ""),
                                timestamp = p.optLong("timestamp", System.currentTimeMillis())
                            )
                        )
                    }
                    if (purList.isNotEmpty()) {
                        database.purchaseDao().insertPurchases(purList)
                    }
                }

                // 9. Expenses
                val expArray = root.optJSONArray("expenses")
                if (expArray != null) {
                    val expList = mutableListOf<com.example.data.model.Expense>()
                    for (i in 0 until expArray.length()) {
                        val e = expArray.getJSONObject(i)
                        expList.add(
                            com.example.data.model.Expense(
                                id = if (replaceExisting) e.optLong("id", 0) else 0,
                                category = e.optString("category", "Operasional"),
                                amount = e.optDouble("amount", 0.0),
                                notes = e.optString("notes", ""),
                                timestamp = e.optLong("timestamp", System.currentTimeMillis())
                            )
                        )
                    }
                    if (expList.isNotEmpty()) {
                        database.expenseDao().insertExpenses(expList)
                    }
                }

                // 10. Subscription Packages
                val subPkgArray = root.optJSONArray("subscriptionPackages")
                if (subPkgArray != null) {
                    val pkgList = mutableListOf<com.example.data.model.SubscriptionPackage>()
                    for (i in 0 until subPkgArray.length()) {
                        val p = subPkgArray.getJSONObject(i)
                        pkgList.add(
                            com.example.data.model.SubscriptionPackage(
                                id = if (replaceExisting) p.optLong("id", 0) else 0,
                                name = p.optString("name", ""),
                                price = p.optDouble("price", 0.0),
                                speedInfo = p.optString("speedInfo", ""),
                                description = p.optString("description", ""),
                                isActive = p.optBoolean("isActive", true),
                                createdAt = p.optLong("createdAt", System.currentTimeMillis())
                            )
                        )
                    }
                    if (pkgList.isNotEmpty()) {
                        database.subscriptionDao().insertPackages(pkgList)
                    }
                }

                // 11. Subscription Customers
                val subCustArray = root.optJSONArray("subscriptionCustomers")
                if (subCustArray != null) {
                    val custList = mutableListOf<com.example.data.model.SubscriptionCustomer>()
                    for (i in 0 until subCustArray.length()) {
                        val c = subCustArray.getJSONObject(i)
                        custList.add(
                            com.example.data.model.SubscriptionCustomer(
                                id = if (replaceExisting) c.optLong("id", 0) else 0,
                                customerCode = c.optString("customerCode", ""),
                                name = c.optString("name", ""),
                                phone = c.optString("phone", ""),
                                address = c.optString("address", ""),
                                packageId = c.optLong("packageId", 0),
                                packageName = c.optString("packageName", ""),
                                monthlyPrice = c.optDouble("monthlyPrice", 0.0),
                                installationDate = c.optLong("installationDate", System.currentTimeMillis()),
                                dueDay = c.optInt("dueDay", 10),
                                paidUntilTimestamp = c.optLong("paidUntilTimestamp", System.currentTimeMillis()),
                                status = c.optString("status", "ACTIVE"),
                                notes = c.optString("notes", ""),
                                createdAt = c.optLong("createdAt", System.currentTimeMillis()),
                                updatedAt = c.optLong("updatedAt", System.currentTimeMillis())
                            )
                        )
                    }
                    if (custList.isNotEmpty()) {
                        database.subscriptionDao().insertSubscriptionCustomers(custList)
                    }
                }

                // 12. Subscription Payments
                val subPayArray = root.optJSONArray("subscriptionPayments")
                if (subPayArray != null) {
                    val payList = mutableListOf<com.example.data.model.SubscriptionPayment>()
                    for (i in 0 until subPayArray.length()) {
                        val py = subPayArray.getJSONObject(i)
                        payList.add(
                            com.example.data.model.SubscriptionPayment(
                                id = if (replaceExisting) py.optLong("id", 0) else 0,
                                customerId = py.optLong("customerId", 0),
                                customerCode = py.optString("customerCode", ""),
                                customerName = py.optString("customerName", ""),
                                invoiceNumber = py.optString("invoiceNumber", ""),
                                packageName = py.optString("packageName", ""),
                                monthlyPrice = py.optDouble("monthlyPrice", 0.0),
                                monthsCount = py.optInt("monthsCount", 1),
                                periodStartTimestamp = py.optLong("periodStartTimestamp", System.currentTimeMillis()),
                                periodEndTimestamp = py.optLong("periodEndTimestamp", System.currentTimeMillis()),
                                totalAmount = py.optDouble("totalAmount", 0.0),
                                paymentMethod = py.optString("paymentMethod", "CASH"),
                                timestamp = py.optLong("timestamp", System.currentTimeMillis()),
                                notes = py.optString("notes", "")
                            )
                        )
                    }
                    if (payList.isNotEmpty()) {
                        database.subscriptionDao().insertSubscriptionPayments(payList)
                    }
                }
            }

            Result.success(summary)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun writeJsonToUri(context: Context, uri: Uri, jsonString: String): Result<Unit> {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { os ->
                OutputStreamWriter(os, Charsets.UTF_8).use { writer ->
                    writer.write(jsonString)
                    writer.flush()
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun readJsonFromUri(context: Context, uri: Uri): Result<String> {
        return try {
            val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            } ?: return Result.failure(Exception("Gagal membuka file backup"))
            Result.success(content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun shareBackupJson(context: Context, jsonString: String) {
        try {
            val sdf = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val filename = "TokoPOS_Backup_${sdf.format(Date())}.json"
            val file = File(context.cacheDir, filename)
            FileOutputStream(file).use { fos ->
                fos.write(jsonString.toByteArray(Charsets.UTF_8))
                fos.flush()
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Backup Data TokoPOS")
                putExtra(Intent.EXTRA_TEXT, "File backup database TokoPOS ($filename). Simpan file ini dengan aman.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Kirim / Simpan File Backup").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
