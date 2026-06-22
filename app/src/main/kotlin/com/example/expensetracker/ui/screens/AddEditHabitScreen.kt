package com.example.expensetracker.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.expensetracker.data.local.HabitEntity
import com.example.expensetracker.ui.viewmodel.HabitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditHabitScreen(
    viewModel: HabitViewModel,
    habitId: Long?,
    onBack: () -> Unit
) {
    val habit by if (habitId != null) {
        viewModel.getHabitById(habitId).collectAsState(initial = null)
    } else {
        remember { mutableStateOf(null) }
    }

    var name by remember { mutableStateOf("") }
    var icon by remember { mutableStateOf("🎯") }
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

    val emojis = listOf("💧", "🧘", "🏃", "🚶", "📖", "✍️", "💊", "🍎", "🛌", "🎯", "🏋️", "🥦")
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

            // Emoji Picker
            Column {
                Text("Select Icon", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    modifier = Modifier.height(100.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(emojis) { e ->
                        val isSelected = e == icon
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .clickable { icon = e },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(e, fontSize = 24.sp)
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
                val calendar = java.util.Calendar.getInstance()
                val initialHour = if (reminderTime.isNotBlank()) reminderTime.split(":")[0].toIntOrNull() ?: calendar.get(java.util.Calendar.HOUR_OF_DAY) else calendar.get(java.util.Calendar.HOUR_OF_DAY)
                val initialMinute = if (reminderTime.isNotBlank()) reminderTime.split(":")[1].toIntOrNull() ?: calendar.get(java.util.Calendar.MINUTE) else calendar.get(java.util.Calendar.MINUTE)

                val showDialog = {
                    android.app.TimePickerDialog(
                        context,
                        { _, hourOfDay, minute ->
                            reminderTime = String.format(java.util.Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                        },
                        initialHour,
                        initialMinute,
                        true // 24-hour format
                    ).show()
                }

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    if (androidx.core.content.ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.POST_NOTIFICATIONS
                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                    ) {
                        showDialog()
                    } else {
                        permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                    }
                } else {
                    showDialog()
                }
            }
            
            OutlinedTextField(
                value = reminderTime,
                onValueChange = { },
                label = { Text("Reminder Time (optional)") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onTimeClick() },
                shape = RoundedCornerShape(12.dp),
                enabled = false, // Disable typing, open picker on click
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                singleLine = true
            )

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
