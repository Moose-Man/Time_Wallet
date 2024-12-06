package com.example.time_wallet_3

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.navigation.compose.rememberNavController
import com.example.time_wallet_3.ui.theme.Time_Wallet_3Theme
import com.example.time_wallet_3.viewmodel.viewmodel
import androidx.lifecycle.ViewModelProvider
import com.example.time_wallet_3.model.DatabaseInstance
import com.example.time_wallet_3.view.TimeLogsActivity.AppWithBottomNavigation
import com.example.time_wallet_3.viewmodel.ViewModelFactory


@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use the unified database instance
        val database = DatabaseInstance.getDatabase(application)
        val timeLogDao = database.userTimeLogDao()
        val activitiesDao = database.userActivityDao()
        val viewModelFactory = ViewModelFactory(timeLogDao, activitiesDao)
        val sharedViewModel: viewmodel = ViewModelProvider(this, viewModelFactory)[viewmodel::class.java]

        setContent {
            Time_Wallet_3Theme {
                val navController = rememberNavController()
                AppWithBottomNavigation(navController, sharedViewModel)
            }
        }
    }
}










