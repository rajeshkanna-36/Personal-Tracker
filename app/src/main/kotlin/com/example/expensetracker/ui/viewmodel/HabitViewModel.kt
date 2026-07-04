package com.example.expensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.expensetracker.ExpenseTrackerApplication
import com.example.expensetracker.data.BackupManager
import com.example.expensetracker.data.HabitRepository
import com.example.expensetracker.data.local.HabitCompletionEntity
import com.example.expensetracker.data.local.HabitEntity
import com.example.expensetracker.ui.notifications.HabitNotificationScheduler
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar

class HabitViewModel(
    private val repository: HabitRepository,
    private val scheduler: HabitNotificationScheduler,
    private val backupManager: BackupManager
) : ViewModel() {

    val allHabits: StateFlow<List<HabitEntity>> = repository.allHabits
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val selectedDateMillis = MutableStateFlow(System.currentTimeMillis())

    fun setSelectedDate(dateMillis: Long) {
        selectedDateMillis.value = dateMillis
    }

    fun getHabitById(habitId: Long): StateFlow<HabitEntity?> {
        return repository.getHabitById(habitId).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )
    }

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val completionsForSelectedDate: StateFlow<List<HabitCompletionEntity>> = selectedDateMillis
        .flatMapLatest { dateMillis ->
            repository.getCompletionsForDate(getStartOfDay(dateMillis))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val habitsWithCompletions: StateFlow<List<HabitUiModel>> = kotlinx.coroutines.flow.combine(
        repository.allHabits,
        repository.getAllCompletions()
    ) { habits, completions ->
        val completionsByHabit = completions.groupBy { it.habitId }
        habits.map { habit ->
            val habitCompletions = completionsByHabit[habit.id] ?: emptyList()
            HabitUiModel(
                habit = habit,
                completions = habitCompletions,
                streak = calculateStreak(habitCompletions, habit.targetDays)
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun saveHabit(habit: HabitEntity) {
        viewModelScope.launch {
            val savedHabit: HabitEntity
            if (habit.id == 0L) {
                val id = repository.insertHabit(habit)
                savedHabit = habit.copy(id = id)
            } else {
                repository.updateHabit(habit)
                savedHabit = habit
            }
            scheduler.scheduleHabit(savedHabit)
            backupManager.triggerAutoBackup()
        }
    }

    fun deleteHabit(habit: HabitEntity) {
        viewModelScope.launch {
            repository.updateHabit(habit.copy(isArchived = true)) // soft delete
            scheduler.cancelHabit(habit.id)
            backupManager.triggerAutoBackup()
        }
    }

    fun toggleCompletion(habitId: Long, dateMillis: Long) {
        viewModelScope.launch {
            val startOfDay = getStartOfDay(dateMillis)
            val existing = repository.getCompletionForHabitOnDate(habitId, startOfDay).first()
            if (existing != null) {
                repository.deleteCompletion(existing)
            } else {
                repository.insertCompletion(
                    HabitCompletionEntity(
                        habitId = habitId,
                        dateMillis = startOfDay
                    )
                )
            }
            backupManager.triggerAutoBackup()
        }
    }

    // Helper to normalize time to 00:00:00 local time
    private fun getStartOfDay(timeMillis: Long): Long {
        val cal = Calendar.getInstance().apply {
            timeInMillis = timeMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    private fun calculateStreak(completions: List<HabitCompletionEntity>, targetDaysStr: String): Int {
        if (completions.isEmpty()) return 0
        
        val targetDays = targetDaysStr.split(",").mapNotNull { it.toIntOrNull() }.toSet()
        if (targetDays.isEmpty()) return 0

        val completedDatesSet = completions.map {
            val cal = Calendar.getInstance().apply { timeInMillis = it.dateMillis }
            "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
        }.toSet()

        var streak = 0
        val cal = Calendar.getInstance()
        
        for (i in 0..365) {
            val dayOfWeekIndex = (cal.get(Calendar.DAY_OF_WEEK) - 2 + 7) % 7
            if (targetDays.contains(dayOfWeekIndex)) {
                val dateKey = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.DAY_OF_YEAR)}"
                if (completedDatesSet.contains(dateKey)) {
                    streak++
                } else if (i > 0) {
                    break
                }
            }
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        
        return streak
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = (this[APPLICATION_KEY] as ExpenseTrackerApplication)
                HabitViewModel(
                    application.container.habitRepository,
                    HabitNotificationScheduler(application),
                    application.container.backupManager
                )
            }
        }
    }
}

data class HabitUiModel(
    val habit: HabitEntity,
    val completions: List<HabitCompletionEntity>,
    val streak: Int
)
