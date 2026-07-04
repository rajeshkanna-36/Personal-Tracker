package com.example.expensetracker

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object MainApp : NavKey
@Serializable data class ProcessDetail(val processId: Long) : NavKey
@Serializable data class AddEditProcess(val processId: Long? = null) : NavKey
@Serializable data class AddEditExpense(val expenseId: Long? = null, val processId: Long? = null) : NavKey
@Serializable data class AddEditHabit(val habitId: Long? = null) : NavKey
@Serializable data object BackupRestore : NavKey
@Serializable data object Profile : NavKey
