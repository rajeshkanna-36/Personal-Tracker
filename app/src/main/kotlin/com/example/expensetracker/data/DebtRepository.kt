package com.example.expensetracker.data

import com.example.expensetracker.data.local.DebtDao
import com.example.expensetracker.data.local.DebtEntity
import kotlinx.coroutines.flow.Flow

class DebtRepository(private val debtDao: DebtDao) {
    
    fun getAllDebts(): Flow<List<DebtEntity>> = debtDao.getAllDebts()

    suspend fun insertDebt(debt: DebtEntity) {
        debtDao.insertDebt(debt)
    }

    suspend fun updateDebt(debt: DebtEntity) {
        debtDao.updateDebt(debt)
    }

    suspend fun deleteDebt(debt: DebtEntity) {
        debtDao.deleteDebt(debt)
    }
}
