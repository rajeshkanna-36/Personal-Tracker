package com.example.expensetracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryDao {
    @Query("SELECT * FROM memories ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<MemoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    suspend fun insertMemory(memory: MemoryEntity): Long

    @Update
    @JvmSuppressWildcards
    suspend fun updateMemory(memory: MemoryEntity): Int

    @Delete
    @JvmSuppressWildcards
    suspend fun deleteMemory(memory: MemoryEntity): Int
}
