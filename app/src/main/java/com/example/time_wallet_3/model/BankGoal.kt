package com.example.time_wallet_3.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bank_goals")
data class BankGoal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val activityName: String,
    val timeGoalMinutes: Int, // Time goal in minutes
    val period: String, // Period: Daily, Weekly, Monthly
    var currentProgress: Int = 0, // Progress in minutes
    var completed: Boolean = false, // Track if the goal is completed
    val lastResetTime: Long = System.currentTimeMillis() // Last reset timestamp
)
