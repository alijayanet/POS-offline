package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transaction_items",
    foreignKeys = [
        ForeignKey(
            entity = TransactionOrder::class,
            parentColumns = ["id"],
            childColumns = ["orderId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("orderId"), Index("productId")]
)
data class TransactionItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val orderId: Long,
    val productId: Long,
    val productName: String,
    val category: String = "Umum",
    val buyPrice: Double = 0.0,
    val sellPrice: Double = 0.0,
    val quantity: Double = 1.0,
    val unit: String = "Pcs",
    val discount: Double = 0.0,
    val subtotal: Double = 0.0,
    val profit: Double = 0.0
)
