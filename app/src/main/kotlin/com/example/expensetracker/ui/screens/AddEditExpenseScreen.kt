package com.example.expensetracker.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.expensetracker.data.local.ExpenseEntity
import com.example.expensetracker.ui.viewmodel.ExpenseViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
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
    var expandedCategory by remember { mutableStateOf(false) }
    val categories = listOf("General", "Medicine", "Travel", "Food", "Groceries", "Entertainment", "Other")
    
    var expandedUnit by remember { mutableStateOf(false) }
    val units = listOf("No Unit", "Hrs", "Items", "Kg", "Liters", "Days")

    val existingExpense by if (expenseId != null) {
        viewModel.getExpenseById(expenseId).collectAsState(initial = null)
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

            // Category Dropdown
            ExposedDropdownMenuBox(
                expanded = expandedCategory,
                onExpandedChange = { expandedCategory = it },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = category,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedCategory,
                    onDismissRequest = { expandedCategory = false }
                ) {
                    categories.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                category = selectionOption
                                expandedCategory = false
                            }
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
            
            Button(
                onClick = {
                    val finalUnit = if (unit == "No Unit") null else unit
                    val expenseEntity = ExpenseEntity(
                        id = expenseId ?: 0L,
                        processId = preselectedProcessId,
                        description = description,
                        amount = amount.toDoubleOrNull() ?: 0.0,
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
    }
}
