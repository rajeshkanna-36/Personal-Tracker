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

    enableEdgeToEdge()
    setContent {
      val systemDark = isSystemInDarkTheme()
      var isDarkTheme by remember { mutableStateOf(systemDark) }

      ExpenseTrackerTheme(darkTheme = isDarkTheme) { 
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { 
            MainNavigation(
                isDarkTheme = isDarkTheme,
                onThemeToggle = { isDarkTheme = !isDarkTheme }
            ) 
        } 
      }
    }
  }
}
