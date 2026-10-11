package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY name ASC")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isActive = 1 AND (name LIKE '%' || :query || '%' OR barcode LIKE '%' || :query || '%' OR sku LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%') ORDER BY name ASC")
    fun searchProducts(query: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isActive = 1 AND category = :category ORDER BY name ASC")
    fun getProductsByCategory(category: String): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE isActive = 1 AND stock <= minStockAlert ORDER BY stock ASC")
    fun getLowStockProducts(): Flow<List<Product>>

    @Query("SELECT COUNT(*) FROM products WHERE isActive = 1 AND stock <= minStockAlert")
    fun getLowStockCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM products WHERE isActive = 1")
    fun getTotalProductCount(): Flow<Int>

    @Query("SELECT SUM(stock * buyPrice) FROM products WHERE isActive = 1")
    fun getTotalInventoryCostValue(): Flow<Double?>

    @Query("SELECT SUM(stock * sellPrice) FROM products WHERE isActive = 1")
    fun getTotalInventoryRetailValue(): Flow<Double?>

    @Query("SELECT * FROM products WHERE (barcode = :barcode OR sku = :barcode) AND isActive = 1 LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): Product?

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProducts(products: List<Product>)

    @Update
    suspend fun updateProduct(product: Product)

    @Query("UPDATE products SET stock = stock - :quantity, updatedAt = :timestamp WHERE id = :productId")
    suspend fun reduceStock(productId: Long, quantity: Double, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE products SET stock = stock + :quantity, updatedAt = :timestamp WHERE id = :productId")
    suspend fun addStock(productId: Long, quantity: Double, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE products SET isActive = 0 WHERE id = :id")
    suspend fun softDeleteProduct(id: Long)

    @Query("SELECT * FROM products")
    suspend fun getAllProductsSync(): List<Product>

    @Query("DELETE FROM products")
    suspend fun clearAllProducts()

    @Delete
    suspend fun deleteProduct(product: Product)
}
