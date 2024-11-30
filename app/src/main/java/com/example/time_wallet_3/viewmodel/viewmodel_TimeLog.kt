package com.example.time_wallet_3.viewmodel

import androidx.lifecycle.ViewModel
import com.example.time_wallet_3.model.UserTimeLog
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

//// this class manages the values displayed on the time log UI
//class viewmodel_TimeLog : ViewModel() {
//    @RequiresApi(Build.VERSION_CODES.O) // for LocalDate.now()
//    var Date = LocalDate.now() // Date of time log
//        private set // prevents external modification of the date
//
//    var activity = "test activity" // Activity being logged
//    var startTime = System.currentTimeMillis() // Start timestamp
//    var endTime = System.currentTimeMillis() // End timestamp (nullable for ongoing sessions)
//    var timeElapsed by mutableStateOf(0L) // Time elapsed in milliseconds (nullable for ongoing sessions)
//    var points = 0 // Points earned from session (nullable for ongoing sessions)
//    var note by mutableStateOf("")// Additional notes taken by user of the session
//
//    // StateFlow to manage the list of logs
//    private val _logs = MutableStateFlow<List<UserTimeLog>>(emptyList())
//    val logs: StateFlow<List<UserTimeLog>> get() = _logs
//
//    // Function to add a new log to the list
//    fun addLog(activity: String, note: String) {
//        val newLog = UserTimeLog(
//            elapsedTime = timeElapsed,
//            activity = activity,
//            points = points
//        )
//        _logs.value = _logs.value + newLog // Add the new log to the list
//        resetTimeLog() // Reset after saving
//    }
//
//    // Function to update the start time
//    fun updateStartTime(newStartTime: Long) {
//        startTime = newStartTime
//        resetEndTime() // Reset endTime when start time is updated
//    }
//
//    // Function to update the end time
//    fun updateEndTime(newEndTime: Long) {
//        endTime = newEndTime
//        calculateTimeElapsed() // Calculate elapsed time automatically
//    }
//
//    // Function to reset the end time (useful for restarting a timer)
//    private fun resetEndTime() {
//        endTime = startTime
//        timeElapsed = 0L // Reset elapsed time
//    }
//
//    fun calculateTimeElapsed() {
//        timeElapsed = (endTime - startTime)/1000
//    }
//
//    // Function to update the note
//    fun updateNote(inputValue: String) {
//        note = inputValue
//    }
//
//    // Function to reset all data (useful for creating a new log)
//    fun resetTimeLog(newActivity: String = "test activity") {
//        activity = newActivity
//        startTime = System.currentTimeMillis()
//        endTime = startTime
//        timeElapsed = 0L
//        points = 0
//        note = ""
//    }
//}

class viewmodel_TimeLog : ViewModel() {

    // Timer-related state
    private var startTime: Long = 0L // Start timestamp
    private var endTime: Long = 0L // End timestamp
    val timeElapsed = MutableStateFlow(0L) // Time elapsed in seconds
    val isTimerRunning = MutableStateFlow(false) // Timer state

    // Log management
    private val _logs = MutableStateFlow<List<UserTimeLog>>(emptyList())
    val logs: StateFlow<List<UserTimeLog>> get() = _logs

    /**
     * Starts the timer by recording the current time.
     */
    fun startTimer(currentTimeMillis: Long) {
        if (!isTimerRunning.value) {
            startTime = System.currentTimeMillis()
            isTimerRunning.value = true
        }
    }

    /**
     * Stops the timer, calculates elapsed time, and updates the state.
     */
    fun stopTimer(currentTimeMillis: Long) {
        if (isTimerRunning.value) {
            endTime = System.currentTimeMillis()
            timeElapsed.value = (endTime - startTime) / 1000 // Convert to seconds
            isTimerRunning.value = false
        }
    }

    /**
     * Adds a new log entry and resets the timer state.
     *
     * @param activity The name of the activity being logged.
     * @param note Additional notes for the log entry.
     */
    fun addLog(activity: String, note: String) {
        val newLog = UserTimeLog(
            elapsedTime = timeElapsed.value,
            activity = activity,
            points = calculatePoints(timeElapsed.value) // Example points calculation
        )
        _logs.value = _logs.value + newLog
        resetTimerState()
    }

    /**
     * Resets the timer and elapsed time state.
     */
    private fun resetTimerState() {
        startTime = 0L
        endTime = 0L
        timeElapsed.value = 0L
        isTimerRunning.value = false
    }

    /**
     * Calculates points based on the elapsed time.
     * Example: 1 point for every 60 seconds.
     *
     * @param elapsedTime Time elapsed in seconds.
     * @return Points earned.
     */
    private fun calculatePoints(elapsedTime: Long): Int {
        return (elapsedTime / 60).toInt() // 1 point per minute
    }
}