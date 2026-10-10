package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transaction_orders")
data class TransactionOrder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val invoiceNumber: String,
    val customerId: Long? = null,
    val customerName: String = "Pelanggan Umum",
    val subtotal: Double = 0.0,
    val discountAmount: Double = 0.0,
    val taxAmount: Double = 0.0,
    val grandTotal: Double = 0.0,
    val paidAmount: Double = 0.0,
    val changeAmount: Double = 0.0,
    val paymentMethod: String = PaymentMethod.CASH.name,
    val totalProfit: Double = 0.0, // Keuntungan bersih dari transaksi ini
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
