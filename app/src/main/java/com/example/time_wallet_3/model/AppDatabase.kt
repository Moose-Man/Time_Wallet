package com.example.time_wallet_3.model

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TimeLog::class, Activity::class], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userTimeLogDao(): TimeLogDao
    abstract fun userActivityDao(): ActivityDao
}
