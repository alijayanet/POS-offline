package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.Category
import com.example.data.model.Customer
import com.example.data.model.DebtRecord
import com.example.data.model.Expense
import com.example.data.model.HeldCart
import com.example.data.model.Product
import com.example.data.model.Purchase
import com.example.data.model.StoreProfile
import com.example.data.model.SubscriptionCustomer
import com.example.data.model.SubscriptionPackage
import com.example.data.model.SubscriptionPayment
import com.example.data.model.Supplier
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionOrder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Product::class,
        Category::class,
        Customer::class,
        TransactionOrder::class,
        TransactionItem::class,
        DebtRecord::class,
        StoreProfile::class,
        HeldCart::class,
        Supplier::class,
        Purchase::class,
        Expense::class,
        SubscriptionPackage::class,
        SubscriptionCustomer::class,
        SubscriptionPayment::class,
        com.example.data.model.Collector::class,
        com.example.data.model.CollectorDepositLog::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun categoryDao(): CategoryDao
    abstract fun customerDao(): CustomerDao
    abstract fun transactionDao(): TransactionDao
    abstract fun debtDao(): DebtDao
    abstract fun storeProfileDao(): StoreProfileDao
    abstract fun heldCartDao(): HeldCartDao
    abstract fun supplierDao(): SupplierDao
    abstract fun purchaseDao(): PurchaseDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun subscriptionDao(): SubscriptionDao
    abstract fun collectorDao(): CollectorDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE products ADD COLUMN sku TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS collectors (
                        id TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        phone TEXT NOT NULL DEFAULT '',
                        assignedArea TEXT NOT NULL DEFAULT '',
                        isActive INTEGER NOT NULL DEFAULT 1,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS collector_deposit_logs (
                        id TEXT PRIMARY KEY NOT NULL,
                        collectorId TEXT NOT NULL,
                        collectorName TEXT NOT NULL,
                        customerId INTEGER NOT NULL,
                        customerCode TEXT NOT NULL DEFAULT '',
                        customerName TEXT NOT NULL,
                        amountPaid REAL NOT NULL,
                        paidAt INTEGER NOT NULL,
                        isDepositedToAdmin INTEGER NOT NULL DEFAULT 0,
                        depositedAt INTEGER
                    )
                """.trimIndent())
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "toko_pos_database.db"
                )
                .addMigrations(MIGRATION_6_7, MIGRATION_7_8)
                .fallbackToDestructiveMigration()
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Seed default data asynchronously
                        CoroutineScope(Dispatchers.IO).launch {
                            getInstance(context).seedInitialData()
                        }
                    }
                })
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    suspend fun seedInitialData() {
        // Seed default store profile
        storeProfileDao().insertOrUpdate(
            StoreProfile(
                id = 1,
                storeName = "Toko Ali Jaya",
                ownerName = "Ali Jaya",
                phone = "0812-3456-7890",
                address = "Jl. Raya Pasar Baru No. 12, Jakarta",
                receiptFooter = "Terima kasih atas kunjungan Anda!\nBarang yang sudah dibeli tidak dapat ditukar/dikembalikan.\nSemoga berkah!",
                currencySymbol = "Rp",
                taxRatePercent = 0.0,
                isTaxEnabled = false
            )
        )

        // Seed default categories
        val defaultCategories = listOf(
            Category(id = 1, name = "Sembako", iconName = "ShoppingBag", colorHex = "#059669"),
            Category(id = 2, name = "Makanan", iconName = "Fastfood", colorHex = "#D97706"),
            Category(id = 3, name = "Minuman", iconName = "LocalDrink", colorHex = "#2563EB"),
            Category(id = 4, name = "Kebersihan", iconName = "CleanHands", colorHex = "#0D9488"),
            Category(id = 5, name = "Rokok & Tembakau", iconName = "SmokingRooms", colorHex = "#7C3AED"),
            Category(id = 6, name = "Elektronik / Pulsa", iconName = "PhoneAndroid", colorHex = "#E11D48"),
            Category(id = 7, name = "Lainnya", iconName = "MoreHoriz", colorHex = "#64748B")
        )
        categoryDao().insertCategories(defaultCategories)

        // Seed default products
        val defaultProducts = listOf(
            Product(
                id = 1,
                barcode = "899277200001",
                name = "Minyak Goreng Bimoli 2L",
                category = "Sembako",
                buyPrice = 33000.0,
                sellPrice = 37000.0,
                stock = 24.0,
                unit = "Pcs",
                minStockAlert = 5.0,
                description = "Minyak goreng kelapa sawit kemasan pouch 2 liter",
                supplier = "Distributor Sembako Jaya"
            ),
            Product(
                id = 2,
                barcode = "899999900123",
                name = "Beras Rojolele Super 5Kg",
                category = "Sembako",
                buyPrice = 64000.0,
                sellPrice = 72000.0,
                stock = 15.0,
                unit = "Karung",
                minStockAlert = 3.0,
                description = "Beras pulen kualitas premium 5kg",
                supplier = "PB Beras Makmur"
            ),
            Product(
                id = 3,
                barcode = "899238811002",
                name = "Gula Pasir Gulaku 1Kg",
                category = "Sembako",
                buyPrice = 14500.0,
                sellPrice = 17500.0,
                stock = 30.0,
                unit = "Pcs",
                minStockAlert = 8.0,
                description = "Gula pasir putih murni 1 kg",
                supplier = "Distributor Sembako Jaya"
            ),
            Product(
                id = 4,
                barcode = "899886620021",
                name = "Indomie Goreng Spesial",
                category = "Makanan",
                buyPrice = 2800.0,
                sellPrice = 3500.0,
                stock = 96.0,
                unit = "Bungkus",
                minStockAlert = 20.0,
                description = "Mi instan goreng rasa original 85g",
                supplier = "Grosir Indofood"
            ),
            Product(
                id = 5,
                barcode = "899886620038",
                name = "Indomie Kuah Ayam Bawang",
                category = "Makanan",
                buyPrice = 2700.0,
                sellPrice = 3500.0,
                stock = 4.0, // Low stock test
                unit = "Bungkus",
                minStockAlert = 10.0,
                description = "Mi instan kuah rasa ayam bawang",
                supplier = "Grosir Indofood"
            ),
            Product(
                id = 6,
                barcode = "899600130421",
                name = "Aqua Botol 600ml",
                category = "Minuman",
                buyPrice = 3200.0,
                sellPrice = 4500.0,
                stock = 48.0,
                unit = "Botol",
                minStockAlert = 12.0,
                description = "Air mineral dalam kemasan 600ml",
                supplier = "Agen Danone Aqua"
            ),
            Product(
                id = 7,
                barcode = "899100140223",
                name = "Teh Botol Sosro Kotak 250ml",
                category = "Minuman",
                buyPrice = 3000.0,
                sellPrice = 4000.0,
                stock = 2.0, // Low stock alert
                unit = "Kotak",
                minStockAlert = 6.0,
                description = "Teh melati manis kemasan tetra pak",
                supplier = "Distributor Minuman Sejuk"
            ),
            Product(
                id = 8,
                barcode = "899990901244",
                name = "Kopi Kapal Api Spesial Mix 1 Renteng",
                category = "Minuman",
                buyPrice = 13500.0,
                sellPrice = 16000.0,
                stock = 18.0,
                unit = "Renteng",
                minStockAlert = 4.0,
                description = "Kopi bubuk + gula (10 sachet)",
                supplier = "Grosir Kopi"
            ),
            Product(
                id = 9,
                barcode = "899277002011",
                name = "Sabun Cuci Sunlight Jeruk Nipis 700ml",
                category = "Kebersihan",
                buyPrice = 13000.0,
                sellPrice = 16500.0,
                stock = 12.0,
                unit = "Pcs",
                minStockAlert = 5.0,
                description = "Sabun cuci piring pouch 700ml",
                supplier = "Unilever Distributor"
            ),
            Product(
                id = 10,
                barcode = "899277003022",
                name = "Deterjen Rinso Molto 770g",
                category = "Kebersihan",
                buyPrice = 21000.0,
                sellPrice = 25000.0,
                stock = 8.0,
                unit = "Pcs",
                minStockAlert = 3.0,
                description = "Deterjen bubuk anti noda dan wangi",
                supplier = "Unilever Distributor"
            ),
            Product(
                id = 11,
                barcode = "899277004033",
                name = "Pasta Gigi Pepsodent 190g",
                category = "Kebersihan",
                buyPrice = 12500.0,
                sellPrice = 15000.0,
                stock = 0.0, // Out of stock test
                unit = "Pcs",
                minStockAlert = 5.0,
                description = "Pasta gigi pencegah gigi berlubang",
                supplier = "Unilever Distributor"
            ),
            Product(
                id = 12,
                barcode = "899398811200",
                name = "Telur Ayam Negeri 1 Kg",
                category = "Sembako",
                buyPrice = 26000.0,
                sellPrice = 29500.0,
                stock = 25.0,
                unit = "Kg",
                minStockAlert = 5.0,
                description = "Telur ayam segar per kilogram",
                supplier = "Peternakan Berkah"
            )
        )
        productDao().insertProducts(defaultProducts)

        // Seed default customers
        val defaultCustomers = listOf(
            Customer(
                id = 1,
                name = "Ibu Siti Rohmah",
                phone = "081299887711",
                address = "RT 02 / RW 04 No. 15",
                totalDebt = 45000.0,
                notes = "Langganan warung, biasa bayar akhir pekan"
            ),
            Customer(
                id = 2,
                name = "Pak Budi Santoso",
                phone = "085712349988",
                address = "RT 01 / RW 04 No. 08",
                totalDebt = 0.0,
                notes = "Pelanggan setia sembako"
            ),
            Customer(
                id = 3,
                name = "Mbak Dewi Lestari",
                phone = "087855443322",
                address = "Blok C2 No. 10",
                totalDebt = 25000.0,
                notes = "Kasbon belanja deterjen & minyak"
            )
        )
        customerDao().insertCustomers(defaultCustomers)

        // Seed initial debt records
        debtDao().insertDebt(
            DebtRecord(
                id = 1,
                customerId = 1,
                customerName = "Ibu Siti Rohmah",
                invoiceNumber = "INV-20260820-001",
                totalDebt = 45000.0,
                amountPaid = 0.0,
                remainingDebt = 45000.0,
                isSettled = false,
                notes = "Beras 5kg dan gula 1kg",
                createdAt = System.currentTimeMillis() - 86400000L
            )
        )
        debtDao().insertDebt(
            DebtRecord(
                id = 2,
                customerId = 3,
                customerName = "Mbak Dewi Lestari",
                invoiceNumber = "INV-20260819-002",
                totalDebt = 25000.0,
                amountPaid = 0.0,
                remainingDebt = 25000.0,
                isSettled = false,
                notes = "Deterjen Rinso Molto",
                createdAt = System.currentTimeMillis() - (86400000L * 2)
            )
        )

        // Seed some sample transactions for analytics & dashboard
        val currentTime = System.currentTimeMillis()
        val order1 = TransactionOrder(
            id = 1,
            invoiceNumber = "INV-20260821-001",
            customerId = null,
            customerName = "Pelanggan Umum",
            subtotal = 74000.0,
            discountAmount = 0.0,
            taxAmount = 0.0,
            grandTotal = 74000.0,
            paidAmount = 100000.0,
            changeAmount = 26000.0,
            paymentMethod = "CASH",
            totalProfit = 8000.0,
            notes = "",
            timestamp = currentTime - 7200000L // 2 hours ago
        )
        val order1Id = transactionDao().insertOrder(order1)
        transactionDao().insertItems(
            listOf(
                TransactionItem(
                    orderId = order1Id,
                    productId = 1,
                    productName = "Minyak Goreng Bimoli 2L",
                    category = "Sembako",
                    buyPrice = 33000.0,
                    sellPrice = 37000.0,
                    quantity = 2.0,
                    unit = "Pcs",
                    discount = 0.0,
                    subtotal = 74000.0,
                    profit = 8000.0
                )
            )
        )

        val order2 = TransactionOrder(
            id = 2,
            invoiceNumber = "INV-20260821-002",
            customerId = null,
            customerName = "Pelanggan Umum",
            subtotal = 24500.0,
            discountAmount = 0.0,
            taxAmount = 0.0,
            grandTotal = 24500.0,
            paidAmount = 24500.0,
            changeAmount = 0.0,
            paymentMethod = "QRIS",
            totalProfit = 4600.0,
            notes = "",
            timestamp = currentTime - 3600000L // 1 hour ago
        )
        val order2Id = transactionDao().insertOrder(order2)
        transactionDao().insertItems(
            listOf(
                TransactionItem(
                    orderId = order2Id,
                    productId = 4,
                    productName = "Indomie Goreng Spesial",
                    category = "Makanan",
                    buyPrice = 2800.0,
                    sellPrice = 3500.0,
                    quantity = 3.0,
                    unit = "Bungkus",
                    discount = 0.0,
                    subtotal = 10500.0,
                    profit = 2100.0
                ),
                TransactionItem(
                    orderId = order2Id,
                    productId = 8,
                    productName = "Kopi Kapal Api Spesial Mix 1 Renteng",
                    category = "Minuman",
                    buyPrice = 13500.0,
                    sellPrice = 16000.0,
                    quantity = 1.0,
                    unit = "Renteng",
                    discount = 2000.0,
                    subtotal = 14000.0,
                    profit = 2500.0
                )
            )
        )

        // Seed Default Subscription Packages
        subscriptionDao().insertPackages(
            listOf(
                SubscriptionPackage(
                    name = "Paket Bronze (10 Mbps)",
                    price = 100000.0,
                    speedInfo = "10 Mbps Unlimited",
                    description = "Cocok untuk penggunaan 2-3 HP / keluarga kecil"
                ),
                SubscriptionPackage(
                    name = "Paket Silver (20 Mbps)",
                    price = 150000.0,
                    speedInfo = "20 Mbps Unlimited",
                    description = "Pilihan favorit untuk streaming HD & browsing cepat"
                ),
                SubscriptionPackage(
                    name = "Paket Gold (50 Mbps)",
                    price = 250000.0,
                    speedInfo = "50 Mbps Unlimited",
                    description = "Kecepatan maksimal untuk game online & kerja"
                ),
                SubscriptionPackage(
                    name = "TV Kabel + Channel Premium",
                    price = 75000.0,
                    speedInfo = "60+ Channel",
                    description = "Paket siaran TV lokal & internasional"
                )
            )
        )
    }
}
