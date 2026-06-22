package com.example.expensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.expensetracker.ExpenseTrackerApplication
import com.example.expensetracker.data.BackupManager
import com.example.expensetracker.data.ExpenseRepository
import com.example.expensetracker.data.local.ExpenseEntity
import com.example.expensetracker.data.local.ProcessEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ExpenseViewModel(
    private val repository: ExpenseRepository,
    private val backupManager: BackupManager
) : ViewModel() {

    val allProcesses: StateFlow<List<ProcessEntity>> = repository.allProcesses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val recentExpenses: StateFlow<List<ExpenseEntity>> = repository.recentExpenses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allExpenses: StateFlow<List<ExpenseEntity>> = repository.allExpenses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalGlobalExpenses: StateFlow<Double> = repository.totalGlobalExpenses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val totalGlobalIncome: StateFlow<Double> = repository.totalGlobalIncome
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val generalExpenses: StateFlow<List<ExpenseEntity>> = repository.generalExpenses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val totalGeneralExpenses: StateFlow<Double> = repository.totalGeneralExpenses
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    val totalGeneralIncome: StateFlow<Double> = repository.totalGeneralIncome
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )

    fun getProcessById(processId: Long): StateFlow<ProcessEntity?> {
        return repository.getProcessById(processId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    }

    fun getExpenseById(expenseId: Long): StateFlow<ExpenseEntity?> {
        return repository.getExpenseById(expenseId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    }

    fun getExpensesForProcess(processId: Long): StateFlow<List<ExpenseEntity>> {
        return repository.getExpensesForProcess(processId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    }
    
    fun getTotalExpensesForProcess(processId: Long): StateFlow<Double> {
        return repository.getTotalExpensesForProcess(processId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )
    }

    fun getTotalIncomeForProcess(processId: Long): StateFlow<Double> {
        return repository.getTotalIncomeForProcess(processId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0.0
        )
    }

    fun saveProcess(process: ProcessEntity) {
        viewModelScope.launch {
            if (process.id == 0L) {
                repository.insertProcess(process)
            } else {
                repository.updateProcess(process)
            }
            backupManager.triggerAutoBackup()
        }
    }

    fun deleteProcess(process: ProcessEntity) {
        viewModelScope.launch {
            repository.deleteProcess(process)
            backupManager.triggerAutoBackup()
        }
    }

    fun saveExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            if (expense.id == 0L) {
                repository.insertExpense(expense)
            } else {
                repository.updateExpense(expense)
            }
            backupManager.triggerAutoBackup()
        }
    }

    fun deleteExpense(expense: ExpenseEntity) {
        viewModelScope.launch {
            repository.deleteExpense(expense)
            backupManager.triggerAutoBackup()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as ExpenseTrackerApplication)
                ExpenseViewModel(
                    application.container.expenseRepository,
                    application.container.backupManager
                )
            }
        }
    }
}
