package com.example.expensetracker.data

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.example.expensetracker.data.local.AppDatabase
import com.example.expensetracker.data.local.ExpenseEntity
import com.example.expensetracker.data.local.HabitCompletionEntity
import com.example.expensetracker.data.local.HabitEntity
import com.example.expensetracker.data.local.ProcessEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BackupManager(
    private val context: Context,
    private val database: AppDatabase,
    private val appPreferences: AppPreferences
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    // CSV helper: quote a field so commas, quotes, and newlines are safe
    private fun csvQuote(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    // CSV helper: parse a single CSV line respecting quoted fields
    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' && !inQuotes -> inQuotes = true
                c == '"' && inQuotes -> {
                    if (i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i++ // skip escaped quote
                    } else {
                        inQuotes = false
                    }
                }
                c == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current = StringBuilder()
                }
                else -> current.append(c)
            }
            i++
        }
        result.add(current.toString())
        return result
    }

    suspend fun exportAllDataToString(): String {
        val processes = database.expenseDao().getAllProcesses().first()
        val expenses = database.expenseDao().getAllExpenses().first()
        val habits = database.habitDao().getAllHabitsIncludingArchived().first()
        val habitCompletions = database.habitDao().getAllCompletions().first()

        val csv = StringBuilder()
        // Header
        csv.append("Type,ID,P1,P2,P3,P4,P5,P6,P7,P8,P9,P10\n")
        
        processes.forEach { p ->
            // Process: Type, id, name, budget, description, createdAt, isCompleted, colorHex, N/A...
            csv.append("Process,${p.id},${csvQuote(p.name)},${p.budget},${csvQuote(p.description)},${p.createdAt},${p.isCompleted},${csvQuote(p.colorHex)},N/A,N/A,N/A,N/A\n")
        }
        
        expenses.forEach { e ->
            // Expense: Type, id, processId, amount, description, date, category, type, receiptUri, quantity, unit, N/A
            csv.append("Expense,${e.id},${e.processId ?: ""},${e.amount},${csvQuote(e.description)},${e.date},${csvQuote(e.category)},${e.type},${csvQuote(e.receiptUri ?: "")},${e.quantity ?: ""},${e.unit ?: ""},N/A\n")
        }

        habits.forEach { h ->
            val safeTargetDays = h.targetDays.replace(",", "|")
            // Habit: Type, id, name, icon, colorHex, reminderTime, category, safeTargetDays, durationMinutes, createdAt, isArchived
            csv.append("Habit,${h.id},${csvQuote(h.name)},${h.icon},${h.colorHex},${h.reminderTime ?: ""},${csvQuote(h.category)},${safeTargetDays},${h.durationMinutes ?: ""},${h.createdAt},${h.isArchived}\n")
        }

        habitCompletions.forEach { hc ->
            // HabitCompletion: Type, id, habitId, dateMillis, completedAt, N/A...
            csv.append("HabitCompletion,${hc.id},${hc.habitId},${hc.dateMillis},${hc.completedAt},N/A,N/A,N/A,N/A,N/A,N/A,N/A\n")
        }

        return csv.toString()
    }

    suspend fun importDataFromString(csvData: String) {
        val lines = csvData.lines()
        if (lines.isEmpty()) return

        lines.drop(1).forEach { line ->
            if (line.isBlank()) return@forEach
            val parts = parseCsvLine(line)
            if (parts.size >= 11) {
                when (parts[0]) {
                    "Process" -> {
                        val process = ProcessEntity(
                            id = parts[1].toLongOrNull() ?: 0L,
                            name = parts[2],
                            budget = parts[3].toDoubleOrNull() ?: 0.0,
                            description = parts[4],
                            createdAt = parts[5].toLongOrNull() ?: System.currentTimeMillis(),
                            isCompleted = parts[6].toBooleanStrictOrNull() ?: false,
                            colorHex = parts[7].takeIf { it.isNotBlank() && it != "N/A" } ?: "#FF6200EE"
                        )
                        database.expenseDao().insertProcess(process)
                    }
                    "Expense" -> {
                        val parsedProcessId = parts[2].toLongOrNull()

                        val expense = ExpenseEntity(
                            id = parts[1].toLongOrNull() ?: 0L,
                            processId = parsedProcessId, // null if blank/unparseable — correct for general expenses
                            amount = parts[3].toDoubleOrNull() ?: 0.0,
                            description = parts[4],
                            date = parts[5].toLongOrNull() ?: System.currentTimeMillis(),
                            category = parts[6],
                            type = parts[7],
                            receiptUri = parts[8].takeIf { it.isNotEmpty() },
                            quantity = parts[9].toDoubleOrNull(),
                            unit = parts[10].takeIf { it.isNotEmpty() }
                        )
                        database.expenseDao().insertExpense(expense)
                    }
                    "Habit" -> {
                        val habit = HabitEntity(
                            id = parts[1].toLongOrNull() ?: 0L,
                            name = parts[2],
                            icon = parts[3],
                            colorHex = parts[4],
                            reminderTime = parts[5].takeIf { it.isNotBlank() },
                            category = parts[6],
                            targetDays = parts[7].replace("|", ","),
                            durationMinutes = parts[8].toIntOrNull(),
                            createdAt = parts[9].toLongOrNull() ?: System.currentTimeMillis(),
                            isArchived = parts.getOrNull(10)?.toBooleanStrictOrNull() ?: false
                        )
                        database.habitDao().insertHabit(habit)
                    }
                    "HabitCompletion" -> {
                        val completion = HabitCompletionEntity(
                            id = parts[1].toLongOrNull() ?: 0L,
                            habitId = parts[2].toLongOrNull() ?: 0L,
                            dateMillis = parts[3].toLongOrNull() ?: System.currentTimeMillis(),
                            completedAt = parts[4].toLongOrNull() ?: System.currentTimeMillis()
                        )
                        database.habitDao().insertCompletion(completion)
                    }
                }
            }
        }
    }

    fun triggerAutoBackup() {
        if (!appPreferences.isAutoBackupEnabled) return
        val uriString = appPreferences.autoBackupUri ?: return

        scope.launch {
            try {
                val folder = java.io.File(uriString)
                if (folder.exists() && folder.isDirectory && folder.canWrite()) {
                    val backupFile = java.io.File(folder, "ExpenseTracker_AutoBackup.csv")
                    val csvData = exportAllDataToString()
                    backupFile.writeText(csvData)
                    Log.d("BackupManager", "Auto-backup successful to ${backupFile.absolutePath}")
                } else {
                    Log.e("BackupManager", "Cannot write to selected auto-backup folder: $uriString")
                }
            } catch (e: Exception) {
                Log.e("BackupManager", "Auto-backup failed: ${e.message}")
            }
        }
    }
}
