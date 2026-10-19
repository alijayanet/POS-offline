package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "collectors")
data class Collector(
    @PrimaryKey
    val id: String, // Contoh: "KOL-17345678"
    val name: String,
    val phone: String = "",
    val assignedArea: String = "", // Wilayah tugas, misal: "RT 01 - 04"
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
