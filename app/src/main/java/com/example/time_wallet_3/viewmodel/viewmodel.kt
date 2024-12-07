package com.example.time_wallet_3.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.time_wallet_3.model.Activity
import com.example.time_wallet_3.model.ActivityDao
import com.example.time_wallet_3.model.Budget
import com.example.time_wallet_3.model.DatabaseInstance.budgetDao
import com.example.time_wallet_3.model.TimeLog
import com.example.time_wallet_3.model.TimeLogDao
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class viewmodel(private val dao: TimeLogDao, private val ActivityDao: ActivityDao) : ViewModel() {

    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
    val activities: Flow<List<Activity>> = ActivityDao.getAllActivities()
    private var simulatedDate: LocalDate? = null // For testing purposes
    @RequiresApi(Build.VERSION_CODES.O)
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private var startTime: Long = 0L
    private var timerJob: Job? = null // Job to manage the timer coroutine
    val timeElapsed = MutableStateFlow(0L) // Elapsed time in seconds
    val isTimerRunning = MutableStateFlow(false) // Timer running state
    private val _logs = MutableStateFlow<List<TimeLog>>(emptyList())
    val logs: StateFlow<List<TimeLog>> get() = _logs
    private val _totalPoints = MutableStateFlow(0) // Points tracked independently
    val totalPoints: Flow<Int> = dao.getTotalPoints() // Fetch total points from the database
    val budgets: Flow<List<Budget>> = budgetDao.getAllBudgets()



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

    fun resetBudgetsIfNeeded() {
        val now = System.currentTimeMillis()
        val startOfDay = getStartOfDayInMillis()
        val oneDayMillis = 24 * 60 * 60 * 1000L
        val oneWeekMillis = 7 * oneDayMillis
        val oneMonthMillis = 30 * oneDayMillis // Approximate month length

        _budgets.value = _budgets.value.map { budget ->
            val shouldReset = when (budget.period) {
                "Daily" -> budget.lastResetTime < startOfDay
                "Weekly" -> budget.lastResetTime < startOfDay - (now % oneWeekMillis)
                "Monthly" -> budget.lastResetTime < startOfDay - (now % oneMonthMillis)
                else -> false
            }

            if (shouldReset) {
                budget.copy(currentProgress = 0, lastResetTime = now)
            } else {
                budget
            }
        }
    }

//    fun resetBudgetProgress(budget: Budget) {
//        viewModelScope.launch {
//            val updatedBudget = budget.copy(currentProgress = 0, lastResetTime = System.currentTimeMillis())
//            updateBudget(updatedBudget)
//        }
//    }

    private fun getStartOfDayInMillis(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    init {
        viewModelScope.launch {
            while (true) {
                resetBudgetsIfNeeded()
                delay(1 * 1000L)
            }
        }
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
            points = calculatePoints(timeElapsed.value), // Points calculation during log creation
            date = currentDate,
            notes = note,
            timeStarted = startTime,
            timeStopped = System.currentTimeMillis()
        )
        viewModelScope.launch {
            dao.insertLog(newLog) // Persist points with the log
        }
        resetTimerState()
    }

    private fun handleBudgetExceed(activityName: String, elapsedTime: Long) {
        val relevantBudget = _budgets.value.find { it.activityName == activityName }
        relevantBudget?.let { budget ->
            val timeLimitInMinutes = budget.timeLimitMinutes
            val timeInMinutes = (elapsedTime / (1000 * 60)).toInt()
            if (timeInMinutes > timeLimitInMinutes) {
                val excessMinutes = timeInMinutes - timeLimitInMinutes
                _totalPoints.value -= excessMinutes * 5 // Deduct points for excess
            }
        }
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

    fun addBudget(activityName: String, timeLimitMinutes: Int, period: String) {
        viewModelScope.launch {
            val newBudget = Budget(
                activityName = activityName,
                timeLimitMinutes = timeLimitMinutes,
                period = period
            )
            budgetDao.insertBudget(newBudget)
        }
    }

    fun updateBudget(originalBudget: Budget, updatedBudget: Budget) {
        viewModelScope.launch {
            // Check if the activity name has changed
            val shouldReset = originalBudget.activityName != updatedBudget.activityName

            val finalBudget = if (shouldReset) {
                updatedBudget.copy(currentProgress = 0, lastResetTime = System.currentTimeMillis())
            } else {
                updatedBudget
            }

            // Update the budget in the database
            budgetDao.updateBudget(finalBudget)

            // Update the in-memory list of budgets
            _budgets.value = _budgets.value.map { budget ->
                if (budget.activityName == originalBudget.activityName) finalBudget else budget
            }
        }
    }



    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            budgetDao.deleteBudget(budget) // Remove the budget from the database
            _budgets.value = _budgets.value.filter { it != budget } // Update the local state
        }
    }

    init {
        viewModelScope.launch {
            budgetDao.getAllBudgets().collect { fetchedBudgets ->
                _budgets.value = fetchedBudgets
            }
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

    fun formatElapsedTime(timeElapsed: Long, timeLimitMinutes: Int): String {
        // Convert timeElapsed (milliseconds) to total minutes
        val totalMinutes = (timeElapsed / 1000 / 60).toInt()
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        // Convert timeLimitMinutes to hours and minutes
        val limitHours = timeLimitMinutes / 60
        val limitMinutes = timeLimitMinutes % 60

        return "${hours}h${minutes}m / ${limitHours}h${limitMinutes}m"
    }

    fun getElapsedTimeForActivity(activityName: String, lastResetTime: Long): Long {
        return _logs.value
            .filter { it.activity == activityName && it.timeStarted >= lastResetTime }
            .sumOf { it.elapsedTime } * 1000L // Convert seconds to milliseconds
    }

    fun calculatePointDeductions(logs: List<TimeLog>, budgets: List<Budget>): Int {
        var totalDeductions = 0
        budgets.forEach { budget ->
            val elapsedTimeForActivity = logs
                .filter { it.activity == budget.activityName }
                .sumOf { it.elapsedTime } // Sum elapsed time in seconds
            val timeLoggedInMinutes = (elapsedTimeForActivity / 60).toInt() // Convert to minutes
            val limitInMinutes = budget.timeLimitMinutes

            val excessMinutes = timeLoggedInMinutes - limitInMinutes
            if (excessMinutes > 0) {
                totalDeductions += excessMinutes * 6 // Deduct 5 points for every excess minute
            }
        }
        return totalDeductions
    }

}