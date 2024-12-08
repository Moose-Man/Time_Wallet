package com.example.time_wallet_3.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.time_wallet_3.model.Activity
import com.example.time_wallet_3.model.ActivityDao
import com.example.time_wallet_3.model.BankGoal
import com.example.time_wallet_3.model.Budget
import com.example.time_wallet_3.model.DatabaseInstance.bankGoalDao
import com.example.time_wallet_3.model.DatabaseInstance.budgetDao
import com.example.time_wallet_3.model.TimeLog
import com.example.time_wallet_3.model.TimeLogDao
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class viewmodel(private val dao: TimeLogDao, private val ActivityDao: ActivityDao) : ViewModel() {

    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
    val budgets: Flow<List<Budget>> = budgetDao.getAllBudgets()
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
    private val _bankGoals = MutableStateFlow<List<BankGoal>>(emptyList())
    val bankGoals: StateFlow<List<BankGoal>> = _bankGoals
    private val _selectedActivity = MutableStateFlow<String?>(null)
    val selectedActivity: StateFlow<String?> get() = _selectedActivity

    fun setSelectedActivity(activityName: String) {
        _selectedActivity.value = activityName
    }

    init {
        viewModelScope.launch {
            bankGoalDao.getAllBankGoals().collect { fetchedGoals ->
                _bankGoals.value = fetchedGoals
            }
        }
    }

    fun addBankGoal(activityName: String, timeGoalMinutes: Int, period: String) {
        val newGoal = BankGoal(activityName = activityName, timeGoalMinutes = timeGoalMinutes, period = period)
        viewModelScope.launch {
            bankGoalDao.insertBankGoal(newGoal)
        }
    }

    init {
        viewModelScope.launch {
            dao.getAllLogs().collect { fetchedLogs ->
                _logs.value = fetchedLogs
            }
        }
    }

    fun updateBankGoalProgress(activityName: String, timeElapsed: Long) {
        val timeInMinutes = (timeElapsed / (1000 * 60)).toInt()
        var bonusPoints = 0

        _bankGoals.value = _bankGoals.value.map { goal ->
            if (goal.activityName == activityName) {
                val newProgress = goal.currentProgress + timeInMinutes
                val completed = newProgress >= goal.timeGoalMinutes

                // Calculate bonus points only if the goal is newly completed
                if (completed && !goal.completed) {
                    bonusPoints = 50 // Add bonus points for completing the goal
                }

                goal.copy(currentProgress = newProgress, completed = completed)
            } else {
                goal
            }
        }

        // Apply bonus points separately to avoid state race conditions
        if (bonusPoints > 0) {
            increasePoints(activityName, bonusPoints = bonusPoints)
        }

        // Regular points for time spent
        increasePoints(activityName, timeElapsed)
    }

    fun increasePoints(activityName: String, timeElapsed: Long = 0, isBonus: Boolean = false, bonusPoints: Int = 0) {
        viewModelScope.launch {
            val pointsEarned = if (isBonus) {
                calculatePoints(0, isBonus = true)
            } else {
                calculatePoints(timeElapsed)
            }

            _totalPoints.value += pointsEarned // Ensure this is executed correctly
            Log.d("TAG", "Total Points Updated: ${_totalPoints.value}")
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

    /**
     * Stops the timer and finalizes elapsed time.
     */
    fun stopTimer() {
        if (isTimerRunning.value) {
            isTimerRunning.value = false
            timerJob?.cancel()

            val elapsedTime = (System.currentTimeMillis() - startTime)
            timeElapsed.value = elapsedTime / 1000

            selectedActivity.value?.let { activityName ->
                updateBankGoalProgress(activityName, elapsedTime)
            }
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
        val elapsedTime = timeElapsed.value * 1000 // Convert seconds to milliseconds
        val newLog = TimeLog(
            elapsedTime = timeElapsed.value,
            activity = activity,
            points = calculatePoints(timeElapsed.value),
            date = currentDate,
            notes = note,
            timeStarted = startTime,
            timeStopped = System.currentTimeMillis()
        )

        viewModelScope.launch {
            dao.insertLog(newLog)
            updateBankGoalProgress(activity, elapsedTime) // Update bank goal progress here
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

    private fun calculatePoints(elapsedTime: Long, isBonus: Boolean = false): Int {
        val basePoints = (elapsedTime / 60).toInt() // 1 point per minute
        val bonusPoints = if (isBonus) 50 else 0
        Log.d("TAG", "Debug message: Bonus Points Earned = $bonusPoints")
        return basePoints + bonusPoints
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

    fun deleteBankGoal(bankGoal: BankGoal) {
        viewModelScope.launch {
            bankGoalDao.deleteBankGoal(bankGoal) // Remove the budget from the database
            _bankGoals.value = _bankGoals.value.filter { it != bankGoal } // Update the local state
        }
    }

    fun updateBankGoal(originalBankGoal: BankGoal, updatedBankGoal: BankGoal) {
        viewModelScope.launch {
            // Check if the activity name has changed
            val shouldReset = originalBankGoal.activityName != updatedBankGoal.activityName

            val finalBankGoal = if (shouldReset) {
                updatedBankGoal.copy(currentProgress = 0, lastResetTime = System.currentTimeMillis())
            } else {
                updatedBankGoal
            }

            // Update the bank goal in the database
            bankGoalDao.updateBankGoal(finalBankGoal)

            // Update the in-memory list of bank goals
            _bankGoals.value = _bankGoals.value.map { bankGoal ->
                if (bankGoal.activityName == originalBankGoal.activityName) finalBankGoal else bankGoal
            }
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


}