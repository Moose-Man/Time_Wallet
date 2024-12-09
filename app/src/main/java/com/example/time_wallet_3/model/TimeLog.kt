package com.example.time_wallet_3.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.CASCADE
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_time_logs",
    foreignKeys = [
        ForeignKey(
            entity = Account::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = CASCADE
        )
    ]
)
data class TimeLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, // Primary key with auto-increment
    val elapsedTime: Long,
    val activity: String,
    val points: Int,
    val date: String,
    val notes: String = "",
    val timeStarted: Long = 0L,
    val timeStopped: Long = 0L,
    val accountId: Int
)