package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscription_customers")
data class SubscriptionCustomer(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerCode: String = "",
    val name: String,
    val phone: String = "",
    val address: String = "",
    val packageId: Long = 0,
    val packageName: String = "",
    val monthlyPrice: Double = 0.0,
    val installationDate: Long = System.currentTimeMillis(),
    val dueDay: Int = 10,
    val paidUntilTimestamp: Long = System.currentTimeMillis(),
    val status: String = "ACTIVE",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
