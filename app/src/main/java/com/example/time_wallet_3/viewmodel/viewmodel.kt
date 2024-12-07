package com.example.time_wallet_3.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.time_wallet_3.model.Activity
import com.example.time_wallet_3.model.ActivityDao
import com.example.time_wallet_3.model.Budget
import com.example.time_wallet_3.model.TimeLog
import com.example.time_wallet_3.model.TimeLogDao
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class viewmodel(private val dao: TimeLogDao, private val ActivityDao: ActivityDao) : ViewModel() {

    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
    val budgets: StateFlow<List<Budget>> = _budgets
    val activities: Flow<List<Activity>> = ActivityDao.getAllActivities()
    private var simulatedDate: LocalDate? = null // For testing purposes
    @RequiresApi(Build.VERSION_CODES.O)
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private var startTime: Long = 0L
    private var timerJob: Job? = null // Job to manage the timer coroutine
    val timeElapsed = MutableStateFlow(0L) // Elapsed time in seconds
    val isTimerRunning = MutableStateFlow(false) // Timer running state

    //val logs: Flow<List<TimeLog>> = dao.getAllLogs()

    private val _logs = MutableStateFlow<List<TimeLog>>(emptyList())
    val logs: StateFlow<List<TimeLog>> get() = _logs

    init {
        viewModelScope.launch {
            dao.getAllLogs().collect { fetchedLogs ->
                _logs.value = fetchedLogs
            }
        }
    }


    /**
     * Starts the timer and updates elapsed time in real-time.
     */
    fun startTimer() {
        if (!isTimerRunning.value) {
            startTime = System.currentTimeMillis()
            isTimerRunning.value = true
            timerJob = viewModelScope.launch {
                while (isTimerRunning.value) {
                    timeElapsed.value = (System.currentTimeMillis() - startTime) / 1000
                    delay(1000) // Update every second
                }
            }
        }
    }

    fun saveBudgetLimits(limits: Map<String, Int>) {
        viewModelScope.launch {
            limits.forEach { (activityName, limit) ->
                val activity = getActivityByName(activityName)
                if (activity != null) {
                    // Update the activity with the new limit
                    activity.limit = limit
                    ActivityDao.updateActivity(activity)
                }
            }
        }
    }

    suspend fun getActivityByName(name: String): Activity? {
        return ActivityDao.getActivityByName(name) // Add this function in your DAO
    }

//    fun startTimer(simulatedSpeed: Int = 10) {
//        if (!isTimerRunning.value) {
//            startTime = System.currentTimeMillis()
//            isTimerRunning.value = true
//            timerJob = viewModelScope.launch {
//                while (isTimerRunning.value) {
//                    // Simulate faster time increment
//                    timeElapsed.value += simulatedSpeed
//                    delay(1000L / simulatedSpeed) // Adjust delay for faster updates
//                }
//            }
//        }
//    }


    /**
     * Stops the timer and finalizes elapsed time.
     */
    fun stopTimer() {
        if (isTimerRunning.value) {
            isTimerRunning.value = false
            timerJob?.cancel() // Cancel the timer coroutine
            timeElapsed.value = (System.currentTimeMillis() - startTime) / 1000
        }
    }

    // Function to calculate total points
    val totalPoints: Flow<Int> = logs.map { logs ->
        logs.sumOf { it.points }
    }

    fun addActivity(name: String) {
        viewModelScope.launch {
            ActivityDao.insertActivity(Activity(name = name))
        }
    }

    fun deleteActivity(activity: Activity) {
        viewModelScope.launch {
            ActivityDao.deleteActivity(activity)
        }
    }

    fun deleteLog(log: TimeLog) {
        viewModelScope.launch {
            dao.deleteLog(log)
        }
    }

    fun getLogById(logId: Int): Flow<TimeLog?> {
        return dao.getLogById(logId)
    }

    // Simulate a specific date (for testing purposes)
    @RequiresApi(Build.VERSION_CODES.O)
    fun setSimulatedDate(date: String) {
        simulatedDate = LocalDate.parse(date, formatter)
    }

    // Get the current date (real or simulated)
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getCurrentDate(): LocalDate {
        return simulatedDate ?: LocalDate.now()
    }

    /**
     * Adds a new log and resets the timer state.
     */

    @RequiresApi(Build.VERSION_CODES.O)
    fun addLog(activity: String, note: String) {
        val currentDate = getCurrentDate().format(formatter)
        val newLog = TimeLog(
            elapsedTime = timeElapsed.value,
            activity = activity,
            points = calculatePoints(timeElapsed.value),
            date = currentDate,
            notes = note, // Adding the note field
            timeStarted = startTime, // Adding the time started field
            timeStopped = System.currentTimeMillis() // Adding the time stopped field
        )
        viewModelScope.launch {
            dao.insertLog(newLog)
        }
        resetTimerState()
    }

    /**
     * Resets the timer state.
     */
    private fun resetTimerState() {
        startTime = 0L
        timeElapsed.value = 0L
        isTimerRunning.value = false
        timerJob?.cancel()
    }

    /**
     * Example points calculation logic.
     */
    private fun calculatePoints(elapsedTime: Long): Int {
        return (elapsedTime / 60).toInt() // 1 point per minute
    }

    fun addBudget(activityName: String, timeLimit: Int?, period: String) {
        if (timeLimit != null) {
            val newBudget = Budget(activityName = activityName, timeLimit = timeLimit, period = period)
            _budgets.value = _budgets.value + newBudget
        } else {
            // Handle the case where timeLimit is null, e.g., log an error or provide a default value
            println("Time limit cannot be null")
        }
    }

    fun updateBudgetProgress(activityName: String, timeElapsed: Long) {
        val timeInHours = (timeElapsed.toDouble()/3600)
        _budgets.value = _budgets.value.map { budget ->
            if (budget.activityName == activityName) {
                budget.copy(currentProgress = (budget.currentProgress + timeInHours).toInt())
            } else {
                budget
            }
        }
    }

    fun formatElapsedTime(timeElapsed: Long, timeLimit: Int): String {
        val totalMinutes = (timeElapsed / 1000 / 60).toInt()
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return "${hours}h${minutes}m/$timeLimit h"
    }

    fun getElapsedTimeForActivity(activityName: String): Long {
        // Replace with your logic to calculate total time for the given activity
        val logs = _logs.value.filter { it.activity == activityName }
        return logs.sumOf { it.elapsedTime } * 1000L // Convert seconds to milliseconds
    }

}