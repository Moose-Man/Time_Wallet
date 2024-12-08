package com.example.time_wallet_3.view.StatisticsActivity

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavHostController
import com.example.time_wallet_3.viewmodel.viewmodel
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener


fun generateColorForActivity(activity: String): Color {
    val random = Random(activity.hashCode())
    return Color(
        red = random.nextInt(100, 255),
        green = random.nextInt(100, 255),
        blue = random.nextInt(100, 255)
    )
}

object PieChartUtils {
    fun isSameDay(date1: Date, date2: Date): Boolean {
        val calendar1 = Calendar.getInstance().apply { time = date1 }
        val calendar2 = Calendar.getInstance().apply { time = date2 }

        return calendar1.get(Calendar.YEAR) == calendar2.get(Calendar.YEAR) &&
                calendar1.get(Calendar.DAY_OF_YEAR) == calendar2.get(Calendar.DAY_OF_YEAR)
    }
}

fun formatElapsedTime(timeInMillis: Long): String {
    val totalMinutes = (timeInMillis / 1000 / 60).toInt()
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    return "${hours}h ${minutes}m"
}

@Composable
fun StatisticsScreen(viewModel: viewmodel, navController: NavHostController) {
    val context = LocalContext.current
    val logs = viewModel.logs.collectAsState().value
    val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())

    // State to track selected activity for filtering
    val (selectedActivity, setSelectedActivity) = remember { mutableStateOf<String?>(null) }

    // State for navigation option and selected date
    var selectedView by remember { mutableStateOf("Daily") }
    var selectedDate by remember { mutableStateOf(Date()) }

    // Get calendar instance for calculations
    val calendar = Calendar.getInstance().apply { time = selectedDate }
    val today = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.time

    // Date range based on selected view
    val dateRange = when (selectedView) {
        "Weekly" -> {
            calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
            val startOfWeek = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_WEEK, 6)
            val endOfWeek = calendar.timeInMillis
            startOfWeek..endOfWeek
        }
        "Monthly" -> {
            calendar.set(Calendar.DAY_OF_MONTH, 1)
            val startOfMonth = calendar.timeInMillis
            calendar.add(Calendar.MONTH, 1)
            calendar.add(Calendar.DAY_OF_MONTH, -1)
            val endOfMonth = calendar.timeInMillis
            startOfMonth..endOfMonth
        }
        "Total" -> {
            Long.MIN_VALUE..Long.MAX_VALUE
        }
        else -> { // "Daily"
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis
            val endOfDay = startOfDay + 24 * 60 * 60 * 1000 - 1
            startOfDay..endOfDay
        }
    }

    // Filter logs based on the selected date range and activity
    val filteredLogs = logs.filter { log ->
        log.timeStarted in dateRange && (selectedActivity == null || log.activity == selectedActivity)
    }

    // Group filtered logs by activity and calculate total time
    val activityTotals = filteredLogs.groupBy { it.activity }.mapValues { (_, logs) ->
        logs.sumOf { it.elapsedTime }
    }

    // Create PieEntry objects with formatted labels
    val pieEntries = activityTotals.map { (activity, timeInSeconds) ->
        val timeInMillis = timeInSeconds * 1000L
        PieEntry(timeInSeconds.toFloat(), activity) // Use only the activity name as the label
    }

    // Configure the PieDataSet
    val pieDataSet = PieDataSet(pieEntries, "Activities").apply {
        colors = ColorTemplate.COLORFUL_COLORS.toList()
        valueTextSize = 12f
    }

    // Hide raw values by using a custom ValueFormatter
    pieDataSet.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
        override fun getFormattedValue(value: Float): String = ""
    }

    val pieData = PieData(pieDataSet)

    // Compose UI
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Navigation bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            listOf("Daily", "Weekly", "Monthly", "Total").forEach { view ->
                Text(
                    text = view,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selectedView == view) MaterialTheme.colorScheme.primary else Color.Gray,
                    modifier = Modifier
                        .clickable { selectedView = view; selectedDate = Date() } // Reset date to today
                        .padding(8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display the selected date
        Text(
            text = when (selectedView) {
                "Weekly" -> {
                    val startDate = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(dateRange.start))
                    val endDate = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(dateRange.endInclusive))
                    "$startDate - $endDate"
                }
                "Monthly" -> SimpleDateFormat("yyyy/MM", Locale.getDefault()).format(selectedDate)
                "Total" -> "All Time"
                else -> sdf.format(selectedDate)
            },
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Row containing arrows and pie chart
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Left arrow (visible only for Daily, Weekly, and Monthly views)
            if (selectedView != "Total") {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Previous",
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            when (selectedView) {
                                "Daily" -> calendar.add(Calendar.DAY_OF_YEAR, -1)
                                "Weekly" -> calendar.add(Calendar.WEEK_OF_YEAR, -1)
                                "Monthly" -> calendar.add(Calendar.MONTH, -1)
                            }
                            selectedDate = calendar.time
                        }
                )
            }

            // Pie chart
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                factory = { PieChart(context).apply {
                    data = pieData
                    setHoleColor(android.graphics.Color.BLACK) // Set the hole color to black
                    setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
                        override fun onValueSelected(e: Entry?, h: Highlight?) {
                            if (e is PieEntry) {
                                val activityName = e.label // Extract the activity name
                                setSelectedActivity(if (selectedActivity == activityName) null else activityName)
                            }
                        }

                        override fun onNothingSelected() {
                            setSelectedActivity(null)
                        }
                    })
                }},
                update = { pieChart ->
                    pieChart.data = pieData
                    pieChart.invalidate() // Refresh the chart
                }
            )

            // Right arrow (visible only if navigating to earlier dates)
            if (selectedView != "Total" && selectedDate.before(today)) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Next",
                    modifier = Modifier
                        .size(32.dp)
                        .clickable {
                            when (selectedView) {
                                "Daily" -> calendar.add(Calendar.DAY_OF_YEAR, 1)
                                "Weekly" -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
                                "Monthly" -> calendar.add(Calendar.MONTH, 1)
                            }
                            selectedDate = calendar.time
                        }
                )
            }
        }

        // LazyColumn to display logs
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredLogs) { log ->
                Text(
                    text = "Activity: ${log.activity}, Time: ${formatElapsedTime(log.elapsedTime * 1000L)}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}







