package com.example.time_wallet_3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.time_wallet_3.model.TimeLogDao
import com.example.time_wallet_3.model.ActivityDao

class ViewModelFactory(
    private val timeLogDao: TimeLogDao,
    private val activityDao: ActivityDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(viewmodel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return viewmodel(timeLogDao, activityDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
