package com.example.time_wallet_3.model

data class Budget(
    val activityName: String,
    val timeLimit: Int, // Time limit in hours
    val period: String // Period: Daily, Weekly, Monthly
)
