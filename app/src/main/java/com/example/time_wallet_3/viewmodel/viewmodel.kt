package com.example.time_wallet_3.viewmodel

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.time_wallet_3.model.Account
import com.example.time_wallet_3.model.Activity
import com.example.time_wallet_3.model.ActivityDao
import com.example.time_wallet_3.model.AccountDao
import com.example.time_wallet_3.model.BankGoal
import com.example.time_wallet_3.model.BankGoalDao
import com.example.time_wallet_3.model.Budget
import com.example.time_wallet_3.model.BudgetDao
import com.example.time_wallet_3.model.TimeLog
import com.example.time_wallet_3.model.TimeLogDao
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

class viewmodel(
    private val dao: TimeLogDao,
    private val activityDao: ActivityDao,
    private val accountDao: AccountDao,
    private val budgetDao: BudgetDao,
    private val bankGoalDao: BankGoalDao
    ) : ViewModel() {

    private val _budgets = MutableStateFlow<List<Budget>>(emptyList())
    val budgets: (accountId: Int) -> Flow<List<Budget>> = { accountId ->
        budgetDao.getBudgetsByAccount(accountId)
    }

    private val _bankGoals = MutableStateFlow<List<BankGoal>>(emptyList())
    val bankGoals: (accountId: Int) -> Flow<List<BankGoal>> = { accountId ->
        bankGoalDao.getBankGoalsByAccount(accountId)
    }

    private val _logs = MutableStateFlow<List<TimeLog>>(emptyList())
    val logs: StateFlow<List<TimeLog>> get() = _logs

    private val _totalPoints = MutableStateFlow(0) // Points tracked independently
    val totalPoints: Flow<Int> = dao.getTotalPoints() // Fetch total points from the database

    private val _currentAccountId = MutableStateFlow<Int?>(null)
    val currentAccountId: StateFlow<Int?> get() = _currentAccountId// Expose it as a read-only StateFlow

    private val _selectedActivity = MutableStateFlow<String?>(null)
    val selectedActivity: StateFlow<String?> get() = _selectedActivity

    val activities: Flow<List<Activity>> = activityDao.getAllActivities()
    private var simulatedDate: LocalDate? = null // For testing purposes
    @RequiresApi(Build.VERSION_CODES.O)
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private var startTime: Long = 0L
    private var timerJob: Job? = null // Job to manage the timer coroutine
    val timeElapsed = MutableStateFlow(0L) // Elapsed time in seconds
    val isTimerRunning = MutableStateFlow(false) // Timer running state

    fun setSelectedActivity(activityName: String) {
        _selectedActivity.value = activityName
    }

    // Update the data whenever the current account changes
    private fun refreshDataForCurrentAccount(accountId: Int) {
        viewModelScope.launch {
            // Refresh all data for the current account
            _budgets.value = budgetDao.getBudgetsByAccount(accountId).first()
            _bankGoals.value = bankGoalDao.getBankGoalsByAccount(accountId).first()
            _logs.value = dao.getLogsByAccount(accountId).first()
        }
    }

    init {
        // Observe the current account id and update the data accordingly
        viewModelScope.launch {
            currentAccountId.collect { accountId ->
                accountId?.let {
                    // Fetch the data for the selected account
                    fetchAccountData(it)
                }
            }
        }
    }

    // Function to fetch data based on current account
    private suspend fun fetchAccountData(accountId: Int) {
        // Get logs, budgets, and bank goals for the current account
        _logs.value = dao.getLogsByAccount(accountId).firstOrNull() ?: emptyList()
        _budgets.value = budgetDao.getBudgetsByAccount(accountId).firstOrNull() ?: emptyList()
        _bankGoals.value = bankGoalDao.getBankGoalsByAccount(accountId).firstOrNull() ?: emptyList()
    }

    init {
        viewModelScope.launch {
            budgetDao.getAllBudgets().collect { fetchedBudgets ->
                _budgets.value = fetchedBudgets
            }
        }

        viewModelScope.launch {
            bankGoalDao.getAllBankGoals().collect { fetchedGoals ->
                _bankGoals.value = fetchedGoals
            }
        }

        viewModelScope.launch {
            dao.getAllLogs().collect { fetchedLogs ->
                _logs.value = fetchedLogs
            }
        }
    }

    // Add new account (for demonstration)
    fun addAccount(accountName: String) {
        viewModelScope.launch {
            val newAccount = Account(name = accountName)
            val accountId = accountDao.insertAccount(newAccount).toInt()
            setCurrentAccount(accountId)  // Automatically set this as the current account after creation
        }
    }

// Update an account's name
    fun updateAccountName(account: Account, newName: String) {
        viewModelScope.launch {
            accountDao.updateAccountName(account.id, newName) // Pass the ID and new name
        }
    }

    // Delete an account
    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            accountDao.deleteAccount(account.id) // Pass the ID
        }
    }

    // Set current active account
    fun setCurrentAccount(accountId: Int) {
        _currentAccountId.value = accountId
        refreshDataForCurrentAccount(accountId) // Refresh data when account changes
    }

    fun addBankGoal(accountId: Int, activityName: String, timeGoalMinutes: Int, period: String) {
        val newGoal = BankGoal(
            accountId = accountId,
            activityName = activityName,
            timeGoalMinutes = timeGoalMinutes,
            period = period
        )
        viewModelScope.launch {
            bankGoalDao.insertBankGoal(newGoal)
            refreshDataForCurrentAccount(accountId) // Refresh after insert
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
            activityDao.insertActivity(Activity(name = name))
        }
    }

    fun deleteActivity(activity: Activity) {
        viewModelScope.launch {
            activityDao.deleteActivity(activity)
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
    fun addLog(accountId: Int, activity: String, note: String) {
        val currentDate = getCurrentDate().format(formatter)
        val elapsedTime = timeElapsed.value * 1000 // Convert seconds to milliseconds
        val newLog = TimeLog(
            elapsedTime = timeElapsed.value,
            activity = activity,
            points = calculatePoints(timeElapsed.value),
            date = currentDate,
            notes = note,
            timeStarted = startTime,
            timeStopped = System.currentTimeMillis(),
            accountId = accountId
        )

        viewModelScope.launch {
            dao.insertLog(newLog)
            updateBankGoalProgress(activity, elapsedTime) // Update bank goal progress here
        }
        resetTimerState()
        refreshDataForCurrentAccount(accountId) // Refresh after insert
    }

    fun initializeDefaultAccountIfNeeded() {
        viewModelScope.launch {
            val existingAccounts = accountDao.getAllAccounts().firstOrNull()
            if (existingAccounts.isNullOrEmpty()) {
                val defaultAccount = Account(name = "Default Account")
                val defaultAccountId = accountDao.insertAccount(defaultAccount).toInt()
                setCurrentAccount(defaultAccountId)
            } else {
                setCurrentAccount(existingAccounts.first().id)
            }
        }
    }

    // Function to get all accounts from the AccountDao
    fun getAllAccounts(): Flow<List<Account>> {
        return accountDao.getAllAccounts() // Assuming this function exists in AccountDao
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

    fun addBudget(accountId: Int, activityName: String, timeLimitMinutes: Int, period: String) {
        viewModelScope.launch {
            val newBudget = Budget(
                accountId = accountId,
                activityName = activityName,
                timeLimitMinutes = timeLimitMinutes,
                period = period
            )
            budgetDao.insertBudget(newBudget)
            refreshDataForCurrentAccount(accountId) // Refresh after insert
        }
    }

    fun updateBudget(originalBudget: Budget, updatedBudget: Budget) {
        viewModelScope.launch {
            val finalBudget = if (originalBudget.activityName != updatedBudget.activityName) {
                updatedBudget.copy(currentProgress = 0, lastResetTime = System.currentTimeMillis())
            } else {
                updatedBudget
            }
            budgetDao.updateBudget(finalBudget)
        }
    }

    fun deleteBudget(budget: Budget) {
        viewModelScope.launch {
            budgetDao.deleteBudget(budget)
        }
    }

    fun deleteBankGoal(bankGoal: BankGoal) {
        viewModelScope.launch {
            bankGoalDao.deleteBankGoal(bankGoal)
        }
    }

    fun updateBankGoal(originalBankGoal: BankGoal, updatedBankGoal: BankGoal) {
        viewModelScope.launch {
            val finalGoal = if (originalBankGoal.activityName != updatedBankGoal.activityName) {
                updatedBankGoal.copy(currentProgress = 0, lastResetTime = System.currentTimeMillis())
            } else {
                updatedBankGoal
            }
            bankGoalDao.updateBankGoal(finalGoal)
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