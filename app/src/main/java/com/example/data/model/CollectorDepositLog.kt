package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "collector_deposit_logs")
data class CollectorDepositLog(
    @PrimaryKey
    val id: String, // ID Transaksi pembayaran kolektor
    val collectorId: String,
    val collectorName: String,
    val customerId: Long,
    val customerCode: String = "",
    val customerName: String,
    val amountPaid: Double,
    val paidAt: Long = System.currentTimeMillis(),
    val isDepositedToAdmin: Boolean = false, // True jika uang fisik sudah diserahkan ke kasir
    val depositedAt: Long? = null
)
