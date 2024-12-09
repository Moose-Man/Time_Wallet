package com.example.time_wallet_3.view.TimeLogs

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.time_wallet_3.model.Activity
import com.example.time_wallet_3.viewmodel.viewmodel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CreateLogScreen(navController: NavHostController, viewModel: viewmodel) {
    val note = remember { mutableStateOf("") }
    val customDate = remember { mutableStateOf("") }
    val newActivity = remember { mutableStateOf("") }
    val timeElapsed by viewModel.timeElapsed.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val selectedActivity by viewModel.selectedActivity.collectAsState() // Observe selected activity
    val showAddActivityDialog = remember { mutableStateOf(false) }
    val activities by viewModel.activities.collectAsState(initial = emptyList())
    val showDeleteDialog = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "Log Time",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            TextField(
                value = note.value,
                onValueChange = { note.value = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Elapsed Time: ${formatElapsedTime(timeElapsed)}",
                style = MaterialTheme.typography.bodyLarge
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row {
                // Start Timer Button
                Button(
                    onClick = { viewModel.startTimer() },
                    enabled = selectedActivity?.isNotEmpty() == true && !isTimerRunning // Enabled only if an activity is selected
                ) {
                    Text("Start Timer")
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Stop Timer Button
                Button(
                    onClick = {
                        viewModel.stopTimer()
                        if (customDate.value.isNotEmpty()) {
                            viewModel.setSimulatedDate(customDate.value)
                        }
                        viewModel.addLog(
                            accountId = viewModel.currentAccountId.value ?: 0, // Pass the current account ID or a default value
                            activity = selectedActivity ?: "",
                            note = note.value
                        )
                        navController.navigate("view_logs")
                    },
                    enabled = isTimerRunning
                ) {
                    Text("Stop Timer")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Select Activity:", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(activities) { activity ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.setSelectedActivity(activity.name)
                            },
                        shape = RoundedCornerShape(0.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedActivity == activity.name) Color.Green else Color.LightGray
                        )
                    ) {
                        Text(
                            text = activity.name,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAddActivityDialog.value = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.Blue)
                    ) {
                        Text(
                            text = "Add Activity",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Add Activity Dialog
        if (showAddActivityDialog.value) {
            AddActivityDialog(
                newActivity = newActivity,
                onConfirm = {
                    viewModel.addActivity(newActivity.value.trim())
                    newActivity.value = ""
                    showAddActivityDialog.value = false
                },
                onDismiss = {
                    showAddActivityDialog.value = false
                }
            )
        }

        // Delete Activity Button positioned in the bottom-right corner
        Button(
            onClick = { showDeleteDialog.value = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(8.dp)
        ) {
            Text("Delete Activity")
        }

        // Show Delete Activity Dialog
        if (showDeleteDialog.value) {
            DeleteActivityDialog(
                activities = activities,
                onDelete = { selectedActivities ->
                    selectedActivities.forEach { viewModel.deleteActivity(it) }
                    showDeleteDialog.value = false
                },
                onDismiss = { showDeleteDialog.value = false }
            )
        }
    }
}



// Helper function to format elapsed time
@SuppressLint("DefaultLocale")
private fun formatElapsedTime(seconds: Long): String {
    return when {
        seconds >= 3600 -> {
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            String.format("%d hr %02d min", hours, minutes)
        }
        seconds >= 60 -> {
            val minutes = seconds / 60
            val remainingSeconds = seconds % 60
            String.format("%d min %02d sec", minutes, remainingSeconds)
        }
        else -> {
            String.format("%d sec", seconds)
        }
    }
}

@Composable
fun AddActivityDialog(
    newActivity: MutableState<String>,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var errorMessage by remember { mutableStateOf<String?>(null) } // Track validation errors

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Add New Activity") },
        text = {
            Column {
                TextField(
                    value = newActivity.value,
                    onValueChange = {
                        newActivity.value = it
                        errorMessage = null // Reset error message on input change
                    },
                    label = { Text("Activity Name") },
                    isError = errorMessage != null // Highlight input field if there's an error
                )
                errorMessage?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (newActivity.value.isBlank()) {
                    errorMessage = "Activity name cannot be empty."
                } else if (newActivity.value.length > 20) {
                    errorMessage = "Activity name cannot exceed 20 characters."
                } else {
                    onConfirm()
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun DeleteActivityDialog(
    activities: List<Activity>,
    onDelete: (List<Activity>) -> Unit,
    onDismiss: () -> Unit
) {
    val selectedActivities = remember { mutableStateListOf<Activity>() }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Delete Activities") },
        text = {
            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) { // Limit height for scrolling
                items(activities) { activity ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically, // Align checkbox and text vertically
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 8.dp) // Add spacing between rows
                    ) {
                        Checkbox(
                            checked = selectedActivities.contains(activity),
                            onCheckedChange = { isChecked ->
                                if (isChecked) {
                                    selectedActivities.add(activity)
                                } else {
                                    selectedActivities.remove(activity)
                                }
                            }
                        )
                        Spacer(modifier = Modifier.width(8.dp)) // Add spacing between checkbox and text
                        Text(
                            text = activity.name,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { onDelete(selectedActivities) }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}