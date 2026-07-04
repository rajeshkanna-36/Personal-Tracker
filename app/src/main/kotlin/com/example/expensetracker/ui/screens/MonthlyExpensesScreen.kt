package com.example.expensetracker.ui.screens

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

import androidx.compose.material3.*

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.automirrored.rounded.TrendingDown
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ChevronLeft
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.filled.Person

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.NavKey
import com.example.expensetracker.AddEditExpense
import com.example.expensetracker.ExpenseTrackerApplication
import com.example.expensetracker.data.local.ExpenseEntity
import com.example.expensetracker.ui.components.CategoryDonutChart
import com.example.expensetracker.ui.components.categoryEmojis
import com.example.expensetracker.ui.viewmodel.ExpenseViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyExpensesScreen(
    viewModel: ExpenseViewModel,
    isDarkTheme: Boolean = true,
    onThemeToggle: () -> Unit = {},
    onNavigate: (NavKey) -> Unit
) {
    val generalExpenses by viewModel.generalExpenses.collectAsState()
    val totalGlobalExpenses by viewModel.totalGlobalExpenses.collectAsState()
    val totalGlobalIncome by viewModel.totalGlobalIncome.collectAsState()
    val totalGeneralExpenses by viewModel.totalGeneralExpenses.collectAsState()
    
    val context = LocalContext.current
    val appPreferences = remember { (context.applicationContext as ExpenseTrackerApplication).container.appPreferences }
    val initialWalletBalance by appPreferences.initialWalletBalanceFlow.collectAsState(initial = appPreferences.initialWalletBalance)

    val walletBalance = initialWalletBalance - totalGeneralExpenses

    var showAddCashDialog by remember { mutableStateOf(false) }
    var cashToAdd by remember { mutableStateOf("") }
    var cashError by remember { mutableStateOf(false) }

    // Generate last 24 months for the switcher
    val months = remember {
        (0..23).map { offset ->
            val cal = Calendar.getInstance()
            cal.add(Calendar.MONTH, -offset)
            Triple(cal.get(Calendar.MONTH), cal.get(Calendar.YEAR), cal.timeInMillis)
        }
    }
    var selectedMonthIndex by remember { mutableIntStateOf(0) }

    // Search state
    var searchQuery by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }

    // Delete confirmation
    var expenseToDelete by remember { mutableStateOf<ExpenseEntity?>(null) }
    
    // Filter expenses by selected month
    val selectedMonth = months[selectedMonthIndex]
    val filteredByMonth = remember(generalExpenses, selectedMonthIndex) {
        generalExpenses.filter { expense ->
            val cal = Calendar.getInstance().apply { timeInMillis = expense.date }
            cal.get(Calendar.MONTH) == selectedMonth.first && cal.get(Calendar.YEAR) == selectedMonth.second
        }
    }

    // Filter by search
    val displayedExpenses = remember(searchQuery, filteredByMonth) {
        if (searchQuery.isBlank()) filteredByMonth
        else filteredByMonth.filter {
            it.description.contains(searchQuery, ignoreCase = true) ||
            it.category.contains(searchQuery, ignoreCase = true)
        }
    }

    // Month totals
    val monthIncome = remember(filteredByMonth) { filteredByMonth.filter { it.type == "Income" }.sumOf { it.amount } }
    val monthExpenses = remember(filteredByMonth) { filteredByMonth.filter { it.type == "Expense" }.sumOf { it.amount } }
    val monthBalance = monthIncome - monthExpenses

    val formatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }
    }

    val fullDateFormat = remember { SimpleDateFormat("EEEE, dd MMM", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (showSearch) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search transactions...", style = MaterialTheme.typography.bodyMedium) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = MaterialTheme.typography.bodyMedium,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    } else {
                        Text("Monthly", fontWeight = FontWeight.ExtraBold, fontSize = 32.sp)
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigate(com.example.expensetracker.Profile) }) {
                        Icon(
                            Icons.Filled.Person,
                            contentDescription = "Profile",
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onThemeToggle) {
                        Icon(
                            if (isDarkTheme) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                            contentDescription = "Toggle Theme"
                        )
                    }
                    IconButton(onClick = {
                        showSearch = !showSearch
                        if (!showSearch) searchQuery = ""
                    }) {
                        Icon(
                            if (showSearch) Icons.Rounded.Close else Icons.Rounded.Search,
                            contentDescription = "Search"
                        )
                    }
                    if (!showSearch) {
                        IconButton(onClick = { onNavigate(AddEditExpense(processId = null)) }) {
                            Icon(Icons.Rounded.Add, contentDescription = "Add Transaction")
                        }
                        IconButton(onClick = { onNavigate(com.example.expensetracker.BackupRestore) }) {
                            Icon(Icons.Rounded.Settings, contentDescription = "Settings")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
        ) {
            // Month Switcher
            item {
                val monthFormat = remember { SimpleDateFormat("MMMM yyyy", Locale.getDefault()) }
                val currentMonthStr = remember(selectedMonth.third) { monthFormat.format(Date(selectedMonth.third)) }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (selectedMonthIndex < months.size - 1) selectedMonthIndex++ },
                        enabled = selectedMonthIndex < months.size - 1,
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(Icons.Rounded.ChevronLeft, contentDescription = "Previous Month")
                    }
                    
                    Text(
                        currentMonthStr,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    
                    IconButton(
                        onClick = { if (selectedMonthIndex > 0) selectedMonthIndex-- },
                        enabled = selectedMonthIndex > 0,
                        modifier = Modifier
                            .background(if (selectedMonthIndex > 0) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else Color.Transparent, CircleShape)
                            .size(36.dp)
                    ) {
                        if (selectedMonthIndex > 0) {
                            Icon(Icons.Rounded.ChevronRight, contentDescription = "Next Month")
                        }
                    }
                }
            }

            // Month Overview Hero Widget
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(32.dp),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                            .animateContentSize()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Column {
                                Text(
                                    "MONTH BALANCE",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    formatter.format(monthBalance),
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "WALLET",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            cashToAdd = if (initialWalletBalance == 0.0) "" else initialWalletBalance.toString()
                                            showAddCashDialog = true
                                        }
                                        .padding(4.dp)
                                ) {
                                    Text(
                                        formatter.format(walletBalance),
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Icon(
                                        Icons.Default.Edit, 
                                        contentDescription = "Edit Wallet", 
                                        modifier = Modifier.size(16.dp).padding(start = 4.dp), 
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Income Pill
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFF34C759).copy(alpha = 0.15f))
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF34C759).copy(alpha = 0.2f),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Rounded.TrendingUp,
                                        contentDescription = null,
                                        tint = Color(0xFF34C759),
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        "Income",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        formatter.format(monthIncome),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFF248A3D) // Darker green for text readability
                                    )
                                }
                            }
                            // Expense Pill
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFFFF3B30).copy(alpha = 0.15f))
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFFF3B30).copy(alpha = 0.2f),
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Rounded.TrendingDown,
                                        contentDescription = null,
                                        tint = Color(0xFFFF3B30),
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        "Spent",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        formatter.format(monthExpenses),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = Color(0xFFC41C13) // Darker red for text readability
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Category Donut Chart Widget
            if (filteredByMonth.any { it.type == "Expense" }) {
                item {
                    ElevatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(32.dp),
                        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "By Category",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            CategoryDonutChart(expenses = filteredByMonth)
                        }
                    }
                }
            }

            // Transactions Header
            item {
                Text(
                    "Transactions",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (displayedExpenses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("??", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                if (searchQuery.isNotBlank()) "No results found" else "No transactions this month",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                if (searchQuery.isNotBlank()) "Try a different search" else "Tap + to add an entry",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            } else {
                val groupedExpenses = displayedExpenses.groupBy { expense ->
                    val cal = Calendar.getInstance().apply { timeInMillis = expense.date }
                    cal.set(Calendar.HOUR_OF_DAY, 0)
                    cal.set(Calendar.MINUTE, 0)
                    cal.set(Calendar.SECOND, 0)
                    cal.set(Calendar.MILLISECOND, 0)
                    cal.timeInMillis
                }.toSortedMap(compareByDescending { it })

                groupedExpenses.forEach { (dateMillis, expensesForDate) ->
                    item {
                        val headerText = remember(dateMillis) {
                            val cal = Calendar.getInstance().apply { timeInMillis = dateMillis }
                            val today = Calendar.getInstance()
                            val yesterday = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -1) }
                            
                            val isToday = cal.get(Calendar.YEAR) == today.get(Calendar.YEAR) && cal.get(Calendar.DAY_OF_YEAR) == today.get(Calendar.DAY_OF_YEAR)
                            val isYesterday = cal.get(Calendar.YEAR) == yesterday.get(Calendar.YEAR) && cal.get(Calendar.DAY_OF_YEAR) == yesterday.get(Calendar.DAY_OF_YEAR)

                            when {
                                isToday -> "Today"
                                isYesterday -> "Yesterday"
                                else -> fullDateFormat.format(Date(dateMillis))
                            }
                        }

                        Text(
                            text = headerText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 8.dp)
                        )
                    }

                    items(expensesForDate, key = { it.id }) { expense ->
                        SwipeToDeleteRow(
                            onDelete = { expenseToDelete = expense }
                        ) {
                            MonthlyExpenseRow(
                                expense = expense,
                                onClick = {
                                    onNavigate(AddEditExpense(expenseId = expense.id, processId = null))
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }

    // Delete confirmation dialog
    expenseToDelete?.let { expense ->
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure you want to delete \"\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteExpense(expense)
                        expenseToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { expenseToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showAddCashDialog) {
        AlertDialog(
            onDismissRequest = { showAddCashDialog = false },
            title = { Text("Edit Initial Wallet Balance") },
            text = {
                OutlinedTextField(
                    value = cashToAdd,
                    onValueChange = { 
                        cashToAdd = it
                        cashError = it.toDoubleOrNull() == null && it.isNotBlank()
                    },
                    label = { Text("Initial Cash Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError = cashError,
                    singleLine = true,
                    supportingText = if (cashError) { { Text("Please enter a valid number") } } else null
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val newAmount = cashToAdd.toDoubleOrNull() ?: 0.0
                        appPreferences.initialWalletBalance = newAmount
                        showAddCashDialog = false
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddCashDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteRow(
    onDelete: () -> Unit,
    content: @Composable () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                false // Don't dismiss instantly, wait for dialog confirmation
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val isSwiping = dismissState.targetValue != SwipeToDismissBoxValue.Settled
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSwiping) MaterialTheme.colorScheme.errorContainer else Color.Transparent),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (isSwiping) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "Delete",
                        modifier = Modifier.padding(end = 24.dp),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        },
        enableDismissFromStartToEnd = false,
        content = { content() }
    )
}

@Composable
fun MonthlyExpenseRow(
    expense: ExpenseEntity,
    onClick: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }
    val isIncome = expense.type == "Income"
    val accentColor = if (isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
    val sign = if (isIncome) "+" else "-"
    val emoji = if (expense.category == "General" && isIncome) "💵" else (categoryEmojis[expense.category] ?: "📦")

    val formatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.size(46.dp),
                shape = CircleShape,
                color = accentColor.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(emoji, fontSize = 22.sp)
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    expense.description,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    "${expense.category} · ${dateFormat.format(Date(expense.date))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            Text(
                "$sign${formatter.format(expense.amount)}",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                color = accentColor
            )
        }
    }
}
