package ni.edu.uam.raccooncash.ui.savings

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.raccooncash.data.model.AccountResponse
import ni.edu.uam.raccooncash.data.model.SavingGoalResponse
import ni.edu.uam.raccooncash.data.model.TransactionResponse
import ni.edu.uam.raccooncash.ui.accounts.AccountsViewModel
import ni.edu.uam.raccooncash.util.formatCurrencyAmount
import ni.edu.uam.raccooncash.util.formatEditableMoney
import ni.edu.uam.raccooncash.util.isPotentialMoneyInput
import ni.edu.uam.raccooncash.util.parseMoneyInput
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private object GoalTransactionPalette {
    val Background = Color(0xFF080B14)
    val BackgroundAlt = Color(0xFF0B1020)
    val Card = Color(0xFF171C2A)
    val ElevatedCard = Color(0xFF202638)
    val Lavender = Color(0xFFA78BFA)
    val LavenderDeep = Color(0xFF8B5CF6)
    val Sky = Color(0xFF74C7EC)
    val Mint = Color(0xFF7EDC8D)
    val Orange = Color(0xFFFFB84D)
    val Coral = Color(0xFFFF7A85)
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFF9CA3AF)
    val Border = Color.White.copy(alpha = 0.08f)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGoalTransactionScreen(
    goal: SavingGoalResponse,
    savingsViewModel: SavingsViewModel,
    accountsViewModel: AccountsViewModel,
    transactionToEdit: TransactionResponse? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val accounts by accountsViewModel.accounts.collectAsState()
    val isLoading by savingsViewModel.isLoading.collectAsState()
    val success by savingsViewModel.addTransactionSuccess.collectAsState()
    val error by savingsViewModel.error.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    var amount by remember(transactionToEdit?.id) { mutableStateOf(formatEditableMoney(transactionToEdit?.amount)) }
    var title by remember { mutableStateOf(transactionToEdit?.description ?: "Ahorro para ${goal.name}") }
    var notes by remember { mutableStateOf(transactionToEdit?.notes ?: "") }
    var selectedAccountId by remember { mutableStateOf<Long?>(transactionToEdit?.accountId) }
    var hasSubmitted by remember { mutableStateOf(false) }

    val initialDate = if (transactionToEdit?.date != null) {
        try { LocalDateTime.parse(transactionToEdit.date, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate() } catch (e: Exception) { LocalDate.now() }
    } else LocalDate.now()
    var selectedDate by remember { mutableStateOf(initialDate) }

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
        },
        selectedDate.year,
        selectedDate.monthValue - 1,
        selectedDate.dayOfMonth
    )

    LaunchedEffect(Unit) {
        savingsViewModel.resetSuccess()
        accountsViewModel.loadAccounts()
    }

    LaunchedEffect(accounts) {
        if (selectedAccountId == null) {
            selectedAccountId = accounts.firstOrNull()?.id
        }
    }

    LaunchedEffect(error) {
        error?.let { snackbarHostState.showSnackbar(it) }
    }

    LaunchedEffect(success) {
        if (success && hasSubmitted) {
            accountsViewModel.loadAccounts()
            savingsViewModel.resetSuccess()
            onBack()
        }
    }

    val amountValue = parseMoneyInput(amount)
    val isFormValid = amountValue != null && amountValue > 0.0 && selectedAccountId != null && title.isNotBlank()
    val goalColor = parseGoalTransactionColor(goal.color) ?: GoalTransactionPalette.Lavender

    fun saveMovement() {
        if (isFormValid) {
            hasSubmitted = true
            if (transactionToEdit != null) {
                savingsViewModel.updateGoalTransaction(
                    transactionId = transactionToEdit.id,
                    goalId = goal.id,
                    accountId = selectedAccountId ?: 0L,
                    amount = amountValue ?: 0.0,
                    description = title,
                    notes = notes,
                    dateTime = LocalDateTime.of(selectedDate, LocalTime.now())
                )
            } else {
                savingsViewModel.addTransactionToGoal(
                    goalId = goal.id,
                    accountId = selectedAccountId ?: 0L,
                    amount = amountValue ?: 0.0,
                    description = title,
                    notes = notes,
                    dateTime = LocalDateTime.of(selectedDate, LocalTime.now())
                )
            }
        }
    }

    Scaffold(
        containerColor = GoalTransactionPalette.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            GoalTransactionTopBar(
                title = if (transactionToEdit != null) "Editar ahorro" else "Agregar transacción",
                goalColor = goalColor,
                showDelete = transactionToEdit != null,
                onBack = onBack,
                onDelete = {
                    transactionToEdit?.let { savingsViewModel.deleteTransaction(it.id, goal.id) }
                }
            )
        },
        bottomBar = {
            GoalTransactionSaveBar(
                isFormValid = isFormValid,
                isLoading = isLoading,
                onClick = ::saveMovement
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            GoalTransactionPalette.Background,
                            GoalTransactionPalette.BackgroundAlt,
                            GoalTransactionPalette.Background
                        )
                    )
                )
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AmountEntryCard(
                currency = goal.currency,
                amount = amount,
                goalColor = goalColor,
                onAmountChange = { if (isPotentialMoneyInput(it)) amount = it }
            )

            DateSelectionCard(
                selectedDate = selectedDate,
                goalColor = goalColor,
                onClick = { datePickerDialog.show() }
            )

            AccountSelectionSection(
                accounts = accounts,
                selectedAccountId = selectedAccountId,
                onAccountSelected = { selectedAccountId = it }
            )

            SelectedGoalCard(
                goal = goal,
                goalColor = goalColor
            )

            PremiumGoalTextField(
                value = title,
                onValueChange = { title = it },
                label = "Título",
                placeholder = "Ahorro para ${goal.name}",
                icon = Icons.Default.Description,
                singleLine = true
            )

            PremiumGoalTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Notas",
                placeholder = "Agrega una nota opcional",
                icon = Icons.AutoMirrored.Filled.Notes,
                minLines = 4,
                modifier = Modifier.heightIn(min = 132.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalTransactionTopBar(
    title: String,
    goalColor: Color,
    showDelete: Boolean,
    onBack: () -> Unit,
    onDelete: () -> Unit
) {
    TopAppBar(
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Movimiento de meta",
                    color = GoalTransactionPalette.TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = title,
                    color = GoalTransactionPalette.TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        navigationIcon = {
            CircularGoalTransactionIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Atrás",
                tint = GoalTransactionPalette.TextPrimary,
                borderColor = goalColor.copy(alpha = 0.32f),
                onClick = onBack,
                modifier = Modifier.padding(start = 12.dp)
            )
        },
        actions = {
            if (showDelete) {
                CircularGoalTransactionIconButton(
                    icon = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = GoalTransactionPalette.Coral,
                    borderColor = GoalTransactionPalette.Coral.copy(alpha = 0.30f),
                    onClick = onDelete,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = GoalTransactionPalette.Background,
            titleContentColor = GoalTransactionPalette.TextPrimary,
            navigationIconContentColor = GoalTransactionPalette.TextPrimary,
            actionIconContentColor = GoalTransactionPalette.Coral
        )
    )
}

@Composable
private fun CircularGoalTransactionIconButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    borderColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(GoalTransactionPalette.ElevatedCard)
            .border(BorderStroke(1.dp, borderColor), CircleShape)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = tint,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
private fun AmountEntryCard(
    currency: String,
    amount: String,
    goalColor: Color,
    onAmountChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = GoalTransactionPalette.ElevatedCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        border = BorderStroke(1.dp, goalColor.copy(alpha = 0.34f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            goalColor.copy(alpha = 0.18f),
                            GoalTransactionPalette.ElevatedCard,
                            GoalTransactionPalette.Card
                        )
                    )
                )
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Monto del aporte",
                        color = GoalTransactionPalette.TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Registra cuánto vas a guardar",
                        color = GoalTransactionPalette.TextSecondary.copy(alpha = 0.72f),
                        fontSize = 12.sp
                    )
                }
                Box(
                    modifier = Modifier
                        .height(28.dp)
                        .width(4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(goalColor)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = currency,
                    color = goalColor,
                    fontSize = 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(8.dp))
                TextField(
                    value = amount,
                    onValueChange = onAmountChange,
                    placeholder = {
                        Text(
                            text = "0",
                            color = GoalTransactionPalette.TextSecondary.copy(alpha = 0.62f),
                            fontSize = 42.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        cursorColor = goalColor,
                        focusedTextColor = GoalTransactionPalette.TextPrimary,
                        unfocusedTextColor = GoalTransactionPalette.TextPrimary
                    ),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Start,
                        lineHeight = 46.sp
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        }
    }
}

@Composable
private fun DateSelectionCard(
    selectedDate: LocalDate,
    goalColor: Color,
    onClick: () -> Unit
) {
    val title = when (selectedDate) {
        LocalDate.now() -> "Hoy"
        else -> selectedDate.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es")))
    }
    val subtitle = selectedDate.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es")))

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = GoalTransactionPalette.Card,
        border = BorderStroke(1.dp, GoalTransactionPalette.Border)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(goalColor.copy(alpha = 0.16f), CircleShape)
                    .border(BorderStroke(1.dp, goalColor.copy(alpha = 0.36f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = goalColor, modifier = Modifier.size(24.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = title,
                    color = GoalTransactionPalette.TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.forLanguageTag("es")) else it.toString() },
                    color = GoalTransactionPalette.TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Cambiar fecha",
                tint = GoalTransactionPalette.TextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun AccountSelectionSection(
    accounts: List<AccountResponse>,
    selectedAccountId: Long?,
    onAccountSelected: (Long) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Debitar de la cuenta:",
            color = GoalTransactionPalette.TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(accounts, key = { it.id }) { account ->
                PremiumGoalAccountChip(
                    account = account,
                    isSelected = selectedAccountId == account.id,
                    onClick = { onAccountSelected(account.id) }
                )
            }
        }
    }
}

@Composable
private fun PremiumGoalAccountChip(
    account: AccountResponse,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val accountColor = parseGoalTransactionColor(account.color) ?: GoalTransactionPalette.Lavender
    val accountVisual = accountVisual(account)

    Card(
        modifier = Modifier
            .widthIn(min = 178.dp, max = 212.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) GoalTransactionPalette.ElevatedCard else GoalTransactionPalette.Card
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 9.dp else 4.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) accountColor else GoalTransactionPalette.Border
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            accountColor.copy(alpha = if (isSelected) 0.18f else 0.08f),
                            Color.Transparent
                        )
                    )
                )
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(accountColor.copy(alpha = 0.16f), CircleShape)
                        .border(BorderStroke(1.dp, accountColor.copy(alpha = 0.38f)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(accountVisual, contentDescription = null, tint = accountColor, modifier = Modifier.size(21.dp))
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = account.name,
                        color = GoalTransactionPalette.TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Cuenta origen",
                        color = GoalTransactionPalette.TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatCurrencyAmount(account.currentBalance, account.currency, account.decimalPrecision ?: 2),
                    color = if (isSelected) accountColor else GoalTransactionPalette.TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(accountColor.copy(alpha = 0.18f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Seleccionada", tint = accountColor, modifier = Modifier.size(15.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectedGoalCard(
    goal: SavingGoalResponse,
    goalColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = "Hacia la meta:",
            color = GoalTransactionPalette.TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = GoalTransactionPalette.ElevatedCard),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = BorderStroke(1.dp, goalColor.copy(alpha = 0.42f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(goalColor.copy(alpha = 0.16f), GoalTransactionPalette.ElevatedCard)
                        )
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(13.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .background(goalColor.copy(alpha = 0.18f), CircleShape)
                        .border(BorderStroke(1.dp, goalColor.copy(alpha = 0.44f)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = goal.icon ?: "💰", fontSize = 27.sp)
                }
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = goal.name,
                        color = GoalTransactionPalette.TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Meta seleccionada",
                        color = GoalTransactionPalette.TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(goalColor.copy(alpha = 0.14f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Flag, contentDescription = null, tint = goalColor, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

@Composable
private fun PremiumGoalTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    singleLine: Boolean = false,
    minLines: Int = 1
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                color = GoalTransactionPalette.TextSecondary,
                fontWeight = FontWeight.SemiBold
            )
        },
        placeholder = { Text(placeholder, color = GoalTransactionPalette.TextSecondary.copy(alpha = 0.72f)) },
        leadingIcon = {
            Icon(icon, contentDescription = null, tint = GoalTransactionPalette.Lavender, modifier = Modifier.size(20.dp))
        },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        minLines = minLines,
        shape = RoundedCornerShape(22.dp),
        colors = TextFieldDefaults.colors(
            focusedContainerColor = GoalTransactionPalette.ElevatedCard,
            unfocusedContainerColor = GoalTransactionPalette.Card,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = GoalTransactionPalette.TextPrimary,
            unfocusedTextColor = GoalTransactionPalette.TextPrimary,
            cursorColor = GoalTransactionPalette.Lavender,
            focusedLabelColor = GoalTransactionPalette.Lavender,
            unfocusedLabelColor = GoalTransactionPalette.TextSecondary
        )
    )
}

@Composable
private fun GoalTransactionSaveBar(
    isFormValid: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit
) {
    val isActive = isFormValid && !isLoading
    Surface(
        color = GoalTransactionPalette.Background.copy(alpha = 0.96f),
        tonalElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 14.dp)
                .height(60.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(
                    Brush.horizontalGradient(
                        if (isActive) {
                            listOf(
                                GoalTransactionPalette.LavenderDeep,
                                GoalTransactionPalette.Lavender,
                                Color(0xFFC4B5FD)
                            )
                        } else {
                            listOf(
                                GoalTransactionPalette.ElevatedCard,
                                GoalTransactionPalette.Card
                            )
                        }
                    )
                )
                .border(
                    BorderStroke(
                        1.dp,
                        if (isActive) Color.White.copy(alpha = 0.18f) else GoalTransactionPalette.Border
                    ),
                    RoundedCornerShape(999.dp)
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = GoalTransactionPalette.TextPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = if (isFormValid) GoalTransactionPalette.TextPrimary else GoalTransactionPalette.TextSecondary,
                        modifier = Modifier.size(19.dp)
                    )
                    Text(
                        text = if (isFormValid) "Guardar movimiento" else "Completa monto y cuenta",
                        color = if (isFormValid) GoalTransactionPalette.TextPrimary else GoalTransactionPalette.TextSecondary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun parseGoalTransactionColor(color: String?): Color? {
    val rawColor = color?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val normalizedColor = if (rawColor.startsWith("#")) rawColor else "#$rawColor"
    return try {
        Color(android.graphics.Color.parseColor(normalizedColor))
    } catch (e: Exception) {
        null
    }
}

private fun accountVisual(account: AccountResponse): ImageVector {
    val normalizedName = account.name.lowercase(Locale.getDefault())
    return when {
        listOf("efectivo", "cash", "moneda", "billetera", "cartera").any { it in normalizedName } -> Icons.Default.Payments
        listOf("tarjeta", "debito", "débito", "credito", "crédito", "banco").any { it in normalizedName } -> Icons.Default.CreditCard
        listOf("ahorro", "meta", "alcancia", "alcancía").any { it in normalizedName } -> Icons.Default.Savings
        else -> Icons.Default.AccountBalanceWallet
    }
}
