package com.example.expensetracker.ui.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.expensetracker.ExpenseTrackerApplication
import com.example.expensetracker.MainActivity
import com.example.expensetracker.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class HabitNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val habitId = intent.getLongExtra("EXTRA_HABIT_ID", -1L)
        val habitName = intent.getStringExtra("EXTRA_HABIT_NAME") ?: "Habit"
        val habitIcon = intent.getStringExtra("EXTRA_HABIT_ICON") ?: "🎯"

        if (habitId == -1L) return

        Log.d("HabitReceiver", "Received alarm for habit: $habitName")

        // 1. Show notification
        showNotification(context, habitId, habitName, habitIcon)

        // 2. Reschedule for next occurrence
        rescheduleNext(context, habitId)
    }

    private fun showNotification(context: Context, habitId: Long, name: String, icon: String) {
        // Check if permission is granted
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w("HabitReceiver", "No notification permission")
                return
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            // We could pass an extra to open specific screen, but opening main app is fine
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            habitId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, "habit_reminders")
            .setSmallIcon(R.mipmap.ic_launcher) // Using app icon
            .setContentTitle("Time for $icon $name!")
            .setContentText("Did you complete your habit today?")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        with(NotificationManagerCompat.from(context)) {
            notify(habitId.toInt(), builder.build())
        }
    }

    private fun rescheduleNext(context: Context, habitId: Long) {
        val application = context.applicationContext as? ExpenseTrackerApplication
        if (application == null) {
            Log.e("HabitReceiver", "Application context is not ExpenseTrackerApplication")
            return
        }
        
        val repository = application.container.habitRepository
        val scheduler = HabitNotificationScheduler(context)

        // GoAsync is better but standard coroutine is usually fine for quick local DB access in receiver
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val habit = repository.getHabitById(habitId).firstOrNull()
                if (habit != null && !habit.isArchived) {
                    scheduler.scheduleHabit(habit)
                }
            } catch (e: Exception) {
                Log.e("HabitReceiver", "Error rescheduling habit", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
