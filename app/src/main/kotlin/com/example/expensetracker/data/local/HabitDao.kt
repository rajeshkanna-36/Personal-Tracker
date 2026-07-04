package com.example.expensetracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    suspend fun insertHabit(habit: HabitEntity): Long

    @Update
    @JvmSuppressWildcards
    suspend fun updateHabit(habit: HabitEntity): Int

    @Delete
    @JvmSuppressWildcards
    suspend fun deleteHabit(habit: HabitEntity): Int

    @Query("DELETE FROM habits")
    fun deleteAllHabits()

    @Query("SELECT * FROM habits WHERE isArchived = 0 ORDER BY createdAt DESC")
    fun getAllHabits(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits ORDER BY createdAt DESC")
    fun getAllHabitsIncludingArchived(): Flow<List<HabitEntity>>

    @Query("SELECT * FROM habits WHERE id = :habitId")
    fun getHabitById(habitId: Long): Flow<HabitEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    @JvmSuppressWildcards
    suspend fun insertCompletion(completion: HabitCompletionEntity): Long

    @Delete
    @JvmSuppressWildcards
    suspend fun deleteCompletion(completion: HabitCompletionEntity): Int

    @Query("SELECT * FROM habit_completions WHERE dateMillis = :dateMillis")
    fun getCompletionsForDate(dateMillis: Long): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY dateMillis DESC")
    fun getCompletionsForHabit(habitId: Long): Flow<List<HabitCompletionEntity>>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId AND dateMillis = :dateMillis")
    fun getCompletionForHabitOnDate(habitId: Long, dateMillis: Long): Flow<HabitCompletionEntity?>

    @Query("SELECT * FROM habit_completions")
    fun getAllCompletions(): Flow<List<HabitCompletionEntity>>
}
