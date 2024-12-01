package com.example.time_wallet_3.model

import android.content.Context
import androidx.room.Room

object DatabaseInstance {
    @Volatile
    private var INSTANCE: TimeLogDatabase? = null

    fun getDatabase(context: Context): TimeLogDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                TimeLogDatabase::class.java,
                "time_log_database"
            ).build()
            INSTANCE = instance
            instance
        }
    }
}