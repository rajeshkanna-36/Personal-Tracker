package com.example.expensetracker.data

import com.example.expensetracker.data.local.ExpenseDao
import com.example.expensetracker.data.local.ExpenseEntity
import com.example.expensetracker.data.local.ProcessEntity
import kotlinx.coroutines.flow.Flow

class ExpenseRepository(private val expenseDao: ExpenseDao) {

    val allProcesses: Flow<List<ProcessEntity>> = expenseDao.getAllProcesses()

    fun getProcessById(processId: Long): Flow<ProcessEntity?> {
        return expenseDao.getProcessById(processId)
    }

    suspend fun insertProcess(process: ProcessEntity): Long {
        return expenseDao.insertProcess(process)
    }

    suspend fun updateProcess(process: ProcessEntity) {
        expenseDao.updateProcess(process)
    }

    suspend fun deleteProcess(process: ProcessEntity) {
        expenseDao.deleteProcess(process)
    }

    fun getExpensesForProcess(processId: Long): Flow<List<ExpenseEntity>> {
        return expenseDao.getExpensesForProcess(processId)
    }
    
    fun getTotalExpensesForProcess(processId: Long): Flow<Double> {
        return expenseDao.getTotalExpensesForProcess(processId)
    }

    fun getTotalIncomeForProcess(processId: Long): Flow<Double> {
        return expenseDao.getTotalIncomeForProcess(processId)
    }

    // Global queries
    val recentExpenses: Flow<List<ExpenseEntity>> = expenseDao.getRecentExpenses(5)
    val allExpenses: Flow<List<ExpenseEntity>> = expenseDao.getAllExpenses()
    val totalGlobalExpenses: Flow<Double> = expenseDao.getTotalGlobalExpenses()
    val totalGlobalIncome: Flow<Double> = expenseDao.getTotalGlobalIncome()

    val generalExpenses: Flow<List<ExpenseEntity>> = expenseDao.getGeneralExpenses()
    val totalGeneralExpenses: Flow<Double> = expenseDao.getTotalGeneralExpenses()
    val totalGeneralIncome: Flow<Double> = expenseDao.getTotalGeneralIncome()

    suspend fun insertExpense(expense: ExpenseEntity): Long {
        return expenseDao.insertExpense(expense)
    }

    suspend fun updateExpense(expense: ExpenseEntity) {
        expenseDao.updateExpense(expense)
    }

    suspend fun deleteExpense(expense: ExpenseEntity) {
        expenseDao.deleteExpense(expense)
    }
}
