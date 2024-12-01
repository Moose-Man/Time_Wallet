package com.example.time_wallet_3.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_time_logs")
data class UserTimeLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Primary key with auto-increment
    val elapsedTime: Long, // Total elapsed time in seconds
    val activity: String,  // Activity name
    val points: Int,        // Points earned
    val date: String      // Added date to represent the log date
)