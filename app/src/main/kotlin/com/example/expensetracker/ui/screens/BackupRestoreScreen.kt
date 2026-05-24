package com.example.expensetracker.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.expensetracker.ExpenseTrackerApplication
import com.example.expensetracker.ui.viewmodel.ExpenseViewModel
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupRestoreScreen(
    viewModel: ExpenseViewModel? = null, // Kept for backwards compatibility in Navigation, though unused
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val application = context.applicationContext as ExpenseTrackerApplication
    val backupManager = application.container.backupManager
    val appPreferences = application.container.appPreferences
    val coroutineScope = rememberCoroutineScope()

    var isAutoBackupEnabled by remember { mutableStateOf(appPreferences.isAutoBackupEnabled) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                try {
                    val csvData = backupManager.exportAllDataToString()
                    context.contentResolver.openOutputStream(it)?.use { outputStream ->
                        outputStream.write(csvData.toByteArray())
                    }
                    Toast.makeText(context, "Backup Exported Successfully!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Export Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                try {
                    context.contentResolver.openInputStream(it)?.use { inputStream ->
                        val reader = BufferedReader(InputStreamReader(inputStream))
                        val csvData = reader.readText()
                        backupManager.importDataFromString(csvData)
                    }
                    Toast.makeText(context, "Data Imported Successfully!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Import Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    val autoBackupFolderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            appPreferences.autoBackupUri = uri.toString()
            appPreferences.isAutoBackupEnabled = true
            isAutoBackupEnabled = true
            Toast.makeText(context, "Auto-Backup Enabled!", Toast.LENGTH_SHORT).show()
            // Trigger an immediate backup
            backupManager.triggerAutoBackup()
        } else {
            // User cancelled
            isAutoBackupEnabled = false
            appPreferences.isAutoBackupEnabled = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Offline Backup") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Secure your data completely offline. Export your entire project history to a CSV file or restore from a previous backup.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            // Auto Backup Section
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Auto-Backup", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Automatically save changes to a selected folder",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isAutoBackupEnabled,
                        onCheckedChange = { isChecked ->
                            if (isChecked) {
                                autoBackupFolderLauncher.launch(null)
                            } else {
                                isAutoBackupEnabled = false
                                appPreferences.isAutoBackupEnabled = false
                                Toast.makeText(context, "Auto-Backup Disabled", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { exportLauncher.launch("ExpenseTrackerBackup_${System.currentTimeMillis()}.csv") },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Export Backup Manually (CSV)")
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = { importLauncher.launch(arrayOf("text/csv", "*/*")) },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Restore from CSV")
            }
        }
    }
}
