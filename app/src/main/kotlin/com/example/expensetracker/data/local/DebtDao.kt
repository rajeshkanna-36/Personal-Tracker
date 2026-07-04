package com.example.expensetracker.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface DebtDao {
    @Query("SELECT * FROM debts ORDER BY isSettled ASC, dateAdded DESC")
    fun getAllDebts(): Flow<List<DebtEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    suspend fun insertDebt(debt: DebtEntity): Long

    @Update
    @JvmSuppressWildcards
    suspend fun updateDebt(debt: DebtEntity): Int

    @Delete
    @JvmSuppressWildcards
    suspend fun deleteDebt(debt: DebtEntity): Int
}
