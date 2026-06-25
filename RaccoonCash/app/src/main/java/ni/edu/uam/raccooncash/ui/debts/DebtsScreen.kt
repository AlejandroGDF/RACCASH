package ni.edu.uam.raccooncash.ui.debts

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ni.edu.uam.raccooncash.data.model.AccountResponse
import ni.edu.uam.raccooncash.data.model.DebtPaymentResponse
import ni.edu.uam.raccooncash.data.model.DebtResponse
import ni.edu.uam.raccooncash.ui.accounts.AccountChip
import ni.edu.uam.raccooncash.ui.components.RaccAddFloatingActionButton
import ni.edu.uam.raccooncash.util.formatEditableMoney
import ni.edu.uam.raccooncash.util.isPotentialMoneyInput
import ni.edu.uam.raccooncash.util.parseMoneyInput
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtsScreen(
    viewModel: DebtsViewModel = viewModel(),
    onAddDebtClick: () -> Unit,
    onDebtClick: (DebtResponse) -> Unit
) {
    val debts by viewModel.debts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedType by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val filteredDebts = rememberDebtsByType(debts, selectedType)
    val pendingDebts = debts.filter { it.status != "PAID" && it.status != "CANCELLED" }
    val totalIOwe = pendingDebts.filter { it.type == "I_OWE" }.sumOf { it.remainingAmount }
    val totalOwedToMe = pendingDebts.filter { it.type == "OWED_TO_ME" }.sumOf { it.remainingAmount }
    val overdueCount = pendingDebts.count { it.overdue }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Deudas", fontSize = 28.sp, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.loadDebts(type = selectedType) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar")
                    }
                }
            )
        },
        floatingActionButton = {
            RaccAddFloatingActionButton(
                onClick = onAddDebtClick,
                contentDescription = "Nueva deuda"
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F111A))
                .padding(paddingValues)
        ) {
            DebtSummaryRow(
                totalIOwe = totalIOwe,
                totalOwedToMe = totalOwedToMe,
                overdueCount = overdueCount
            )

            DebtTypeSelector(selectedType = selectedType, onSelected = { type ->
                selectedType = type
                viewModel.loadDebts(type = type)
            })

            if (isLoading && debts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (filteredDebts.isEmpty()) {
                        item { EmptyDebtsCard(onClick = onAddDebtClick) }
                    } else {
                        items(filteredDebts) { debt ->
                            DebtCard(debt = debt, onClick = { onDebtClick(debt) })
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDebtScreen(
    viewModel: DebtsViewModel,
    debtToEdit: DebtResponse? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val accounts by viewModel.accounts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val success by viewModel.operationSuccess.collectAsState()
    val error by viewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var personName by remember(debtToEdit?.id) { mutableStateOf(debtToEdit?.personName ?: "") }
    var description by remember(debtToEdit?.id) { mutableStateOf(debtToEdit?.description ?: "") }
    var totalAmount by remember(debtToEdit?.id) { mutableStateOf(formatEditableMoney(debtToEdit?.totalAmount)) }
    var type by remember(debtToEdit?.id) { mutableStateOf(debtToEdit?.type ?: "I_OWE") }
    var selectedAccountId by remember(debtToEdit?.id) { mutableStateOf(debtToEdit?.accountId) }
    var dueDate by remember(debtToEdit?.id) { mutableStateOf(parseLocalDate(debtToEdit?.dueDate)) }
    var reminderEnabled by remember(debtToEdit?.id) { mutableStateOf(debtToEdit?.reminderEnabled ?: false) }

    LaunchedEffect(Unit) {
        viewModel.resetSuccess()
        viewModel.loadAccounts()
    }

    LaunchedEffect(success) {
        if (success) {
            viewModel.resetSuccess()
            onBack()
        }
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val amountValue = parseMoneyInput(totalAmount)
    val isFormValid = personName.isNotBlank() && amountValue != null && amountValue > 0.0 && selectedAccountId != null

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (debtToEdit == null) "Nueva deuda" else "Editar deuda") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                color = if (isFormValid && !isLoading) Color(0xFFD1C4E9) else Color.Gray.copy(alpha = 0.5f),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                onClick = {
                    if (isFormValid) {
                        val accountId = selectedAccountId ?: 0L
                        val amount = amountValue ?: 0.0
                        val dueDateText = dueDate?.format(DateTimeFormatter.ISO_LOCAL_DATE)
                        val reminderAt = if (reminderEnabled) {
                            dueDate?.minusDays(1)?.atTime(9, 0)?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
                        } else {
                            null
                        }
                        if (debtToEdit == null) {
                            viewModel.createDebt(
                                personName = personName.trim(),
                                description = description.takeIf { it.isNotBlank() },
                                totalAmount = amount,
                                type = type,
                                dueDate = dueDateText,
                                accountId = accountId,
                                reminderEnabled = reminderEnabled,
                                reminderAt = reminderAt
                            )
                        } else {
                            viewModel.updateDebt(
                                id = debtToEdit.id,
                                personName = personName.trim(),
                                description = description.takeIf { it.isNotBlank() },
                                totalAmount = amount,
                                type = type,
                                dueDate = dueDateText,
                                accountId = accountId,
                                reminderEnabled = reminderEnabled,
                                reminderAt = reminderAt
                            )
                        }
                    }
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.Black)
                    } else {
                        Text(
                            text = if (isFormValid) "Guardar deuda" else "Completa los datos",
                            color = if (isFormValid) Color.Black else Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F111A))
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DebtTypeSelector(selectedType = type, onSelected = { selected -> type = selected ?: "I_OWE" }, includeAll = false)

            OutlinedTextField(
                value = personName,
                onValueChange = { personName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(if (type == "I_OWE") "A quien le debo" else "Quien me debe") },
                singleLine = true
            )

            OutlinedTextField(
                value = totalAmount,
                onValueChange = { if (isPotentialMoneyInput(it)) totalAmount = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Monto total") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                prefix = { Text("C$") }
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Descripción") },
                minLines = 2
            )

            AccountSelector(
                accounts = accounts,
                selectedAccountId = selectedAccountId,
                label = "Cuenta relacionada",
                onSelected = { selectedAccountId = it }
            )

            OutlinedButton(
                onClick = {
                    val initial = dueDate ?: LocalDate.now()
                    DatePickerDialog(
                        context,
                        { _, year, month, day -> dueDate = LocalDate.of(year, month + 1, day) },
                        initial.year,
                        initial.monthValue - 1,
                        initial.dayOfMonth
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(dueDate?.let { "Vence: ${formatDate(it)}" } ?: "Agregar fecha límite")
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E222D), RoundedCornerShape(16.dp))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Recordatorio", color = Color.White, fontWeight = FontWeight.Bold)
                    Text(
                        text = if (dueDate == null) "Elige una fecha límite para activarlo" else "Se programará un día antes a las 9:00 AM",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
                Switch(
                    checked = reminderEnabled,
                    enabled = dueDate != null,
                    onCheckedChange = { reminderEnabled = it }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtDetailsScreen(
    debtId: Long,
    viewModel: DebtsViewModel,
    onEditDebt: (DebtResponse) -> Unit,
    onAddPayment: (DebtResponse) -> Unit,
    onPaymentChanged: () -> Unit,
    onBack: () -> Unit
) {
    val debt by viewModel.selectedDebt.collectAsState()
    val payments by viewModel.payments.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val success by viewModel.operationSuccess.collectAsState()
    val error by viewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var confirmDelete by remember { mutableStateOf(false) }
    var closeAfterDelete by remember { mutableStateOf(false) }

    LaunchedEffect(debtId) {
        viewModel.resetSuccess()
        viewModel.loadDebtDetails(debtId)
    }

    LaunchedEffect(success) {
        if (success && closeAfterDelete) {
            viewModel.resetSuccess()
            closeAfterDelete = false
            onBack()
        }
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    if (confirmDelete) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Eliminar deuda") },
            text = { Text("La deuda se cancelará y dejará de mostrarse como activa.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    closeAfterDelete = true
                    viewModel.deleteDebt(debtId)
                }) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancelar") }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Detalle de deuda") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    debt?.let { currentDebt ->
                        TextButton(onClick = { onEditDebt(currentDebt) }) { Text("Editar") }
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            val canPay = debt?.let { it.status != "PAID" && it.status != "CANCELLED" } == true
            if (canPay) {
                RaccAddFloatingActionButton(
                    onClick = { debt?.let(onAddPayment) },
                    contentDescription = "Registrar pago"
                )
            }
        }
    ) { paddingValues ->
        if (debt == null && isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        val currentDebt = debt
        if (currentDebt == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No se encontró la deuda", color = Color.Gray)
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F111A))
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { DebtDetailHeader(currentDebt) }

            item {
                Text("Pagos", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            if (payments.isEmpty()) {
                item {
                    Text(
                        text = "Todavía no hay pagos registrados.",
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                items(payments) { payment ->
                    DebtPaymentItem(payment = payment, onDelete = {
                        viewModel.deletePayment(currentDebt.id, payment.id, onCompleted = onPaymentChanged)
                    })
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDebtPaymentScreen(
    debt: DebtResponse,
    viewModel: DebtsViewModel,
    onPaymentSaved: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val accounts by viewModel.accounts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var amount by remember(debt.id) { mutableStateOf(formatEditableMoney(debt.remainingAmount)) }
    var selectedDate by remember(debt.id) { mutableStateOf(LocalDate.now()) }
    var selectedAccountId by remember(debt.id, accounts) {
        mutableStateOf(accounts.firstOrNull { it.id == debt.accountId }?.id ?: accounts.firstOrNull()?.id)
    }
    var notes by remember(debt.id) { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.loadAccounts()
    }

    LaunchedEffect(error) {
        error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val amountValue = parseMoneyInput(amount)
    val selectedAccount = accounts.firstOrNull { it.id == selectedAccountId }
    val currencySymbol = selectedAccount?.currency ?: "C$"
    val categoryName = if (debt.type == "I_OWE") "Deudas" else "Pagos"
    val transactionType = if (debt.type == "I_OWE") "Gasto" else "Ingreso"
    val isFormValid = amountValue != null && amountValue > 0.0 && selectedAccountId != null

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (debt.type == "I_OWE") "Pagar deuda" else "Registrar pago recibido") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                color = if (isFormValid && !isLoading) Color(0xFFD1C4E9) else Color.Gray.copy(alpha = 0.5f),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                onClick = {
                    if (isFormValid) {
                        viewModel.addPayment(
                            debtId = debt.id,
                            amount = amountValue ?: 0.0,
                            paymentDate = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                            accountId = selectedAccountId ?: 0L,
                            notes = notes.takeIf { it.isNotBlank() },
                            onCompleted = onPaymentSaved
                        )
                    }
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.Black)
                    } else {
                        Text(
                            text = if (isFormValid) "Guardar pago" else "Completa el monto y la cuenta",
                            color = if (isFormValid) Color.Black else Color.White.copy(alpha = 0.7f),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F111A))
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E222D))
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(debt.personName, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text("Pendiente: ${formatMoney(debt.remainingAmount)}", color = amountColor(debt.type), fontWeight = FontWeight.Bold)
                    Text("Se creará una transacción de tipo $transactionType con categoría $categoryName.", color = Color.Gray, fontSize = 13.sp)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = currencySymbol,
                    style = MaterialTheme.typography.headlineLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (isPotentialMoneyInput(it)) amount = it },
                    placeholder = { Text("0", style = MaterialTheme.typography.headlineLarge) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold, textAlign = TextAlign.End),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            OutlinedButton(
                onClick = {
                    DatePickerDialog(
                        context,
                        { _, year, month, day -> selectedDate = LocalDate.of(year, month + 1, day) },
                        selectedDate.year,
                        selectedDate.monthValue - 1,
                        selectedDate.dayOfMonth
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Fecha: ${formatDate(selectedDate)}")
            }

            Text("Seleccionar cuenta", style = MaterialTheme.typography.titleMedium, color = Color.White)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(accounts) { account ->
                    AccountChip(
                        account = account,
                        isSelected = selectedAccountId == account.id,
                        onClick = { selectedAccountId = account.id }
                    )
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF1E222D),
                border = BorderStroke(1.dp, amountColor(debt.type).copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Categoría automática", color = Color.Gray, fontSize = 12.sp)
                        Text(categoryName, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    StatusBadge(transactionType, amountColor(debt.type))
                }
            }

            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notas") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
        }
    }
}

@Composable
private fun DebtSummaryRow(totalIOwe: Double, totalOwedToMe: Double, overdueCount: Int) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { SummaryCard("Debo", formatMoney(totalIOwe), Color(0xFFEF9A9A)) }
        item { SummaryCard("Me deben", formatMoney(totalOwedToMe), Color(0xFFA5D6A7)) }
        item { SummaryCard("Vencidas", overdueCount.toString(), Color(0xFFFFCC80)) }
    }
}

@Composable
private fun SummaryCard(label: String, value: String, color: Color) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E222D)),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.width(150.dp).padding(16.dp)) {
            Text(label, color = Color.Gray, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DebtTypeSelector(
    selectedType: String?,
    onSelected: (String?) -> Unit,
    includeAll: Boolean = true
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (includeAll) {
            item {
                DebtTypeButton("Todas", selected = selectedType == null, onClick = { onSelected(null) })
            }
        }
        item {
            DebtTypeButton("Debo", selected = selectedType == "I_OWE", onClick = { onSelected("I_OWE") })
        }
        item {
            DebtTypeButton("Me deben", selected = selectedType == "OWED_TO_ME", onClick = { onSelected("OWED_TO_ME") })
        }
    }
}

@Composable
private fun DebtTypeButton(text: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFFD1C4E9) else Color(0xFF1E222D),
            contentColor = if (selected) Color.Black else Color.White
        ),
        shape = RoundedCornerShape(50)
    ) {
        Text(text)
    }
}

@Composable
private fun DebtCard(debt: DebtResponse, onClick: () -> Unit) {
    val progress = if (debt.totalAmount > 0) (debt.paidAmount / debt.totalAmount).toFloat().coerceIn(0f, 1f) else 0f
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E222D))
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(debt.personName, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Text(debtTypeLabel(debt.type), color = Color.Gray, fontSize = 13.sp)
                }
                StatusBadge(label = debtStatusLabel(debt.status), color = debtStatusColor(debt.status, debt.overdue))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("Pendiente", color = Color.Gray, fontSize = 12.sp)
                    Text(formatMoney(debt.remainingAmount), color = amountColor(debt.type), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Total", color = Color.Gray, fontSize = 12.sp)
                    Text(formatMoney(debt.totalAmount), color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }

            androidx.compose.material3.LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = amountColor(debt.type),
                trackColor = Color.Gray.copy(alpha = 0.2f)
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(debt.accountName ?: "Cuenta", color = Color.Gray, fontSize = 13.sp)
                Text(debt.dueDate?.let { "Vence ${formatDateText(it)}" } ?: "Sin fecha límite", color = if (debt.overdue) Color(0xFFFFCC80) else Color.Gray, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun DebtDetailHeader(debt: DebtResponse) {
    val progress = if (debt.totalAmount > 0) (debt.paidAmount / debt.totalAmount).toFloat().coerceIn(0f, 1f) else 0f
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E222D))
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(debt.personName, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Text(debtTypeLabel(debt.type), color = Color.Gray)
                }
                StatusBadge(label = debtStatusLabel(debt.status), color = debtStatusColor(debt.status, debt.overdue))
            }

            if (!debt.description.isNullOrBlank()) {
                Text(debt.description, color = Color.White.copy(alpha = 0.8f))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                AmountBlock("Total", debt.totalAmount, Color.White)
                AmountBlock("Pagado", debt.paidAmount, Color(0xFFA5D6A7))
                AmountBlock("Pendiente", debt.remainingAmount, amountColor(debt.type))
            }

            androidx.compose.material3.LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape),
                color = amountColor(debt.type),
                trackColor = Color.Gray.copy(alpha = 0.2f)
            )

            HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))

            DetailLine("Cuenta", debt.accountName ?: "Sin nombre")
            DetailLine("Fecha límite", debt.dueDate?.let { formatDateText(it) } ?: "Sin fecha")
            DetailLine("Recordatorio", if (debt.reminderEnabled) debt.reminderAt?.let { formatDateTimeText(it) } ?: "Activo" else "Inactivo")
        }
    }
}

@Composable
private fun AmountBlock(label: String, value: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = Color.Gray, fontSize = 12.sp)
        Text(formatMoney(value), color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun DetailLine(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray)
        Text(value, color = Color.White, textAlign = TextAlign.End)
    }
}

@Composable
private fun DebtPaymentItem(payment: DebtPaymentResponse, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E222D))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(formatMoney(payment.amount), color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(payment.accountName ?: "Cuenta", color = Color.Gray, fontSize = 13.sp)
                Text(payment.paymentDate?.let { formatDateText(it) } ?: "Sin fecha", color = Color.Gray, fontSize = 13.sp)
                if (!payment.notes.isNullOrBlank()) {
                    Text(payment.notes, color = Color.White.copy(alpha = 0.75f), fontSize = 13.sp)
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Anular pago", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun AccountSelector(
    accounts: List<AccountResponse>,
    selectedAccountId: Long?,
    label: String,
    onSelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = accounts.firstOrNull { it.id == selectedAccountId }

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(selected?.name ?: label, modifier = Modifier.weight(1f), textAlign = TextAlign.Start)
            Text("C$${String.format(Locale.getDefault(), "%.2f", selected?.currentBalance ?: 0.0)}", color = Color.Gray)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = { Text("${account.name} · ${formatMoney(account.currentBalance)}") },
                    onClick = {
                        expanded = false
                        onSelected(account.id)
                    }
                )
            }
        }
    }
}

@Composable
private fun StatusBadge(label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.18f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, color.copy(alpha = 0.45f))
    ) {
        Text(
            text = label,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun EmptyDebtsCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(42.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("Registra tu primera deuda o préstamo", color = Color.Gray, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun rememberDebtsByType(debts: List<DebtResponse>, selectedType: String?): List<DebtResponse> {
    return remember(debts, selectedType) {
        debts.filter { selectedType == null || it.type == selectedType }
    }
}

private fun debtTypeLabel(type: String): String = when (type) {
    "I_OWE" -> "Dinero que debo pagar"
    "OWED_TO_ME" -> "Dinero que me deben"
    else -> type
}

private fun debtStatusLabel(status: String): String = when (status) {
    "PENDING" -> "Pendiente"
    "PARTIALLY_PAID" -> "Parcial"
    "PAID" -> "Pagado"
    "CANCELLED" -> "Cancelado"
    else -> status
}

private fun debtStatusColor(status: String, overdue: Boolean): Color = when {
    overdue -> Color(0xFFFFCC80)
    status == "PAID" -> Color(0xFFA5D6A7)
    status == "PARTIALLY_PAID" -> Color(0xFF90CAF9)
    status == "CANCELLED" -> Color.Gray
    else -> Color(0xFFD1C4E9)
}

private fun amountColor(type: String): Color = if (type == "I_OWE") Color(0xFFEF9A9A) else Color(0xFFA5D6A7)

private fun formatMoney(value: Double): String = "C$${String.format(Locale.getDefault(), "%.2f", value)}"

private fun parseLocalDate(value: String?): LocalDate? = try {
    value?.let { LocalDate.parse(it, DateTimeFormatter.ISO_LOCAL_DATE) }
} catch (e: Exception) {
    null
}

private fun formatDate(date: LocalDate): String = date.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es")))

private fun formatDateText(value: String): String = parseLocalDate(value)?.let { formatDate(it) } ?: value

private fun formatDateTimeText(value: String): String = try {
    LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        .format(DateTimeFormatter.ofPattern("d MMM yyyy, h:mm a", Locale.forLanguageTag("es")))
} catch (e: Exception) {
    value
}
