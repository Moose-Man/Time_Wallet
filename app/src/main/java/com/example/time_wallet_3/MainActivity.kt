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
import com.example.time_wallet_3.view.TimeLogs.AppWithBottomNavigation
import com.example.time_wallet_3.viewmodel.ViewModelFactory


@RequiresApi(Build.VERSION_CODES.O)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Use the unified database instance
        val database = DatabaseInstance.getDatabase(application)
        val timeLogDao = database.timeLogDao()
        val activityDao = database.activityDao()
        val accountDao = database.accountDao()

        // Pass the DAOs into the ViewModelFactory
        val viewModelFactory = ViewModelFactory(
            timeLogDao, activityDao, accountDao, database.budgetDao(), database.bankGoalDao()
        )

        val sharedViewModel: viewmodel = ViewModelProvider(this, viewModelFactory)[viewmodel::class.java]

        // Initialize default account using the shared ViewModel
        sharedViewModel.initializeDefaultAccountIfNeeded()

        setContent {
            Time_Wallet_3Theme {
                val navController = rememberNavController()
                AppWithBottomNavigation(navController, sharedViewModel)
            }
        }
    }
}











