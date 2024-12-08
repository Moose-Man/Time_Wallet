package com.example.time_wallet_3.view.BudgetActivity

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import com.example.time_wallet_3.model.Budget
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import androidx.navigation.NavHostController
import com.example.time_wallet_3.model.Activity
import com.example.time_wallet_3.view.TimeLogsActivity.HeaderSection


@Composable
fun BudgetScreen(viewModel: viewmodel, navController: NavHostController) {
    val budgets by viewModel.budgets.collectAsState(initial = emptyList()) // Live data of budgets
    val activities by viewModel.activities.collectAsState(initial = emptyList()) // List of activities
    val currentAccountId by viewModel.currentAccountId.collectAsState() // Observe the active account
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
                modifier = Modifier.fillMaxSize()
            ) {
                // Header section
                HeaderSection(viewModel, navController)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp) // Optional padding for content below the header
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        items(budgets) { budget ->
                            BudgetItem(budget = budget, viewModel = viewModel, activities = activities)
                        }
                    }
                }
            }

            if (showAddBudgetDialog.value) {
                AddBudgetDialog(
                    activities = activities,
                    existingBudgets = budgets.map { it.activityName }, // Pass existing budget activities
                    onConfirm = { activityName, timeLimit, period ->
                        currentAccountId?.let { accountId ->
                            viewModel.addBudget(accountId, activityName, timeLimit, period)
                        }
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
    existingBudgets: List<String>, // Pass existing budget activities
    onConfirm: (String, Int, String) -> Unit,
    onDismiss: () -> Unit
) {
    val isActivityDropDownExpanded = remember { mutableStateOf(false) }
    val timeLimitHours = remember { mutableStateOf("") }
    val timeLimitMinutes = remember { mutableStateOf("") }
    val periods = listOf("Daily", "Weekly", "Monthly")
    val selectedPeriod = remember { mutableStateOf(periods.first()) }
    val isPeriodDropDownExpanded = remember { mutableStateOf(false) }
    val selectedActivity = remember { mutableStateOf("") }
    val errorMessage = remember { mutableStateOf<String?>(null) } // Track error message

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
                                    errorMessage.value = null // Clear error if valid activity is selected
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Error message for duplicate activity
                errorMessage.value?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Time Limit Hours TextField
                OutlinedTextField(
                    value = timeLimitHours.value,
                    onValueChange = { timeLimitHours.value = it.filter { char -> char.isDigit() } },
                    label = { Text("Hours") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Time Limit Minutes TextField
                OutlinedTextField(
                    value = timeLimitMinutes.value,
                    onValueChange = { timeLimitMinutes.value = it.filter { char -> char.isDigit() } },
                    label = { Text("Minutes") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
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
                if (selectedActivity.value.isEmpty()) {
                    errorMessage.value = "Please select an activity."
                } else if (existingBudgets.contains(selectedActivity.value)) {
                    errorMessage.value = "A budget for this activity already exists."
                } else if (timeLimitHours.value.isEmpty()) {
                    errorMessage.value = "Please specify the hours."
                } else {
                    val totalMinutes = (timeLimitHours.value.toIntOrNull() ?: 0) * 60 +
                            (timeLimitMinutes.value.toIntOrNull() ?: 0)
                    onConfirm(selectedActivity.value, totalMinutes, selectedPeriod.value)
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
fun BudgetItem(budget: Budget, viewModel: viewmodel, activities: List<Activity>) {
    val showEditDialog = remember { mutableStateOf(false) }

    if (showEditDialog.value) {
        EditBudgetDialog(
            budget = budget,
            activities = activities,
            onConfirm = { originalBudget, updatedBudget ->
                viewModel.updateBudget(originalBudget, updatedBudget)
                showEditDialog.value = false
            },
            onDismiss = { showEditDialog.value = false }
        )
    }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .pointerInput(Unit) {
                detectTapGestures(onLongPress = {
                    showEditDialog.value = true
                })
            },
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "${budget.activityName} - ${budget.period}", style = MaterialTheme.typography.bodyLarge)
                LinearProgressIndicator(
                    progress = {
                        (viewModel.getElapsedTimeForActivity(budget.activityName, budget.lastResetTime) /
                                (budget.timeLimitMinutes * 60f * 1000f)).coerceIn(0f, 1f)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = viewModel.formatElapsedTime(
                        viewModel.getElapsedTimeForActivity(budget.activityName, budget.lastResetTime),
                        budget.timeLimitMinutes
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            IconButton(onClick = { viewModel.deleteBudget(budget) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Budget",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EditBudgetDialog(
    budget: Budget,
    activities: List<Activity>, // Pass available activities
    onConfirm: (Budget, Budget) -> Unit, // Pass both original and updated budget
    onDismiss: () -> Unit
) {
    val selectedActivity = remember { mutableStateOf(budget.activityName) }
    val isActivityDropDownExpanded = remember { mutableStateOf(false) }
    val updatedTimeLimitHours = remember { mutableStateOf((budget.timeLimitMinutes / 60).toString()) }
    val updatedTimeLimitMinutes = remember { mutableStateOf((budget.timeLimitMinutes % 60).toString()) }
    val periods = listOf("Daily", "Weekly", "Monthly")
    val updatedPeriod = remember { mutableStateOf(budget.period) }
    val isPeriodDropDownExpanded = remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Edit Budget") },
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

                // Time Limit Hours Field
                OutlinedTextField(
                    value = updatedTimeLimitHours.value,
                    onValueChange = { updatedTimeLimitHours.value = it.filter { char -> char.isDigit() } },
                    label = { Text("Hours") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Time Limit Minutes Field
                OutlinedTextField(
                    value = updatedTimeLimitMinutes.value,
                    onValueChange = { updatedTimeLimitMinutes.value = it.filter { char -> char.isDigit() } },
                    label = { Text("Minutes") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Period Dropdown
                Box {
                    Text(
                        text = updatedPeriod.value,
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
                                    updatedPeriod.value = period
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
                if (selectedActivity.value.isNotEmpty() && updatedTimeLimitHours.value.isNotEmpty()) {
                    val totalMinutes = (updatedTimeLimitHours.value.toIntOrNull() ?: 0) * 60 +
                            (updatedTimeLimitMinutes.value.toIntOrNull() ?: 0)
                    val updatedBudget = budget.copy(
                        activityName = selectedActivity.value,
                        timeLimitMinutes = totalMinutes,
                        period = updatedPeriod.value
                    )
                    onConfirm(budget, updatedBudget) // Pass both original and updated budgets
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}











