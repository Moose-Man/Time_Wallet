package com.example.time_wallet_3.view.TimeLogsActivity

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.time_wallet_3.view.AccountActivity.AccountsScreen
import com.example.time_wallet_3.view.AchievementsActivity.AchievementsScreen
import com.example.time_wallet_3.view.BankActivity.BankScreen
import com.example.time_wallet_3.view.BudgetActivity.BudgetScreen
import com.example.time_wallet_3.view.StatisticsActivity.StatisticsScreen
import com.example.time_wallet_3.viewmodel.viewmodel

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppWithBottomNavigation(navController: NavHostController, sharedViewModel: viewmodel) {
    Scaffold(
        bottomBar = {
            BottomNavigationBar(navController, sharedViewModel)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AppNavigation(navController, sharedViewModel)
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController, sharedViewModel: viewmodel) {
    val currentAccountId by sharedViewModel.currentAccountId.collectAsState()

    BottomNavigation {
        BottomNavigationItem(
            selected = false, // Update this dynamically based on the current route
            onClick = { navController.navigate("view_logs") },
            icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Logs") },
            label = { Text("Logs") }
        )
        BottomNavigationItem(
            selected = false, // Update this dynamically based on the current route
            onClick = { navController.navigate("budget") },
            icon = { Icon(Icons.Default.Search, contentDescription = "Budget") },
            label = { Text("Budget") }
        )
        BottomNavigationItem(
            selected = false, // Update this dynamically based on the current route
            onClick = { navController.navigate("bank_goals/${currentAccountId ?: 0}") }, // Include accountId
            icon = { Icon(Icons.Default.Search, contentDescription = "Bank") },
            label = { Text("Bank") }
        )
        BottomNavigationItem(
            selected = false, // Update this dynamically based on the current route
            onClick = { navController.navigate("statistics") },
            icon = { Icon(Icons.Default.Search, contentDescription = "Statistics") },
            label = { Text("Statistics") }
        )
        BottomNavigationItem(
            selected = false, // Update this dynamically based on the current route
            onClick = { navController.navigate("accounts") },
            icon = { Icon(Icons.Default.Search, contentDescription = "Accounts") },
            label = { Text("Accounts") }
        )
    }
}



@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation(navController: NavHostController, sharedViewModel: viewmodel) {
    NavHost(
        navController = navController,
        startDestination = "view_logs"
    ) {
        composable("achievements") {
            AchievementsScreen(viewModel = sharedViewModel, navController)
        }
        composable("bank_goals/{accountId}") { backStackEntry ->
            val accountId = backStackEntry.arguments?.getString("accountId")?.toIntOrNull() ?: 0
            BankScreen(viewModel = sharedViewModel, navController, accountId)
        }
        composable("budget") {
            BudgetScreen(viewModel = sharedViewModel, navController)
        }
        composable("view_logs") {
            ViewLogsScreen(navController, sharedViewModel)
        }
        composable("create_log") {
            CreateLogScreen(navController, sharedViewModel)
        }
        composable("statistics") {
            StatisticsScreen(viewModel = sharedViewModel, navController)
        }
        composable("accounts") {
            AccountsScreen(viewModel = sharedViewModel, navController)
        }
        composable("log_inspection/{logId}") { backStackEntry ->
            val logId = backStackEntry.arguments?.getString("logId")?.toIntOrNull()
            if (logId != null) {
                LogInspectionScreen(navController, sharedViewModel, logId)
            }
        }
    }
}


