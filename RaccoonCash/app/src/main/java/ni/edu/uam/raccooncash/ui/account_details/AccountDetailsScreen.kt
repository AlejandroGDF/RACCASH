package ni.edu.uam.raccooncash.ui.account_details

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ni.edu.uam.raccooncash.data.model.AccountResponse
import ni.edu.uam.raccooncash.ui.accounts.AccountsViewModel
import ni.edu.uam.raccooncash.ui.accounts.TransactionItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailsScreen(
    account: AccountResponse,
    viewModel: AccountsViewModel,
    onEditAccount: () -> Unit,
    onTransactionClick: (ni.edu.uam.raccooncash.data.model.TransactionResponse) -> Unit,
    onBack: () -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    val accountTransactions = transactions.filter { it.accountId == account.id || it.toAccountId == account.id }
    
    val totalIncome = accountTransactions.filter { it.type == "INCOME" || (it.type == "TRANSFER" && it.toAccountId == account.id) }.sumOf { it.amount }
    val totalExpense = accountTransactions.filter { it.type == "EXPENSE" || (it.type == "TRANSFER" && it.accountId == account.id) }.sumOf { it.amount }

    var showDeleteDialog by remember { mutableStateOf(false) }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Eliminar cuenta") },
            text = { Text("¿Estás seguro de que deseas eliminar esta cuenta? Esta acción no se puede deshacer y podría afectar el historial de transacciones.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAccount(account.id)
                        showDeleteDialog = false
                        onBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(account.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = onEditAccount) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar Cuenta")
                    }
                    IconButton(onClick = { showDeleteDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar Cuenta",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Saldo Actual",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )
                        Text(
                            text = "${account.currency}${String.format("%.${account.decimalPrecision ?: 2}f", account.currentBalance)}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Entradas", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text(
                                    "+ ${account.currency}${String.format("%.${account.decimalPrecision ?: 2}f", totalIncome)}",
                                    color = Color(0xFFA5D6A7),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Salidas", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text(
                                    "- ${account.currency}${String.format("%.${account.decimalPrecision ?: 2}f", totalExpense)}",
                                    color = Color(0xFFEF9A9A),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Movimientos",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (accountTransactions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Text("No hay movimientos en esta cuenta", color = Color.Gray)
                    }
                }
            } else {
                items(accountTransactions.sortedByDescending { it.date }) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        onClick = { onTransactionClick(transaction) }
                    )
                }
            }
        }
    }
}
