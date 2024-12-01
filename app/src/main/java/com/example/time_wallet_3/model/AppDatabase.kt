package com.example.time_wallet_3.model

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [UserTimeLog::class, UserActivity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userTimeLogDao(): UserTimeLogDao
    abstract fun userActivityDao(): UserActivityDao
}
