package com.example.time_wallet_3.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserActivityDao {
    @Query("SELECT * FROM user_activities")
    fun getAllActivities(): Flow<List<UserActivity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: UserActivity)

    @Delete
    suspend fun deleteActivity(activity: UserActivity)
}
