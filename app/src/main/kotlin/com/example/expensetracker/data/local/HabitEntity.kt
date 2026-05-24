package com.example.expensetracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habits")
data class HabitEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String = "🎯",
    val category: String = "Other",
    val targetDays: String = "0,1,2,3,4,5,6", // comma-separated day indices (Mon=0..Sun=6)
    val reminderTime: String? = null, // "HH:mm" format
    val durationMinutes: Int? = null,
    val colorHex: String = "#4F46E5",
    val createdAt: Long = System.currentTimeMillis(),
    val isArchived: Boolean = false
)
