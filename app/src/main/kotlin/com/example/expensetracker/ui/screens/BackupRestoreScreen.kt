package com.example.expensetracker.ui.screens

import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.expensetracker.ui.components.FilePickerDialog
import com.example.expensetracker.ui.viewmodel.ExpenseViewModel
import kotlinx.coroutines.launch
import java.io.File

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
    val database = com.example.expensetracker.data.local.AppDatabase.getDatabase(context)

    var isAutoBackupEnabled by remember { mutableStateOf(appPreferences.isAutoBackupEnabled) }
    
    var showResetDialog by remember { mutableStateOf<String?>(null) }
    
    var showExportPicker by remember { mutableStateOf(false) }
    var showImportPicker by remember { mutableStateOf(false) }
    var showAutoBackupPicker by remember { mutableStateOf(false) }

    fun checkPermissionsAndExecute(action: () -> Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Toast.makeText(context, "Please grant 'All files access' to use the custom file picker.", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                    data = android.net.Uri.parse("package:${context.packageName}")
                }
                context.startActivity(intent)
                return
            }
        }
        action()
    }

    if (showExportPicker) {
        FilePickerDialog(
            title = "Select Folder to Export",
            isFolderSelection = true,
            onDismiss = { showExportPicker = false },
            onFileSelected = { folder ->
                showExportPicker = false
                coroutineScope.launch {
                    try {
                        val csvData = backupManager.exportAllDataToString()
                        val file = File(folder, "ExpenseTrackerBackup_${System.currentTimeMillis()}.csv")
                        file.writeText(csvData)
                        Toast.makeText(context, "Backup Exported to: ${file.absolutePath}", Toast.LENGTH_LONG).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Export Failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    if (showImportPicker) {
        FilePickerDialog(
            title = "Select Backup CSV",
            isFolderSelection = false,
            onDismiss = { showImportPicker = false },
            onFileSelected = { file ->
                showImportPicker = false
                coroutineScope.launch {
                    try {
                        val csvData = file.readText()
                        backupManager.importDataFromString(csvData)
                        Toast.makeText(context, "Data Imported Successfully!", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Import Failed: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    if (showAutoBackupPicker) {
        FilePickerDialog(
            title = "Select Auto-Backup Folder",
            isFolderSelection = true,
            onDismiss = { 
                showAutoBackupPicker = false
                isAutoBackupEnabled = false
            },
            onFileSelected = { folder ->
                showAutoBackupPicker = false
                appPreferences.autoBackupUri = folder.absolutePath
                appPreferences.isAutoBackupEnabled = true
                isAutoBackupEnabled = true
                Toast.makeText(context, "Auto-Backup Enabled at: ${folder.absolutePath}", Toast.LENGTH_SHORT).show()
                backupManager.triggerAutoBackup()
            }
        )
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
                .verticalScroll(rememberScrollState())
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
                                checkPermissionsAndExecute {
                                    showAutoBackupPicker = true
                                }
                            } else {
                                isAutoBackupEnabled = false
                                appPreferences.autoBackupUri = null
                                appPreferences.isAutoBackupEnabled = false
                                Toast.makeText(context, "Auto-Backup Disabled", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = { 
                    checkPermissionsAndExecute {
                        showExportPicker = true
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Export Backup Manually (CSV)")
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = { 
                    checkPermissionsAndExecute {
                        showImportPicker = true
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Restore from CSV")
            }

            Spacer(modifier = Modifier.height(48.dp))

            // Danger Zone Section
            Text(
                "Danger Zone",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.align(Alignment.Start)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedButton(
                onClick = { showResetDialog = "General Expenses" },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Reset General Expenses")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showResetDialog = "Projects & Budgets" },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Reset Projects & Budgets")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showResetDialog = "Habits" },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Reset All Habits")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = { showResetDialog = "Wallet Balance" },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Reset Wallet Balance")
            }
        }
    }

    if (showResetDialog != null) {
        val target = showResetDialog!!
        AlertDialog(
            onDismissRequest = { showResetDialog = null },
            title = { Text("Reset $target") },
            text = { 
                if (target == "Wallet Balance") {
                    Text("Are you sure you want to cleanly reset your Initial Wallet Balance to 0? This will remove any previously set initial cash offset.")
                } else {
                    Text("Are you sure you want to delete all data in $target? This action cannot be undone.")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch {
                            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                when (target) {
                                    "General Expenses" -> database.expenseDao().deleteAllGeneralExpenses()
                                    "Projects & Budgets" -> database.expenseDao().deleteAllProcesses()
                                    "Habits" -> database.habitDao().deleteAllHabits()
                                    "Wallet Balance" -> {
                                        appPreferences.initialWalletBalance = 0.0
                                    }
                                }
                            }
                            backupManager.triggerAutoBackup()
                            Toast.makeText(context, "$target Reset Successfully", Toast.LENGTH_SHORT).show()
                            showResetDialog = null
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
