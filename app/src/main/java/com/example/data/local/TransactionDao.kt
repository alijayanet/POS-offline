package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.data.model.TransactionItem
import com.example.data.model.TransactionOrder
import kotlinx.coroutines.flow.Flow

data class BestSellingProduct(
    val productId: Long,
    val productName: String,
    val totalSold: Double,
    val totalRevenue: Double
)

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transaction_orders ORDER BY timestamp DESC")
    fun getAllOrders(): Flow<List<TransactionOrder>>

    @Query("SELECT * FROM transaction_orders WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    fun getOrdersByDateRange(startTime: Long, endTime: Long): Flow<List<TransactionOrder>>

    @Query("SELECT * FROM transaction_orders WHERE id = :orderId LIMIT 1")
    suspend fun getOrderById(orderId: Long): TransactionOrder?

    @Query("SELECT * FROM transaction_orders WHERE invoiceNumber = :invoice LIMIT 1")
    suspend fun getOrderByInvoice(invoice: String): TransactionOrder?

    @Query("SELECT * FROM transaction_items WHERE orderId = :orderId")
    fun getItemsForOrder(orderId: Long): Flow<List<TransactionItem>>

    @Query("SELECT * FROM transaction_items WHERE orderId = :orderId")
    suspend fun getItemsForOrderSync(orderId: Long): List<TransactionItem>

    @Query("SELECT COUNT(*) FROM transaction_orders WHERE timestamp >= :startTime AND timestamp <= :endTime")
    fun getOrderCount(startTime: Long, endTime: Long): Flow<Int>

    @Query("SELECT SUM(grandTotal) FROM transaction_orders WHERE timestamp >= :startTime AND timestamp <= :endTime")
    fun getTotalRevenue(startTime: Long, endTime: Long): Flow<Double?>

    @Query("SELECT SUM(totalProfit) FROM transaction_orders WHERE timestamp >= :startTime AND timestamp <= :endTime")
    fun getTotalProfit(startTime: Long, endTime: Long): Flow<Double?>

    @Query("SELECT productId, productName, SUM(quantity) as totalSold, SUM(subtotal) as totalRevenue FROM transaction_items GROUP BY productId ORDER BY totalSold DESC LIMIT :limit")
    fun getBestSellingProducts(limit: Int = 5): Flow<List<BestSellingProduct>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: TransactionOrder): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<TransactionItem>)

    @Delete
    suspend fun deleteOrder(order: TransactionOrder)

    @Query("DELETE FROM transaction_items WHERE orderId = :orderId")
    suspend fun deleteItemsForOrder(orderId: Long)

    @Query("SELECT * FROM transaction_orders")
    suspend fun getAllOrdersSync(): List<TransactionOrder>

    @Query("SELECT * FROM transaction_items")
    suspend fun getAllItemsSync(): List<TransactionItem>

    @Query("DELETE FROM transaction_orders")
    suspend fun clearAllOrders()

    @Query("DELETE FROM transaction_items")
    suspend fun clearAllItems()
}
