package com.example.time_wallet_3.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.time_wallet_3.model.UserTimeLogDao

class ViewModelFactory(private val dao: UserTimeLogDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(viewmodel_TimeLog::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return viewmodel_TimeLog(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}