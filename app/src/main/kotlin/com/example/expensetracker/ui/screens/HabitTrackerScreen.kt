package com.example.expensetracker.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation3.runtime.NavKey
import com.example.expensetracker.AddEditHabit
import com.example.expensetracker.data.local.HabitCompletionEntity
import com.example.expensetracker.data.local.HabitEntity
import com.example.expensetracker.ui.viewmodel.HabitViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitTrackerScreen(
    viewModel: HabitViewModel,
    onNavigate: (NavKey) -> Unit
) {
    val allHabits by viewModel.allHabits.collectAsState()
    
    // Manage current selected date
    var selectedDateMillis by remember { mutableLongStateOf(System.currentTimeMillis()) }
    val completions by viewModel.getCompletionsForDate(selectedDateMillis).collectAsState(initial = emptyList())
    
    // We only show habits that are targeted for the selected day of week
    val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
    val dayOfWeekIndex = (cal.get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7 // Monday = 0, Sunday = 6
    
    val activeHabits = allHabits.filter { habit ->
        habit.targetDays.split(",").contains(dayOfWeekIndex.toString())
    }
    
    val hasAnyReminder = allHabits.any { !it.reminderTime.isNullOrBlank() }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigate(AddEditHabit()) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add Habit")
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(top = 40.dp, bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header: Greeting and Date
            item {
                HeaderSection()
            }

            // Weekly Calendar Strip
            item {
                WeeklyCalendarRow(
                    selectedDateMillis = selectedDateMillis,
                    onDateSelected = { selectedDateMillis = it }
                )
            }

            // Reminder Card
            item {
                AnimatedVisibility(
                    visible = !hasAnyReminder,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    ReminderCard(onSetReminder = { onNavigate(AddEditHabit()) })
                }
            }

            // Daily Routine Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Daily routine",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "See all",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { /* Future: expand full list */ }
                    )
                }
            }

            // Habits List
            if (activeHabits.isEmpty()) {
                item {
                    Text(
                        "No habits scheduled for this day. Tap + to add one!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 32.dp)
                    )
                }
            } else {
                items(activeHabits, key = { it.id }) { habit ->
                    val isCompleted = completions.any { it.habitId == habit.id }
                    
                    // Retrieve all completions to compute streak
                    val allCompletions by viewModel.getCompletionsForHabit(habit.id).collectAsState(initial = emptyList())
                    val streak = calculateStreak(allCompletions, habit.targetDays)

                    HabitRow(
                        habit = habit,
                        isCompleted = isCompleted,
                        streak = streak,
                        allCompletions = allCompletions,
                        onToggle = { viewModel.toggleCompletion(habit.id, selectedDateMillis) },
                        onEdit = { onNavigate(AddEditHabit(habit.id)) },
                        onDelete = { viewModel.deleteHabit(habit) }
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderSection() {
    val dateStr = SimpleDateFormat("EEEE, dd MMMM, yyyy", Locale.getDefault()).format(Date())
    
    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (currentHour) {
        in 0..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "$greeting, Budi", // Placeholder name based on design, could be dynamic
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = dateStr,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        // Avatar placeholder
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text("🐯", fontSize = 24.sp)
        }
    }
}

@Composable
fun WeeklyCalendarRow(
    selectedDateMillis: Long,
    onDateSelected: (Long) -> Unit
) {
    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    
    // Get dates for the current week based on selected date
    val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
    val currentDayOfWeek = (cal.get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7 // Monday = 0
    cal.add(Calendar.DAY_OF_YEAR, -currentDayOfWeek) // Move back to Monday
    
    val weekDates = (0..6).map { 
        val time = cal.timeInMillis
        cal.add(Calendar.DAY_OF_YEAR, 1)
        time
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        weekDates.forEachIndexed { index, dateMillis ->
            val isSelected = isSameDay(dateMillis, selectedDateMillis)
            val dayCal = Calendar.getInstance().apply { timeInMillis = dateMillis }
            val dayOfMonth = dayCal.get(Calendar.DAY_OF_MONTH).toString()
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = daysOfWeek[index],
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        )
                        .clickable { onDateSelected(dateMillis) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = dayOfMonth,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun ReminderCard(onSetReminder: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Set the reminder",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Never miss your morning routine!\nSet a reminder to stay on track",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 18.sp
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onSetReminder,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(16.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text("Set Now", color = MaterialTheme.colorScheme.onPrimary)
                }
            }
            
            // Bell icon illustration
            Box(modifier = Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                Text("🔔", fontSize = 60.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitRow(
    habit: HabitEntity,
    isCompleted: Boolean,
    streak: Int,
    allCompletions: List<HabitCompletionEntity>,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val alpha by animateFloatAsState(if (isCompleted) 0.6f else 1f)
    
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Habit") },
            text = { Text("Are you sure you want to delete '${habit.name}'?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirmation = false
                    onDelete()
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) { Text("Cancel") }
            }
        )
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                showDeleteConfirmation = true
            }
            false // Don't dismiss immediately, wait for dialog
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            val color by androidx.compose.animation.animateColorAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) 
                    MaterialTheme.colorScheme.error 
                else 
                    Color.Transparent,
                label = "swipe_color"
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(color)
                    .padding(end = 24.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                if (color != Color.Transparent) {
                    Icon(Icons.Rounded.Delete, contentDescription = "Delete", tint = Color.White)
                }
            }
        },
        enableDismissFromStartToEnd = false
    ) {
        var expanded by remember { mutableStateOf(false) }
        val cardAlpha by animateFloatAsState(if (isCompleted && !expanded) 0.7f else 1f, label = "alpha")
        
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .alpha(cardAlpha)
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
                .clickable { expanded = !expanded }
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Icon Box
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color(android.graphics.Color.parseColor(habit.colorHex)).copy(alpha = 0.2f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(habit.icon, fontSize = 24.sp)
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = habit.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black,
                            textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Streak $streak days",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            MiniStreakCalendar(habit = habit, completions = allCompletions)
                        }
                    }
                    
                    // Divider line
                    Box(modifier = Modifier.width(1.dp).height(40.dp).background(Color(0xFFEEEEEE)))
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Completion Checkbox (Fluid & Minimal)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .clickable { onToggle() },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted) {
                            Icon(
                                Icons.Rounded.CheckCircle, 
                                contentDescription = "Done", 
                                tint = Color(android.graphics.Color.parseColor(habit.colorHex)),
                                modifier = Modifier.size(32.dp)
                            )
                        } else {
                            androidx.compose.foundation.Canvas(modifier = Modifier.size(24.dp)) {
                                drawCircle(
                                    color = Color(0xFFE5E7EB),
                                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                                )
                            }
                        }
                    }
                }
                
                // Expanded Details
                AnimatedVisibility(visible = expanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEEEEEE)))
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        FullStreakCalendar(completions = allCompletions, targetDays = habit.targetDays, colorHex = habit.colorHex)
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total Completions: ${allCompletions.size}",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.Gray
                            )
                            TextButton(onClick = onEdit) {
                                Text("Edit Habit", color = Color(android.graphics.Color.parseColor(habit.colorHex)), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper to check if two timestamps are the same day
fun isSameDay(time1: Long, time2: Long): Boolean {
    val cal1 = Calendar.getInstance().apply { timeInMillis = time1 }
    val cal2 = Calendar.getInstance().apply { timeInMillis = time2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
           cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}

// Simple streak calculator (consecutive days looking backwards)
fun calculateStreak(completions: List<HabitCompletionEntity>, targetDaysStr: String): Int {
    if (completions.isEmpty()) return 0
    
    val targetDays = targetDaysStr.split(",").mapNotNull { it.toIntOrNull() }.toSet()
    if (targetDays.isEmpty()) return 0

    val completedDatesSet = completions.map {
        val cal = Calendar.getInstance().apply { timeInMillis = it.dateMillis }
        "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
    }.toSet()

    var streak = 0
    val cal = Calendar.getInstance()
    
    // Check backwards from today for up to 365 days
    for (i in 0..365) {
        val dayOfWeekIndex = (cal.get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7
        
        // If this day is a target day for the habit
        if (targetDays.contains(dayOfWeekIndex)) {
            val dateKey = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
            if (completedDatesSet.contains(dateKey)) {
                streak++
            } else if (i > 0) { // If it's not today and we missed it, streak ends
                break
            }
        }
        cal.add(Calendar.DAY_OF_YEAR, -1)
    }
    
    return streak
}

@Composable
fun MiniStreakCalendar(habit: HabitEntity, completions: List<HabitCompletionEntity>) {
    val completedDatesSet = completions.map {
        val cal = Calendar.getInstance().apply { timeInMillis = it.dateMillis }
        "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
    }.toSet()
    val targetDays = habit.targetDays.split(",").mapNotNull { it.toIntOrNull() }.toSet()
    
    // Generate the last 7 days (including today)
    val last7Days = (6 downTo 0).map { i ->
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -i)
        cal
    }
    
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        last7Days.forEach { cal ->
            val dayOfWeekIndex = (cal.get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7
            val dateKey = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
            
            val isTargetDay = targetDays.contains(dayOfWeekIndex)
            val isCompleted = completedDatesSet.contains(dateKey)
            
            if (isTargetDay || isCompleted) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(
                            color = if (isCompleted) Color(android.graphics.Color.parseColor(habit.colorHex)) else Color(0xFFEEEEEE),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
fun FullStreakCalendar(completions: List<HabitCompletionEntity>, targetDays: String, colorHex: String) {
    // Generate days for the current month
    val calendar = Calendar.getInstance()
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)
    
    calendar.set(Calendar.DAY_OF_MONTH, 1)
    val startDayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7 // 0=Mon
    val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    
    val completedDatesSet = completions.map {
        val cal = Calendar.getInstance().apply { timeInMillis = it.dateMillis }
        "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
    }.toSet()
    
    val targetDaysSet = targetDays.split(",").mapNotNull { it.toIntOrNull() }.toSet()
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Calendar.getInstance().time),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(12.dp))
        
        // Days of week header
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("M", "T", "W", "T", "F", "S", "S").forEach {
                Text(it, style = MaterialTheme.typography.labelSmall, color = Color.Gray, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        
        // Grid
        var currentDay = 1
        for (week in 0..5) {
            if (currentDay > daysInMonth) break
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                for (dayOfWeek in 0..6) {
                    if ((week == 0 && dayOfWeek < startDayOfWeek) || currentDay > daysInMonth) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        val dayCal = Calendar.getInstance().apply {
                            set(Calendar.YEAR, currentYear)
                            set(Calendar.MONTH, currentMonth)
                            set(Calendar.DAY_OF_MONTH, currentDay)
                        }
                        val dateKey = "${dayCal.get(Calendar.YEAR)}-${dayCal.get(Calendar.DAY_OF_YEAR)}"
                        val isCompleted = completedDatesSet.contains(dateKey)
                        val isTarget = targetDaysSet.contains(dayOfWeek)
                        val isToday = dayCal.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp)
                                .background(
                                    color = if (isCompleted) Color(android.graphics.Color.parseColor(colorHex)) 
                                            else if (isToday) Color(0xFFF3F4F6) 
                                            else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentDay.toString(),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isCompleted) Color.White 
                                        else if (isTarget) Color.Black 
                                        else Color.LightGray,
                                fontWeight = if (isToday || isCompleted) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        currentDay++
                    }
                }
            }
        }
    }
}
