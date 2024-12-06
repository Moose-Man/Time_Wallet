package com.example.time_wallet_3.view.TimeLogsActivity

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.time_wallet_3.viewmodel.viewmodel

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