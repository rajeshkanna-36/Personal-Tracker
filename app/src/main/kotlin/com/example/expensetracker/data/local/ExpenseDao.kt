package com.example.expensetracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    // Process Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    suspend fun insertProcess(process: ProcessEntity): Long

    @Update
    @JvmSuppressWildcards
    suspend fun updateProcess(process: ProcessEntity): Int

    @Delete
    @JvmSuppressWildcards
    suspend fun deleteProcess(process: ProcessEntity): Int

    @Query("SELECT * FROM processes ORDER BY createdAt DESC")
    fun getAllProcesses(): Flow<List<ProcessEntity>>

    @Query("SELECT * FROM processes WHERE id = :processId")
    fun getProcessById(processId: Long): Flow<ProcessEntity?>

    // Expense Operations
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    @JvmSuppressWildcards
    suspend fun updateExpense(expense: ExpenseEntity): Int

    @Delete
    @JvmSuppressWildcards
    suspend fun deleteExpense(expense: ExpenseEntity): Int

    @Query("SELECT * FROM expenses WHERE processId = :processId ORDER BY date DESC")
    fun getExpensesForProcess(processId: Long): Flow<List<ExpenseEntity>>
    
    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE processId = :processId AND type = 'Expense'")
    fun getTotalExpensesForProcess(processId: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE processId = :processId AND type = 'Income'")
    fun getTotalIncomeForProcess(processId: Long): Flow<Double>

    // Global Queries for Dashboard
    @Query("SELECT * FROM expenses ORDER BY date DESC LIMIT :limit")
    fun getRecentExpenses(limit: Int = 5): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses ORDER BY date ASC") // ASC to draw chronological graph
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE type = 'Expense'")
    fun getTotalGlobalExpenses(): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE type = 'Income'")
    fun getTotalGlobalIncome(): Flow<Double>

    // General Expenses (No Process)
    @Query("SELECT * FROM expenses WHERE processId IS NULL ORDER BY date DESC")
    fun getGeneralExpenses(): Flow<List<ExpenseEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE processId IS NULL AND type = 'Expense'")
    fun getTotalGeneralExpenses(): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM expenses WHERE processId IS NULL AND type = 'Income'")
    fun getTotalGeneralIncome(): Flow<Double>
}
