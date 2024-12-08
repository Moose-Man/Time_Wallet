package com.example.time_wallet_3.view.TimeLogsActivity

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.time_wallet_3.model.TimeLog
import com.example.time_wallet_3.viewmodel.viewmodel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@Composable
fun HeaderSection(viewModel: viewmodel, navController: NavHostController) {
    val totalPoints = viewModel.totalPoints.collectAsState(initial = 0)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Blue) // Set blue background color
            .padding(16.dp) // Padding inside the box
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Badge Icon
            IconButton(onClick = { navController.navigate("achievements") }) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Achievements",
                    modifier = Modifier.size(32.dp),
                    tint = Color.White // Adjust icon color for better contrast
                )
            }

            // Points Badge
            Card(
                shape = RoundedCornerShape(50),
                modifier = Modifier.wrapContentSize()
            ) {
                Text(
                    text = "${totalPoints.value} points",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = Color.Black // Adjust text color for better readability
                )
            }

            // Filter Icon
            IconButton(onClick = { /* Add action for filtering */ }) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Settings",
                    modifier = Modifier.size(24.dp),
                    tint = Color.White // Adjust icon color for better contrast
                )
            }
        }
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
fun LogItem(log: TimeLog, navController: NavHostController) {
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
