package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SubscriptionCustomer
import com.example.data.model.SubscriptionPackage
import com.example.data.model.SubscriptionPayment
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {

    // --- Subscription Packages ---
    @Query("SELECT * FROM subscription_packages WHERE isActive = 1 ORDER BY price ASC")
    fun getAllPackages(): Flow<List<SubscriptionPackage>>

    @Query("SELECT * FROM subscription_packages WHERE id = :id")
    suspend fun getPackageById(id: Long): SubscriptionPackage?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackage(pkg: SubscriptionPackage): Long

    @Update
    suspend fun updatePackage(pkg: SubscriptionPackage)

    @Delete
    suspend fun deletePackage(pkg: SubscriptionPackage)

    @Query("SELECT * FROM subscription_packages")
    fun getAllPackagesSync(): List<SubscriptionPackage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPackages(packages: List<SubscriptionPackage>)

    @Query("DELETE FROM subscription_packages")
    suspend fun clearAllPackages()


    // --- Subscription Customers ---
    @Query("SELECT * FROM subscription_customers ORDER BY name ASC")
    fun getAllSubscriptionCustomers(): Flow<List<SubscriptionCustomer>>

    @Query("SELECT * FROM subscription_customers WHERE id = :id")
    suspend fun getSubscriptionCustomerById(id: Long): SubscriptionCustomer?

    @Query("""
        SELECT * FROM subscription_customers 
        WHERE name LIKE '%' || :query || '%' 
           OR customerCode LIKE '%' || :query || '%' 
           OR phone LIKE '%' || :query || '%'
           OR address LIKE '%' || :query || '%'
        ORDER BY name ASC
    """)
    fun searchSubscriptionCustomers(query: String): Flow<List<SubscriptionCustomer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscriptionCustomer(customer: SubscriptionCustomer): Long

    @Update
    suspend fun updateSubscriptionCustomer(customer: SubscriptionCustomer)

    @Delete
    suspend fun deleteSubscriptionCustomer(customer: SubscriptionCustomer)

    @Query("UPDATE subscription_customers SET paidUntilTimestamp = :paidUntilTimestamp, updatedAt = :updatedAt WHERE id = :customerId")
    suspend fun updateCustomerPaidUntil(customerId: Long, paidUntilTimestamp: Long, updatedAt: Long)

    @Query("SELECT * FROM subscription_customers")
    fun getAllSubscriptionCustomersSync(): List<SubscriptionCustomer>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscriptionCustomers(customers: List<SubscriptionCustomer>)

    @Query("DELETE FROM subscription_customers")
    suspend fun clearAllSubscriptionCustomers()


    // --- Subscription Payments History ---
    @Query("SELECT * FROM subscription_payments ORDER BY timestamp DESC")
    fun getAllSubscriptionPayments(): Flow<List<SubscriptionPayment>>

    @Query("SELECT * FROM subscription_payments WHERE customerId = :customerId ORDER BY timestamp DESC")
    fun getPaymentsByCustomerId(customerId: Long): Flow<List<SubscriptionPayment>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscriptionPayment(payment: SubscriptionPayment): Long

    @Query("SELECT * FROM subscription_payments")
    fun getAllSubscriptionPaymentsSync(): List<SubscriptionPayment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscriptionPayments(payments: List<SubscriptionPayment>)

    @Query("DELETE FROM subscription_payments")
    suspend fun clearAllSubscriptionPayments()
}
