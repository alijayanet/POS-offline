package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "held_carts")
data class HeldCart(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val note: String = "Antrean Pelanggan",
    val customerName: String = "Pelanggan Umum",
    val totalAmount: Double = 0.0,
    val itemsCount: Int = 0,
    val itemsJson: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
