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

    fun exportDataCsv(): String {
        val processes = allProcesses.value
        val expenses = allExpenses.value
        val csv = StringBuilder()
        csv.append("Type,ID,ProcessId/Name,Amount/Budget,Description,Date,Category,ExpenseType,ReceiptUri,Quantity,Unit\n")
        processes.forEach { p ->
            csv.append("Process,${p.id},${p.name},${p.budget},${p.description},${p.createdAt},N/A,N/A,N/A,N/A,N/A\n")
        }
        expenses.forEach { e ->
            csv.append("Expense,${e.id},${e.processId},${e.amount},${e.description},${e.date},${e.category},${e.type},${e.receiptUri ?: ""},${e.quantity ?: ""},${e.unit ?: ""}\n")
        }
        return csv.toString()
    }

    fun importDataCsv(csvData: String) {
        viewModelScope.launch {
            val lines = csvData.lines()
            if (lines.isEmpty()) return@launch
            
            lines.drop(1).forEach { line ->
                val parts = line.split(",")
                if (parts.size >= 7) {
                    val type = parts[0]
                    if (type == "Process") {
                        val process = ProcessEntity(
                            id = parts[1].toLongOrNull() ?: 0L,
                            name = parts[2],
                            budget = parts[3].toDoubleOrNull() ?: 0.0,
                            description = parts[4],
                            createdAt = parts[5].toLongOrNull() ?: System.currentTimeMillis()
                        )
                        repository.insertProcess(process)
                    } else if (type == "Expense" && parts.size >= 11) {
                        val expense = ExpenseEntity(
                            id = parts[1].toLongOrNull() ?: 0L,
                            processId = parts[2].toLongOrNull() ?: 0L,
                            amount = parts[3].toDoubleOrNull() ?: 0.0,
                            description = parts[4],
                            date = parts[5].toLongOrNull() ?: System.currentTimeMillis(),
                            category = parts[6],
                            type = parts[7],
                            receiptUri = parts[8].takeIf { it.isNotEmpty() },
                            quantity = parts[9].toDoubleOrNull(),
                            unit = parts[10].takeIf { it.isNotEmpty() }
                        )
                        repository.insertExpense(expense)
                    }
                }
            }
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
