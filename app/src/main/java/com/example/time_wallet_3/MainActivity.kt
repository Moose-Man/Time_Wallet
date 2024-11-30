package com.example.time_wallet_3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.time_wallet_3.model.UserTimeLog
import com.example.time_wallet_3.ui.theme.Time_Wallet_3Theme
import com.example.time_wallet_3.viewmodel.viewmodel_TimeLog

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Time_Wallet_3Theme {
                Scaffold {
                    MyApp(
                        name = "Android",
                        modifier = Modifier.padding(it)
                    )
                }
            }
        }
    }
}

//@Composable
//fun MyApp(name: String, modifier: Modifier = Modifier) {
//    // create an instance of the viewmodel_TimeLog class
//    var viewModel: viewmodel_TimeLog = viewModel()
//
//    // display UI
//    TimeLogScreen(
//        points = viewModel.points,
//        activity = viewModel.activity,
//        note = viewModel.note,
//        onTimerStart = { viewModel.updateStartTime(it) },
//        onTimerStop = { viewModel.updateEndTime(it) },
//        timeEllapsed = { viewModel.calculateTimeEllapsed() },
//        onNoteChange = { viewModel.note = it }
//    )
//}

//@Composable
//fun TimeLogScreen(points: Int,
//                  timeEllapsed: () -> Unit,
//                  activity: String,
//                  note: String,
//                  onTimerStart: () -> Unit,
//                  onTimerStop: () -> Unit,
//                  onNoteChange: () -> Unit
//) {
//    // define UI elements here
//    Column() {
//        Text("Time tracking app")
//        Card(){
//            Text(text = "input stuff goes here")
//        }
//        Text("Points: $points") // display points
//        Button(onClick =)
//    }
//}

@Composable
fun MyApp(name: String, modifier: Modifier = Modifier) {
    val viewModel: viewmodel_TimeLog = viewModel()

    // State to hold the list of logs
    var logs by remember { mutableStateOf(listOf<UserTimeLog>()) }

    // Display UI
    TimeLogScreen(
        points = viewModel.points,
        activity = viewModel.activity,
        note = viewModel.note,
        timeElapsed = viewModel.timeElapsed, // Time elapsed in seconds
        logs = logs, // Pass the logs to the UI
        onTimerStart = {
            viewModel.updateStartTime(System.currentTimeMillis())
        },
        onTimerStop = {
            viewModel.updateEndTime(System.currentTimeMillis()) // Stop the timer
            logs = logs + UserTimeLog(
                elapsedTime = viewModel.timeElapsed, // Use the recalculated time
                activity = viewModel.activity,
                points = viewModel.points
            )
        },
        onNoteChange = { viewModel.updateNote(it) },
    )
}


//@Composable
//fun TimeLogScreen(
//    points: Int,
//    timeElapsed: Long,
//    activity: String,
//    note: String,
//    logs: List<UserTimeLog>, // List of log entries
//    onTimerStart: () -> Unit,
//    onTimerStop: () -> Unit,
//    onNoteChange: (String) -> Unit,
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
//        // Display activity and points
//        Text("Activity: $activity", style = MaterialTheme.typography.bodyLarge)
//        Text("Points: $points", style = MaterialTheme.typography.bodyLarge)
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Timer control buttons
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
//                    onTimerStop() //update logs
//                    isTimerRunning = false
//                }
//            }) {
//                Text("Stop Timer")
//            }
//        }
//
//        Spacer(modifier = Modifier.height(16.dp))
//
//        // Text input for the note
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
//        // Display logs
//        Text("Session Logs:", style = MaterialTheme.typography.titleLarge)
//        Spacer(modifier = Modifier.height(8.dp))
//
//        // Iterate over logs to display them
//        logs.forEach { log ->
//            Row(modifier = Modifier.padding(vertical = 4.dp)) {
//                Text("Time: ${log.elapsedTime}s", style = MaterialTheme.typography.bodyMedium)
//                Spacer(modifier = Modifier.width(8.dp))
//                Text("Activity: ${log.activity}", style = MaterialTheme.typography.bodyMedium)
//                Spacer(modifier = Modifier.width(8.dp))
//                Text("Points: ${log.points}", style = MaterialTheme.typography.bodyMedium)
//            }
//        }
//    }
//}
@Composable
fun TimeLogScreen(
    points: Int,
    timeElapsed: Long,
    activity: String,
    note: String,
    logs: List<UserTimeLog>,
    onTimerStart: () -> Unit,
    onTimerStop: () -> Unit,
    onNoteChange: (String) -> Unit
) {
    var isTimerRunning by remember { mutableStateOf(false) }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "Time Tracking App",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Activity: $activity", style = MaterialTheme.typography.bodyLarge)
        Text("Points: $points", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(16.dp))

        Row {
            Button(onClick = {
                if (!isTimerRunning) {
                    onTimerStart()
                    isTimerRunning = true
                }
            }) {
                Text("Start Timer")
            }

            Spacer(modifier = Modifier.width(16.dp))

            Button(onClick = {
                if (isTimerRunning) {
                    onTimerStop()
                    isTimerRunning = false
                }
            }) {
                Text("Stop Timer")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = note,
            onValueChange = { newNote -> onNoteChange(newNote) },
            label = { Text("Add Note") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Elapsed Time: ${timeElapsed}s", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(16.dp))

        // Display logs in a scrollable LazyColumn
        Text("Session Logs:", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Allow scrolling within available space
        ) {
            items(logs) { log ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text("Time: ${log.elapsedTime}s", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Activity: ${log.activity}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Points: ${log.points}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
