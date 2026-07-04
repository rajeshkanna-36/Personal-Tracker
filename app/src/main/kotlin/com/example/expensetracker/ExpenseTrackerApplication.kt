package com.example.expensetracker

import android.app.Application
import com.example.expensetracker.data.AppPreferences
import com.example.expensetracker.data.BackupManager
import com.example.expensetracker.data.ExpenseRepository
import com.example.expensetracker.data.HabitRepository
import com.example.expensetracker.data.local.AppDatabase

interface AppContainer {
    val expenseRepository: ExpenseRepository
    val habitRepository: HabitRepository
    val memoryRepository: com.example.expensetracker.data.MemoryRepository
    val debtRepository: com.example.expensetracker.data.DebtRepository
    val appPreferences: AppPreferences
    val backupManager: BackupManager
}

class DefaultAppContainer(private val application: Application) : AppContainer {
    override val expenseRepository: ExpenseRepository by lazy {
        ExpenseRepository(AppDatabase.getDatabase(application).expenseDao())
    }
    override val habitRepository: HabitRepository by lazy {
        HabitRepository(AppDatabase.getDatabase(application).habitDao())
    }
    override val memoryRepository: com.example.expensetracker.data.MemoryRepository by lazy {
        com.example.expensetracker.data.MemoryRepository(AppDatabase.getDatabase(application).memoryDao())
    }
    override val debtRepository: com.example.expensetracker.data.DebtRepository by lazy {
        com.example.expensetracker.data.DebtRepository(AppDatabase.getDatabase(application).debtDao())
    }
    override val appPreferences: AppPreferences by lazy {
        AppPreferences(application)
    }
    override val backupManager: BackupManager by lazy {
        BackupManager(application, AppDatabase.getDatabase(application), appPreferences)
    }
}

class ExpenseTrackerApplication : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val name = "Habit Reminders"
            val descriptionText = "Notifications for habit reminders"
            val importance = android.app.NotificationManager.IMPORTANCE_HIGH
            val channel = android.app.NotificationChannel("habit_reminders", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: android.app.NotificationManager =
                getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}
