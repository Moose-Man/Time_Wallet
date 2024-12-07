package com.example.time_wallet_3.model

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface BankGoalDao {
    @Query("SELECT * FROM bank_goals")
    fun getAllBankGoals(): Flow<List<BankGoal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBankGoal(bankGoal: BankGoal)

    @Update
    suspend fun updateBankGoal(bankGoal: BankGoal)

    @Delete
    suspend fun deleteBankGoal(bankGoal: BankGoal)
}