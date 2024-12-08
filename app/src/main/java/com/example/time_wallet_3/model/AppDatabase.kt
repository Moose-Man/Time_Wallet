package com.example.time_wallet_3.model

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Account::class, Activity::class, TimeLog::class, Budget::class, BankGoal::class], version = 7, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun activityDao(): ActivityDao
    abstract fun timeLogDao(): TimeLogDao
    abstract fun budgetDao(): BudgetDao
    abstract fun bankGoalDao(): BankGoalDao
}
val MIGRATION_1_2 = object : Migration(6, 7) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("CREATE TABLE accounts (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL)")
        database.execSQL("ALTER TABLE budgets ADD COLUMN accountId INTEGER NOT NULL DEFAULT 0 REFERENCES accounts(id) ON DELETE CASCADE")
        database.execSQL("ALTER TABLE time_logs ADD COLUMN accountId INTEGER NOT NULL DEFAULT 0 REFERENCES accounts(id) ON DELETE CASCADE")
    }
}
