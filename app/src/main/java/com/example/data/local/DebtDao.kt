package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DebtRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {
    @Query("SELECT * FROM debt_records ORDER BY createdAt DESC")
    fun getAllDebts(): Flow<List<DebtRecord>>

    @Query("SELECT * FROM debt_records WHERE isSettled = 0 ORDER BY createdAt DESC")
    fun getUnsettledDebts(): Flow<List<DebtRecord>>

    @Query("SELECT * FROM debt_records WHERE customerId = :customerId ORDER BY createdAt DESC")
    fun getDebtsByCustomer(customerId: Long): Flow<List<DebtRecord>>

    @Query("SELECT * FROM debt_records WHERE id = :id LIMIT 1")
    suspend fun getDebtById(id: Long): DebtRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebt(debt: DebtRecord): Long

    @Update
    suspend fun updateDebt(debt: DebtRecord)

    @Query("UPDATE debt_records SET amountPaid = amountPaid + :payment, remainingDebt = remainingDebt - :payment, isSettled = CASE WHEN (remainingDebt - :payment) <= 0 THEN 1 ELSE 0 END, settledAt = CASE WHEN (remainingDebt - :payment) <= 0 THEN :timestamp ELSE settledAt END WHERE id = :id")
    suspend fun payDebt(id: Long, payment: Double, timestamp: Long = System.currentTimeMillis())

    @Delete
    suspend fun deleteDebt(debt: DebtRecord)

    @Query("SELECT * FROM debt_records")
    suspend fun getAllDebtsSync(): List<DebtRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDebts(debts: List<DebtRecord>)

    @Query("DELETE FROM debt_records")
    suspend fun clearAllDebts()
}
