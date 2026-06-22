package com.example.expensetracker.ui.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.expensetracker.ExpenseTrackerApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Re-schedules all habit notifications after a device reboot.
 * Android cancels all AlarmManager alarms on reboot, so this receiver
 * re-registers them when the device boots up.
 */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        Log.d("BootReceiver", "Device booted — rescheduling habit notifications")

        val application = context.applicationContext as? ExpenseTrackerApplication
        if (application == null) {
            Log.e("BootReceiver", "Application context is not ExpenseTrackerApplication")
            return
        }

        val repository = application.container.habitRepository
        val scheduler = HabitNotificationScheduler(context)

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val habits = repository.allHabits.first()
                habits.forEach { habit ->
                    if (!habit.reminderTime.isNullOrBlank()) {
                        scheduler.scheduleHabit(habit)
                    }
                }
                Log.d("BootReceiver", "Rescheduled ${habits.size} habits")
            } catch (e: Exception) {
                Log.e("BootReceiver", "Error rescheduling habits on boot", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
