package com.example.expensetracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val type: String = "NOTE", // "NOTE" or "PASSWORD"
    val title: String, // Used as Note content OR Password title/website
    val username: String? = null,
    val password: String? = null,
    val timestamp: Long,
    val isCompleted: Boolean = false // Legacy/Unused for now
)
