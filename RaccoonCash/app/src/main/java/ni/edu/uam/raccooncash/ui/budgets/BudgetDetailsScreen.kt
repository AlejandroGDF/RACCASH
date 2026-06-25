package ni.edu.uam.raccooncash.ui.budgets

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.raccooncash.data.model.CategoryResponse
import ni.edu.uam.raccooncash.data.model.PresupuestoRespuesta
import ni.edu.uam.raccooncash.data.model.TipoPeriodoPresupuesto
import ni.edu.uam.raccooncash.data.model.TransactionResponse
import ni.edu.uam.raccooncash.ui.accounts.AccountsViewModel
import ni.edu.uam.raccooncash.ui.accounts.TransactionItem
import ni.edu.uam.raccooncash.ui.accounts.getEmojiForCategory
import ni.edu.uam.raccooncash.ui.components.RaccAddFloatingActionButton
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetDetailsScreen(
    budgetId: Long,
    viewModel: BudgetsViewModel,
    accountsViewModel: AccountsViewModel,
    onAddTransaction: (PresupuestoRespuesta, Long?) -> Unit,
    onEditBudget: (PresupuestoRespuesta) -> Unit,
    onTransactionClick: (TransactionResponse) -> Unit,
    onBack: () -> Unit
) {
    val budgets by viewModel.budgets.collectAsState()
    val transactions by accountsViewModel.transactions.collectAsState()
    val accounts by accountsViewModel.accounts.collectAsState()
    val categories by accountsViewModel.categories.collectAsState()
    val categoryLimits by viewModel.currentBudgetCategoryLimits.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val budget = remember(budgets, budgetId) { budgets.find { it.id == budgetId } }

    LaunchedEffect(budgetId) {
        viewModel.loadBudgets()
        accountsViewModel.loadAccounts()
    }

    LaunchedEffect(budgetId, budget?.esGasto) {
        if (budget?.esGasto == true) {
            viewModel.loadBudgetCategoryLimits(budgetId)
        } else {
            viewModel.clearBudgetCategoryLimits()
        }
    }

    if (budget == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Presupuesto") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    Text("Presupuesto no encontrado", color = Color.Gray)
                }
            }
        }
        return
    }

    val budgetColor = remember(budget.color) {
        try {
            Color(android.graphics.Color.parseColor(budget.color))
        } catch (e: Exception) {
            Color(0xFF7E57C2)
        }
    }
    val startDate = remember(budget.fechaInicio) { parseBudgetDate(budget.fechaInicio) }
    val endDate = remember(startDate, budget.tipoPeriodo, budget.valorPeriodo) {
        calculateBudgetEndDate(startDate, budget.tipoPeriodo, budget.valorPeriodo)
    }
    val limitedCategoryIds = remember(categoryLimits) { categoryLimits.map { it.categoryId }.toSet() }
    val selectedCategoryId = categoryLimits.firstOrNull()?.categoryId
    val selectedBudgetCategory = remember(categories, selectedCategoryId) {
        selectedCategoryId?.let { findCategoryById(categories, it) }
    }
    val budgetCategoryIcon = remember(budget.esGasto, selectedBudgetCategory) {
        if (budget.esGasto) {
            getEmojiForCategory(selectedBudgetCategory?.name, selectedBudgetCategory?.icon)
        } else {
            "💰"
        }
    }
    val needsCategory = budget.esGasto && selectedCategoryId == null
    val budgetTransactions = remember(transactions, budget, startDate, endDate, limitedCategoryIds) {
        transactions
            .filter { transaction -> transaction.belongsToBudget(budget, startDate, endDate, limitedCategoryIds) }
            .sortedByDescending { it.date }
    }
    val remainingAmount = budget.monto - budget.montoActual
    val daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), endDate).coerceAtLeast(1)
    val dailyAmount = if (remainingAmount > 0) remainingAmount / daysRemaining else 0.0
    val currency = budget.moneda ?: "C$"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(budget.nombre, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Buscar */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                    IconButton(onClick = { onEditBudget(budget) }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = budgetColor.copy(alpha = 0.2f),
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            RaccAddFloatingActionButton(
                onClick = {
                    if (needsCategory) {
                        onEditBudget(budget)
                    } else {
                        onAddTransaction(budget, selectedCategoryId)
                    }
                },
                contentDescription = "Agregar transacción"
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(budgetColor.copy(alpha = 0.2f))
                        .padding(bottom = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    BudgetProgressCircle(budget = budget, color = budgetColor, categoryIcon = budgetCategoryIcon)
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${startDate.format(DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("es")))} - ${endDate.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es")))}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${budgetTransactions.size} transacciones",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Gray.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = budgetColor)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (budget.esGasto) "Disponible diario" else "Ahorro diario necesario",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = "$currency${String.format(Locale.getDefault(), "%.2f", dailyAmount)}",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            if (needsCategory) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 8.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFFFFF3CD).copy(alpha = 0.14f),
                        onClick = { onEditBudget(budget) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Elige una categoría",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Text(
                                text = "Este presupuesto necesita una categoría para preseleccionarla al crear transacciones.",
                                color = Color.Gray,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }

            if (budgetTransactions.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier.size(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(100.dp),
                                tint = Color.Gray.copy(alpha = 0.2f)
                            )
                        }
                        Text("No se encontraron transacciones", color = Color.Gray)
                    }
                }
            } else {
                items(budgetTransactions) { transaction ->
                    TransactionItem(
                        transaction = transaction,
                        allCategories = categories,
                        allAccounts = accounts,
                        onClick = { onTransactionClick(transaction) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BudgetProgressCircle(budget: PresupuestoRespuesta, color: Color, categoryIcon: String) {
    val progress = if (budget.monto > 0) (budget.montoActual / budget.monto).toFloat().coerceIn(0f, 1f) else 0f
    val percentage = (progress * 100).toInt()
    val currency = budget.moneda ?: "C$"

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
        Canvas(modifier = Modifier.size(220.dp)) {
            drawCircle(
                color = Color.Gray.copy(alpha = 0.2f),
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360 * progress,
                useCenter = false,
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center
            ) {
                Text(categoryIcon, fontSize = 38.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "$percentage%",
                fontSize = 40.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "$currency${String.format(Locale.getDefault(), "%.0f", budget.montoActual)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = " / $currency${String.format(Locale.getDefault(), "%.0f", budget.monto)}",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

private fun findCategoryById(categories: List<CategoryResponse>, categoryId: Long): CategoryResponse? {
    for (category in categories) {
        if (category.id == categoryId) return category

        val subcategory = category.subcategories?.let { findCategoryById(it, categoryId) }
        if (subcategory != null) return subcategory
    }

    return null
}

private fun TransactionResponse.belongsToBudget(
    budget: PresupuestoRespuesta,
    startDate: LocalDate,
    endDate: LocalDate,
    limitedCategoryIds: Set<Long>
): Boolean {
    val transactionDate = try {
        LocalDateTime.parse(date, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate()
    } catch (e: Exception) {
        return false
    }

    if (transactionDate.isBefore(startDate) || transactionDate.isAfter(endDate)) {
        return false
    }

    if (!budget.esGasto) {
        return savingGoalId != null
    }

    if (type != "EXPENSE") {
        return false
    }

    return budget.incluirTodasLasTransacciones || (categoryId != null && categoryId in limitedCategoryIds)
}

private fun parseBudgetDate(value: String): LocalDate {
    return try {
        LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (e: Exception) {
        LocalDate.now()
    }
}

private fun calculateBudgetEndDate(startDate: LocalDate, periodType: TipoPeriodoPresupuesto, periodValue: Int): LocalDate {
    val value = periodValue.toLong().coerceAtLeast(1)
    return when (periodType) {
        TipoPeriodoPresupuesto.DIARIO -> startDate.plusDays(value)
        TipoPeriodoPresupuesto.SEMANAL -> startDate.plusWeeks(value)
        TipoPeriodoPresupuesto.MENSUAL -> startDate.plusMonths(value)
        TipoPeriodoPresupuesto.ANUAL -> startDate.plusYears(value)
        TipoPeriodoPresupuesto.PERSONALIZADO -> startDate.plusDays(value)
    }.minusDays(1)
}
