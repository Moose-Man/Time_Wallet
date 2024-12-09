package com.example.time_wallet_3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.time_wallet_3.model.AccountDao
import com.example.time_wallet_3.model.TimeLogDao
import com.example.time_wallet_3.model.ActivityDao
import com.example.time_wallet_3.model.BankGoalDao
import com.example.time_wallet_3.model.BudgetDao

class ViewModelFactory(
    private val dao: TimeLogDao,
    private val activityDao: ActivityDao,
    private val accountDao: AccountDao,
    private val budgetDao: BudgetDao,
    private val bankGoalDao: BankGoalDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(viewmodel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return viewmodel(dao, activityDao, accountDao, budgetDao, bankGoalDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}




