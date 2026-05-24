package com.example.expensetracker.domain.model

data class ProcessStage(
    val id: Long,
    val processId: Long,
    val name: String,
    val displayOrder: Int,
    val actualSpent: Double = 0.0
)

data class Process(
    val id: Long,
    val name: String,
    val description: String,
    val budget: Double,
    val status: String, // ACTIVE, COMPLETED
    val startDate: Long,
    val endDate: Long?,
    val category: String,
    val stages: List<ProcessStage> = emptyList(),
    val expenses: List<Expense> = emptyList()
) {
    val actualSpent: Double
        get() = expenses.sumOf { it.amount }

    val remainingBudget: Double
        get() = budget - actualSpent

    val percentSpent: Float
        get() = if (budget > 0) (actualSpent / budget).toFloat() else 0f
}

data class Expense(
    val id: Long,
    val processId: Long,
    val processName: String = "",
    val stageId: Long? = null,
    val stageName: String? = null,
    val title: String,
    val amount: Double,
    val timestamp: Long,
    val category: String,
    val paymentMethod: String,
    val notes: String = ""
)
