package com.example.expensetracker.ui.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.expensetracker.data.local.HabitEntity
import java.util.Calendar

class HabitNotificationScheduler(private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleHabit(habit: HabitEntity) {
        if (habit.isArchived || habit.reminderTime.isNullOrBlank()) {
            cancelHabit(habit.id)
            return
        }

        val targetDays = habit.targetDays.split(",").mapNotNull { it.toIntOrNull() }
        if (targetDays.isEmpty()) return

        val timeParts = habit.reminderTime.split(":")
        if (timeParts.size != 2) return
        val hour = timeParts[0].toIntOrNull() ?: return
        val minute = timeParts[1].toIntOrNull() ?: return

        // Calculate next occurrence
        val now = Calendar.getInstance()
        var nextTime = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If today's time has passed, or today is not a target day, we need to find the next day
        // targetDays are 0=Mon, 1=Tue... 6=Sun.
        // Calendar days are Calendar.SUNDAY=1, Calendar.MONDAY=2...
        val calendarTargetDays = targetDays.map { dayIndex ->
            when (dayIndex) {
                0 -> Calendar.MONDAY
                1 -> Calendar.TUESDAY
                2 -> Calendar.WEDNESDAY
                3 -> Calendar.THURSDAY
                4 -> Calendar.FRIDAY
                5 -> Calendar.SATURDAY
                6 -> Calendar.SUNDAY
                else -> -1
            }
        }.filter { it != -1 }

        var daysToAdd = 0
        while (daysToAdd <= 7) {
            val checkDay = Calendar.getInstance()
            checkDay.add(Calendar.DAY_OF_YEAR, daysToAdd)
            val dayOfWeek = checkDay.get(Calendar.DAY_OF_WEEK)

            if (calendarTargetDays.contains(dayOfWeek)) {
                nextTime = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_YEAR, daysToAdd)
                    set(Calendar.HOUR_OF_DAY, hour)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (nextTime.after(now)) {
                    break
                }
            }
            daysToAdd++
        }

        if (nextTime.before(now)) {
            // Should not happen with the logic above, but just in case
            return
        }

        val intent = Intent(context, HabitNotificationReceiver::class.java).apply {
            putExtra("EXTRA_HABIT_ID", habit.id)
            putExtra("EXTRA_HABIT_NAME", habit.name)
            putExtra("EXTRA_HABIT_ICON", habit.icon)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habit.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextTime.timeInMillis,
                        pendingIntent
                    )
                } else {
                    // Fallback if exact alarms are heavily restricted
                    alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        nextTime.timeInMillis,
                        pendingIntent
                    )
                    Log.w("HabitScheduler", "Exact alarms not permitted, using inexact fallback.")
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextTime.timeInMillis,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    nextTime.timeInMillis,
                    pendingIntent
                )
            }
            Log.d("HabitScheduler", "Scheduled habit ${habit.name} for ${nextTime.time}")
        } catch (e: SecurityException) {
            Log.e("HabitScheduler", "Missing exact alarm permission", e)
        }
    }

    fun cancelHabit(habitId: Long) {
        val intent = Intent(context, HabitNotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            habitId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        Log.d("HabitScheduler", "Cancelled habit $habitId")
    }
}
