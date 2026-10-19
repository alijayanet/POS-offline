package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.Product
import com.example.data.model.StoreProfile
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionOrder
import com.example.ui.components.CurrencyUtils
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvSpreadsheetUtils {

    private const val UTF8_BOM = "\uFEFF"

    /**
     * Export all products to CSV format compatible with Microsoft Excel & Google Sheets
     */
    fun exportProductsToCsv(products: List<Product>): String {
        val sb = StringBuilder()
        sb.append(UTF8_BOM) // Enables Excel to recognize UTF-8 instantly

        // Header
        sb.append("SKU,Barcode,Nama Produk,Kategori,Harga Beli (Modal),Harga Jual,Stok,Satuan,Min Stok Alert,Harga Grosir,Min Qty Grosir,Supplier,Deskripsi\n")

        for (p in products) {
            sb.append(escapeCsv(p.sku)).append(",")
            sb.append(escapeCsv(p.barcode)).append(",")
            sb.append(escapeCsv(p.name)).append(",")
            sb.append(escapeCsv(p.category)).append(",")
            sb.append(p.buyPrice.toLong()).append(",")
            sb.append(p.sellPrice.toLong()).append(",")
            sb.append(CurrencyUtils.formatQty(p.stock)).append(",")
            sb.append(escapeCsv(p.unit)).append(",")
            sb.append(CurrencyUtils.formatQty(p.minStockAlert)).append(",")
            sb.append(p.wholesalePrice.toLong()).append(",")
            sb.append(CurrencyUtils.formatQty(p.wholesaleMinQty)).append(",")
            sb.append(escapeCsv(p.supplier)).append(",")
            sb.append(escapeCsv(p.description)).append("\n")
        }

        return sb.toString()
    }

    /**
     * Generate sample CSV template for users to fill in
     */
    fun generateProductTemplateCsv(): String {
        val sb = StringBuilder()
        sb.append(UTF8_BOM)
        sb.append("SKU,Barcode,Nama Produk,Kategori,Harga Beli (Modal),Harga Jual,Stok,Satuan,Min Stok Alert,Harga Grosir,Min Qty Grosir,Supplier,Deskripsi\n")
        sb.append("SKU-001,8991234567890,Beras Premium Ramos 5kg,Sembako,65000,75000,50,Karung,5,72000,5,CV Sumber Beras,Beras Ramos pulen super\n")
        sb.append("SKU-002,8992345678901,Minyak Goreng Bimoli 2L,Minyak & Bumbu,32000,36000,30,Pouch,6,34500,6,PT Distribusi Sembako,Minyak goreng kelapa sawit\n")
        sb.append("SKU-003,8993456789012,Gula Pasir Gulaku 1kg,Sembako,15000,17500,100,Kg,10,16500,10,PT Distribusi Sembako,Gula tebu murni\n")
        sb.append("SKU-004,,Kopi Tubruk Manis,Minuman,3000,5000,100,Cup,10,0,0,,Kopi hitam khas warung\n")
        sb.append("SKU-005,,Nasi Goreng Spesial,Makanan,12000,18000,50,Porsi,5,0,0,,Nasi goreng telur + ayam\n")
        sb.append("SKU-006,,Kain Katun Rayon,Tekstil,25000,35000,150,Meter,10,30000,10,Pabrik Tekstil,Kain meteran motif polos\n")
        return sb.toString()
    }

    /**
     * Parse products from imported CSV string
     */
    fun parseProductsFromCsv(csvContent: String): List<Product> {
        val cleanContent = if (csvContent.startsWith(UTF8_BOM)) csvContent.substring(1) else csvContent
        val lines = cleanContent.lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return emptyList()

        val products = mutableListOf<Product>()
        val currentTime = System.currentTimeMillis()

        // Detect delimiter (comma or semicolon)
        val headerLine = lines.first()
        val delimiter = if (headerLine.count { it == ';' } > headerLine.count { it == ',' }) ';' else ','
        val hasSkuColumn = headerLine.contains("SKU", ignoreCase = true)

        for (i in 1 until lines.size) {
            val line = lines[i]
            val tokens = splitCsvLine(line, delimiter)
            if (tokens.isEmpty()) continue

            val sku: String
            val barcode: String
            val name: String
            val category: String
            val buyPrice: Double
            val sellPrice: Double
            val stock: Double
            val unit: String
            val minStock: Double
            val wholesalePrice: Double
            val wholesaleMinQty: Double
            val supplier: String
            val description: String

            if (hasSkuColumn) {
                sku = tokens.getOrNull(0)?.trim() ?: ""
                barcode = tokens.getOrNull(1)?.trim() ?: ""
                name = tokens.getOrNull(2)?.trim() ?: ""
                category = tokens.getOrNull(3)?.trim()?.ifBlank { "Umum" } ?: "Umum"
                buyPrice = tokens.getOrNull(4)?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
                sellPrice = tokens.getOrNull(5)?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
                stock = tokens.getOrNull(6)?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
                unit = tokens.getOrNull(7)?.trim()?.ifBlank { "Pcs" } ?: "Pcs"
                minStock = tokens.getOrNull(8)?.replace(",", ".")?.toDoubleOrNull() ?: 5.0
                wholesalePrice = tokens.getOrNull(9)?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
                wholesaleMinQty = tokens.getOrNull(10)?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
                supplier = tokens.getOrNull(11)?.trim() ?: ""
                description = tokens.getOrNull(12)?.trim() ?: ""
            } else {
                sku = ""
                barcode = tokens.getOrNull(0)?.trim() ?: ""
                name = tokens.getOrNull(1)?.trim() ?: ""
                category = tokens.getOrNull(2)?.trim()?.ifBlank { "Umum" } ?: "Umum"
                buyPrice = tokens.getOrNull(3)?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
                sellPrice = tokens.getOrNull(4)?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
                stock = tokens.getOrNull(5)?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
                unit = tokens.getOrNull(6)?.trim()?.ifBlank { "Pcs" } ?: "Pcs"
                minStock = tokens.getOrNull(7)?.replace(",", ".")?.toDoubleOrNull() ?: 5.0
                wholesalePrice = tokens.getOrNull(8)?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
                wholesaleMinQty = tokens.getOrNull(9)?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
                supplier = tokens.getOrNull(10)?.trim() ?: ""
                description = tokens.getOrNull(11)?.trim() ?: ""
            }

            if (name.isBlank()) continue

            products.add(
                Product(
                    id = 0L,
                    barcode = barcode,
                    sku = sku,
                    name = name,
                    category = category,
                    buyPrice = buyPrice,
                    sellPrice = sellPrice,
                    stock = stock,
                    unit = unit,
                    minStockAlert = minStock,
                    wholesalePrice = wholesalePrice,
                    wholesaleMinQty = wholesaleMinQty,
                    supplier = supplier,
                    description = description,
                    isActive = true,
                    createdAt = currentTime,
                    updatedAt = currentTime
                )
            )
        }

        return products
    }

    /**
     * Export Sales & Financial Report to CSV
     */
    fun exportSalesReportToCsv(
        orders: List<TransactionOrder>,
        startDate: Long,
        endDate: Long,
        totalExpenses: Double = 0.0
    ): String {
        val sb = StringBuilder()
        sb.append(UTF8_BOM)

        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val rangeStr = "${sdf.format(Date(startDate))} - ${sdf.format(Date(endDate))}"

        sb.append("LAPORAN PENJUALAN TOKOPOS\n")
        sb.append("Periode:,").append(escapeCsv(rangeStr)).append("\n")
        sb.append("Dicetak Pada:,").append(escapeCsv(SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()))).append("\n\n")

        // Summary KPI
        val totalRevenue = orders.sumOf { it.grandTotal }
        val totalGrossProfit = orders.sumOf { it.totalProfit }
        val netProfit = totalGrossProfit - totalExpenses

        sb.append("RINGKASAN KEUANGAN\n")
        sb.append("Total Transaksi:,").append(orders.size).append("\n")
        sb.append("Total Omset Penjualan:,").append(totalRevenue.toLong()).append("\n")
        sb.append("Laba Kotor Penjualan:,").append(totalGrossProfit.toLong()).append("\n")
        sb.append("Total Biaya Pengeluaran:,").append(totalExpenses.toLong()).append("\n")
        sb.append("Laba Bersih Toko:,").append(netProfit.toLong()).append("\n\n")

        // Orders Table
        sb.append("DETAIL TRANSAKSI\n")
        sb.append("No Faktur,Tanggal,Pelanggan,Metode Bayar,Total Belanja,Diskon,Pajak,Laba Transaksi,Catatan\n")

        val timeFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        for (o in orders) {
            sb.append(escapeCsv(o.invoiceNumber)).append(",")
            sb.append(escapeCsv(timeFormat.format(Date(o.timestamp)))).append(",")
            sb.append(escapeCsv(o.customerName)).append(",")
            sb.append(escapeCsv(o.paymentMethod)).append(",")
            sb.append(o.grandTotal.toLong()).append(",")
            sb.append(o.discountAmount.toLong()).append(",")
            sb.append(o.taxAmount.toLong()).append(",")
            sb.append(o.totalProfit.toLong()).append(",")
            sb.append(escapeCsv(o.notes)).append("\n")
        }

        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains(";")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }

    private fun splitCsvLine(line: String, delimiter: Char): List<String> {
        val tokens = mutableListOf<String>()
        val sb = java.lang.StringBuilder()
        var inQuotes = false
        var i = 0

        while (i < line.length) {
            val c = line[i]
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                    sb.append('\"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == delimiter && !inQuotes) {
                tokens.add(sb.toString().trim())
                sb.setLength(0)
            } else {
                sb.append(c)
            }
            i++
        }
        tokens.add(sb.toString().trim())
        return tokens
    }

    fun writeCsvToUri(context: Context, uri: Uri, csvString: String): Result<Unit> {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { os ->
                OutputStreamWriter(os, Charsets.UTF_8).use { writer ->
                    writer.write(csvString)
                    writer.flush()
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun readCsvFromUri(context: Context, uri: Uri): Result<String> {
        return try {
            val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            } ?: return Result.failure(Exception("Gagal membuka file"))
            Result.success(content)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun shareCsvFile(context: Context, csvString: String, fileName: String, subject: String) {
        try {
            val file = File(context.cacheDir, fileName)
            FileOutputStream(file).use { fos ->
                fos.write(csvString.toByteArray(Charsets.UTF_8))
                fos.flush()
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, "File ekspor data TokoPOS ($fileName).")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(Intent.createChooser(shareIntent, "Kirim / Bagikan File Excel CSV").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // ==========================================
    // CUSTOMER SPREADSHEET UTILITIES
    // ==========================================

    fun exportCustomersToCsv(customers: List<com.example.data.model.Customer>): String {
        val sb = StringBuilder()
        sb.append(UTF8_BOM)
        sb.append("Nama Pelanggan,No WhatsApp / HP,Alamat,Total Hutang,Catatan\n")
        for (c in customers) {
            sb.append(escapeCsv(c.name)).append(",")
            sb.append(escapeCsv(c.phone)).append(",")
            sb.append(escapeCsv(c.address)).append(",")
            sb.append(c.totalDebt.toLong()).append(",")
            sb.append(escapeCsv(c.notes)).append("\n")
        }
        return sb.toString()
    }

    fun generateCustomerTemplateCsv(): String {
        val sb = StringBuilder()
        sb.append(UTF8_BOM)
        sb.append("Nama Pelanggan,No WhatsApp / HP,Alamat,Total Hutang,Catatan\n")
        sb.append("Budi Santoso,081234567890,Jl. Mawar No. 12 RT 01/02,0,Langganan warung\n")
        sb.append("Ibu Siti Rahma,085678901234,Perum Griya Asri Blok B3,0,Katering mingguan\n")
        sb.append("Pak Haji Ahmad,087812345678,Kp. Sawah No. 45,0,Warung tetangga\n")
        return sb.toString()
    }

    fun parseCustomersFromCsv(csvContent: String): List<com.example.data.model.Customer> {
        val cleanContent = if (csvContent.startsWith(UTF8_BOM)) csvContent.substring(1) else csvContent
        val lines = cleanContent.lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return emptyList()

        val customers = mutableListOf<com.example.data.model.Customer>()
        val headerLine = lines.first()
        val delimiter = if (headerLine.count { it == ';' } > headerLine.count { it == ',' }) ';' else ','

        for (i in 1 until lines.size) {
            val line = lines[i]
            val tokens = splitCsvLine(line, delimiter)
            if (tokens.isEmpty()) continue

            val name = tokens.getOrNull(0)?.trim() ?: ""
            if (name.isBlank()) continue

            val phone = tokens.getOrNull(1)?.trim() ?: ""
            val address = tokens.getOrNull(2)?.trim() ?: ""
            val totalDebt = tokens.getOrNull(3)?.replace(",", ".")?.toDoubleOrNull() ?: 0.0
            val notes = tokens.getOrNull(4)?.trim() ?: ""

            customers.add(
                com.example.data.model.Customer(
                    name = name,
                    phone = phone,
                    address = address,
                    totalDebt = totalDebt,
                    notes = notes,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
        return customers
    }

    // ==========================================
    // SUBSCRIPTION CUSTOMER SPREADSHEET UTILITIES
    // ==========================================

    fun exportSubscriptionCustomersToCsv(customers: List<com.example.data.model.SubscriptionCustomer>): String {
        val sb = StringBuilder()
        sb.append(UTF8_BOM)
        sb.append("ID Pelanggan,Nama Lengkap,No WhatsApp / HP,Alamat,Paket Langganan,Tarif Bulanan (Rp),Tanggal Jatuh Tempo (1-31),Status (ACTIVE/ISOLIR/INACTIVE),Catatan\n")
        for (c in customers) {
            sb.append(escapeCsv(c.customerCode)).append(",")
            sb.append(escapeCsv(c.name)).append(",")
            sb.append(escapeCsv(c.phone)).append(",")
            sb.append(escapeCsv(c.address)).append(",")
            sb.append(escapeCsv(c.packageName)).append(",")
            sb.append(c.monthlyPrice.toLong()).append(",")
            sb.append(c.dueDay).append(",")
            sb.append(escapeCsv(c.status)).append(",")
            sb.append(escapeCsv(c.notes)).append("\n")
        }
        return sb.toString()
    }

    fun generateSubscriptionCustomerTemplateCsv(packages: List<com.example.data.model.SubscriptionPackage>): String {
        val defaultPkgName = packages.firstOrNull()?.name ?: "Paket Gold 30 Mbps"
        val defaultPrice = packages.firstOrNull()?.price?.toLong() ?: 150000L

        val sb = StringBuilder()
        sb.append(UTF8_BOM)
        sb.append("ID Pelanggan,Nama Lengkap,No WhatsApp / HP,Alamat,Paket Langganan,Tarif Bulanan (Rp),Tanggal Jatuh Tempo (1-31),Status (ACTIVE/ISOLIR/INACTIVE),Catatan\n")
        sb.append("PLG-001,Budi Santoso,081234567890,Jl. Merpati No. 15,$defaultPkgName,$defaultPrice,10,ACTIVE,Port 3 ODP-02\n")
        sb.append("PLG-002,Ibu Siti Rahma,085678901234,Jl. Kenanga No. 8,$defaultPkgName,$defaultPrice,15,ACTIVE,Rumah tingkat\n")
        sb.append("PLG-003,Pak Haji Ahmad,087812345678,Kp. Melati RT 03/02,$defaultPkgName,$defaultPrice,20,ACTIVE,Router lantai 2\n")
        return sb.toString()
    }

    fun parseSubscriptionCustomersFromCsv(
        csvContent: String,
        availablePackages: List<com.example.data.model.SubscriptionPackage>
    ): List<com.example.data.model.SubscriptionCustomer> {
        val cleanContent = if (csvContent.startsWith(UTF8_BOM)) csvContent.substring(1) else csvContent
        val lines = cleanContent.lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return emptyList()

        val customers = mutableListOf<com.example.data.model.SubscriptionCustomer>()
        val headerLine = lines.first()
        val delimiter = if (headerLine.count { it == ';' } > headerLine.count { it == ',' }) ';' else ','

        for (i in 1 until lines.size) {
            val line = lines[i]
            val tokens = splitCsvLine(line, delimiter)
            if (tokens.isEmpty()) continue

            val customerCode = tokens.getOrNull(0)?.trim() ?: ""
            val name = tokens.getOrNull(1)?.trim() ?: ""
            if (name.isBlank()) continue

            val phone = tokens.getOrNull(2)?.trim() ?: ""
            val address = tokens.getOrNull(3)?.trim() ?: ""
            val pkgNameInput = tokens.getOrNull(4)?.trim() ?: ""
            val matchedPkg = availablePackages.find { it.name.equals(pkgNameInput, ignoreCase = true) }
                ?: availablePackages.firstOrNull()

            val priceInput = tokens.getOrNull(5)?.replace(",", ".")?.toDoubleOrNull()
            val monthlyPrice = priceInput ?: (matchedPkg?.price ?: 100000.0)
            val packageName = if (pkgNameInput.isNotBlank()) pkgNameInput else (matchedPkg?.name ?: "Paket Standar")
            val packageId = matchedPkg?.id ?: 0L

            val dueDay = tokens.getOrNull(6)?.toIntOrNull()?.coerceIn(1, 31) ?: 10
            val statusRaw = tokens.getOrNull(7)?.trim()?.uppercase() ?: "ACTIVE"
            val status = if (statusRaw in listOf("ACTIVE", "ISOLIR", "INACTIVE")) statusRaw else "ACTIVE"
            val notes = tokens.getOrNull(8)?.trim() ?: ""

            customers.add(
                com.example.data.model.SubscriptionCustomer(
                    customerCode = customerCode.ifBlank { "PLG-" + (100..999).random() },
                    name = name,
                    phone = phone,
                    address = address,
                    packageId = packageId,
                    packageName = packageName,
                    monthlyPrice = monthlyPrice,
                    installationDate = System.currentTimeMillis(),
                    dueDay = dueDay,
                    paidUntilTimestamp = System.currentTimeMillis(),
                    status = status,
                    notes = notes,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
        return customers
    }

    /**
     * Export Collector transactions & deposits to Excel / CSV format
     */
    fun exportCollectorDepositLogsToCsv(logs: List<com.example.data.model.CollectorDepositLog>): String {
        val sb = StringBuilder()
        sb.append(UTF8_BOM)

        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val totalAmount = logs.sumOf { it.amountPaid }
        val undepositedLogs = logs.filter { !it.isDepositedToAdmin }
        val undepositedAmount = undepositedLogs.sumOf { it.amountPaid }
        val depositedLogs = logs.filter { it.isDepositedToAdmin }
        val depositedAmount = depositedLogs.sumOf { it.amountPaid }

        // Report Title and Summary
        sb.append("LAPORAN TRANSAKSI & SETORAN KOLEKTOR LAPANGAN\n")
        sb.append("Tanggal Cetak:,").append(dateFormat.format(Date())).append("\n")
        sb.append("Total Transaksi Masuk:,").append(logs.size).append("\n")
        sb.append("Total Uang Tagihan Diterima (Rp):,").append(totalAmount.toLong()).append("\n")
        sb.append("Sudah Disetor ke Kasir (Rp):,").append(depositedAmount.toLong()).append("\n")
        sb.append("Belum Disetor ke Kasir (Rp):,").append(undepositedAmount.toLong()).append("\n\n")

        // Detail Table Header
        sb.append("No,Tanggal & Jam,No Kuitansi/ID,Nama Kolektor,ID Pelanggan,Nama Pelanggan,Nominal Bayar (Rp),Status Setor,Waktu Setor ke Kasir\n")

        for ((index, log) in logs.withIndex()) {
            val paidDateStr = dateFormat.format(Date(log.paidAt))
            val statusStr = if (log.isDepositedToAdmin) "SUDAH DISETOR" else "BELUM DISETOR"
            val depositDateStr = if (log.depositedAt != null) dateFormat.format(Date(log.depositedAt)) else "-"

            sb.append(index + 1).append(",")
            sb.append(escapeCsv(paidDateStr)).append(",")
            sb.append(escapeCsv(log.id)).append(",")
            sb.append(escapeCsv(log.collectorName)).append(",")
            sb.append(escapeCsv(log.customerCode)).append(",")
            sb.append(escapeCsv(log.customerName)).append(",")
            sb.append(log.amountPaid.toLong()).append(",")
            sb.append(escapeCsv(statusStr)).append(",")
            sb.append(escapeCsv(depositDateStr)).append("\n")
        }

        return sb.toString()
    }
}

