package com.example.time_wallet_3

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.ui.Alignment


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

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ViewLogsScreen(navController: NavHostController, viewModel: viewmodel_TimeLog) {
    val logs = viewModel.logs.collectAsState(initial = emptyList())

    // Group logs by date
    val groupedLogs = logs.value.groupBy { it.date }

    // Use Box to overlay the button at the bottom of the screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 64.dp) // Reserve space for the button
        ) {
            Text(
                text = "View Logs",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                groupedLogs.forEach { (date, logsForDate) ->
                    // Header for each date
                    item {
                        DateHeaderCard(date)
                    }

                    // Logs under each header
                    items(logsForDate) { log ->
                        LogItem(log)
                    }
                }
            }
        }

        // Button at the bottom of the screen
        Button(
            onClick = { navController.navigate("create_log") },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Text("Create New Log")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateHeaderCard(date: String) {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    val dayOfWeek = LocalDate.parse(date, formatter).dayOfWeek.name.capitalize() // Get day of the week

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Card(
            modifier = Modifier
                //.align(Alignment.CenterHorizontally) // cant get the card
                .wrapContentSize(), // Slightly narrower than full width
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp) // Padding inside the card
            ) {
                Text(
                    text = "$dayOfWeek, $date",
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@Composable
fun LogItem(log: UserTimeLog) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = "Time: ${log.elapsedTime}s",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Activity: ${log.activity}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = "Points: ${log.points}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun CreateLogScreen(navController: NavHostController, viewModel: viewmodel_TimeLog) {
    val activity = remember { mutableStateOf("") }
    val note = remember { mutableStateOf("") }
    val customDate = remember { mutableStateOf("") }

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

        Spacer(modifier = Modifier.height(8.dp))

        // Field for custom date
        TextField(
            value = customDate.value,
            onValueChange = { customDate.value = it },
            label = { Text("Custom Date (yyyy-MM-dd)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (customDate.value.isNotEmpty()) {
                viewModel.setSimulatedDate(customDate.value)
            }
            viewModel.addLog(activity.value, note.value)
            navController.navigate("view_logs")
        }) {
            Text("Save Log")
        }
    }
}
