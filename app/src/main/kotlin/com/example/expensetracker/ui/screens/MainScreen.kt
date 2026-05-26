package com.example.expensetracker.ui.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.Workspaces
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.example.expensetracker.ui.viewmodel.ExpenseViewModel
import com.example.expensetracker.ui.viewmodel.HabitViewModel

@Composable
fun MainScreen(
    expenseViewModel: ExpenseViewModel,
    habitViewModel: HabitViewModel,
    isDarkTheme: Boolean = true,
    onThemeToggle: () -> Unit = {},
    onNavigate: (NavKey) -> Unit
) {
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Rounded.CalendarMonth, contentDescription = "Monthly") },
                    label = { Text("Monthly") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Rounded.Workspaces, contentDescription = "Projects") },
                    label = { Text("Projects") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Rounded.FavoriteBorder, contentDescription = "Habits") },
                    label = { Text("Habits") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    )
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Crossfade(
                targetState = selectedTab,
                animationSpec = tween(durationMillis = 300),
                label = "tab_crossfade"
            ) { targetTab ->
                when (targetTab) {
                    0 -> MonthlyExpensesScreen(viewModel = expenseViewModel, onNavigate = onNavigate)
                    1 -> DashboardScreen(
                        viewModel = expenseViewModel,
                        isDarkTheme = isDarkTheme,
                        onThemeToggle = onThemeToggle,
                        onNavigate = onNavigate
                    )
                    2 -> HabitTrackerScreen(
                        viewModel = habitViewModel,
                        onNavigate = onNavigate
                    )
                }
            }
        }
    }
}
