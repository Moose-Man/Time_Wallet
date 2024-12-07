package com.example.time_wallet_3.model

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Activity::class, TimeLog::class, Budget::class, BankGoal::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun timeLogDao(): TimeLogDao
    abstract fun budgetDao(): BudgetDao
    abstract fun bankGoalDao(): BankGoalDao
}