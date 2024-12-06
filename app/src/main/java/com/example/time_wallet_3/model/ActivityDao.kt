package com.example.time_wallet_3.model

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    // Fetch all activities as a Flow for live updates
    @Query("SELECT * FROM user_activities")
    fun getAllActivities(): Flow<List<Activity>>

    // Insert or replace an activity
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivity(activity: Activity)

    // Delete an activity
    @Delete
    suspend fun deleteActivity(activity: Activity)

    // Update an activity
    @Update
    suspend fun updateActivity(activity: Activity)

    // Fetch a single activity by name
    @Query("SELECT * FROM user_activities WHERE name = :name LIMIT 1")
    suspend fun getActivityByName(name: String): Activity?
}

