package com.example.expensetracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "processes")
data class ProcessEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val budget: Double,
    val createdAt: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val colorHex: String = "#FF6200EE" // For modern UI colors
)
