package com.example.expensetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.expensetracker.theme.ExpenseTrackerTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val appPreferences = (application as ExpenseTrackerApplication).container.appPreferences

    enableEdgeToEdge()
    setContent {
      val systemDark = isSystemInDarkTheme()
      // Restore persisted preference, or fall back to system default
      var isDarkTheme by remember { mutableStateOf(appPreferences.isDarkTheme ?: systemDark) }

      // Request SMS Permission for Auto-Tracking
      val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
          androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
      ) { isGranted ->
          if (isGranted) {
              android.util.Log.d("MainActivity", "SMS Permission Granted")
          }
      }

      androidx.compose.runtime.LaunchedEffect(Unit) {
          if (androidx.core.content.ContextCompat.checkSelfPermission(
                  this@MainActivity,
                  android.Manifest.permission.RECEIVE_SMS
              ) != android.content.pm.PackageManager.PERMISSION_GRANTED
          ) {
              permissionLauncher.launch(android.Manifest.permission.RECEIVE_SMS)
          }
      }

      ExpenseTrackerTheme(darkTheme = isDarkTheme) { 
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { 
            MainNavigation(
                isDarkTheme = isDarkTheme,
                onThemeToggle = {
                    isDarkTheme = !isDarkTheme
                    appPreferences.isDarkTheme = isDarkTheme // persist the choice
                }
            ) 
        } 
      }
    }
  }
}
