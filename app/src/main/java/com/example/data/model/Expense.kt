package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String = "Operasional", // Listrik & Air, Sewa Tempat, Gaji Karyawan, Transport & BBM, Kemasan/Kresek, ATK, Lain-lain
    val amount: Double,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
