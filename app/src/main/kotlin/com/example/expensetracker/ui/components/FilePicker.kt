package com.example.expensetracker.ui.components

import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilePickerDialog(
    title: String = "Select",
    isFolderSelection: Boolean = false,
    onDismiss: () -> Unit,
    onFileSelected: (File) -> Unit
) {
    var currentDir by remember { mutableStateOf(Environment.getExternalStorageDirectory()) }
    var files by remember { mutableStateOf(getFilesList(currentDir, isFolderSelection)) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text(title, style = MaterialTheme.typography.titleMedium) },
                    navigationIcon = {
                        if (currentDir.absolutePath != Environment.getExternalStorageDirectory().absolutePath) {
                            TextButton(onClick = {
                                val parent = currentDir.parentFile
                                if (parent != null && parent.absolutePath.startsWith(Environment.getExternalStorageDirectory().absolutePath)) {
                                    currentDir = parent
                                    files = getFilesList(currentDir, isFolderSelection)
                                }
                            }) {
                                Text("Back")
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                )

                Text(
                    text = currentDir.absolutePath,
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                LazyColumn(modifier = Modifier.weight(1f)) {
                    items(files) { file ->
                        ListItem(
                            headlineContent = { Text(file.name) },
                            leadingContent = {
                                Icon(
                                    if (file.isDirectory) Icons.Default.List else Icons.Default.Create,
                                    contentDescription = null
                                )
                            },
                            modifier = Modifier.clickable {
                                if (file.isDirectory) {
                                    currentDir = file
                                    files = getFilesList(currentDir, isFolderSelection)
                                } else if (!isFolderSelection) {
                                    onFileSelected(file)
                                }
                            }
                        )
                        HorizontalDivider()
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    if (isFolderSelection) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(onClick = { onFileSelected(currentDir) }) {
                            Text("Select This Folder")
                        }
                    }
                }
            }
        }
    }
}

private fun getFilesList(dir: File, foldersOnly: Boolean): List<File> {
    val listedFiles = dir.listFiles() ?: return emptyList()
    return listedFiles.filter {
        !it.isHidden && (it.isDirectory || (!foldersOnly && it.name.endsWith(".csv", ignoreCase = true)))
    }.sortedWith(compareBy({ !it.isDirectory }, { it.name.lowercase() }))
}
