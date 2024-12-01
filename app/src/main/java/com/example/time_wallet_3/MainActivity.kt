package com.example.time_wallet_3

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.getValue

@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Time_Wallet_3Theme {
                val navController = rememberNavController()
                val sharedViewModel: viewmodel_TimeLog = viewModel() // Shared ViewModel instance
                AppWithBottomNavigation(navController, sharedViewModel)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppWithBottomNavigation(navController: NavHostController, sharedViewModel: viewmodel_TimeLog) {
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
fun AppNavigation(navController: NavHostController, sharedViewModel: viewmodel_TimeLog) {
    NavHost(
        navController = navController,
        startDestination = "view_logs"
    ) {
        composable("view_logs") { ViewLogsScreen(navController, sharedViewModel) }
        composable("create_log") { CreateLogScreen(navController, sharedViewModel) }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ViewLogsScreen(navController: NavHostController, viewModel: viewmodel_TimeLog) {
    val logs = viewModel.logs.collectAsState(initial = emptyList())
    val groupedLogs = logs.value.groupBy { it.date }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("create_log") },
                modifier = Modifier
                    //.align(Alignment.BottomEnd)
                    .padding(16.dp)
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
    ) {innerPadding ->
        // Apply Scaffold's innerPadding to the Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Use innerPadding for proper layout
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding() // Allow content to slide under the navigation bar
            ) {
                groupedLogs.forEach { (date, logsForDate) ->
                    item {
                        DateHeaderCard(date)
                    }
                    items(logsForDate) { log ->
                        LogItem(log)
                    }
                }
            }
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
    val dayOfWeek = LocalDate.parse(date, formatter).dayOfWeek.name.capitalize()

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

@Composable
fun LogItem(log: UserTimeLog) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 5.dp)
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
    val timeElapsed by viewModel.timeElapsed.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    val customDate = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
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

        TextField(
            value = customDate.value,
            onValueChange = { customDate.value = it },
            label = { Text("Custom Date (yyyy-MM-dd)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
        Text("Elapsed Time: ${timeElapsed}s", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Button(
                onClick = { viewModel.startTimer() },
                enabled = !isTimerRunning
            ) {
                Text("Start Timer")
            }
            Spacer(modifier = Modifier.width(16.dp))
            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = { viewModel.stopTimer() },
                enabled = isTimerRunning
            ) {
                Text("Stop Timer")
            }
        }

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
