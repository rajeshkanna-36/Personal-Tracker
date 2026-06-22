package com.example.expensetracker.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.example.expensetracker.AddEditProcess
import com.example.expensetracker.ProcessDetail
import com.example.expensetracker.data.local.ProcessEntity
import com.example.expensetracker.data.local.ExpenseEntity
import com.example.expensetracker.ui.viewmodel.ExpenseViewModel
import com.example.expensetracker.ui.components.ExpenseGraph
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: ExpenseViewModel,
    isDarkTheme: Boolean = true,
    onThemeToggle: () -> Unit = {},
    onNavigate: (NavKey) -> Unit
) {
    val processes by viewModel.allProcesses.collectAsState()
    val allExpenses by viewModel.allExpenses.collectAsState()
    val recentExpenses by viewModel.recentExpenses.collectAsState()
    val totalGlobalExpenses by viewModel.totalGlobalExpenses.collectAsState()
    val totalGlobalIncome by viewModel.totalGlobalIncome.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Portfolios", fontWeight = FontWeight.ExtraBold, fontSize = 32.sp)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                actions = {
                    IconButton(onClick = onThemeToggle) {
                        Icon(
                            if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                            contentDescription = "Toggle Theme"
                        )
                    }
                    IconButton(onClick = { onNavigate(AddEditProcess(null)) }) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add Portfolio")
                    }
                    IconButton(onClick = { onNavigate(com.example.expensetracker.BackupRestore) }) {
                        Icon(Icons.Rounded.Settings, contentDescription = "Settings")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
        ) {
            // Graph Section
            item {
                Text(
                    "Weekly Activity",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Box(modifier = Modifier.padding(16.dp)) {
                        ExpenseGraph(expenses = allExpenses)
                    }
                }
            }

            // Projects Section
            item {
                Text(
                    "Portfolios & Projects",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (processes.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📁", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "No projects yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Tap + to create your first project",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            } else {
                items(processes) { process ->
                    val processExpenses = allExpenses.filter { it.processId == process.id }
                    ProcessCard(process = process, expenses = processExpenses, onClick = {
                        onNavigate(ProcessDetail(process.id))
                    })
                }
            }

            // Recent Transactions Section
            item {
                Text(
                    "Recent",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (recentExpenses.isEmpty()) {
                item {
                    Text(
                        "No recent transactions.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(recentExpenses) { expense ->
                    RecentExpenseItem(expense)
                }
            }
        }
    }
}

@Composable
fun ProcessCard(process: ProcessEntity, expenses: List<ExpenseEntity>, onClick: () -> Unit) {
    val income = expenses.filter { it.type == "Income" }.sumOf { it.amount }
    val spent = expenses.filter { it.type == "Expense" }.sumOf { it.amount }
    val profit = income - spent
    
    val budgetProgress = if (process.budget > 0) (spent / process.budget).toFloat().coerceIn(0f, 1f) else 0f

    val isAgriculture = process.colorHex.equals("#FF4CAF50", ignoreCase = true)
    val isInvestment = process.colorHex.equals("#FF2196F3", ignoreCase = true)

    val emoji = when {
        isAgriculture -> "🚜"
        isInvestment -> "📈"
        else -> process.name.take(1).uppercase()
    }

    val typeColor = when {
        isAgriculture -> Color(0xFF4CAF50)
        isInvestment -> Color(0xFF2196F3)
        else -> MaterialTheme.colorScheme.primary
    }

    val profitLabel = when {
        isAgriculture -> "Yield"
        isInvestment -> "Returns"
        else -> "Profit"
    }

    val spentLabel = when {
        isAgriculture -> "Expenses"
        isInvestment -> "Invested"
        else -> "Spent"
    }

    val budgetLabel = when {
        isAgriculture -> "Expected Budget"
        isInvestment -> "Target Capital"
        else -> "Budget"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Modern Initial Avatar
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = typeColor.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = emoji,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = typeColor
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        process.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        if (process.description.isNotBlank()) process.description else "Active Portfolio",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Profit tag
                val profitColor = if (profit >= 0) typeColor else MaterialTheme.colorScheme.error
                val profitBg = if (profit >= 0) typeColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.error.copy(alpha = 0.15f)
                val profitSign = if (profit > 0) "+" else ""
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        profitLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = profitBg
                    ) {
                        Text(
                            "$profitSign₹${String.format("%.0f", profit)}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            color = profitColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Budget tracking
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "$spentLabel: ₹${String.format("%.0f", spent)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "$budgetLabel: ₹${String.format("%.0f", process.budget)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LinearProgressIndicator(
                progress = { budgetProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (budgetProgress >= 1f) MaterialTheme.colorScheme.error else typeColor,
                trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )
        }
    }
}

@Composable
fun RecentExpenseItem(expense: ExpenseEntity) {
    val isIncome = expense.type == "Income"
    val color = if (isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val sign = if (isIncome) "+" else "-"
    val emoji = when (expense.category) {
        "Materials" -> "🧱"
        "Labor" -> "👷"
        "Transport" -> "🚚"
        "Software" -> "💻"
        "General" -> if (isIncome) "💵" else "🛒"
        else -> "📦"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = color.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(emoji, fontSize = 18.sp)
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                expense.description,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1
            )
            Text(
                "${expense.category} · ${SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(expense.date))}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }

        Text(
            "$sign₹${String.format("%.0f", expense.amount)}",
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = color
        )
    }
}
