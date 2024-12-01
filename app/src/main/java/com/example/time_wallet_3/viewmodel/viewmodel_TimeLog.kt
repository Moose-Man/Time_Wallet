package com.example.time_wallet_3.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.time_wallet_3.model.UserTimeLog
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class viewmodel_TimeLog : ViewModel() {
    private var simulatedDate: LocalDate? = null // For testing purposes
    @RequiresApi(Build.VERSION_CODES.O)
    private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private var startTime: Long = 0L
    private var timerJob: Job? = null // Job to manage the timer coroutine
    val timeElapsed = MutableStateFlow(0L) // Elapsed time in seconds
    val isTimerRunning = MutableStateFlow(false) // Timer running state

    private val _logs = MutableStateFlow<List<UserTimeLog>>(emptyList())
    val logs: StateFlow<List<UserTimeLog>> get() = _logs

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
            timerJob?.cancel() // Cancel the timer coroutine
            timeElapsed.value = (System.currentTimeMillis() - startTime) / 1000
        }
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
        val newLog = UserTimeLog(
            elapsedTime = timeElapsed.value,
            activity = activity,
            points = calculatePoints(timeElapsed.value),
            date = currentDate
        )
        _logs.value = _logs.value + newLog
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
}