package com.example.time_wallet_3.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE

@Entity(
    tableName = "budgets",
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = CASCADE
        )
    ]
)
data class Budget(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val activityName: String,
    val timeLimitMinutes: Int, // Time limit in minutes
    val period: String, // Period: Daily, Weekly, Monthly
    val currentProgress: Int = 0, // Time logged in minutes
    val lastResetTime: Long = System.currentTimeMillis(), // Last reset timestamp
    val accountId: Int
)