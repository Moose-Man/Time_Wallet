package com.example.time_wallet_3

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.time_wallet_3.model.UserTimeLog
import com.example.time_wallet_3.ui.theme.Time_Wallet_3Theme
import com.example.time_wallet_3.viewmodel.viewmodel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.material.BottomNavigation
//noinspection UsingMaterialAndMaterial3Libraries
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModelProvider
import com.example.time_wallet_3.model.DatabaseInstance
import com.example.time_wallet_3.model.UserActivity
import com.example.time_wallet_3.viewmodel.ViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use the unified database instance
        val database = DatabaseInstance.getDatabase(application)
        val timeLogDao = database.userTimeLogDao()
        val activitiesDao = database.userActivityDao()

        val viewModelFactory = ViewModelFactory(timeLogDao, activitiesDao)
        val sharedViewModel: viewmodel = ViewModelProvider(this, viewModelFactory)[viewmodel::class.java]

        setContent {
            Time_Wallet_3Theme {
                val navController = rememberNavController()
                AppWithBottomNavigation(navController, sharedViewModel)
            }
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppWithBottomNavigation(navController: NavHostController, sharedViewModel: viewmodel) {
    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AppNavigation(navController, sharedViewModel)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(navController: NavHostController, sharedViewModel: viewmodel) {
    NavHost(
        navController = navController,
        startDestination = "view_logs"
    ) {
        composable("view_logs") { ViewLogsScreen(navController, sharedViewModel) }
        composable("create_log") { CreateLogScreen(navController, sharedViewModel) }
        composable("log_inspection/{logId}") { backStackEntry ->
            val logId = backStackEntry.arguments?.getString("logId")?.toIntOrNull()
            if (logId != null) {
                LogInspectionScreen(navController, sharedViewModel, logId)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ViewLogsScreen(navController: NavHostController, viewModel: viewmodel) {
    val logs = viewModel.logs.collectAsState(initial = emptyList())
    val groupedLogs = logs.value.groupBy { it.date }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("create_log") },
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "+",
                    style = TextStyle(
                        fontSize = 50.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Adjust for Scaffold's padding
        ) {
            // Header Section
            HeaderSection(viewModel)

            // LazyColumn for Logs
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding() // Allow content to slide under the navigation bar
            ) {
                groupedLogs.forEach { (date, logsForDate) ->
                    item {
                        DateHeaderCard(date)
                    }
                    val sortedLogsForDate = logsForDate.sortedBy { it.timeStopped }
                    items(sortedLogsForDate) { log ->
                        LogItem(log = log, navController = navController)
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection(viewModel: viewmodel) {
    val totalPoints = viewModel.totalPoints.collectAsState(initial = 0)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp), // Padding around the header
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Points Badge
        Card(
            shape = RoundedCornerShape(50),
            modifier = Modifier.wrapContentSize()
        ) {
            Text(
                text = "${totalPoints.value} points", // Replace with dynamic value if needed
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        // Filter Icon
        IconButton(onClick = { /* Add action for filtering */ }) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Settings",
                modifier = Modifier.size(24.dp)
            )
        }
    }
}


@Composable
fun BottomNavigationBar(navController: NavHostController) {
    BottomNavigation {
        BottomNavigationItem(
            selected = false, // Replace with logic to check the current screen
            onClick = { navController.navigate("view_logs") },
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Logs") },
            label = { Text("Logs") }
        )
        BottomNavigationItem(
            selected = false, // Replace with logic to check the current screen
            onClick = { navController.navigate("create_log") },
            icon = { Icon(Icons.Default.Add, contentDescription = "Create") },
            label = { Text("Create") }
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateHeaderCard(date: String) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val dayOfWeek = LocalDate.parse(date, formatter).dayOfWeek.name.replaceFirstChar {
        if (it.isLowerCase()) it.titlecase(
            Locale.ROOT
        ) else it.toString()
    }

    Card(
        modifier = Modifier
            .wrapContentSize()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(8.dp),
        shape = RoundedCornerShape(0.dp) // no rounding
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "$dayOfWeek, $date",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault()) // e.g., 14:45
    return sdf.format(Date(timestamp))
}

@Composable
fun LogItem(log: UserTimeLog, navController: NavHostController) {
    val customGreen = Color(0xFF4CAF50) // Replace with your desired color code
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 6.dp)
            .clickable {
                navController.navigate("log_inspection/${log.id}") // Pass the log ID
            }
    ) {
        Text(
            text = log.activity,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${formatTime(log.timeStarted)}-${formatTime(log.timeStopped)}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "${log.points}",
            style = MaterialTheme.typography.bodyMedium,
            color = customGreen,
            modifier = Modifier.weight(1f)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LogInspectionScreen(navController: NavHostController, viewModel: viewmodel, logId: Int) {
    val log = viewModel.getLogById(logId).collectAsState(initial = null).value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 64.dp) // Reserve space for the delete button
        ) {
            Text(
                text = "Log Details",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (log != null) {
                Text("Activity: ${log.activity}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Elapsed Time: ${log.elapsedTime}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Notes: ${log.notes}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Start Time: ${formatTime(log.timeStarted)}", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("End Time: ${formatTime(log.timeStopped)}", style = MaterialTheme.typography.bodyLarge)
            } else {
                Text("Log not found.", style = MaterialTheme.typography.bodyLarge)
            }
        }

        // Delete Log Button
        Button(
            onClick = {
                if (log != null) {
                    viewModel.deleteLog(log) // Delete the log from the database
                    navController.navigate("view_logs") // Navigate back to the logs screen
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Text("Delete Log")
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CreateLogScreen(navController: NavHostController, viewModel: viewmodel) {
    val note = remember { mutableStateOf("") }
    val customDate = remember { mutableStateOf("") }
    val newActivity = remember { mutableStateOf("") }
    val timeElapsed by viewModel.timeElapsed.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val selectedActivity = remember { mutableStateOf("") }
    val showAddActivityDialog = remember { mutableStateOf(false) }
    val showDeleteDialog = remember { mutableStateOf(false) }
    // Fetch activities from the database
    val activities by viewModel.activities.collectAsState(initial = emptyList()) // Observes database changes

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Title
            Text(
                text = "Log Time", // Title text
                style = MaterialTheme.typography.headlineSmall, // Use an appropriate style
                modifier = Modifier.padding(bottom = 16.dp) // Add spacing below the title
            )

            // Notes TextField
            TextField(
                value = note.value,
                onValueChange = { note.value = it },
                label = { Text("Notes") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Custom Date Field
            TextField(
                value = customDate.value,
                onValueChange = { customDate.value = it },
                label = { Text("Custom Date (yyyy-MM-dd)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Elapsed time display
            Text(
                text = "Elapsed Time: ${formatElapsedTime(timeElapsed)}",
                style = MaterialTheme.typography.bodyLarge
            )

            // Start and Stop Timer Buttons
            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Button(
                    onClick = { viewModel.startTimer() },
                    enabled = !isTimerRunning
                ) {
                    Text("Start Timer")
                }
                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = {
                        viewModel.stopTimer()
                        if (customDate.value.isNotEmpty()) {
                            viewModel.setSimulatedDate(customDate.value)
                        }
                        viewModel.addLog(selectedActivity.value, note.value) // Use selected activity
                        navController.navigate("view_logs")
                    },
                    enabled = isTimerRunning
                ) {
                    Text("Stop Timer")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Activity Grid Header
            Text("Select Activity:", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(16.dp))

            // LazyVerticalGrid for Activities
            LazyVerticalGrid(
                columns = GridCells.Fixed(3), // Number of columns
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f), // Ensure the grid takes remaining space
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(activities) { activity ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedActivity.value = activity.name },
                        shape = RoundedCornerShape(0.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedActivity.value == activity.name) Color.Green else Color.LightGray
                        )
                    ) {
                        Text(
                            text = activity.name,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                // Add Activity Card
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showAddActivityDialog.value = true },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Blue
                        )
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
    activities: List<UserActivity>,
    onDelete: (List<UserActivity>) -> Unit,
    onDismiss: () -> Unit
) {
    val selectedActivities = remember { mutableStateListOf<UserActivity>() }

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





