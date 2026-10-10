package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "purchases")
data class Purchase(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val supplierId: Long? = null,
    val supplierName: String = "Supplier Umum",
    val invoiceNumber: String = "",
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val debtBalance: Double = 0.0,
    val paymentType: String = "CASH", // "CASH", "CREDIT"
    val paymentStatus: String = "PAID", // "PAID", "UNPAID", "PARTIAL"
    val dueDate: Long? = null,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
