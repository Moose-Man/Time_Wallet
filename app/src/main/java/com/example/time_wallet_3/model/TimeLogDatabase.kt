package com.example.time_wallet_3.model

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [UserTimeLog::class], version = 1, exportSchema = false)
abstract class TimeLogDatabase : RoomDatabase() {
    abstract fun userTimeLogDao(): UserTimeLogDao
}