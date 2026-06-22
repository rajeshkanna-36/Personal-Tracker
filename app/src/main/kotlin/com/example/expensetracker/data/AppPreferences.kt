package com.example.expensetracker.data

import android.content.Context
import android.content.SharedPreferences

class AppPreferences(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("expense_tracker_prefs", Context.MODE_PRIVATE)

    var autoBackupUri: String?
        get() = prefs.getString("auto_backup_uri", null)
        set(value) {
            prefs.edit().putString("auto_backup_uri", value).apply()
        }

    var isAutoBackupEnabled: Boolean
        get() = prefs.getBoolean("is_auto_backup_enabled", false)
        set(value) {
            prefs.edit().putBoolean("is_auto_backup_enabled", value).apply()
        }

    /** Tri-state: null = follow system, true = dark, false = light */
    var isDarkTheme: Boolean?
        get() = if (prefs.contains("is_dark_theme")) prefs.getBoolean("is_dark_theme", true) else null
        set(value) {
            if (value == null) {
                prefs.edit().remove("is_dark_theme").apply()
            } else {
                prefs.edit().putBoolean("is_dark_theme", value).apply()
            }
        }
}
