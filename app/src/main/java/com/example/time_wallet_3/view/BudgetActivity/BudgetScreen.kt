package com.example.time_wallet_3.view.BudgetActivity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.time_wallet_3.viewmodel.viewmodel
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Card
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import com.example.time_wallet_3.model.Budget
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import com.example.time_wallet_3.R
import com.example.time_wallet_3.model.Activity


@Composable
fun BudgetScreen(viewModel: viewmodel) {
    val budgets by viewModel.budgets.collectAsState(initial = emptyList()) // Live data of budgets
    val activities by viewModel.activities.collectAsState(initial = emptyList()) // List of activities
    val showAddBudgetDialog = remember { mutableStateOf(false) } // Track dialog visibility

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddBudgetDialog.value = true }) {
                Text("+", style = MaterialTheme.typography.bodyLarge)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(16.dp)
            ) {
                Text("Budgets", style = MaterialTheme.typography.headlineSmall)

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn {
                    items(budgets) { budget ->
                        BudgetItem(budget)
                    }
                }
            }

            if (showAddBudgetDialog.value) {
                AddBudgetDialog(
                    activities = activities,
                    onConfirm = { activityName, timeLimit, period ->
                        viewModel.addBudget(activityName, timeLimit, period) // Pass all parameters
                        showAddBudgetDialog.value = false
                    },
                    onDismiss = { showAddBudgetDialog.value = false }
                )
            }
        }
    }
}

@Composable
fun AddBudgetDialog(
    activities: List<Activity>,
    onConfirm: (String, Int, String) -> Unit,
    onDismiss: () -> Unit
) {
    val selectedActivity = remember { mutableStateOf(activities.firstOrNull()?.name ?: "") }
    val isActivityDropDownExpanded = remember { mutableStateOf(false) }
    val timeLimit = remember { mutableStateOf("") }

    val periods = listOf("Daily", "Weekly", "Monthly")
    val selectedPeriod = remember { mutableStateOf(periods.first()) }
    val isPeriodDropDownExpanded = remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Add New Budget") },
        text = {
            Column {
                // Activity Dropdown
                Box {
                    Text(
                        text = if (selectedActivity.value.isNotEmpty()) selectedActivity.value else "Select Activity",
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable { isActivityDropDownExpanded.value = true }
                            .background(Color.LightGray)
                            .padding(8.dp)
                    )
                    DropdownMenu(
                        expanded = isActivityDropDownExpanded.value,
                        onDismissRequest = { isActivityDropDownExpanded.value = false }
                    ) {
                        activities.forEach { activity ->
                            DropdownMenuItem(
                                text = { Text(activity.name) },
                                onClick = {
                                    selectedActivity.value = activity.name
                                    isActivityDropDownExpanded.value = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Time Limit TextField
                OutlinedTextField(
                    value = timeLimit.value,
                    onValueChange = { newValue ->
                        timeLimit.value = newValue.filter { it.isDigit() } // Allow only numbers
                    },
                    label = { Text("Time Limit (hours)") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Period Dropdown
                Box {
                    Text(
                        text = selectedPeriod.value,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .clickable { isPeriodDropDownExpanded.value = true }
                            .background(Color.LightGray)
                            .padding(8.dp)
                    )
                    DropdownMenu(
                        expanded = isPeriodDropDownExpanded.value,
                        onDismissRequest = { isPeriodDropDownExpanded.value = false }
                    ) {
                        periods.forEach { period ->
                            DropdownMenuItem(
                                text = { Text(period) },
                                onClick = {
                                    selectedPeriod.value = period
                                    isPeriodDropDownExpanded.value = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                if (selectedActivity.value.isNotEmpty() && timeLimit.value.isNotEmpty()) {
                    onConfirm(selectedActivity.value, timeLimit.value.toInt(), selectedPeriod.value)
                }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}



@Composable
fun BudgetItem(budget: Budget) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(budget.activityName, style = MaterialTheme.typography.bodyLarge)
            Text("${budget.timeLimit} hrs/${budget.period}", style = MaterialTheme.typography.bodyMedium)
        }
    }
}


