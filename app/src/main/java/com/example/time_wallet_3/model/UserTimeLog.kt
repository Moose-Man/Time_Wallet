package com.example.time_wallet_3.model

data class UserTimeLog(
//    val id: Int = 0, // Auto-generated ID
//    val date: Long, // Date of time log
//    val activity: Int = 0, // Activity being logged
//    val startTime: Long, // Start timestamp
//    val endTime: Long?, // End timestamp (nullable for ongoing sessions)
//    val points: Int, // Points earned from session (nullable for ongoing sessions)
//    val Note: String // Additional notes taken by user of the session
    val elapsedTime: Long, // Total elapsed time in seconds
    val activity: String,  // Activity name
    val points: Int        // Points earned
)