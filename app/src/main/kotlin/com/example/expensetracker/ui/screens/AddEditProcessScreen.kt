package com.example.expensetracker.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.expensetracker.data.local.ProcessEntity
import com.example.expensetracker.ui.viewmodel.ExpenseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditProcessScreen(
    viewModel: ExpenseViewModel,
    processId: Long?,
    onBack: () -> Unit
) {
    val process by if (processId != null) {
        viewModel.getProcessById(processId).collectAsState(initial = null)
    } else {
        remember { mutableStateOf(null) }
    }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }

    LaunchedEffect(process) {
        process?.let {
            name = it.name
            description = it.description
            budget = it.budget.toString()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (processId == null) "New Project" else "Edit Project") },
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
            val commonCrops = listOf("Corn", "Paddy", "Sugarcane", "Coconut", "Other")

            Text("Crop Type", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(commonCrops) { crop ->
                    val isSelected = if (crop == "Other") {
                        name.isNotBlank() && name !in listOf("Corn", "Paddy", "Sugarcane", "Coconut")
                    } else {
                        name == crop
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            if (crop != "Other") {
                                name = crop
                            } else {
                                if (name in listOf("Corn", "Paddy", "Sugarcane", "Coconut")) {
                                    name = ""
                                }
                            }
                        },
                        label = { Text(crop) },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Project Name (e.g. 2026 Corn Crop)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                minLines = 3
            )
            
            OutlinedTextField(
                value = budget,
                onValueChange = { budget = it },
                label = { Text("Budget (₹)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    val processEntity = ProcessEntity(
                        id = processId ?: 0L,
                        name = name,
                        description = description,
                        budget = budget.toDoubleOrNull() ?: 0.0,
                        createdAt = process?.createdAt ?: System.currentTimeMillis()
                    )
                    viewModel.saveProcess(processEntity)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(if (processId == null) "Create Project" else "Save Changes", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
