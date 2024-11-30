package com.example.time_wallet_3.viewmodel

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.input.key.Key.Companion.O
import androidx.lifecycle.ViewModel
import java.time.LocalDate

// this class manages the values displayed on the time log UI
class viewmodel_TimeLog : ViewModel() {
    @RequiresApi(Build.VERSION_CODES.O) // for LocalDate.now()
    var Date = LocalDate.now() // Date of time log
        private set // prevents external modification of the date

    var activity = "test activity" // Activity being logged
    var startTime = System.currentTimeMillis() // Start timestamp
    var endTime = System.currentTimeMillis() // End timestamp (nullable for ongoing sessions)
    var timeElapsed by mutableStateOf(0L) // Time elapsed in milliseconds (nullable for ongoing sessions)
    var points = 0 // Points earned from session (nullable for ongoing sessions)
    var note by mutableStateOf("")// Additional notes taken by user of the session


    // Function to update the start time
    fun updateStartTime(newStartTime: Long) {
        startTime = newStartTime
        resetEndTime() // Reset endTime when start time is updated
    }

    // Function to update the end time
    fun updateEndTime(newEndTime: Long) {
        endTime = newEndTime
        calculateTimeElapsed() // Calculate elapsed time automatically
    }

    // Function to reset the end time (useful for restarting a timer)
    private fun resetEndTime() {
        endTime = startTime
        timeElapsed = 0L // Reset elapsed time
    }

    fun calculateTimeElapsed() {
        timeElapsed = (endTime - startTime)/1000
    }

    // Function to update the note
    fun updateNote(inputValue: String) {
        note = inputValue
    }

    // Function to reset all data (useful for creating a new log)
    fun resetTimeLog(newActivity: String = "test activity") {
        activity = newActivity
        startTime = System.currentTimeMillis()
        endTime = startTime
        timeElapsed = 0L
        points = 0
        note = ""
    }
}