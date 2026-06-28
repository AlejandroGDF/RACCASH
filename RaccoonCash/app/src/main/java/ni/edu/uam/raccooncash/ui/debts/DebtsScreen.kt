package ni.edu.uam.raccooncash.ui.debts

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ni.edu.uam.raccooncash.data.model.AccountResponse
import ni.edu.uam.raccooncash.data.model.DebtPaymentResponse
import ni.edu.uam.raccooncash.data.model.DebtResponse
import ni.edu.uam.raccooncash.ui.components.RaccAddFloatingActionButton
import ni.edu.uam.raccooncash.util.formatCurrencyAmount
import ni.edu.uam.raccooncash.util.formatEditableMoney
import ni.edu.uam.raccooncash.util.isPotentialMoneyInput
import ni.edu.uam.raccooncash.util.parseMoneyInput
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private object DebtsPalette {
    val Background = Color(0xFF080B14)
    val BackgroundAlt = Color(0xFF0B1020)
    val Card = Color(0xFF171C2A)
    val ElevatedCard = Color(0xFF202638)
    val Border = Color.White.copy(alpha = 0.09f)
    val Lavender = Color(0xFFA78BFA)
    val LavenderDeep = Color(0xFF31254B)
    val Mint = Color(0xFF7EDC8D)
    val Sky = Color(0xFF74C7EC)
    val Orange = Color(0xFFFFB84D)
    val Coral = Color(0xFFFF7A85)
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFF9CA3AF)
}

private data class DebtVisualState(
    val label: String,
    val color: Color
)

private data class DebtPaymentAccountVisual(
    val icon: ImageVector,
    val color: Color,
    val backgroundColor: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebtsScreen(
    viewModel: DebtsViewModel = viewModel(),
    onAddDebtClick: () -> Unit,
    onDebtClick: (DebtResponse) -> Unit
) {
    val context = LocalContext.current
    val debts by viewModel.debts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedType by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val filteredDebts = rememberDebtsByType(debts, selectedType)
    val pendingDebts = debts.filter { it.status != "PAID" && it.status != "CANCELLED" }
    val totalIOwe = pendingDebts.filter { it.type == "I_OWE" }.sumOf { it.remainingAmount }
    val totalOwedToMe = pendingDebts.filter { it.type == "OWED_TO_ME" }.sumOf { it.remainingAmount }
    val iOweCount = debts.count { it.type == "I_OWE" }
    val owedToMeCount = debts.count { it.type == "OWED_TO_ME" }
    val pendingIOweCount = pendingDebts.count { it.type == "I_OWE" }
    val pendingOwedToMeCount = pendingDebts.count { it.type == "OWED_TO_ME" }
    val overdueCount = pendingDebts.count { it.overdue }

    Scaffold(
        containerColor = DebtsPalette.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Deudas", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Controla lo que debes y lo que te deben",
                            color = DebtsPalette.TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DebtsPalette.Background,
                    titleContentColor = Color.White,
                    actionIconContentColor = DebtsPalette.Lavender
                )
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
                .background(DebtsPalette.Background)
                .padding(paddingValues)
        ) {
            DebtSummaryRow(
                totalIOwe = totalIOwe,
                totalOwedToMe = totalOwedToMe,
                overdueCount = overdueCount,
                iOweCount = pendingIOweCount,
                owedToMeCount = pendingOwedToMeCount
            )

            DebtTypeSelector(
                selectedType = selectedType,
                onSelected = { type ->
                    selectedType = type
                    viewModel.loadDebts(type = type)
                },
                allCount = debts.size,
                iOweCount = iOweCount,
                owedToMeCount = owedToMeCount
            )

            if (isLoading && debts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DebtsPalette.Lavender)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 112.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (filteredDebts.isEmpty()) {
                        item { EmptyDebtsCard(hasAnyDebts = debts.isNotEmpty(), onClick = onAddDebtClick) }
                    } else {
                        items(filteredDebts) { debt ->
                            DebtCard(debt = debt, onClick = { onDebtClick(debt) })
                        }
                    }
                    item { NewDebtActionCard(onClick = onAddDebtClick) }
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
    onSaved: () -> Unit = {},
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val accounts by viewModel.accounts.collectAsState()
    val error by viewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var personName by remember(debtToEdit?.id) { mutableStateOf(debtToEdit?.personName ?: "") }
    var description by remember(debtToEdit?.id) { mutableStateOf(debtToEdit?.description ?: "") }
    var totalAmount by remember(debtToEdit?.id) { mutableStateOf(formatEditableMoney(debtToEdit?.totalAmount)) }
    var type by remember(debtToEdit?.id) { mutableStateOf(debtToEdit?.type ?: "I_OWE") }
    var dueDate by remember(debtToEdit?.id) { mutableStateOf(parseLocalDate(debtToEdit?.dueDate)) }
    var selectedInitialAccountId by remember(debtToEdit?.id) { mutableStateOf(debtToEdit?.accountId) }
    var reminderEnabled by remember(debtToEdit?.id) { mutableStateOf(debtToEdit?.reminderEnabled ?: false) }
    var isSaving by remember(debtToEdit?.id) { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.resetSuccess()
        viewModel.loadAccounts()
    }

    LaunchedEffect(accounts, debtToEdit?.id) {
        if (debtToEdit == null && accounts.isNotEmpty() && accounts.none { it.id == selectedInitialAccountId }) {
            selectedInitialAccountId = accounts.first().id
        }
    }

    LaunchedEffect(error) {
        error?.let {
            isSaving = false
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    val amountValue = parseMoneyInput(totalAmount)
    val effectiveReminderEnabled = reminderEnabled && dueDate != null
    val isFormValid = personName.isNotBlank() &&
        amountValue != null &&
        amountValue > 0.0 &&
        (debtToEdit != null || selectedInitialAccountId != null)
    val accentColor = debtFormAccent(type)

    fun showDueDatePicker() {
        val initial = dueDate ?: LocalDate.now()
        DatePickerDialog(
            context,
            { _, year, month, day -> dueDate = LocalDate.of(year, month + 1, day) },
            initial.year,
            initial.monthValue - 1,
            initial.dayOfMonth
        ).show()
    }

    fun saveDebt() {
        if (isSaving) return
        if (personName.isBlank()) {
            Toast.makeText(context, "Ingresa el nombre de la persona.", Toast.LENGTH_SHORT).show()
            return
        }
        if (amountValue == null || amountValue <= 0.0) {
            Toast.makeText(context, "Ingresa un monto mayor a cero.", Toast.LENGTH_SHORT).show()
            return
        }
        if (debtToEdit == null && selectedInitialAccountId == null) {
            Toast.makeText(context, "Selecciona la cuenta del movimiento inicial.", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountValue
        val dueDateText = dueDate?.format(DateTimeFormatter.ISO_LOCAL_DATE)
        val reminderAt = if (effectiveReminderEnabled) {
            dueDate?.minusDays(1)?.atTime(9, 0)?.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        } else {
            null
        }
        isSaving = true

        if (debtToEdit == null) {
            viewModel.createDebt(
                personName = personName.trim(),
                description = description.takeIf { it.isNotBlank() },
                totalAmount = amount,
                type = type,
                dueDate = dueDateText,
                accountId = selectedInitialAccountId,
                reminderEnabled = effectiveReminderEnabled,
                reminderAt = reminderAt,
                onCompleted = {
                    onSaved()
                    onBack()
                }
            )
        } else {
            viewModel.updateDebt(
                id = debtToEdit.id,
                personName = personName.trim(),
                description = description.takeIf { it.isNotBlank() },
                totalAmount = amount,
                type = type,
                dueDate = dueDateText,
                accountId = debtToEdit.accountId,
                reminderEnabled = effectiveReminderEnabled,
                reminderAt = reminderAt,
                onCompleted = {
                    onSaved()
                    onBack()
                }
            )
        }
    }

    Scaffold(
        containerColor = DebtsPalette.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (debtToEdit == null) "Nueva deuda" else "Editar deuda",
                        color = DebtsPalette.TextPrimary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                navigationIcon = {
                    Surface(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(42.dp)
                            .clickable { onBack() },
                        shape = CircleShape,
                        color = DebtsPalette.ElevatedCard,
                        border = BorderStroke(1.dp, DebtsPalette.Border)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Atrás",
                                tint = DebtsPalette.TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            SaveDebtBottomBar(
                enabled = isFormValid && !isSaving,
                isLoading = isSaving,
                onClick = ::saveDebt
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            DebtsPalette.Background,
                            DebtsPalette.BackgroundAlt,
                            DebtsPalette.Background
                        )
                    )
                )
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(top = 8.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AddDebtTypeSegmentedControl(
                type = type,
                onTypeChange = { type = it }
            )

            DebtAmountInputCard(
                amount = totalAmount,
                accentColor = accentColor,
                onAmountChange = { if (isPotentialMoneyInput(it)) totalAmount = it }
            )

            DebtPremiumTextField(
                value = personName,
                onValueChange = { personName = it },
                placeholder = if (type == "I_OWE") "A quién le debo" else "Quién me debe",
                icon = Icons.Default.Person,
                singleLine = true
            )

            DebtPremiumTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = "Descripción",
                icon = Icons.Default.Description,
                minLines = 3
            )

            if (debtToEdit == null) {
                DebtPaymentAccountSelector(
                    accounts = accounts,
                    selectedAccountId = selectedInitialAccountId,
                    onSelected = { selectedInitialAccountId = it },
                    title = "Cuenta del movimiento inicial",
                    supportingText = if (type == "I_OWE") {
                        "Aquí entrará el dinero prestado al crear la deuda."
                    } else {
                        "De aquí saldrá el dinero que prestaste al crear la deuda."
                    },
                    selectedLabel = "Movimiento inicial"
                )
            }

            DebtDueDateCard(
                dueDate = dueDate,
                onClick = ::showDueDatePicker
            )

            DebtReminderCard(
                checked = effectiveReminderEnabled,
                enabled = dueDate != null,
                onCheckedChange = { reminderEnabled = it }
            )
        }
    }
}

@Composable
private fun AddDebtTypeSegmentedControl(
    type: String,
    onTypeChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = DebtsPalette.ElevatedCard,
        border = BorderStroke(1.dp, DebtsPalette.Border),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(5.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            AddDebtTypeSegment(
                text = "Debo",
                selected = type == "I_OWE",
                accentColor = DebtsPalette.Coral,
                modifier = Modifier.weight(1f),
                onClick = { onTypeChange("I_OWE") }
            )
            AddDebtTypeSegment(
                text = "Me deben",
                selected = type == "OWED_TO_ME",
                accentColor = DebtsPalette.Mint,
                modifier = Modifier.weight(1f),
                onClick = { onTypeChange("OWED_TO_ME") }
            )
        }
    }
}

@Composable
private fun AddDebtTypeSegment(
    text: String,
    selected: Boolean,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(19.dp)
    Box(
        modifier = modifier
            .height(48.dp)
            .clip(shape)
            .background(
                if (selected) {
                    Brush.horizontalGradient(listOf(accentColor.copy(alpha = 0.92f), DebtsPalette.Lavender))
                } else {
                    Brush.horizontalGradient(listOf(DebtsPalette.ElevatedCard, DebtsPalette.ElevatedCard))
                }
            )
            .border(
                1.dp,
                if (selected) accentColor.copy(alpha = 0.72f) else DebtsPalette.Border,
                shape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) DebtsPalette.Background else DebtsPalette.TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold
        )
    }
}

@Composable
private fun DebtAmountInputCard(
    amount: String,
    accentColor: Color,
    onAmountChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = DebtsPalette.Card,
        border = BorderStroke(1.dp, DebtsPalette.Lavender.copy(alpha = 0.22f)),
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Monto total", color = DebtsPalette.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("C$", color = accentColor, fontSize = 30.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.width(10.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    modifier = Modifier.weight(1f),
                    placeholder = {
                        Text(
                            "0.00",
                            color = DebtsPalette.TextSecondary.copy(alpha = 0.55f),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = TextStyle(
                        color = DebtsPalette.TextPrimary,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.End
                    ),
                    colors = debtTransparentTextFieldColors()
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, DebtsPalette.Lavender, accentColor.copy(alpha = 0.85f))
                        ),
                        RoundedCornerShape(999.dp)
                    )
            )
        }
    }
}

@Composable
private fun DebtPremiumTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    singleLine: Boolean = false,
    minLines: Int = 1
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = if (minLines > 1) 112.dp else 58.dp),
        placeholder = { Text(placeholder) },
        leadingIcon = {
            Icon(icon, contentDescription = null, tint = DebtsPalette.Lavender, modifier = Modifier.size(21.dp))
        },
        singleLine = singleLine,
        minLines = minLines,
        shape = RoundedCornerShape(20.dp),
        textStyle = TextStyle(color = DebtsPalette.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold),
        colors = debtOutlinedTextFieldColors()
    )
}

@Composable
private fun DebtDueDateCard(
    dueDate: LocalDate?,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        color = DebtsPalette.ElevatedCard,
        border = BorderStroke(1.dp, DebtsPalette.Border),
        shadowElevation = 7.dp
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(DebtsPalette.Lavender.copy(alpha = 0.16f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = DebtsPalette.Lavender, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Fecha límite", color = DebtsPalette.TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = dueDate?.let { formatDate(it) } ?: "Agregar fecha límite",
                    color = if (dueDate == null) DebtsPalette.TextSecondary else DebtsPalette.Lavender,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = DebtsPalette.TextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun DebtReminderCard(
    checked: Boolean,
    enabled: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = DebtsPalette.ElevatedCard,
        border = BorderStroke(
            1.dp,
            if (checked) DebtsPalette.Lavender.copy(alpha = 0.68f) else DebtsPalette.Border
        ),
        shadowElevation = if (checked) 8.dp else 4.dp
    ) {
        Row(
            modifier = Modifier.padding(15.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(DebtsPalette.Lavender.copy(alpha = if (checked) 0.24f else 0.12f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = DebtsPalette.Lavender, modifier = Modifier.size(22.dp))
                }
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text("Recordatorio", color = DebtsPalette.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(
                        text = if (enabled) "Se programará un día antes a las 9:00 AM" else "Elige una fecha límite para activarlo",
                        color = DebtsPalette.TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 15.sp
                    )
                }
            }
            Switch(
                checked = checked,
                enabled = enabled,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = DebtsPalette.TextPrimary,
                    checkedTrackColor = DebtsPalette.Lavender.copy(alpha = 0.72f),
                    uncheckedThumbColor = DebtsPalette.TextSecondary,
                    uncheckedTrackColor = DebtsPalette.Card,
                    uncheckedBorderColor = DebtsPalette.Border,
                    disabledUncheckedThumbColor = DebtsPalette.TextSecondary.copy(alpha = 0.45f),
                    disabledUncheckedTrackColor = DebtsPalette.Card.copy(alpha = 0.78f)
                )
            )
        }
    }
}

@Composable
private fun SaveDebtBottomBar(
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val buttonShape = RoundedCornerShape(999.dp)
    val buttonBrush = if (enabled) {
        Brush.horizontalGradient(listOf(DebtsPalette.LavenderDeep, DebtsPalette.Lavender))
    } else {
        Brush.horizontalGradient(listOf(DebtsPalette.ElevatedCard, Color(0xFF252B3A)))
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .background(DebtsPalette.Background.copy(alpha = 0.96f))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        color = Color.Transparent
    ) {
        Button(
            onClick = onClick,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .clip(buttonShape)
                .background(buttonBrush),
            shape = buttonShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = DebtsPalette.TextPrimary,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = DebtsPalette.TextSecondary
            ),
            contentPadding = PaddingValues(0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(buttonBrush),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = DebtsPalette.TextPrimary, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Icon(
                        if (enabled) Icons.Default.Check else Icons.Default.Lock,
                        contentDescription = null,
                        tint = if (enabled) DebtsPalette.TextPrimary else DebtsPalette.TextSecondary,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (enabled) "Guardar deuda" else "Completa los datos",
                        color = if (enabled) DebtsPalette.TextPrimary else DebtsPalette.TextSecondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun debtOutlinedTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = DebtsPalette.TextPrimary,
    unfocusedTextColor = DebtsPalette.TextPrimary,
    focusedContainerColor = DebtsPalette.ElevatedCard,
    unfocusedContainerColor = DebtsPalette.ElevatedCard,
    disabledContainerColor = DebtsPalette.ElevatedCard,
    focusedBorderColor = DebtsPalette.Lavender,
    unfocusedBorderColor = DebtsPalette.Border,
    focusedPlaceholderColor = DebtsPalette.TextSecondary,
    unfocusedPlaceholderColor = DebtsPalette.TextSecondary,
    cursorColor = DebtsPalette.Lavender
)

@Composable
private fun debtTransparentTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = DebtsPalette.TextPrimary,
    unfocusedTextColor = DebtsPalette.TextPrimary,
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    focusedBorderColor = Color.Transparent,
    unfocusedBorderColor = Color.Transparent,
    focusedPlaceholderColor = DebtsPalette.TextSecondary,
    unfocusedPlaceholderColor = DebtsPalette.TextSecondary,
    cursorColor = DebtsPalette.Lavender
)

private fun debtFormAccent(type: String): Color = if (type == "OWED_TO_ME") DebtsPalette.Mint else DebtsPalette.Coral

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
    val isPaymentReceived = debt.type != "I_OWE"
    val accentColor = if (isPaymentReceived) DebtsPalette.Mint else DebtsPalette.Coral
    val screenTitle = if (isPaymentReceived) "Registrar pago recibido" else "Registrar pago realizado"
    val amountLabel = if (isPaymentReceived) "Monto recibido" else "Monto pagado"
    val categoryName = "Pagos"
    val transactionType = if (isPaymentReceived) "Ingreso" else "Gasto"
    val isFormValid = amountValue != null && amountValue > 0.0 && selectedAccountId != null
    val canSave = isFormValid && !isLoading

    fun showPaymentDatePicker() {
        DatePickerDialog(
            context,
            { _, year, month, day -> selectedDate = LocalDate.of(year, month + 1, day) },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        ).show()
    }

    fun savePayment() {
        if (!canSave) return
        viewModel.addPayment(
            debtId = debt.id,
            amount = amountValue ?: 0.0,
            paymentDate = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
            accountId = selectedAccountId ?: 0L,
            notes = notes.takeIf { it.isNotBlank() },
            onCompleted = onPaymentSaved
        )
    }

    Scaffold(
        containerColor = DebtsPalette.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = screenTitle,
                        color = DebtsPalette.TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    Surface(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(42.dp)
                            .clickable { onBack() },
                        shape = CircleShape,
                        color = DebtsPalette.ElevatedCard,
                        border = BorderStroke(1.dp, DebtsPalette.Border)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Atrás",
                                tint = DebtsPalette.TextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            SaveDebtPaymentBottomBar(
                enabled = canSave,
                isLoading = isLoading,
                onClick = ::savePayment
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            DebtsPalette.Background,
                            DebtsPalette.BackgroundAlt,
                            DebtsPalette.Background
                        )
                    )
                )
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp)
                .padding(top = 8.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DebtPaymentSummaryCard(
                debt = debt,
                accentColor = accentColor,
                transactionType = transactionType,
                categoryName = categoryName
            )

            DebtPaymentAmountCard(
                amount = amount,
                label = amountLabel,
                currencySymbol = currencySymbol,
                accentColor = accentColor,
                onAmountChange = { if (isPotentialMoneyInput(it)) amount = it }
            )

            DebtPaymentDateCard(
                selectedDate = selectedDate,
                onClick = ::showPaymentDatePicker
            )

            DebtPaymentAccountSelector(
                accounts = accounts,
                selectedAccountId = selectedAccountId,
                onSelected = { selectedAccountId = it }
            )

            DebtPaymentCategoryCard(
                categoryName = categoryName,
                transactionType = transactionType,
                accentColor = accentColor
            )

            DebtPremiumTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = "Notas opcionales",
                icon = Icons.Default.Description,
                minLines = 3
            )
        }
    }
}

@Composable
private fun DebtPaymentSummaryCard(
    debt: DebtResponse,
    accentColor: Color,
    transactionType: String,
    categoryName: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = DebtsPalette.Card),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.36f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            DebtsPalette.ElevatedCard.copy(alpha = 0.98f),
                            DebtsPalette.Card,
                            accentColor.copy(alpha = 0.16f)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(accentColor.copy(alpha = 0.18f), CircleShape)
                            .border(BorderStroke(1.dp, accentColor.copy(alpha = 0.55f)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = debtPersonInitials(debt.personName),
                            color = accentColor,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            text = debt.personName,
                            color = DebtsPalette.TextPrimary,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = debtTypeLabel(debt.type),
                            color = DebtsPalette.TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = DebtsPalette.Background.copy(alpha = 0.48f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Monto pendiente",
                                color = DebtsPalette.TextSecondary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = formatMoney(debt.remainingAmount),
                                color = accentColor,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
                        }
                        StatusBadge(label = "Pendiente: ${formatMoney(debt.remainingAmount)}", color = accentColor)
                    }
                }

                Text(
                    text = "Se creará una transacción de tipo $transactionType con categoría $categoryName.",
                    color = DebtsPalette.TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
private fun DebtPaymentAmountCard(
    amount: String,
    label: String,
    currencySymbol: String,
    accentColor: Color,
    onAmountChange: (String) -> Unit
) {
    val amountFontSize = when {
        amount.length > 11 -> 30.sp
        amount.length > 8 -> 34.sp
        else -> 40.sp
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = DebtsPalette.Card,
        border = BorderStroke(1.dp, DebtsPalette.Lavender.copy(alpha = 0.30f)),
        shadowElevation = 10.dp
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            DebtsPalette.ElevatedCard.copy(alpha = 0.95f),
                            DebtsPalette.Card,
                            DebtsPalette.Lavender.copy(alpha = 0.11f)
                        )
                    )
                )
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = label,
                color = DebtsPalette.TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currencySymbol,
                    color = accentColor,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(10.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 72.dp),
                    placeholder = {
                        Text(
                            text = "0.00",
                            color = DebtsPalette.TextSecondary.copy(alpha = 0.55f),
                            fontSize = amountFontSize,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    singleLine = true,
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    textStyle = TextStyle(
                        color = DebtsPalette.TextPrimary,
                        fontSize = amountFontSize,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.End
                    ),
                    colors = debtTransparentTextFieldColors()
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color.Transparent, DebtsPalette.Lavender, accentColor)
                        ),
                        RoundedCornerShape(999.dp)
                    )
            )
        }
    }
}

@Composable
private fun DebtPaymentDateCard(
    selectedDate: LocalDate,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = DebtsPalette.ElevatedCard,
        border = BorderStroke(1.dp, DebtsPalette.Border),
        shadowElevation = 7.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(DebtsPalette.Lavender.copy(alpha = 0.16f), CircleShape)
                    .border(BorderStroke(1.dp, DebtsPalette.Lavender.copy(alpha = 0.36f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = DebtsPalette.Lavender, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Fecha", color = DebtsPalette.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = formatDate(selectedDate),
                    color = DebtsPalette.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = DebtsPalette.TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun DebtPaymentAccountSelector(
    accounts: List<AccountResponse>,
    selectedAccountId: Long?,
    onSelected: (Long) -> Unit,
    title: String = "Seleccionar cuenta",
    supportingText: String? = null,
    selectedLabel: String = "Seleccionada"
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            color = DebtsPalette.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )
        supportingText?.let {
            Text(
                text = it,
                color = DebtsPalette.TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }

        if (accounts.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = DebtsPalette.ElevatedCard,
                border = BorderStroke(1.dp, DebtsPalette.Border)
            ) {
                Text(
                    text = "No hay cuentas disponibles.",
                    color = DebtsPalette.TextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(end = 4.dp)
            ) {
                items(accounts) { account ->
                    DebtPaymentAccountCard(
                        account = account,
                        selected = selectedAccountId == account.id,
                        selectedLabel = selectedLabel,
                        onClick = { onSelected(account.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DebtPaymentAccountCard(
    account: AccountResponse,
    selected: Boolean,
    selectedLabel: String = "Seleccionada",
    onClick: () -> Unit
) {
    val visual = getDebtPaymentAccountVisual(account)

    Surface(
        modifier = Modifier
            .width(226.dp)
            .height(112.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = if (selected) visual.color.copy(alpha = 0.14f) else DebtsPalette.Card,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) visual.color else DebtsPalette.Border
        ),
        shadowElevation = if (selected) 10.dp else 4.dp
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            DebtsPalette.ElevatedCard.copy(alpha = 0.92f),
                            DebtsPalette.Card,
                            visual.color.copy(alpha = if (selected) 0.18f else 0.08f)
                        )
                    )
                )
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(visual.backgroundColor, CircleShape)
                        .border(BorderStroke(1.dp, visual.color.copy(alpha = 0.32f)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = visual.icon,
                        contentDescription = null,
                        tint = visual.color,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = account.name,
                        color = DebtsPalette.TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatAccountBalance(account),
                        color = DebtsPalette.TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(visual.color, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (selected) selectedLabel else "Cuenta",
                    color = if (selected) DebtsPalette.TextPrimary else DebtsPalette.TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
                if (selected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Cuenta seleccionada",
                        tint = visual.color,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DebtPaymentCategoryCard(
    categoryName: String,
    transactionType: String,
    accentColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = DebtsPalette.ElevatedCard,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.30f)),
        shadowElevation = 7.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(accentColor.copy(alpha = 0.16f), CircleShape)
                    .border(BorderStroke(1.dp, accentColor.copy(alpha = 0.34f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.Label, contentDescription = null, tint = accentColor, modifier = Modifier.size(23.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Categoría automática", color = DebtsPalette.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = categoryName,
                    color = DebtsPalette.TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            StatusBadge(label = transactionType, color = accentColor)
        }
    }
}

@Composable
private fun SaveDebtPaymentBottomBar(
    enabled: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = DebtsPalette.Background.copy(alpha = 0.96f),
        shadowElevation = 12.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            val shape = RoundedCornerShape(999.dp)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .clip(shape)
                    .background(
                        if (enabled) {
                            Brush.horizontalGradient(listOf(DebtsPalette.Lavender, Color(0xFF8B5CF6)))
                        } else {
                            Brush.horizontalGradient(listOf(DebtsPalette.ElevatedCard, DebtsPalette.Card))
                        }
                    )
                    .border(
                        BorderStroke(
                            1.dp,
                            if (enabled) DebtsPalette.Lavender.copy(alpha = 0.72f) else DebtsPalette.Border
                        ),
                        shape
                    )
                    .clickable(enabled = enabled) { onClick() },
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = DebtsPalette.TextPrimary,
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = if (enabled) DebtsPalette.TextPrimary else DebtsPalette.TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Guardar pago",
                            color = if (enabled) DebtsPalette.TextPrimary else DebtsPalette.TextSecondary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DebtSummaryRow(
    totalIOwe: Double,
    totalOwedToMe: Double,
    overdueCount: Int,
    iOweCount: Int,
    owedToMeCount: Int
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            SummaryCard(
                title = "Debo",
                value = formatMoney(totalIOwe),
                auxiliary = debtCountText(iOweCount),
                color = DebtsPalette.Coral,
                icon = Icons.Default.KeyboardArrowDown
            )
        }
        item {
            SummaryCard(
                title = "Me deben",
                value = formatMoney(totalOwedToMe),
                auxiliary = debtCountText(owedToMeCount),
                color = DebtsPalette.Mint,
                icon = Icons.Default.KeyboardArrowUp
            )
        }
        item {
            SummaryCard(
                title = "Vencidas",
                value = overdueCount.toString(),
                auxiliary = when (overdueCount) {
                    0 -> "sin alertas"
                    1 -> "requiere atención"
                    else -> "requieren atención"
                },
                color = DebtsPalette.Orange,
                icon = Icons.Default.AccessTime
            )
        }
    }
}

@Composable
private fun SummaryCard(
    title: String,
    value: String,
    auxiliary: String,
    color: Color,
    icon: ImageVector
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DebtsPalette.ElevatedCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .width(178.dp)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(color.copy(alpha = 0.18f), CircleShape)
                        .border(BorderStroke(1.dp, color.copy(alpha = 0.35f)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(26.dp))
                }
                Text(title, color = DebtsPalette.TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            }
            Text(value, color = color, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(auxiliary, color = DebtsPalette.TextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
private fun DebtTypeSelector(
    selectedType: String?,
    onSelected: (String?) -> Unit,
    includeAll: Boolean = true,
    allCount: Int? = null,
    iOweCount: Int? = null,
    owedToMeCount: Int? = null
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (includeAll) {
            item {
                DebtTypeButton("Todas", count = allCount, selected = selectedType == null, onClick = { onSelected(null) })
            }
        }
        item {
            DebtTypeButton("Debo", count = iOweCount, selected = selectedType == "I_OWE", onClick = { onSelected("I_OWE") })
        }
        item {
            DebtTypeButton("Me deben", count = owedToMeCount, selected = selectedType == "OWED_TO_ME", onClick = { onSelected("OWED_TO_ME") })
        }
    }
}

@Composable
private fun DebtTypeButton(text: String, count: Int?, selected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.clickable { onClick() },
        color = if (selected) DebtsPalette.Lavender else DebtsPalette.Card,
        shape = RoundedCornerShape(50),
        border = BorderStroke(
            1.dp,
            if (selected) DebtsPalette.Lavender else DebtsPalette.TextSecondary.copy(alpha = 0.18f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = text,
                color = if (selected) Color(0xFF120B22) else Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            count?.let {
                Surface(
                    color = if (selected) Color.White.copy(alpha = 0.28f) else DebtsPalette.ElevatedCard,
                    shape = CircleShape
                ) {
                    Text(
                        text = it.toString(),
                        color = if (selected) Color(0xFF120B22) else DebtsPalette.TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DebtCard(debt: DebtResponse, onClick: () -> Unit) {
    val progress = debtProgress(debt)
    val visualState = debtVisualState(debt)
    val typeColor = debtTypeColor(debt.type)
    val dueColor = if (visualState.label == "Vencido") DebtsPalette.Coral else DebtsPalette.TextSecondary
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DebtsPalette.ElevatedCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, visualState.color.copy(alpha = 0.22f))
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        debt.personName,
                        color = Color.White,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!debt.description.isNullOrBlank()) {
                        Text(
                            debt.description,
                            color = DebtsPalette.TextSecondary,
                            fontSize = 13.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                StatusBadge(label = visualState.label, color = visualState.color)
            }

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { DebtInfoChip(text = debtTypeChipLabel(debt.type), color = typeColor) }
                item { DebtInfoChip(text = debt.accountName ?: "Sin cuenta", color = DebtsPalette.Sky) }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DebtsPalette.Card,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DebtAmountLine("Pendiente", debt.remainingAmount, typeColor)
                    DebtAmountLine("Pagado", debt.paidAmount, DebtsPalette.Mint)
                    DebtAmountLine("Total", debt.totalAmount, Color.White)
                }
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = visualState.color,
                trackColor = Color.White.copy(alpha = 0.10f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = dueColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        debt.dueDate?.let { "Vence ${formatDateText(it)}" } ?: "Sin fecha límite",
                        color = dueColor,
                        fontSize = 13.sp
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Abrir detalle",
                    tint = DebtsPalette.TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun DebtInfoChip(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.12f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, color.copy(alpha = 0.34f))
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun DebtAmountLine(label: String, value: Double, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$label:",
            color = DebtsPalette.TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.width(86.dp)
        )
        Text(
            text = formatMoney(value),
            color = color,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun DebtDetailHeader(debt: DebtResponse) {
    val progress = debtProgress(debt)
    val visualState = debtVisualState(debt)
    val typeColor = debtTypeColor(debt.type)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = DebtsPalette.ElevatedCard),
        border = BorderStroke(1.dp, visualState.color.copy(alpha = 0.22f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(debt.personName, color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    Text(debtTypeLabel(debt.type), color = DebtsPalette.TextSecondary)
                }
                StatusBadge(label = visualState.label, color = visualState.color)
            }

            if (!debt.description.isNullOrBlank()) {
                Text(debt.description, color = Color.White.copy(alpha = 0.8f))
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                AmountBlock("Total", debt.totalAmount, Color.White)
                AmountBlock("Pagado", debt.paidAmount, DebtsPalette.Mint)
                AmountBlock("Pendiente", debt.remainingAmount, typeColor)
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape),
                color = visualState.color,
                trackColor = Color.White.copy(alpha = 0.10f)
            )

            HorizontalDivider(color = Color.White.copy(alpha = 0.10f))

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
            Text(selected?.let { formatAccountBalance(it) } ?: formatMoney(0.0), color = Color.Gray)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = { Text("${account.name} · ${formatAccountBalance(account)}") },
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
private fun EmptyDebtsCard(hasAnyDebts: Boolean, onClick: () -> Unit) {
    val title = if (hasAnyDebts) "No hay deudas en este filtro" else "No tienes deudas registradas"
    val subtitle = if (hasAnyDebts) {
        "Cambia el filtro o agrega una deuda nueva."
    } else {
        "Agrega una deuda para comenzar a llevar el control."
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DebtsPalette.Card),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, DebtsPalette.Lavender.copy(alpha = 0.28f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(DebtsPalette.Lavender.copy(alpha = 0.16f), CircleShape)
                    .border(BorderStroke(1.dp, DebtsPalette.Lavender.copy(alpha = 0.45f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = DebtsPalette.Lavender, modifier = Modifier.size(34.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(title, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
            Text(subtitle, color = DebtsPalette.TextSecondary, textAlign = TextAlign.Center, fontSize = 14.sp)
        }
    }
}

@Composable
private fun NewDebtActionCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = DebtsPalette.Card),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, DebtsPalette.Lavender.copy(alpha = 0.46f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(DebtsPalette.Lavender.copy(alpha = 0.18f), CircleShape)
                    .border(BorderStroke(1.dp, DebtsPalette.Lavender.copy(alpha = 0.42f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = DebtsPalette.Lavender, modifier = Modifier.size(28.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Nueva deuda", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Agrega una nueva deuda para llevar el control",
                    color = DebtsPalette.TextSecondary,
                    fontSize = 13.sp
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = DebtsPalette.Lavender,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun rememberDebtsByType(debts: List<DebtResponse>, selectedType: String?): List<DebtResponse> {
    return remember(debts, selectedType) {
        debts.filter { selectedType == null || it.type == selectedType }
    }
}

private fun debtPersonInitials(name: String): String {
    val parts = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
    if (parts.isEmpty()) return "?"

    return parts.take(2).joinToString("") { part ->
        part.first().uppercaseChar().toString()
    }
}

private fun parseDebtAccountColor(color: String?): Color? {
    val rawColor = color?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val normalizedColor = if (rawColor.startsWith("#")) rawColor else "#$rawColor"

    return try {
        Color(android.graphics.Color.parseColor(normalizedColor))
    } catch (e: IllegalArgumentException) {
        null
    }
}

private fun getDebtPaymentAccountVisual(account: AccountResponse): DebtPaymentAccountVisual {
    val normalizedName = "${account.name} ${account.type}".trim().lowercase(Locale.getDefault())
    val fallback = when {
        listOf("débito", "debito", "tarjeta", "banco", "credit", "crédito", "credito")
            .any { it in normalizedName } -> DebtPaymentAccountVisual(
            icon = Icons.Default.CreditCard,
            color = DebtsPalette.Sky,
            backgroundColor = DebtsPalette.Sky.copy(alpha = 0.14f)
        )

        listOf("efectivo", "cash", "cartera", "dinero", "bolsillo", "moneda")
            .any { it in normalizedName } -> DebtPaymentAccountVisual(
            icon = Icons.Default.Payments,
            color = DebtsPalette.Mint,
            backgroundColor = DebtsPalette.Mint.copy(alpha = 0.14f)
        )

        listOf("ahorro", "meta", "guardado", "alcancía", "alcancia")
            .any { it in normalizedName } -> DebtPaymentAccountVisual(
            icon = Icons.Default.Star,
            color = DebtsPalette.Lavender,
            backgroundColor = DebtsPalette.Lavender.copy(alpha = 0.16f)
        )

        else -> DebtPaymentAccountVisual(
            icon = Icons.Default.AccountBalanceWallet,
            color = Color(0xFFB6C2D9),
            backgroundColor = DebtsPalette.ElevatedCard.copy(alpha = 0.84f)
        )
    }

    val savedColor = parseDebtAccountColor(account.color) ?: fallback.color
    return fallback.copy(
        color = savedColor,
        backgroundColor = savedColor.copy(alpha = 0.16f)
    )
}

private fun formatAccountBalance(account: AccountResponse): String {
    val precision = (account.decimalPrecision ?: 2).coerceIn(0, 6)
    val currency = account.currency.ifBlank { "C$" }
    return formatCurrencyAmount(account.currentBalance, currency, precision)
}

private fun debtTypeLabel(type: String): String = when (type) {
    "I_OWE" -> "Dinero que debo pagar"
    "OWED_TO_ME" -> "Dinero que me deben"
    else -> type
}

private fun debtTypeChipLabel(type: String): String = when (type) {
    "I_OWE" -> "Debo pagar"
    "OWED_TO_ME" -> "Me deben"
    else -> type
}

private fun debtStatusLabel(status: String): String = when (status) {
    "PENDING" -> "Pendiente"
    "PARTIALLY_PAID" -> "Parcial"
    "PAID" -> "Pagado"
    "CANCELLED" -> "Cancelado"
    else -> status
}

private fun debtVisualState(debt: DebtResponse): DebtVisualState = when {
    debt.status == "PAID" -> DebtVisualState("Pagado", DebtsPalette.Mint)
    debt.status == "CANCELLED" -> DebtVisualState("Cancelado", DebtsPalette.TextSecondary)
    debt.overdue -> DebtVisualState("Vencido", DebtsPalette.Coral)
    debt.status == "PARTIALLY_PAID" -> DebtVisualState("Parcial", DebtsPalette.Sky)
    debt.status == "PENDING" -> DebtVisualState("Pendiente", DebtsPalette.Orange)
    else -> DebtVisualState(debtStatusLabel(debt.status), DebtsPalette.Lavender)
}

private fun debtProgress(debt: DebtResponse): Float {
    return if (debt.totalAmount > 0) {
        (debt.paidAmount / debt.totalAmount).toFloat().coerceIn(0f, 1f)
    } else {
        0f
    }
}

private fun debtTypeColor(type: String): Color = if (type == "I_OWE") DebtsPalette.Coral else DebtsPalette.Mint

private fun amountColor(type: String): Color = debtTypeColor(type)

private fun debtCountText(count: Int): String = if (count == 1) "1 deuda" else "$count deudas"

private fun formatMoney(value: Double): String = formatCurrencyAmount(value)

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
