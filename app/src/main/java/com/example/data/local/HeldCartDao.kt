package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.HeldCart
import kotlinx.coroutines.flow.Flow

@Dao
interface HeldCartDao {
    @Query("SELECT * FROM held_carts ORDER BY timestamp DESC")
    fun getAllHeldCarts(): Flow<List<HeldCart>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHeldCart(cart: HeldCart): Long

    @Delete
    suspend fun deleteHeldCart(cart: HeldCart)

    @Query("DELETE FROM held_carts WHERE id = :id")
    suspend fun deleteHeldCartById(id: Long)
}
