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
        remember(processId) { viewModel.getProcessById(processId) }.collectAsState(initial = null)
    } else {
        remember { mutableStateOf(null) }
    }

    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var budget by remember { mutableStateOf("") }
    var portfolioType by remember { mutableStateOf("General") }

    LaunchedEffect(process) {
        process?.let {
            name = it.name
            description = it.description
            budget = it.budget.toString()
            portfolioType = when (it.colorHex.uppercase()) {
                "#FF4CAF50" -> "Agriculture"
                "#FF2196F3" -> "Investment"
                else -> "General"
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (processId == null) "New Portfolio" else "Edit Portfolio") },
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
            // Portfolio Type Selector
            Text("Portfolio Type", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Agriculture" to "🌾", "Investment" to "📈", "General" to "📁").forEach { (type, emoji) ->
                    FilterChip(
                        selected = portfolioType == type,
                        onClick = { portfolioType = type },
                        label = { Text("$emoji $type") },
                        shape = RoundedCornerShape(16.dp),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            selectedLabelColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            }

            val commonTags = when (portfolioType) {
                "Agriculture" -> listOf("Corn", "Paddy", "Sugarcane", "Coconut", "Other")
                "Investment" -> listOf("Stocks", "Mutual Funds", "Crypto", "Real Estate", "Other")
                else -> emptyList()
            }

            if (commonTags.isNotEmpty()) {
                Text(
                    if (portfolioType == "Agriculture") "Crop Type" else "Asset Class", 
                    style = MaterialTheme.typography.labelLarge, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(commonTags) { tag ->
                        val isSelected = if (tag == "Other") {
                            name.isNotBlank() && name !in commonTags.filter { it != "Other" }
                        } else {
                            name == tag
                        }

                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (tag != "Other") {
                                    name = tag
                                } else {
                                    if (name in commonTags) {
                                        name = ""
                                    }
                                }
                            },
                            label = { Text(tag) },
                            shape = RoundedCornerShape(16.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        )
                    }
                }
            }

            val nameLabel = when (portfolioType) {
                "Agriculture" -> "Crop / Farm Name (e.g. 2026 Corn Crop)"
                "Investment" -> "Portfolio Name (e.g. Tech Stocks)"
                else -> "Project Name"
            }

            val budgetLabel = when (portfolioType) {
                "Agriculture" -> "Expected Budget (₹)"
                "Investment" -> "Target Capital (₹)"
                else -> "Budget (₹)"
            }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(nameLabel) },
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
                label = { Text(budgetLabel) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Button(
                onClick = {
                    val colorHex = when (portfolioType) {
                        "Agriculture" -> "#FF4CAF50"
                        "Investment" -> "#FF2196F3"
                        else -> "#FF6200EE"
                    }

                    val processEntity = ProcessEntity(
                        id = processId ?: 0L,
                        name = name,
                        description = description,
                        budget = budget.toDoubleOrNull() ?: 0.0,
                        createdAt = process?.createdAt ?: System.currentTimeMillis(),
                        colorHex = colorHex
                    )
                    viewModel.saveProcess(processEntity)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = name.isNotBlank()
            ) {
                Text(if (processId == null) "Create Portfolio" else "Save Changes", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (name.isBlank()) {
                Text(
                    "Please enter a name",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
        }
    }
}
