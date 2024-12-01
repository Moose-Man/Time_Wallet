package com.example.time_wallet_3.model

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserTimeLogDao {

    @Insert
    suspend fun insertLog(log: UserTimeLog)

    @Query("SELECT * FROM user_time_logs ORDER BY date DESC")
    fun getAllLogs(): Flow<List<UserTimeLog>>

    @Query("DELETE FROM user_time_logs")
    suspend fun deleteAllLogs()
}