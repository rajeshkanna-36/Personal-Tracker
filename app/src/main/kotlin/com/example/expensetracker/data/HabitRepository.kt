package com.example.expensetracker.data

import com.example.expensetracker.data.local.HabitCompletionEntity
import com.example.expensetracker.data.local.HabitDao
import com.example.expensetracker.data.local.HabitEntity
import kotlinx.coroutines.flow.Flow

class HabitRepository(private val habitDao: HabitDao) {

    val allHabits: Flow<List<HabitEntity>> = habitDao.getAllHabits()

    fun getHabitById(habitId: Long): Flow<HabitEntity?> {
        return habitDao.getHabitById(habitId)
    }

    suspend fun insertHabit(habit: HabitEntity): Long {
        return habitDao.insertHabit(habit)
    }

    suspend fun updateHabit(habit: HabitEntity) {
        habitDao.updateHabit(habit)
    }

    suspend fun deleteHabit(habit: HabitEntity) {
        habitDao.deleteHabit(habit)
    }

    suspend fun insertCompletion(completion: HabitCompletionEntity): Long {
        return habitDao.insertCompletion(completion)
    }

    suspend fun deleteCompletion(completion: HabitCompletionEntity) {
        habitDao.deleteCompletion(completion)
    }

    fun getCompletionsForDate(dateMillis: Long): Flow<List<HabitCompletionEntity>> {
        return habitDao.getCompletionsForDate(dateMillis)
    }

    fun getCompletionsForHabit(habitId: Long): Flow<List<HabitCompletionEntity>> {
        return habitDao.getCompletionsForHabit(habitId)
    }

    fun getCompletionForHabitOnDate(habitId: Long, dateMillis: Long): Flow<HabitCompletionEntity?> {
        return habitDao.getCompletionForHabitOnDate(habitId, dateMillis)
    }
}
