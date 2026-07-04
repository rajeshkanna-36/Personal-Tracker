package com.example.expensetracker.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.data.local.HabitEntity
import com.example.expensetracker.ui.components.habitIconsList
import com.example.expensetracker.ui.components.getHabitIcon
import com.example.expensetracker.ui.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHabitScreen(
    viewModel: HabitViewModel,
    habitId: Long?,
    onBack: () -> Unit
) {
    val habit by if (habitId != null) {
        remember(habitId) { viewModel.getHabitById(habitId) }.collectAsState(initial = null)
    } else {
        remember { mutableStateOf(null) }
    }

    var name by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("target") }
    var category by remember { mutableStateOf("Other") }
    var selectedDays by remember { mutableStateOf(setOf(0, 1, 2, 3, 4, 5, 6)) }
    var durationMinutes by remember { mutableStateOf("15") }
    var colorHex by remember { mutableStateOf("#4F46E5") } // Default Indigo
    var reminderTime by remember { mutableStateOf("") }
    var showTimePicker by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showTimePicker = true
        }
    }

    LaunchedEffect(habit) {
        habit?.let {
            name = it.name
            icon = it.icon
            category = it.category
            selectedDays = it.targetDays.split(",").mapNotNull { day -> day.toIntOrNull() }.toSet()
            durationMinutes = it.durationMinutes?.toString() ?: ""
            colorHex = it.colorHex
            reminderTime = it.reminderTime ?: ""
        }
    }

    // Icon list from shared registry
    val categories = listOf("Health", "Fitness", "Mindfulness", "Productivity", "Other")
    val colors = listOf("#4F46E5", "#059669", "#E11D48", "#D97706", "#9333EA", "#5A3114")
    val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (habitId == null) "New Habit" else "Edit Habit") },
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
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Name Input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Habit Name (e.g. Drink Water)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Icon Picker - Apple Fitness Style
            Column {
                Text("Select Icon", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(12.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    modifier = Modifier.height(200.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(habitIconsList) { item ->
                        val isSelected = item.key == icon
                        val accentColor = try {
                            Color(android.graphics.Color.parseColor(colorHex))
                        } catch (e: Exception) {
                            MaterialTheme.colorScheme.primary
                        }
                        Box(
                            modifier = Modifier
                                .size(52.dp)
                                .clip(CircleShape)
                                .background(
                                    brush = if (isSelected) Brush.radialGradient(
                                        colors = listOf(
                                            accentColor.copy(alpha = 0.9f),
                                            accentColor
                                        ),
                                        radius = 80f
                                    ) else Brush.radialGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            MaterialTheme.colorScheme.surfaceVariant
                                        )
                                    )
                                )
                                .then(
                                    if (isSelected) Modifier.border(2.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                                    else Modifier
                                )
                                .clickable { icon = item.key },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }

            // Category Picker
            Column {
                Text("Category", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) },
                            shape = RoundedCornerShape(16.dp)
                        )
                    }
                }
            }

            // Target Days
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Target Days", style = MaterialTheme.typography.labelLarge)
                    TextButton(onClick = { selectedDays = setOf(0, 1, 2, 3, 4, 5, 6) }) {
                        Text("Everyday")
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    daysOfWeek.forEachIndexed { index, day ->
                        val isSelected = selectedDays.contains(index)
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable(
                                    role = androidx.compose.ui.semantics.Role.Checkbox,
                                    onClickLabel = "Toggle $day"
                                ) {
                                    selectedDays = if (isSelected) {
                                        selectedDays - index
                                    } else {
                                        selectedDays + index
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Reminder Time Input
            val onTimeClick = {
                val showDialog = {
                    showTimePicker = true
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    if (androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.POST_NOTIFICATIONS
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        showDialog()
                    } else {
                        try {
                            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        } catch (e: Exception) {
                            showDialog()
                        }
                    }
                } else {
                    showDialog()
                }
            }
            
            // Format reminder time for display
            val displayTime = if (reminderTime.isNotBlank()) {
                val parts = reminderTime.split(":")
                if (parts.size == 2) {
                    val hour24 = parts[0].toIntOrNull() ?: 12
                    val minute = parts[1].toIntOrNull() ?: 0
                    val amPm = if (hour24 >= 12) "PM" else "AM"
                    val hour12 = if (hour24 % 12 == 0) 12 else hour24 % 12
                    "${hour12.toString().padStart(2, '0')}:${minute.toString().padStart(2, '0')} $amPm"
                } else reminderTime
            } else ""

            OutlinedTextField(
                value = displayTime,
                onValueChange = { },
                label = { Text("Reminder Time (optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = onTimeClick
                    ),
                shape = RoundedCornerShape(12.dp),
                enabled = false, // Disable typing, open picker on click
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                singleLine = true
            )

            if (showTimePicker) {
                val calendar = java.util.Calendar.getInstance()
                val parts = if (reminderTime.isNotBlank()) reminderTime.split(":") else emptyList()
                val initialHour24 = parts.getOrNull(0)?.toIntOrNull() ?: calendar.get(java.util.Calendar.HOUR_OF_DAY)
                val initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: calendar.get(java.util.Calendar.MINUTE)

                val timePickerState = rememberTimePickerState(
                    initialHour = initialHour24,
                    initialMinute = initialMinute,
                    is24Hour = false
                )

                AlertDialog(
                    onDismissRequest = { showTimePicker = false },
                    text = {
                        TimePicker(state = timePickerState, modifier = Modifier.fillMaxWidth())
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            reminderTime = "${timePickerState.hour.toString().padStart(2, '0')}:${timePickerState.minute.toString().padStart(2, '0')}"
                            showTimePicker = false
                        }) {
                            Text("OK")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showTimePicker = false
                        }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            // Duration Input
            OutlinedTextField(
                value = durationMinutes,
                onValueChange = { durationMinutes = it },
                label = { Text("Duration (minutes)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            // Color Accent Picker
            Column {
                Text("Card Accent Color", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    colors.forEach { hex ->
                        val isSelected = colorHex == hex
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(android.graphics.Color.parseColor(hex)))
                                .clickable { colorHex = hex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(Icons.Rounded.Check, contentDescription = "Selected", tint = Color.White)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = {
                    if (name.isNotBlank() && selectedDays.isNotEmpty()) {
                        val habitEntity = HabitEntity(
                            id = habitId ?: 0L,
                            name = name,
                            icon = icon,
                            category = category,
                            targetDays = selectedDays.sorted().joinToString(","),
                            reminderTime = reminderTime.takeIf { it.isNotBlank() },
                            durationMinutes = durationMinutes.toIntOrNull(),
                            colorHex = colorHex,
                            createdAt = habit?.createdAt ?: System.currentTimeMillis()
                        )
                        viewModel.saveHabit(habitEntity)
                        onBack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(if (habitId == null) "Create Habit" else "Save Changes", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
