package com.example.time_wallet_3.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeLogDao {

    @Insert
    suspend fun insertLog(log: TimeLog)

    @Query("SELECT * FROM user_time_logs ORDER BY date DESC")
    fun getAllLogs(): Flow<List<TimeLog>>

    @Query("DELETE FROM user_time_logs")
    suspend fun deleteAllLogs()

    @Query("SELECT * FROM user_time_logs WHERE id = :logId")
    fun getLogById(logId: Int): Flow<TimeLog?>

    @Query("SELECT SUM(points) FROM user_time_logs")
    fun getTotalPoints(): Flow<Int>

    @Delete
    suspend fun deleteLog(log: TimeLog)

    @Query("SELECT * FROM user_time_logs WHERE accountId = :accountId")
    fun getLogsByAccount(accountId: Int): Flow<List<TimeLog>>
}