package com.example.time_wallet_3.view.TimeLogs

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.time_wallet_3.viewmodel.viewmodel

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
                Text(
                    "Elapsed Time: ${formatElapsedTime(log.elapsedTime)}, from ${formatTime(log.timeStarted)} to ${formatTime(log.timeStopped)}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Notes: ${log.notes}", style = MaterialTheme.typography.bodyLarge)
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

// Helper function to format elapsed time
private fun formatElapsedTime(elapsedTimeSeconds: Long): String {
    val minutes = (elapsedTimeSeconds % 3600) / 60
    val hours = elapsedTimeSeconds / 3600
    return if (hours > 0) {
        "$hours hour${if (hours > 1) "s" else ""} $minutes minute${if (minutes != 1L) "s" else ""}"
    } else {
        "$minutes minute${if (minutes != 1L) "s" else ""}"
    }
}
