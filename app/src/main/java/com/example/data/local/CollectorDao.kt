package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.Collector
import com.example.data.model.CollectorDepositLog
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectorDao {

    // --- Collector Management ---
    @Query("SELECT * FROM collectors ORDER BY name ASC")
    fun getAllCollectors(): Flow<List<Collector>>

    @Query("SELECT * FROM collectors WHERE isActive = 1 ORDER BY name ASC")
    fun getActiveCollectors(): Flow<List<Collector>>

    @Query("SELECT * FROM collectors WHERE id = :id")
    suspend fun getCollectorById(id: String): Collector?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollector(collector: Collector)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCollectors(collectors: List<Collector>)

    @Update
    suspend fun updateCollector(collector: Collector)

    @Delete
    suspend fun deleteCollector(collector: Collector)

    @Query("DELETE FROM collectors WHERE id = :id")
    suspend fun deleteCollectorById(id: String)

    @Query("SELECT * FROM collectors")
    fun getAllCollectorsSync(): List<Collector>

    // --- Deposit & Collection Logs ---
    @Query("SELECT * FROM collector_deposit_logs ORDER BY paidAt DESC")
    fun getAllDepositLogs(): Flow<List<CollectorDepositLog>>

    @Query("SELECT * FROM collector_deposit_logs WHERE collectorId = :collectorId ORDER BY paidAt DESC")
    fun getLogsByCollector(collectorId: String): Flow<List<CollectorDepositLog>>

    @Query("SELECT * FROM collector_deposit_logs WHERE collectorId = :collectorId AND isDepositedToAdmin = 0 ORDER BY paidAt DESC")
    fun getUndepositedLogsByCollector(collectorId: String): Flow<List<CollectorDepositLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepositLog(log: CollectorDepositLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDepositLogs(logs: List<CollectorDepositLog>)

    @Query("UPDATE collector_deposit_logs SET isDepositedToAdmin = 1, depositedAt = :depositedAt WHERE collectorId = :collectorId AND isDepositedToAdmin = 0")
    suspend fun markAllDepositedForCollector(collectorId: String, depositedAt: Long = System.currentTimeMillis())

    @Query("SELECT SUM(amountPaid) FROM collector_deposit_logs WHERE collectorId = :collectorId AND isDepositedToAdmin = 0")
    fun getUndepositedTotalByCollector(collectorId: String): Flow<Double?>
}
