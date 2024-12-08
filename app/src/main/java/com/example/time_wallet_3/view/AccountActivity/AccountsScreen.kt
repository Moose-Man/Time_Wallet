package com.example.time_wallet_3.view.AccountActivity

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.unit.dp
import androidx.compose.material3.FloatingActionButton
import androidx.compose.runtime.mutableStateOf
import androidx.navigation.NavHostController
import com.example.time_wallet_3.model.Account
import androidx.compose.material3.TextField
import androidx.compose.ui.Alignment
import androidx.compose.material3.IconButton
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.setValue


@Composable
fun AccountsScreen(viewModel: viewmodel, navController: NavHostController) {
    val accounts by viewModel.getAllAccounts().collectAsState(initial = emptyList())
    val currentAccountId by viewModel.currentAccountId.collectAsState(initial = null)
    val showAddAccountDialog = remember { mutableStateOf(false) }
    val showEditAccountDialog = remember { mutableStateOf<Pair<Boolean, Account?>>(false to null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddAccountDialog.value = true }) {
                Text("+", style = MaterialTheme.typography.bodyLarge)
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                items(accounts) { account ->
                    AccountItem(
                        account = account,
                        isCurrent = account.id == currentAccountId,
                        onEdit = { showEditAccountDialog.value = true to account },
                        onDelete = {
                            // Make sure you can't delete the current account
                            if (account.id != currentAccountId) {
                                viewModel.deleteAccount(account)
                            }
                        },
                        onSelect = {
                            viewModel.setCurrentAccount(account.id)
                        }
                    )
                }
            }
        }

        // Add Account Dialog
        if (showAddAccountDialog.value) {
            AddAccountDialog(
                onConfirm = { accountName ->
                    viewModel.addAccount(accountName)
                    showAddAccountDialog.value = false
                },
                onDismiss = { showAddAccountDialog.value = false }
            )
        }

        // Edit Account Dialog
        if (showEditAccountDialog.value.first) {
            showEditAccountDialog.value.second?.let { account ->
                EditAccountDialog(
                    account = account,
                    onConfirm = { updatedName ->
                        viewModel.updateAccountName(account, updatedName)
                        showEditAccountDialog.value = false to null
                    },
                    onDismiss = { showEditAccountDialog.value = false to null }
                )
            }
        }
    }
}


@Composable
fun AddAccountDialog(onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var accountName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Account") },
        text = {
            OutlinedTextField(
                value = accountName,
                onValueChange = { accountName = it },
                label = { Text("Account Name") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (accountName.isNotBlank()) {
                        onConfirm(accountName)
                    }
                }
            ) {
                Text("Create")
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
fun EditAccountDialog(account: Account, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var updatedName by remember { mutableStateOf(account.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Account") },
        text = {
            OutlinedTextField(
                value = updatedName,
                onValueChange = { updatedName = it },
                label = { Text("New Account Name") },
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    if (updatedName.isNotBlank() && updatedName != account.name) {
                        onConfirm(updatedName)
                    }
                }
            ) {
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


@Composable
fun AccountItem(
    account: Account,
    isCurrent: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onSelect() },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = account.name, style = MaterialTheme.typography.bodyLarge)
                if (isCurrent) {
                    Text(text = "Current Account", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
            Row {
                // Edit Button
                IconButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Account")
                }

                // Delete Button (only show if not the current account)
                if (!isCurrent) {
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Account")
                    }
                }
            }
        }
    }
}





