package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "debt_records",
    foreignKeys = [
        ForeignKey(
            entity = Customer::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("customerId")]
)
data class DebtRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val customerId: Long,
    val customerName: String,
    val orderId: Long? = null,
    val invoiceNumber: String = "",
    val totalDebt: Double = 0.0,
    val amountPaid: Double = 0.0,
    val remainingDebt: Double = 0.0,
    val isSettled: Boolean = false,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val settledAt: Long? = null
)
