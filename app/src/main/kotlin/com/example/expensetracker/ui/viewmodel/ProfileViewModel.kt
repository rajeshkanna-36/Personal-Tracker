package com.example.expensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.expensetracker.data.DebtRepository
import com.example.expensetracker.data.local.DebtEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ProfileViewModel(private val debtRepository: DebtRepository) : ViewModel() {

    val debts: StateFlow<List<DebtEntity>> = debtRepository.getAllDebts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun addDebt(personName: String, amount: Double, type: String, dueDate: Long?) {
        viewModelScope.launch {
            val newDebt = DebtEntity(
                personName = personName,
                amount = amount,
                type = type,
                dueDate = dueDate
            )
            debtRepository.insertDebt(newDebt)
        }
    }

    fun toggleSettleStatus(debt: DebtEntity) {
        viewModelScope.launch {
            debtRepository.updateDebt(debt.copy(isSettled = !debt.isSettled))
        }
    }

    fun deleteDebt(debt: DebtEntity) {
        viewModelScope.launch {
            debtRepository.deleteDebt(debt)
        }
    }
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as com.example.expensetracker.ExpenseTrackerApplication)
                ProfileViewModel(application.container.debtRepository)
            }
        }
    }
}
