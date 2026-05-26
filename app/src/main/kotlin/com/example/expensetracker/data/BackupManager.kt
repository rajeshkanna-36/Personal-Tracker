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

    suspend fun exportAllDataToString(): String {
        val processes = database.expenseDao().getAllProcesses().first()
        val expenses = database.expenseDao().getAllExpenses().first()
        val habits = database.habitDao().getAllHabits().first()
        val habitCompletions = database.habitDao().getAllCompletions().first()

        val csv = StringBuilder()
        // Header
        csv.append("Type,ID,P1,P2,P3,P4,P5,P6,P7,P8,P9,P10\n")
        
        processes.forEach { p ->
            // Process: Type, id, name, budget, description, createdAt, N/A...
            csv.append("Process,${p.id},${p.name},${p.budget},${p.description},${p.createdAt},N/A,N/A,N/A,N/A,N/A,N/A\n")
        }
        
        expenses.forEach { e ->
            // Expense: Type, id, processId, amount, description, date, category, type, receiptUri, quantity, unit, N/A
            csv.append("Expense,${e.id},${e.processId},${e.amount},${e.description},${e.date},${e.category},${e.type},${e.receiptUri ?: ""},${e.quantity ?: ""},${e.unit ?: ""},N/A\n")
        }

        habits.forEach { h ->
            val safeTargetDays = h.targetDays.replace(",", "|")
            // Habit: Type, id, name, icon, colorHex, reminderTime, category, safeTargetDays, durationMinutes, createdAt, isArchived
            csv.append("Habit,${h.id},${h.name},${h.icon},${h.colorHex},${h.reminderTime ?: ""},${h.category},${safeTargetDays},${h.durationMinutes ?: ""},${h.createdAt},${h.isArchived}\n")
        }

        habitCompletions.forEach { hc ->
            // HabitCompletion: Type, id, habitId, dateMillis, N/A...
            csv.append("HabitCompletion,${hc.id},${hc.habitId},${hc.dateMillis},N/A,N/A,N/A,N/A,N/A,N/A,N/A,N/A\n")
        }

        return csv.toString()
    }

    suspend fun importDataFromString(csvData: String) {
        val lines = csvData.lines()
        if (lines.isEmpty()) return

        lines.drop(1).forEach { line ->
            val parts = line.split(",")
            if (parts.size >= 11) {
                when (parts[0]) {
                    "Process" -> {
                        val process = ProcessEntity(
                            id = parts[1].toLongOrNull() ?: 0L,
                            name = parts[2],
                            budget = parts[3].toDoubleOrNull() ?: 0.0,
                            description = parts[4],
                            createdAt = parts[5].toLongOrNull() ?: System.currentTimeMillis()
                        )
                        database.expenseDao().insertProcess(process)
                    }
                    "Expense" -> {
                        val parsedProcessId = parts[2].toLongOrNull()
                        val processId = if (parsedProcessId == 0L) null else parsedProcessId

                        val expense = ExpenseEntity(
                            id = parts[1].toLongOrNull() ?: 0L,
                            processId = processId,
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
                            dateMillis = parts[3].toLongOrNull() ?: System.currentTimeMillis()
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
                val treeUri = Uri.parse(uriString)
                val documentFile = DocumentFile.fromTreeUri(context, treeUri)
                if (documentFile != null && documentFile.canWrite()) {
                    var backupFile = documentFile.findFile("ExpenseTracker_AutoBackup.csv")
                    if (backupFile == null) {
                        backupFile = documentFile.createFile("text/csv", "ExpenseTracker_AutoBackup.csv")
                    }
                    
                    backupFile?.let { file ->
                        val csvData = exportAllDataToString()
                        context.contentResolver.openOutputStream(file.uri, "wt")?.use { outputStream ->
                            outputStream.write(csvData.toByteArray())
                        }
                        Log.d("BackupManager", "Auto-backup successful")
                    }
                } else {
                    Log.e("BackupManager", "Cannot write to selected auto-backup folder")
                }
            } catch (e: Exception) {
                Log.e("BackupManager", "Auto-backup failed: ${e.message}")
            }
        }
    }
}
