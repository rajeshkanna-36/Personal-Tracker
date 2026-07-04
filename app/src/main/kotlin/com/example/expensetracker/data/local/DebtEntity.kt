package com.example.expensetracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "debts")
data class DebtEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val personName: String,
    val amount: Double,
    val type: String, // "Borrowed" or "Lent"
    val dueDate: Long? = null,
    val isSettled: Boolean = false,
    val dateAdded: Long = System.currentTimeMillis()
)
