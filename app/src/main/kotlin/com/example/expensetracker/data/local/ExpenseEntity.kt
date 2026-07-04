package com.example.expensetracker.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = ProcessEntity::class,
            parentColumns = ["id"],
            childColumns = ["processId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("processId"),
        Index("date"),
        Index("type")
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val processId: Long? = null,
    val amount: Double,
    val description: String,
    val date: Long = System.currentTimeMillis(),
    val category: String = "General",
    val type: String = "Expense", // "Expense" or "Income"
    val receiptUri: String? = null,
    val quantity: Double? = null,
    val unit: String? = null
)
