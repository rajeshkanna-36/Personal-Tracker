package com.example.expensetracker

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.expensetracker.ui.screens.*
import com.example.expensetracker.ui.viewmodel.ExpenseViewModel
import com.example.expensetracker.ui.viewmodel.HabitViewModel

@Composable
fun MainNavigation(
    isDarkTheme: Boolean = true,
    onThemeToggle: () -> Unit = {}
) {
  val backStack = rememberNavBackStack(MainApp)
  val expenseViewModel: ExpenseViewModel = viewModel(factory = ExpenseViewModel.Factory)
  val habitViewModel: HabitViewModel = viewModel(factory = HabitViewModel.Factory)

  NavDisplay(
    backStack = backStack,
    onBack = { backStack.removeLastOrNull() },
    entryProvider =
      entryProvider {
        entry<MainApp> {
          MainScreen(
            expenseViewModel = expenseViewModel,
            habitViewModel = habitViewModel,
            isDarkTheme = isDarkTheme,
            onThemeToggle = onThemeToggle,
            onNavigate = { key -> backStack.add(key) }
          )
        }
        entry<ProcessDetail> { key ->
          ProcessDetailScreen(
            viewModel = expenseViewModel,
            processId = key.processId,
            onNavigate = { nextKey -> backStack.add(nextKey) },
            onBack = { backStack.removeLastOrNull() }
          )
        }
        entry<AddEditProcess> { key ->
          AddEditProcessScreen(
            viewModel = expenseViewModel,
            processId = key.processId,
            onBack = { backStack.removeLastOrNull() }
          )
        }
        entry<AddEditExpense> { key ->
          AddEditExpenseScreen(
            viewModel = expenseViewModel,
            expenseId = key.expenseId,
            preselectedProcessId = key.processId,
            onBack = { backStack.removeLastOrNull() }
          )
        }
        entry<BackupRestore> {
          BackupRestoreScreen(
            viewModel = expenseViewModel,
            onBack = { backStack.removeLastOrNull() }
          )
        }
        entry<AddEditHabit> { key ->
          AddEditHabitScreen(
            viewModel = habitViewModel,
            habitId = key.habitId,
            onBack = { backStack.removeLastOrNull() }
          )
        }
      },
  )
}
