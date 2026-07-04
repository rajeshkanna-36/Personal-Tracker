package com.example.expensetracker

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import com.example.expensetracker.theme.ExpenseTrackerTheme
import androidx.fragment.app.FragmentActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

class MainActivity : FragmentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val appPreferences = (application as ExpenseTrackerApplication).container.appPreferences

    if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECEIVE_SMS) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
        androidx.core.app.ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.RECEIVE_SMS), 1)
    }

    enableEdgeToEdge()
    setContent {
      val systemDark = isSystemInDarkTheme()
      var isDarkTheme by remember { mutableStateOf(appPreferences.isDarkTheme ?: systemDark) }
      
      var isAuthenticated by remember { mutableStateOf(false) }
      var biometricError by remember { mutableStateOf<String?>(null) }
      var showPrompt by remember { mutableStateOf(true) }

      val promptBiometric = {
          val executor = ContextCompat.getMainExecutor(this@MainActivity)
          val biometricPrompt = BiometricPrompt(this@MainActivity, executor,
              object : BiometricPrompt.AuthenticationCallback() {
                  override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                      super.onAuthenticationError(errorCode, errString)
                      biometricError = "Authentication error: $errString"
                  }

                  override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                      super.onAuthenticationSucceeded(result)
                      isAuthenticated = true
                  }

                  override fun onAuthenticationFailed() {
                      super.onAuthenticationFailed()
                      biometricError = "Authentication failed. Please try again."
                  }
              })

          val promptInfo = BiometricPrompt.PromptInfo.Builder()
              .setTitle("Unlock Personal Tracker")
              .setSubtitle("Confirm your identity to access your financial data")
              .setAllowedAuthenticators(androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG or androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL)
              .build()

          biometricPrompt.authenticate(promptInfo)
      }

      LaunchedEffect(showPrompt) {
          if (showPrompt && !isAuthenticated) {
              promptBiometric()
              showPrompt = false
          }
      }

      ExpenseTrackerTheme(darkTheme = isDarkTheme) { 
        Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { 
            if (isAuthenticated) {
                MainNavigation(
                    isDarkTheme = isDarkTheme,
                    onThemeToggle = {
                        isDarkTheme = !isDarkTheme
                        appPreferences.isDarkTheme = isDarkTheme
                    }
                ) 
            } else {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Rounded.Lock,
                        contentDescription = "Locked",
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "App is Locked",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (biometricError != null) {
                        Text(
                            biometricError ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(onClick = { promptBiometric() }) {
                        Text("Unlock App")
                    }
                }
            }
        } 
      }
    }
  }
}
