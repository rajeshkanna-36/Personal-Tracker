package com.example.expensetracker.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.example.expensetracker.AddEditExpense
import com.example.expensetracker.AddEditProcess
import com.example.expensetracker.R
import com.example.expensetracker.data.local.ExpenseEntity
import com.example.expensetracker.ui.viewmodel.ExpenseViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun getCropImageRes(cropName: String): Int {
    return when {
        cropName.contains("Corn", ignoreCase = true) -> R.drawable.crop_corn
        cropName.contains("Paddy", ignoreCase = true) -> R.drawable.crop_paddy
        cropName.contains("Sugarcane", ignoreCase = true) -> R.drawable.crop_sugarcane
        cropName.contains("Coconut", ignoreCase = true) -> R.drawable.crop_coconut
        else -> R.drawable.crop_default
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessDetailScreen(
    viewModel: ExpenseViewModel,
    processId: Long,
    onNavigate: (NavKey) -> Unit,
    onBack: () -> Unit
) {
    val process by viewModel.getProcessById(processId).collectAsState(initial = null)
    val expenses by viewModel.getExpensesForProcess(processId).collectAsState(initial = emptyList())
    val totalExpenses by viewModel.getTotalExpensesForProcess(processId).collectAsState(initial = 0.0)
    val totalIncome by viewModel.getTotalIncomeForProcess(processId).collectAsState(initial = 0.0)

    // Delete confirmation
    var expenseToDelete by remember { mutableStateOf<ExpenseEntity?>(null) }
    var showDeleteProject by remember { mutableStateOf(false) }
    var showOptionsMenu by remember { mutableStateOf(false) }

    val formatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Scrollable content
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // 1. Hero Image Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp)
                ) {
                    process?.let { p ->
                        Image(
                            painter = painterResource(id = getCropImageRes(p.name)),
                            contentDescription = "Crop Image",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        // Gradient Overlay for Text Readability
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.Black.copy(alpha = 0.3f),
                                            Color.Transparent,
                                            MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                                            MaterialTheme.colorScheme.background
                                        ),
                                        startY = 0f,
                                        endY = Float.POSITIVE_INFINITY
                                    )
                                )
                        )
                        // Header Text positioned at the bottom of the image area
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .padding(24.dp)
                        ) {
                            Text(
                                p.name,
                                color = Color.White,
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            // Status Pill
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Box(modifier = Modifier.padding(horizontal = 12.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        "Active Project",
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Stats Dashboard
            item {
                process?.let { p ->
                    val totalFunds = p.budget + totalIncome
                    val remaining = totalFunds - totalExpenses

                    val isAgriculture = p.colorHex.equals("#FF4CAF50", ignoreCase = true)
                    val isInvestment = p.colorHex.equals("#FF2196F3", ignoreCase = true)

                    val balanceLabel = when {
                        isAgriculture -> "Remaining Budget"
                        isInvestment -> "Remaining Target Capital"
                        else -> "Remaining Balance"
                    }

                    val budgetLabel = when {
                        isAgriculture -> "Expected Budget"
                        isInvestment -> "Target Capital"
                        else -> "Budget"
                    }

                    val incomeLabel = when {
                        isAgriculture -> "Yield"
                        isInvestment -> "Returns"
                        else -> "Income"
                    }

                    val spentLabel = when {
                        isAgriculture -> "Expenses"
                        isInvestment -> "Invested"
                        else -> "Spent"
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .offset(y = (-10).dp),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .animateContentSize()
                        ) {
                            Text(
                                balanceLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                formatter.format(remaining),
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (remaining < 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                StatPill(
                                    label = budgetLabel,
                                    value = formatter.format(p.budget),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    bgAlpha = 0.06f,
                                    modifier = Modifier.weight(1f)
                                )
                                if (totalIncome > 0) {
                                    StatPill(
                                        label = incomeLabel,
                                        value = "+${formatter.format(totalIncome)}",
                                        color = MaterialTheme.colorScheme.primary,
                                        bgAlpha = 0.1f,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                StatPill(
                                    label = spentLabel,
                                    value = formatter.format(totalExpenses),
                                    color = MaterialTheme.colorScheme.error,
                                    bgAlpha = 0.1f,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            // Expenses Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Transactions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { onNavigate(AddEditExpense(processId = processId)) },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                    ) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            if (expenses.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📋", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No expenses yet",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Tap + to record an expense",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                            )
                        }
                    }
                }
            } else {
                items(expenses, key = { it.id }) { expense ->
                    Box(modifier = Modifier.padding(horizontal = 20.dp)) {
                        SwipeToDeleteRow(
                            onDelete = { expenseToDelete = expense }
                        ) {
                            ExpenseItem(
                                expense = expense,
                                onClick = {
                                    onNavigate(AddEditExpense(expenseId = expense.id, processId = processId))
                                }
                            )
                        }
                    }
                }
            }
        }

        // Custom Floating Top Bar (Back & Delete Buttons)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back Button
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.3f),
                modifier = Modifier.size(48.dp)
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
            }

            Box {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.3f),
                    modifier = Modifier.size(48.dp)
                ) {
                    IconButton(onClick = { showOptionsMenu = true }) {
                        Icon(Icons.Rounded.MoreVert, contentDescription = "More Options", tint = Color.White)
                    }
                }
                DropdownMenu(
                    expanded = showOptionsMenu,
                    onDismissRequest = { showOptionsMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit Project") },
                        onClick = {
                            showOptionsMenu = false
                            onNavigate(AddEditProcess(processId = processId))
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete Project", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            showOptionsMenu = false
                            showDeleteProject = true
                        }
                    )
                }
            }
        }
    }

    // Dialogs remain unchanged
    expenseToDelete?.let { expense ->
        AlertDialog(
            onDismissRequest = { expenseToDelete = null },
            title = { Text("Delete Transaction") },
            text = { Text("Are you sure you want to delete \"${expense.description}\"?") },
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

    if (showDeleteProject) {
        AlertDialog(
            onDismissRequest = { showDeleteProject = false },
            title = { Text("Delete Project") },
            text = { Text("This will permanently delete \"${process?.name}\" and all its expenses. This cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        process?.let { viewModel.deleteProcess(it) }
                        showDeleteProject = false
                        onBack()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteProject = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun StatPill(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color,
    bgAlpha: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = bgAlpha)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = color,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ExpenseItem(
    expense: ExpenseEntity,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
    val isIncome = expense.type == "Income"
    val accentColor = if (isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    val sign = if (isIncome) "+" else "-"
    val emoji = when (expense.category) {
        "Materials" -> "🧱"
        "Labor" -> "👷"
        "Transport" -> "🚚"
        "Software" -> "💻"
        "General" -> if (isIncome) "💵" else "🛒"
        else -> "📦"
    }

    val formatter = remember {
        NumberFormat.getCurrencyInstance(Locale("en", "IN")).apply {
            maximumFractionDigits = 0
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            modifier = Modifier.size(44.dp),
            shape = CircleShape,
            color = accentColor.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(emoji, fontSize = 20.sp)
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                expense.description,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    "${expense.category} · ${dateFormat.format(Date(expense.date))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
                if (expense.quantity != null) {
                    Text(
                        "· ${expense.quantity} ${expense.unit ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
                    )
                }
            }
        }

        Text(
            "$sign${formatter.format(expense.amount)}",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = accentColor
        )
    }
}
