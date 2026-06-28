package ni.edu.uam.raccooncash.ui.budgets

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.raccooncash.data.model.CategoryResponse
import ni.edu.uam.raccooncash.data.model.PresupuestoRespuesta
import ni.edu.uam.raccooncash.data.model.TipoPeriodoPresupuesto
import ni.edu.uam.raccooncash.ui.accounts.AccountsViewModel
import ni.edu.uam.raccooncash.ui.accounts.accountColors
import ni.edu.uam.raccooncash.ui.accounts.getEmojiForCategory
import ni.edu.uam.raccooncash.util.formatCurrencyAmount
import ni.edu.uam.raccooncash.util.formatEditableMoney
import ni.edu.uam.raccooncash.util.isPotentialMoneyInput
import ni.edu.uam.raccooncash.util.parseMoneyInput
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private object AddBudgetPalette {
    val Background = Color(0xFF080B14)
    val BackgroundAlt = Color(0xFF0B1020)
    val Card = Color(0xFF171C2A)
    val ElevatedCard = Color(0xFF202638)
    val Border = Color.White.copy(alpha = 0.08f)
    val Lavender = Color(0xFFA78BFA)
    val Mint = Color(0xFF7EDC8D)
    val Sky = Color(0xFF74C7EC)
    val Orange = Color(0xFFFFB84D)
    val Coral = Color(0xFFFF7A85)
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFF9CA3AF)
    val TextMuted = Color(0xFF6B7280)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBudgetScreen(
    viewModel: BudgetsViewModel,
    accountsViewModel: AccountsViewModel,
    budgetToEdit: PresupuestoRespuesta? = null,
    onBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var nombre by remember { mutableStateOf(budgetToEdit?.nombre ?: "") }
    var monto by remember { mutableStateOf(formatEditableMoney(budgetToEdit?.monto)) }
    var esGasto by remember { mutableStateOf(true) }
    var valorPeriodo by remember { mutableStateOf(budgetToEdit?.valorPeriodo?.toString() ?: "1") }
    var tipoPeriodo by remember { mutableStateOf(budgetToEdit?.tipoPeriodo ?: TipoPeriodoPresupuesto.MENSUAL) }

    val initialDate = if (budgetToEdit != null) {
        try {
            LocalDate.parse(budgetToEdit.fechaInicio, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (e: Exception) {
            LocalDate.now()
        }
    } else {
        LocalDate.now()
    }
    var fechaInicio by remember { mutableStateOf(initialDate) }

    val initialColor = if (budgetToEdit != null) {
        try {
            Color(android.graphics.Color.parseColor(budgetToEdit.color))
        } catch (e: Exception) {
            accountColors[0]
        }
    } else {
        accountColors[0]
    }
    var selectedColor by remember { mutableStateOf(initialColor) }

    var incluirTodasLasTransacciones by remember { mutableStateOf(budgetToEdit?.incluirTodasLasTransacciones ?: true) }
    var selectedCategoryId by remember(budgetToEdit?.id) { mutableStateOf<Long?>(null) }

    val success by viewModel.operationSuccess.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val categories by accountsViewModel.categories.collectAsState()
    val categoryLimits by viewModel.currentBudgetCategoryLimits.collectAsState()
    val expenseCategories = categories.filter { it.type == "EXPENSE" }
    val selectedCategory = expenseCategories.firstOrNull { it.id == selectedCategoryId }
    val montoValue = parseMoneyInput(monto)
    val isFormValid = nombre.isNotBlank() && montoValue != null && montoValue > 0.0 && (!esGasto || selectedCategoryId != null)
    val periodValue = valorPeriodo.toLongOrNull()?.coerceAtLeast(1L) ?: 1L
    val periodLabel = budgetPeriodLabel(tipoPeriodo, periodValue)
    val endDate = remember(fechaInicio, periodValue, tipoPeriodo) {
        calculateBudgetEndDate(fechaInicio, tipoPeriodo, periodValue)
    }
    val days = remember(fechaInicio, endDate) { ChronoUnit.DAYS.between(fechaInicio, endDate).coerceAtLeast(0) + 1 }
    val dailyAmount = remember(montoValue, days) { (montoValue ?: 0.0) / days }

    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                fechaInicio = LocalDate.of(year, month + 1, dayOfMonth)
            },
            fechaInicio.year,
            fechaInicio.monthValue - 1,
            fechaInicio.dayOfMonth
        )
    }

    LaunchedEffect(success) {
        if (success) {
            onBack()
            viewModel.resetSuccess()
        }
    }

    LaunchedEffect(budgetToEdit?.id) {
        accountsViewModel.loadAccounts()
        if (budgetToEdit != null) {
            viewModel.loadBudgetCategoryLimits(budgetToEdit.id)
        } else {
            viewModel.clearBudgetCategoryLimits()
        }
    }

    LaunchedEffect(categoryLimits, budgetToEdit?.id) {
        if (budgetToEdit != null && selectedCategoryId == null) {
            selectedCategoryId = categoryLimits.firstOrNull()?.categoryId
        }
    }

    LaunchedEffect(error) {
        error?.let { snackbarHostState.showSnackbar(it) }
    }

    fun saveBudget() {
        if (!isFormValid || isLoading) return

        val argb = selectedColor.toArgb()
        val colorHex = String.format("#%06X", 0xFFFFFF and argb)
        val finalMonto = montoValue ?: 0.0
        val finalValorPeriodo = valorPeriodo.toIntOrNull() ?: 1

        if (budgetToEdit != null) {
            viewModel.updateBudget(
                id = budgetToEdit.id,
                nombre = nombre,
                monto = finalMonto,
                tipoPeriodo = tipoPeriodo,
                valorPeriodo = finalValorPeriodo,
                fechaInicio = fechaInicio.format(DateTimeFormatter.ISO_LOCAL_DATE),
                color = colorHex,
                esGasto = esGasto,
                incluirTodasLasTransacciones = if (esGasto) false else incluirTodasLasTransacciones,
                categoryId = if (esGasto) selectedCategoryId else null
            )
        } else {
            viewModel.createBudget(
                nombre = nombre,
                monto = finalMonto,
                tipoPeriodo = tipoPeriodo,
                valorPeriodo = finalValorPeriodo,
                fechaInicio = fechaInicio.format(DateTimeFormatter.ISO_LOCAL_DATE),
                color = colorHex,
                esGasto = esGasto,
                incluirTodasLasTransacciones = if (esGasto) false else incluirTodasLasTransacciones,
                categoryId = if (esGasto) selectedCategoryId else null
            )
        }
    }

    Scaffold(
        containerColor = AddBudgetPalette.Background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AddBudgetTopBar(
                title = if (budgetToEdit != null) "Editar presupuesto" else "Añadir presupuesto",
                onBack = onBack,
                onDelete = if (budgetToEdit != null) {
                    { viewModel.deleteBudget(budgetToEdit.id) }
                } else {
                    null
                }
            )
        },
        bottomBar = {
            SaveBudgetBottomBar(
                enabled = isFormValid && !isLoading,
                isLoading = isLoading,
                text = when {
                    !isFormValid -> "Completa los campos"
                    budgetToEdit != null -> "Guardar cambios"
                    else -> "Crear presupuesto"
                },
                accentColor = selectedColor,
                onClick = ::saveBudget
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            AddBudgetPalette.Background,
                            AddBudgetPalette.BackgroundAlt,
                            AddBudgetPalette.Background
                        )
                    )
                )
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BudgetConfigurationCard(
                name = nombre,
                onNameChange = { nombre = it },
                amount = monto,
                amountValue = montoValue,
                onAmountChange = { if (isPotentialMoneyInput(it)) monto = it },
                periodValue = valorPeriodo,
                onPeriodValueChange = { value -> if (value.isEmpty() || value.toIntOrNull() != null) valorPeriodo = value },
                periodLabel = periodLabel,
                onPeriodTypeClick = { tipoPeriodo = nextBudgetPeriodType(tipoPeriodo) },
                startDate = fechaInicio,
                endDate = endDate,
                dailyAmount = dailyAmount,
                days = days,
                accentColor = selectedColor,
                onStartDateClick = { datePickerDialog.show() }
            )

            BudgetCategorySelector(
                categories = expenseCategories,
                selectedCategoryId = selectedCategoryId,
                onCategorySelected = { selectedCategoryId = it }
            )

            BudgetColorSelector(
                selectedColor = selectedColor,
                onColorSelected = { selectedColor = it }
            )

            BudgetPreviewCard(
                name = nombre,
                category = selectedCategory,
                amountValue = montoValue,
                periodValue = valorPeriodo,
                periodLabel = periodLabel,
                accentColor = selectedColor
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddBudgetTopBar(
    title: String,
    onBack: () -> Unit,
    onDelete: (() -> Unit)?
) {
    TopAppBar(
        title = {
            Text(
                text = title,
                color = AddBudgetPalette.TextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            Surface(
                modifier = Modifier
                    .padding(start = 12.dp)
                    .size(42.dp)
                    .clip(CircleShape)
                    .clickable { onBack() },
                shape = CircleShape,
                color = AddBudgetPalette.ElevatedCard,
                border = BorderStroke(1.dp, AddBudgetPalette.Border)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Atrás",
                        tint = AddBudgetPalette.TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        actions = {
            onDelete?.let { delete ->
                IconButton(onClick = delete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = AddBudgetPalette.Coral
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
private fun BudgetConfigurationCard(
    name: String,
    onNameChange: (String) -> Unit,
    amount: String,
    amountValue: Double?,
    onAmountChange: (String) -> Unit,
    periodValue: String,
    onPeriodValueChange: (String) -> Unit,
    periodLabel: String,
    onPeriodTypeClick: () -> Unit,
    startDate: LocalDate,
    endDate: LocalDate,
    dailyAmount: Double,
    days: Long,
    accentColor: Color,
    onStartDateClick: () -> Unit
) {
    PremiumCard(accentColor = accentColor) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Configura tu plan",
                color = AddBudgetPalette.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.ExtraBold
            )

            BudgetNameField(
                value = name,
                onValueChange = onNameChange
            )

            BudgetAmountField(
                value = amount,
                amountValue = amountValue,
                accentColor = accentColor,
                onValueChange = onAmountChange
            )

            BudgetPeriodSelector(
                periodValue = periodValue,
                onPeriodValueChange = onPeriodValueChange,
                periodLabel = periodLabel,
                accentColor = accentColor,
                onPeriodTypeClick = onPeriodTypeClick
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BudgetDateChip(
                    label = "Comienzo",
                    value = formatBudgetDateShort(startDate),
                    accentColor = accentColor,
                    onClick = onStartDateClick,
                    modifier = Modifier.weight(1f)
                )
                BudgetInfoChip(
                    label = "Duración",
                    value = "${periodValue.ifBlank { "1" }} $periodLabel",
                    accentColor = AddBudgetPalette.Sky,
                    modifier = Modifier.weight(1f)
                )
            }

            PeriodSummaryCard(
                startDate = startDate,
                endDate = endDate,
                dailyAmount = dailyAmount,
                days = days,
                accentColor = accentColor
            )
        }
    }
}

@Composable
private fun BudgetNameField(
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Nombre del presupuesto",
            color = AddBudgetPalette.TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = AddBudgetPalette.ElevatedCard.copy(alpha = 0.92f),
            border = BorderStroke(1.dp, AddBudgetPalette.Border)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 15.dp),
                textStyle = TextStyle(
                    color = AddBudgetPalette.TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold
                ),
                cursorBrush = SolidColor(AddBudgetPalette.Lavender),
                singleLine = true,
                decorationBox = { innerTextField ->
                    Box(modifier = Modifier.fillMaxWidth()) {
                        if (value.isEmpty()) {
                            Text(
                                text = "Ej. Comida, Transporte, Lipton...",
                                color = AddBudgetPalette.TextMuted,
                                fontSize = 17.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

@Composable
private fun BudgetAmountField(
    value: String,
    amountValue: Double?,
    accentColor: Color,
    onValueChange: (String) -> Unit
) {
    val amountFontSize = when {
        value.length > 13 -> 28.sp
        value.length > 9 -> 34.sp
        else -> 42.sp
    }
    val previewText = amountValue?.let { formatCurrencyAmount(it) } ?: formatCurrencyAmount(0.0)

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = AddBudgetPalette.BackgroundAlt.copy(alpha = 0.72f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.34f))
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            AddBudgetPalette.ElevatedCard.copy(alpha = 0.92f),
                            AddBudgetPalette.Card,
                            accentColor.copy(alpha = 0.12f)
                        )
                    )
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Monto límite",
                    color = AddBudgetPalette.TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = previewText,
                    color = accentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "C$",
                    color = accentColor,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 58.dp),
                    textStyle = TextStyle(
                        color = AddBudgetPalette.TextPrimary,
                        fontSize = amountFontSize,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.End
                    ),
                    cursorBrush = SolidColor(accentColor),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            if (value.isEmpty()) {
                                Text(
                                    text = "0.00",
                                    color = AddBudgetPalette.TextMuted,
                                    fontSize = amountFontSize,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun BudgetPeriodSelector(
    periodValue: String,
    onPeriodValueChange: (String) -> Unit,
    periodLabel: String,
    accentColor: Color,
    onPeriodTypeClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Duración del presupuesto",
            color = AddBudgetPalette.TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.widthIn(min = 82.dp),
                shape = RoundedCornerShape(18.dp),
                color = AddBudgetPalette.ElevatedCard,
                border = BorderStroke(1.dp, AddBudgetPalette.Border)
            ) {
                BasicTextField(
                    value = periodValue,
                    onValueChange = onPeriodValueChange,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                    textStyle = TextStyle(
                        color = AddBudgetPalette.TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    ),
                    cursorBrush = SolidColor(accentColor),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { onPeriodTypeClick() },
                shape = RoundedCornerShape(18.dp),
                color = accentColor.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.44f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = periodLabel,
                            color = AddBudgetPalette.TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Toca para cambiar",
                            color = AddBudgetPalette.TextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(accentColor, CircleShape)
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetDateChip(
    label: String,
    value: String,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = AddBudgetPalette.ElevatedCard,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.30f))
    ) {
        Row(
            modifier = Modifier.padding(13.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(accentColor.copy(alpha = 0.16f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(label, color = AddBudgetPalette.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = value,
                    color = AddBudgetPalette.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun BudgetInfoChip(
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = AddBudgetPalette.ElevatedCard,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.26f))
    ) {
        Column(modifier = Modifier.padding(13.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text(label, color = AddBudgetPalette.TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(
                text = value.ifBlank { "1 mes" },
                color = AddBudgetPalette.TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PeriodSummaryCard(
    startDate: LocalDate,
    endDate: LocalDate,
    dailyAmount: Double,
    days: Long,
    accentColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = AddBudgetPalette.Background.copy(alpha = 0.48f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryLine(
                label = "Período actual",
                value = "${formatBudgetDateShort(startDate)} - ${formatBudgetDateShort(endDate)}",
                valueColor = AddBudgetPalette.TextPrimary
            )
            SummaryLine(
                label = "Disponible diario",
                value = "${formatCurrencyAmount(dailyAmount)}/día para $days días",
                valueColor = accentColor
            )
        }
    }
}

@Composable
private fun SummaryLine(
    label: String,
    value: String,
    valueColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, color = AddBudgetPalette.TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        Text(
            text = value,
            color = valueColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

@Composable
private fun BudgetCategorySelector(
    categories: List<CategoryResponse>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long) -> Unit
) {
    PremiumSectionCard {
        Text(
            text = "Categoría del presupuesto",
            color = AddBudgetPalette.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Las transacciones creadas desde este presupuesto usarán esta categoría.",
            color = AddBudgetPalette.TextSecondary,
            fontSize = 13.sp,
            lineHeight = 18.sp
        )

        if (categories.isEmpty()) {
            Text(
                text = "No hay categorías de gasto disponibles.",
                color = AddBudgetPalette.TextMuted,
                fontSize = 14.sp
            )
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(categories, key = { it.id }) { category ->
                    val categoryColor = parseCategoryColor(category.color) ?: AddBudgetPalette.Lavender
                    BudgetCategoryChip(
                        category = category,
                        color = categoryColor,
                        isSelected = selectedCategoryId == category.id,
                        onClick = { onCategorySelected(category.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetCategoryChip(
    category: CategoryResponse,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .heightIn(min = 52.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        color = if (isSelected) color.copy(alpha = 0.18f) else AddBudgetPalette.ElevatedCard,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) color else AddBudgetPalette.Border
        ),
        shadowElevation = if (isSelected) 8.dp else 0.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 13.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(color.copy(alpha = 0.18f), CircleShape)
                    .border(BorderStroke(1.dp, color.copy(alpha = 0.42f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = getEmojiForCategory(category.name, category.icon), fontSize = 17.sp)
            }
            Text(
                text = category.name,
                color = if (isSelected) AddBudgetPalette.TextPrimary else AddBudgetPalette.TextSecondary,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 138.dp)
            )
        }
    }
}

@Composable
private fun BudgetColorSelector(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    PremiumSectionCard {
        Text(
            text = "Color del presupuesto",
            color = AddBudgetPalette.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = "Este color se aplica a la vista previa y a los acentos del plan.",
            color = AddBudgetPalette.TextSecondary,
            fontSize = 13.sp
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(accountColors) { color ->
                val selected = selectedColor == color
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            BorderStroke(if (selected) 3.dp else 1.dp, if (selected) Color.White else Color.White.copy(alpha = 0.22f)),
                            CircleShape
                        )
                        .clickable { onColorSelected(color) },
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color.Black.copy(alpha = 0.28f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Color seleccionado",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetPreviewCard(
    name: String,
    category: CategoryResponse?,
    amountValue: Double?,
    periodValue: String,
    periodLabel: String,
    accentColor: Color
) {
    PremiumCard(accentColor = accentColor.copy(alpha = 0.86f)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = "Vista previa",
                        color = AddBudgetPalette.TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = name.ifBlank { "Nuevo presupuesto" },
                        color = AddBudgetPalette.TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = amountValue?.let { formatCurrencyAmount(it) } ?: formatCurrencyAmount(0.0),
                    color = accentColor,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val categoryColor = parseCategoryColor(category?.color) ?: accentColor
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .background(categoryColor.copy(alpha = 0.18f), CircleShape)
                        .border(BorderStroke(1.dp, categoryColor.copy(alpha = 0.42f)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category?.let { getEmojiForCategory(it.name, it.icon) } ?: "•",
                        color = categoryColor,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = category?.name ?: "Sin categoría seleccionada",
                        color = AddBudgetPalette.TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Duración: ${periodValue.ifBlank { "1" }} $periodLabel",
                        color = AddBudgetPalette.TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumCard(
    accentColor: Color,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = AddBudgetPalette.Card,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.28f)),
        shadowElevation = 10.dp
    ) {
        Box(
            modifier = Modifier.background(
                Brush.linearGradient(
                    listOf(
                        AddBudgetPalette.ElevatedCard.copy(alpha = 0.96f),
                        AddBudgetPalette.Card,
                        accentColor.copy(alpha = 0.10f)
                    )
                )
            )
        ) {
            content()
        }
    }
}

@Composable
private fun PremiumSectionCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = AddBudgetPalette.Card,
        border = BorderStroke(1.dp, AddBudgetPalette.Border),
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .background(AddBudgetPalette.ElevatedCard.copy(alpha = 0.55f))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content
        )
    }
}

@Composable
private fun SaveBudgetBottomBar(
    enabled: Boolean,
    isLoading: Boolean,
    text: String,
    accentColor: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = AddBudgetPalette.Background.copy(alpha = 0.97f),
        shadowElevation = 14.dp
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
                            Brush.horizontalGradient(listOf(AddBudgetPalette.Lavender, accentColor))
                        } else {
                            Brush.horizontalGradient(listOf(AddBudgetPalette.ElevatedCard, AddBudgetPalette.Card))
                        }
                    )
                    .border(
                        BorderStroke(1.dp, if (enabled) AddBudgetPalette.Lavender.copy(alpha = 0.78f) else AddBudgetPalette.Border),
                        shape
                    )
                    .clickable(enabled = enabled) { onClick() },
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = AddBudgetPalette.TextPrimary, modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                } else {
                    Text(
                        text = text,
                        color = if (enabled) AddBudgetPalette.TextPrimary else AddBudgetPalette.TextSecondary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun budgetPeriodLabel(type: TipoPeriodoPresupuesto, value: Long): String {
    return when (type) {
        TipoPeriodoPresupuesto.DIARIO -> if (value == 1L) "día" else "días"
        TipoPeriodoPresupuesto.SEMANAL -> if (value == 1L) "semana" else "semanas"
        TipoPeriodoPresupuesto.MENSUAL -> if (value == 1L) "mes" else "meses"
        TipoPeriodoPresupuesto.ANUAL -> if (value == 1L) "año" else "años"
        TipoPeriodoPresupuesto.PERSONALIZADO -> "período"
    }
}

private fun nextBudgetPeriodType(type: TipoPeriodoPresupuesto): TipoPeriodoPresupuesto {
    return when (type) {
        TipoPeriodoPresupuesto.DIARIO -> TipoPeriodoPresupuesto.SEMANAL
        TipoPeriodoPresupuesto.SEMANAL -> TipoPeriodoPresupuesto.MENSUAL
        TipoPeriodoPresupuesto.MENSUAL -> TipoPeriodoPresupuesto.ANUAL
        else -> TipoPeriodoPresupuesto.DIARIO
    }
}

private fun calculateBudgetEndDate(startDate: LocalDate, periodType: TipoPeriodoPresupuesto, periodValue: Long): LocalDate {
    val value = periodValue.coerceAtLeast(1L)
    return when (periodType) {
        TipoPeriodoPresupuesto.DIARIO -> startDate.plusDays(value)
        TipoPeriodoPresupuesto.SEMANAL -> startDate.plusWeeks(value)
        TipoPeriodoPresupuesto.MENSUAL -> startDate.plusMonths(value)
        TipoPeriodoPresupuesto.ANUAL -> startDate.plusYears(value)
        TipoPeriodoPresupuesto.PERSONALIZADO -> startDate.plusDays(value)
    }.minusDays(1)
}

private fun formatBudgetDateShort(date: LocalDate): String {
    return date.format(DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("es")))
}

private fun parseCategoryColor(color: String?): Color? {
    val rawColor = color?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val normalizedColor = if (rawColor.startsWith("#")) rawColor else "#$rawColor"
    return try {
        Color(android.graphics.Color.parseColor(normalizedColor))
    } catch (e: IllegalArgumentException) {
        null
    }
}
