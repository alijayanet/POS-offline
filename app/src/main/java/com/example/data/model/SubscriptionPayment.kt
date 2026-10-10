package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscription_payments")
data class SubscriptionPayment(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val customerCode: String = "",
    val customerName: String,
    val invoiceNumber: String,
    val packageName: String,
    val monthlyPrice: Double,
    val monthsCount: Int = 1,
    val periodStartTimestamp: Long,
    val periodEndTimestamp: Long,
    val totalAmount: Double,
    val paymentMethod: String = "CASH",
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)
