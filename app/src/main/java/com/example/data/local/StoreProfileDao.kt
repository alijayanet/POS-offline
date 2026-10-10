package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.StoreProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreProfileDao {
    @Query("SELECT * FROM store_profile WHERE id = 1 LIMIT 1")
    fun getStoreProfile(): Flow<StoreProfile?>

    @Query("SELECT * FROM store_profile WHERE id = 1 LIMIT 1")
    suspend fun getStoreProfileSync(): StoreProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: StoreProfile)
}
