package com.example.time_wallet_3

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.time_wallet_3.model.UserTimeLog
import com.example.time_wallet_3.ui.theme.Time_Wallet_3Theme
import com.example.time_wallet_3.viewmodel.viewmodel_TimeLog

@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Time_Wallet_3Theme {
                val navController = rememberNavController()
                val sharedViewModel: viewmodel_TimeLog = viewModel() // Shared ViewModel instance
                AppNavigation(navController, sharedViewModel)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(navController: NavHostController, sharedViewModel: viewmodel_TimeLog) {
    NavHost(
        navController = navController,
        startDestination = "view_logs"
    ) {
        composable("view_logs") { ViewLogsScreen(navController, sharedViewModel) }
        composable("create_log") { CreateLogScreen(navController, sharedViewModel) }
    }
}

@Composable
fun ViewLogsScreen(navController: NavHostController, viewModel: viewmodel_TimeLog) {
    val logs = viewModel.logs.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "View Logs", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            items(logs.value) { log: UserTimeLog ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text("Time: ${log.elapsedTime}s", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Activity: ${log.activity}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Points: ${log.points}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.navigate("create_log") }) {
            Text("Create New Log")
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CreateLogScreen(navController: NavHostController, viewModel: viewmodel_TimeLog) {
    val activity = remember { mutableStateOf("") }
    val note = remember { mutableStateOf("") }
    val timeElapsed by viewModel.timeElapsed.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Create New Log", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = activity.value,
            onValueChange = { activity.value = it },
            label = { Text("Activity Name") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        TextField(
            value = note.value,
            onValueChange = { note.value = it },
            label = { Text("Notes") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Elapsed Time: ${timeElapsed}s", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(
                onClick = { viewModel.startTimer(System.currentTimeMillis()) },
                enabled = !isTimerRunning
            ) {
                Text("Start Timer")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(
                onClick = { viewModel.stopTimer(System.currentTimeMillis()) },
                enabled = isTimerRunning
            ) {
                Text("Stop Timer")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            viewModel.addLog(activity.value, note.value)
            navController.navigate("view_logs") // Navigate back to View Logs screen
        }) {
            Text("Save Log")
        }
    }
}



//@RequiresApi(Build.VERSION_CODES.O)
//@Composable
//fun MyApp(name: String, modifier: Modifier = Modifier) {
//    val viewModel: viewmodel_TimeLog = viewModel()
//
//    // State to hold the list of logs
//    var logs by remember { mutableStateOf(listOf<UserTimeLog>()) }
//
//    // Display UI
//    TimeLogScreen(
//        points = viewModel.points,
//        activity = viewModel.activity,
//        note = viewModel.note,
//        timeElapsed = viewModel.timeElapsed, // Time elapsed in seconds
//        logs = logs, // Pass the logs to the UI
//        onTimerStart = {
//            viewModel.updateStartTime(System.currentTimeMillis())
//        },
//        onTimerStop = {
//            viewModel.updateEndTime(System.currentTimeMillis()) // Stop the timer
//            logs = logs + UserTimeLog(
//                elapsedTime = viewModel.timeElapsed, // Use the recalculated time
//                activity = viewModel.activity,
//                points = viewModel.points
//            )
//        },
//        onNoteChange = { viewModel.updateNote(it) },
//    )
//}

//@Composable
//fun TimeLogScreen(
//    points: Int,
//    timeElapsed: MutableStateFlow<Long>,
//    activity: String,
//    note: String,
//    logs: List<UserTimeLog>,
//    onTimerStart: () -> Unit,
//    onTimerStop: () -> Unit,
//    onNoteChange: (String) -> Unit
//) {
//    var isTimerRunning by remember { mutableStateOf(false) }
//
//    Column(modifier = Modifier.padding(16.dp)) {
//        Text(
//            text = "Time Tracking App",
//            style = MaterialTheme.typography.headlineSmall
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Text("Activity: $activity", style = MaterialTheme.typography.bodyLarge)
//        Text("Points: $points", style = MaterialTheme.typography.bodyLarge)
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Row {
//            Button(onClick = {
//                if (!isTimerRunning) {
//                    onTimerStart()
//                    isTimerRunning = true
//                }
//            }) {
//                Text("Start Timer")
//            }
//
//            Spacer(modifier = Modifier.width(16.dp))
//
//            Button(onClick = {
//                if (isTimerRunning) {
//                    onTimerStop()
//                    isTimerRunning = false
//                }
//            }) {
//                Text("Stop Timer")
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        TextField(
//            value = note,
//            onValueChange = { newNote -> onNoteChange(newNote) },
//            label = { Text("Add Note") },
//            modifier = Modifier.fillMaxWidth()
//        )
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        Text("Elapsed Time: ${timeElapsed}s", style = MaterialTheme.typography.bodyLarge)
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Display logs in a scrollable LazyColumn
//        Text("Session Logs:", style = MaterialTheme.typography.titleLarge)
//        Spacer(modifier = Modifier.height(8.dp))
//
//        LazyColumn(
//            modifier = Modifier
//                .fillMaxWidth()
//                .weight(1f) // Allow scrolling within available space
//        ) {
//            items(logs) { log ->
//                Row(modifier = Modifier.padding(vertical = 4.dp)) {
//                    Text("Time: ${log.elapsedTime}s", style = MaterialTheme.typography.bodyMedium)
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text("Activity: ${log.activity}", style = MaterialTheme.typography.bodyMedium)
//                    Spacer(modifier = Modifier.width(8.dp))
//                    Text("Points: ${log.points}", style = MaterialTheme.typography.bodyMedium)
//                }
//            }
//        }
//    }
//}
