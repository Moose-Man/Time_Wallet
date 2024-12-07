package com.example.time_wallet_3.view.BankActivity

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.input.KeyboardType
import com.example.time_wallet_3.model.Activity
import com.example.time_wallet_3.model.BankGoal

@Composable
fun BankScreen(viewModel: viewmodel) {
    val bankGoals by viewModel.bankGoals.collectAsState(initial = emptyList())
    val activities by viewModel.activities.collectAsState(initial = emptyList())
    val showAddBankGoalDialog = remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddBankGoalDialog.value = true }) {
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
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text("Bank Goals", style = MaterialTheme.typography.headlineSmall)

                Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    items(bankGoals) { bankGoal ->
                        BankGoalItem(bankGoal = bankGoal, viewModel = viewModel, activities = activities)
                    }
                }
            }

            if (showAddBankGoalDialog.value) {
                AddBankGoalDialog(
                    activities = activities,
                    onConfirm = { activityName, timeGoalMinutes, period ->
                        viewModel.addBankGoal(activityName, timeGoalMinutes, period)
                        showAddBankGoalDialog.value = false
                    },
                    onDismiss = { showAddBankGoalDialog.value = false }
                )
            }
        }
    }
}

//@Composable
//fun AddBankGoalDialog(
//    activities: List<Activity>,
//    onConfirm: (String, Int, String) -> Unit,
//    onDismiss: () -> Unit
//) {
//    val isActivityDropDownExpanded = remember { mutableStateOf(false) }
//    val timeGoalHours = remember { mutableStateOf("") }
//    val timeGoalMinutes = remember { mutableStateOf("") }
//    val periods = listOf("Daily", "Weekly", "Monthly")
//    val selectedPeriod = remember { mutableStateOf(periods.first()) }
//    val isPeriodDropDownExpanded = remember { mutableStateOf(false) }
//    val selectedActivity = remember { mutableStateOf("") }
//
//    AlertDialog(
//        onDismissRequest = { onDismiss() },
//        title = { Text("Add New Time Goal") },
//        text = {
//            Column {
//                // Activity Dropdown
//                Box {
//                    Text(
//                        text = if (selectedActivity.value.isNotEmpty()) selectedActivity.value else "Select Activity",
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(8.dp)
//                            .clickable { isActivityDropDownExpanded.value = true }
//                            .background(Color.LightGray)
//                            .padding(8.dp)
//                    )
//                    DropdownMenu(
//                        expanded = isActivityDropDownExpanded.value,
//                        onDismissRequest = { isActivityDropDownExpanded.value = false }
//                    ) {
//                        activities.forEach { activity ->
//                            DropdownMenuItem(
//                                text = { Text(activity.name) },
//                                onClick = {
//                                    selectedActivity.value = activity.name
//                                    isActivityDropDownExpanded.value = false
//                                }
//                            )
//                        }
//                    }
//                }
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Time Goal Hours TextField
//                OutlinedTextField(
//                    value = timeGoalHours.value,
//                    onValueChange = { timeGoalHours.value = it.filter { char -> char.isDigit() } },
//                    label = { Text("Hours") },
//                    modifier = Modifier.fillMaxWidth(),
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
//                )
//
//                Spacer(modifier = Modifier.height(8.dp))
//
//                // Time Goal Minutes TextField
//                OutlinedTextField(
//                    value = timeGoalMinutes.value,
//                    onValueChange = { timeGoalMinutes.value = it.filter { char -> char.isDigit() } },
//                    label = { Text("Minutes") },
//                    modifier = Modifier.fillMaxWidth(),
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
//                )
//
//                Spacer(modifier = Modifier.height(16.dp))
//
//                // Period Dropdown
//                Box {
//                    Text(
//                        text = selectedPeriod.value,
//                        modifier = Modifier
//                            .fillMaxWidth()
//                            .padding(8.dp)
//                            .clickable { isPeriodDropDownExpanded.value = true }
//                            .background(Color.LightGray)
//                            .padding(8.dp)
//                    )
//                    DropdownMenu(
//                        expanded = isPeriodDropDownExpanded.value,
//                        onDismissRequest = { isPeriodDropDownExpanded.value = false }
//                    ) {
//                        periods.forEach { period ->
//                            DropdownMenuItem(
//                                text = { Text(period) },
//                                onClick = {
//                                    selectedPeriod.value = period
//                                    isPeriodDropDownExpanded.value = false
//                                }
//                            )
//                        }
//                    }
//                }
//            }
//        },
//        confirmButton = {
//            Button(onClick = {
//                if (selectedActivity.value.isNotEmpty() && timeGoalHours.value.isNotEmpty()) {
//                    val totalMinutes = (timeGoalHours.value.toIntOrNull() ?: 0) * 60 +
//                            (timeGoalMinutes.value.toIntOrNull() ?: 0)
//                    onConfirm(selectedActivity.value, totalMinutes, selectedPeriod.value)
//                }
//            }) {
//                Text("Add")
//            }
//        },
//        dismissButton = {
//            Button(onClick = onDismiss) {
//                Text("Cancel")
//            }
//        }
//    )
//}
//
//
//@Composable
//fun BankGoalItem(bankGoal: BankGoal, viewModel: viewmodel, activities: List<Activity>) {
//    val timeElapsed = viewModel.getElapsedTimeForActivity(bankGoal.activityName, bankGoal.lastResetTime)
//    val progress = timeElapsed / (bankGoal.timeGoalMinutes * 60f * 1000f)
//    val progressText = viewModel.formatElapsedTime(timeElapsed, bankGoal.timeGoalMinutes)
//
//    Card(
//        modifier = Modifier.padding(8.dp),
//        elevation = CardDefaults.cardElevation(4.dp),
//        shape = RoundedCornerShape(8.dp)
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp),
//            verticalArrangement = Arrangement.spacedBy(8.dp)
//        ) {
//            Text(text = bankGoal.activityName, style = MaterialTheme.typography.bodyLarge)
//            LinearProgressIndicator(
//                progress = { progress.coerceIn(0f, 1f) },
//                modifier = Modifier.fillMaxWidth(),
//            )
//            Text(text = progressText, style = MaterialTheme.typography.bodySmall)
//        }
//    }
//}

@Composable
fun AddBankGoalDialog(
    activities: List<Activity>,
    onConfirm: (String, Int, String) -> Unit,
    onDismiss: () -> Unit
) {
    val isActivityDropDownExpanded = remember { mutableStateOf(false) }
    val timeGoalHours = remember { mutableStateOf("") }
    val timeGoalMinutes = remember { mutableStateOf("") }
    val periods = listOf("Daily", "Weekly", "Monthly")
    val selectedPeriod = remember { mutableStateOf(periods.first()) }
    val isPeriodDropDownExpanded = remember { mutableStateOf(false) }
    val selectedActivity = remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Add New Bank Goal") },
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

                // Time Goal Hours TextField
                OutlinedTextField(
                    value = timeGoalHours.value,
                    onValueChange = { timeGoalHours.value = it.filter { char -> char.isDigit() } },
                    label = { Text("Hours") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Time Goal Minutes TextField
                OutlinedTextField(
                    value = timeGoalMinutes.value,
                    onValueChange = { timeGoalMinutes.value = it.filter { char -> char.isDigit() } },
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
                if (selectedActivity.value.isNotEmpty() && timeGoalHours.value.isNotEmpty()) {
                    val totalMinutes = (timeGoalHours.value.toIntOrNull() ?: 0) * 60 +
                            (timeGoalMinutes.value.toIntOrNull() ?: 0)
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
fun BankGoalItem(bankGoal: BankGoal, viewModel: viewmodel, activities: List<Activity>) {
    val showEditDialog = remember { mutableStateOf(false) }

    if (showEditDialog.value) {
        EditBankGoalDialog(
            bankGoal = bankGoal,
            activities = activities,
            onConfirm = { originalBankGoal, updatedBankGoal ->
                viewModel.updateBankGoal(originalBankGoal, updatedBankGoal)
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
                Text(text = "${bankGoal.activityName} - ${bankGoal.period}", style = MaterialTheme.typography.bodyLarge)
                LinearProgressIndicator(
                    progress = {
                        (viewModel.getElapsedTimeForActivity(bankGoal.activityName, bankGoal.lastResetTime) /
                                (bankGoal.timeGoalMinutes * 60f * 1000f)).coerceIn(0f, 1f)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = viewModel.formatElapsedTime(
                        viewModel.getElapsedTimeForActivity(bankGoal.activityName, bankGoal.lastResetTime),
                        bankGoal.timeGoalMinutes
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
            }

            IconButton(onClick = { viewModel.deleteBankGoal(bankGoal) }) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Bank Goal",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EditBankGoalDialog(
    bankGoal: BankGoal,
    activities: List<Activity>, // Pass available activities
    onConfirm: (BankGoal, BankGoal) -> Unit, // Pass both original and updated bank goal
    onDismiss: () -> Unit
) {
    val selectedActivity = remember { mutableStateOf(bankGoal.activityName) }
    val isActivityDropDownExpanded = remember { mutableStateOf(false) }
    val updatedTimeGoalHours = remember { mutableStateOf((bankGoal.timeGoalMinutes / 60).toString()) }
    val updatedTimeGoalMinutes = remember { mutableStateOf((bankGoal.timeGoalMinutes % 60).toString()) }
    val periods = listOf("Daily", "Weekly", "Monthly")
    val updatedPeriod = remember { mutableStateOf(bankGoal.period) }
    val isPeriodDropDownExpanded = remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Edit Goal") },
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

                // Time Goal Hours Field
                OutlinedTextField(
                    value = updatedTimeGoalHours.value,
                    onValueChange = { updatedTimeGoalHours.value = it.filter { char -> char.isDigit() } },
                    label = { Text("Hours") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Time Goal Minutes Field
                OutlinedTextField(
                    value = updatedTimeGoalMinutes.value,
                    onValueChange = { updatedTimeGoalMinutes.value = it.filter { char -> char.isDigit() } },
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
                if (selectedActivity.value.isNotEmpty() && updatedTimeGoalHours.value.isNotEmpty()) {
                    val totalMinutes = (updatedTimeGoalHours.value.toIntOrNull() ?: 0) * 60 +
                            (updatedTimeGoalMinutes.value.toIntOrNull() ?: 0)
                    val updatedBankGoal = bankGoal.copy(
                        activityName = selectedActivity.value,
                        timeGoalMinutes = totalMinutes,
                        period = updatedPeriod.value
                    )
                    onConfirm(bankGoal, updatedBankGoal)
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
