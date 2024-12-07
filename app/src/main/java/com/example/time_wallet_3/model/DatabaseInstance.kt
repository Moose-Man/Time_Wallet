package com.example.time_wallet_3.model

import android.content.Context
import androidx.room.Room

object DatabaseInstance {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "app_database" // Ensure a single database name
            )
                .fallbackToDestructiveMigration() // Enable destructive migration
                .build()
            INSTANCE = instance
            instance
        }
    }

    // Lazy initialization for the BudgetDao
    val budgetDao: BudgetDao
        get() = INSTANCE?.budgetDao()
            ?: throw IllegalStateException("Database has not been initialized. Call getDatabase() first.")
}

