package com.example.time_wallet_3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.time_wallet_3.model.UserTimeLogDao
import com.example.time_wallet_3.model.UserActivityDao

class ViewModelFactory(
    private val timeLogDao: UserTimeLogDao,
    private val activityDao: UserActivityDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(viewmodel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return viewmodel(timeLogDao, activityDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
