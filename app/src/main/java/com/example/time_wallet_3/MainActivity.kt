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
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.ViewModelProvider
import com.example.time_wallet_3.model.DatabaseInstance
import com.example.time_wallet_3.viewmodel.ViewModelFactory

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
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
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
            HeaderSection()

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
                    items(logsForDate) { log ->
                        LogItem(log)
                    }
                }
            }
        }
    }
}

@Composable
fun HeaderSection() {
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
                text = "240 points", // Replace with dynamic value if needed
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
fun CreateLogScreen(navController: NavHostController, viewModel: viewmodel) {
    val note = remember { mutableStateOf("") }
    val customDate = remember { mutableStateOf("") }
    val newActivity = remember { mutableStateOf("") }
    val timeElapsed by viewModel.timeElapsed.collectAsState()
    val isTimerRunning by viewModel.isTimerRunning.collectAsState()
    // Fetch activities from the database
    val activities by viewModel.activities.collectAsState(initial = emptyList()) // Observes database changes
    val mExpanded = remember { mutableStateOf(false) }
    val mSelectedText = remember { mutableStateOf("") }
    val mTextFieldSize = remember { mutableStateOf(Size.Zero) }

    // Up Icon when expanded and down icon when collapsed
    val icon = if (mExpanded.value)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Create New Log", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        // Dropdown for Activity Selection
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = mSelectedText.value,
                onValueChange = { /* No direct input */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .onGloballyPositioned { coordinates ->
                        mTextFieldSize.value = coordinates.size.toSize()
                    },
                label = { Text("Activity") },
                trailingIcon = {
                    Icon(
                        icon,
                        contentDescription = null,
                        Modifier.clickable { mExpanded.value = !mExpanded.value }
                    )
                },
                readOnly = true // Prevent manual input
            )

            DropdownMenu(
                expanded = mExpanded.value,
                onDismissRequest = { mExpanded.value = false },
                modifier = Modifier.width(with(LocalDensity.current) { mTextFieldSize.value.width.toDp() })
            ) {
                activities.forEach { activity ->
                    DropdownMenuItem(
                        onClick = {
                            mSelectedText.value = activity.name // Update selected activity
                            mExpanded.value = false // Close dropdown
                        },
                        text = {
                            Row(
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(text = activity.name)
                                IconButton(
                                    onClick = {
                                        viewModel.deleteActivity(activity) // Interact with the database
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Activity",
                                        tint = Color.Red
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Input for Adding New Activity
        OutlinedTextField(
            value = newActivity.value,
            onValueChange = { newActivity.value = it },
            label = { Text("New Activity") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            if (newActivity.value.isNotBlank()) {
                viewModel.addActivity(newActivity.value.trim()) // Add activity to the database
                newActivity.value = "" // Clear input field
            }
        }) {
            Text("Add Activity")
        }

        Spacer(modifier = Modifier.height(8.dp))

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

        // Save Log Button
        Button(onClick = {
            if (customDate.value.isNotEmpty()) {
                viewModel.setSimulatedDate(customDate.value)
            }
            viewModel.addLog(mSelectedText.value, note.value) // Use selected activity
            navController.navigate("view_logs")
        }) {
            Text("Save Log")
        }
    }
}


