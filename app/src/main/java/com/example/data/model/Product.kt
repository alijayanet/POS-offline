package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val barcode: String = "",
    val sku: String = "",
    val name: String,
    val category: String = "Umum",
    val buyPrice: Double = 0.0, // Modal / COGS
    val sellPrice: Double = 0.0, // Harga Jual
    val stock: Double = 0.0,
    val unit: String = "Pcs", // Pcs, Kg, Liter, Dus, Botol, Sachet, dll.
    val minStockAlert: Double = 5.0,
    val wholesalePrice: Double = 0.0,
    val wholesaleMinQty: Double = 0.0,
    val unitsJson: String = "",
    val description: String = "",
    val supplier: String = "",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class ProductUnit(
    val unitName: String,
    val conversionFactor: Double = 1.0,
    val priceRetail: Double,
    val priceWholesale: Double = 0.0,
    val wholesaleMinQty: Double = 0.0
)
