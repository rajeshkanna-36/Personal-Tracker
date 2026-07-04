package com.example.expensetracker.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.data.local.ExpenseEntity
import com.example.expensetracker.ui.viewmodel.ExpenseViewModel
import com.example.expensetracker.ui.components.categoryColors
import com.example.expensetracker.ui.components.categoryEmojis
import androidx.compose.ui.platform.LocalContext
import com.example.expensetracker.ExpenseTrackerApplication
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddEditExpenseScreen(
    viewModel: ExpenseViewModel,
    expenseId: Long?,
    preselectedProcessId: Long?,
    onBack: () -> Unit
) {
    var description by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("Expense") }
    var category by remember { mutableStateOf("General") }
    var receiptUri by remember { mutableStateOf<String?>(null) }
    
    // New Quantity and Unit fields
    var quantity by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("No Unit") }

    // Date Picker State
    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
    var selectedDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    // Dropdown States
    var showCategorySheet by remember { mutableStateOf(false) }
    
    val process by if (preselectedProcessId != null) {
        remember(preselectedProcessId) { viewModel.getProcessById(preselectedProcessId) }.collectAsState(initial = null)
    } else {
        remember { mutableStateOf(null) }
    }

    val categories = remember(process) {
        if (process != null) {
            val p = process!!
            val isAgriculture = p.colorHex.equals("#FF4CAF50", ignoreCase = true) || 
                                p.name.contains("Agriculture", ignoreCase = true) || 
                                p.name.contains("Corn", ignoreCase = true) || 
                                p.name.contains("Paddy", ignoreCase = true) || 
                                p.name.contains("Sugarcane", ignoreCase = true) || 
                                p.name.contains("Coconut", ignoreCase = true)
            val isInvestment = p.colorHex.equals("#FF2196F3", ignoreCase = true) || 
                               p.name.contains("Investment", ignoreCase = true)
            
            when {
                isAgriculture -> listOf("Seeds", "Fertilizer", "Labor", "Equipment", "Pesticides", "Transport", "Other")
                isInvestment -> listOf("Stocks", "Mutual Funds", "Real Estate", "Bonds", "Crypto", "Other")
                else -> listOf("Materials", "Labor", "Transport", "Software", "General", "Other")
            }
        } else {
            listOf("General", "Medicine", "Travel", "Food", "Groceries", "Entertainment", "Other")
        }
    }
    
    LaunchedEffect(categories) {
        if (category !in categories) {
            category = categories.first()
        }
    }
    
    var expandedUnit by remember { mutableStateOf(false) }
    val units = listOf("No Unit", "Hrs", "Items", "Kg", "Liters", "Days")

    val existingExpense by if (expenseId != null) {
        remember(expenseId) { viewModel.getExpenseById(expenseId) }.collectAsState(initial = null)
    } else {
        remember { mutableStateOf(null) }
    }

    LaunchedEffect(existingExpense) {
        existingExpense?.let { existing ->
            description = existing.description
            amount = existing.amount.toString()
            type = existing.type
            category = existing.category
            receiptUri = existing.receiptUri
            quantity = existing.quantity?.toString() ?: ""
            unit = existing.unit ?: "No Unit"
            selectedDateMillis = existing.date
            datePickerState.selectedDateMillis = existing.date
        }
    }

    LaunchedEffect(datePickerState.selectedDateMillis) {
        datePickerState.selectedDateMillis?.let { selectedDateMillis = it }
    }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { receiptUri = it.toString() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (expenseId == null) "Add Transaction" else "Edit Transaction") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Type Toggle
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = type == "Expense",
                    onClick = { type = "Expense" },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("Expense")
                }
                SegmentedButton(
                    selected = type == "Income",
                    onClick = { type = "Income" },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text("Income/Funding")
                }
            }

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Total Amount (₹)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Qty/Time (Opt)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                ExposedDropdownMenuBox(
                    expanded = expandedUnit,
                    onExpandedChange = { expandedUnit = it },
                    modifier = Modifier.weight(1f)
                ) {
                    OutlinedTextField(
                        value = unit,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Unit") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnit) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedUnit,
                        onDismissRequest = { expandedUnit = false }
                    ) {
                        units.forEach { selectionOption ->
                            DropdownMenuItem(
                                text = { Text(selectionOption) },
                                onClick = {
                                    unit = selectionOption
                                    expandedUnit = false
                                }
                            )
                        }
                    }
                }
            }

            // Category Selection (iOS Style Menu Row)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCategorySheet = true },
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Category",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val currentEmoji = categoryEmojis[category] ?: "📦"
                        val currentColor = categoryColors[category] ?: MaterialTheme.colorScheme.primary
                        Text(currentEmoji, fontSize = 18.sp)
                        Text(
                            category,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = currentColor
                        )
                        Icon(
                            Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // Date Selection
            OutlinedTextField(
                value = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(selectedDateMillis)),
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                trailingIcon = { Icon(Icons.Rounded.CalendarMonth, contentDescription = "Select Date") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                shape = RoundedCornerShape(12.dp)
            )

            // Receipt Attachment
            Button(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant)
            ) {
                Icon(Icons.Rounded.Image, contentDescription = "Attach Receipt", modifier = Modifier.padding(end = 8.dp))
                Text(if (receiptUri == null) "Attach Receipt (Optional)" else "Receipt Attached!")
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            val context = LocalContext.current
            val appPreferences = remember { (context.applicationContext as ExpenseTrackerApplication).container.appPreferences }
            val initialWalletBalance by appPreferences.initialWalletBalanceFlow.collectAsState(initial = appPreferences.initialWalletBalance)
            val totalGeneralExpenses by viewModel.totalGeneralExpenses.collectAsState()
            val walletBalance = initialWalletBalance - totalGeneralExpenses
            
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    if (preselectedProcessId == null && type == "Expense" && amountValue > walletBalance) {
                        android.widget.Toast.makeText(context, "Insufficient Wallet Balance!", android.widget.Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    val finalUnit = if (unit == "No Unit") null else unit
                    val expenseEntity = ExpenseEntity(
                        id = expenseId ?: 0L,
                        processId = preselectedProcessId,
                        description = description,
                        amount = amountValue,
                        date = selectedDateMillis,
                        category = category,
                        type = type,
                        receiptUri = receiptUri,
                        quantity = quantity.toDoubleOrNull(),
                        unit = finalUnit
                    )
                    viewModel.saveExpense(expenseEntity)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = description.isNotBlank() && amount.isNotBlank()
            ) {
                Text("Save Transaction", style = MaterialTheme.typography.titleMedium)
            }
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        // iOS Style Category Bottom Sheet
        if (showCategorySheet) {
            ModalBottomSheet(
                onDismissRequest = { showCategorySheet = false },
                containerColor = MaterialTheme.colorScheme.surface,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Text(
                        "Select Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyColumn {
                        items(categories) { cat ->
                            val isSelected = category == cat
                            val emoji = categoryEmojis[cat] ?: "📦"
                            val catColor = categoryColors[cat] ?: MaterialTheme.colorScheme.primary

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        category = cat
                                        showCategorySheet = false
                                    }
                                    .padding(horizontal = 24.dp, vertical = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = catColor.copy(alpha = 0.1f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(emoji, fontSize = 20.sp)
                                        }
                                    }
                                    Text(
                                        cat,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) catColor else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                if (isSelected) {
                                    Icon(
                                        Icons.Rounded.Check,
                                        contentDescription = "Selected",
                                        tint = catColor,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
