package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "store_profile")
data class StoreProfile(
    @PrimaryKey
    val id: Int = 1,
    val storeName: String = "Toko Berkah Jaya",
    val ownerName: String = "Ali Jaya",
    val phone: String = "081234567890",
    val address: String = "Jl. Merdeka No. 45, Jakarta",
    val receiptFooter: String = "Terima kasih atas kunjungan Anda!\nBarang yang sudah dibeli tidak dapat ditukar/dikembalikan.",
    val currencySymbol: String = "Rp",
    val taxRatePercent: Double = 0.0,
    val isTaxEnabled: Boolean = false,
    val qrisStaticPayload: String = "",
    val printerType: String = "BLUETOOTH", // "BLUETOOTH", "RAWBT", "SYSTEM"
    val bluetoothPrinterMac: String = "",
    val paperSize: String = "58mm", // "58mm", "80mm"
    val bankAccountsInfo: String = "🏧 BCA : 8820 1928 334 (a.n Ali Jaya)\n🏧 BRI : 0123 0100 4567 508 (a.n Ali Jaya)\n🏧 Dana/OVO/ShopeePay : 0812 3456 7890\n📱 Kasir Toko : Tersedia Tunai / QRIS di toko"
)
