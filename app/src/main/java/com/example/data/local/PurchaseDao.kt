package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.Purchase
import kotlinx.coroutines.flow.Flow

@Dao
interface PurchaseDao {
    @Query("SELECT * FROM purchases ORDER BY timestamp DESC")
    fun getAllPurchases(): Flow<List<Purchase>>

    @Query("SELECT * FROM purchases WHERE debtBalance > 0 ORDER BY timestamp DESC")
    fun getUnpaidPurchases(): Flow<List<Purchase>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchase(purchase: Purchase): Long

    @Query("UPDATE purchases SET debtBalance = :newDebt, paymentStatus = :newStatus WHERE id = :id")
    suspend fun updatePurchaseDebt(id: Long, newDebt: Double, newStatus: String)

    @Query("SELECT * FROM purchases")
    suspend fun getAllPurchasesSync(): List<Purchase>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPurchases(purchases: List<Purchase>)

    @Query("DELETE FROM purchases")
    suspend fun clearAllPurchases()
}
