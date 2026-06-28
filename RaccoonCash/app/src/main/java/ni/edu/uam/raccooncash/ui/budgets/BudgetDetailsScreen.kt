package ni.edu.uam.raccooncash.ui.budgets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.raccooncash.data.model.AccountResponse
import ni.edu.uam.raccooncash.data.model.CategoryResponse
import ni.edu.uam.raccooncash.data.model.PresupuestoRespuesta
import ni.edu.uam.raccooncash.data.model.TipoPeriodoPresupuesto
import ni.edu.uam.raccooncash.data.model.TransactionResponse
import ni.edu.uam.raccooncash.ui.accounts.AccountsViewModel
import ni.edu.uam.raccooncash.ui.accounts.getEmojiForCategory
import ni.edu.uam.raccooncash.ui.components.RaccAddFloatingActionButton
import ni.edu.uam.raccooncash.util.formatCurrencyAmount
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt

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
        MissingBudgetState(isLoading = isLoading, onBack = onBack)
        return
    }

    val startDate = remember(budget.fechaInicio) { parseBudgetDate(budget.fechaInicio) }
    val endDate = remember(startDate, budget.tipoPeriodo, budget.valorPeriodo) {
        calculateBudgetEndDate(startDate, budget.tipoPeriodo, budget.valorPeriodo)
    }
    val selectedCategoryId = categoryLimits.firstOrNull()?.categoryId
    val selectedBudgetCategory = remember(categories, selectedCategoryId) {
        selectedCategoryId?.let { findCategoryById(categories, it) }
    }
    val categoryName = selectedBudgetCategory?.name
        ?: if (budget.incluirTodasLasTransacciones) "Todas las categorías" else "Sin categoría fija"
    val categoryIcon = if (budget.esGasto) {
        getEmojiForCategory(selectedBudgetCategory?.name, selectedBudgetCategory?.icon)
    } else {
        "💰"
    }
    val budgetTransactions = remember(transactions, budget.id) {
        transactions
            .filter { transaction -> transaction.budgetId == budget.id && transaction.active }
            .sortedByDescending { it.date }
    }
    val spentAmount = remember(budgetTransactions) { budgetTransactions.sumOf { it.amount } }
    val remainingAmount = budget.monto - spentAmount
    val daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), endDate).coerceAtLeast(1)
    val dailyAmount = if (remainingAmount > 0.0) remainingAmount / daysRemaining else 0.0
    val progressRaw = if (budget.monto > 0.0) spentAmount / budget.monto else 0.0
    val percentage = (progressRaw * 100).roundToInt().coerceAtLeast(0)
    val status = budgetStatusFor(progressRaw)
    val currency = budget.moneda ?: "C$"

    Scaffold(
        containerColor = BudgetDetailPalette.Background,
        floatingActionButton = {
            RaccAddFloatingActionButton(
                onClick = { onAddTransaction(budget, selectedCategoryId) },
                contentDescription = "Agregar transacción al presupuesto"
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            BudgetDetailPalette.Background,
                            BudgetDetailPalette.BackgroundAlt,
                            BudgetDetailPalette.Background
                        )
                    )
                )
                .padding(padding),
            contentPadding = PaddingValues(start = 20.dp, top = 18.dp, end = 20.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                BudgetHeader(
                    title = budget.nombre,
                    onBack = onBack,
                    onSearch = { },
                    onEdit = { onEditBudget(budget) }
                )
            }

            item {
                BudgetProgressCard(
                    categoryIcon = categoryIcon,
                    categoryName = categoryName,
                    percentage = percentage,
                    progress = progressRaw.toFloat().coerceIn(0f, 1f),
                    spentAmount = spentAmount,
                    limitAmount = budget.monto,
                    currency = currency,
                    status = status
                )
            }

            item {
                PeriodInfoCard(
                    startDate = startDate,
                    endDate = endDate,
                    transactionCount = budgetTransactions.size
                )
            }

            item {
                DailyAvailableCard(
                    amount = dailyAmount,
                    currency = currency
                )
            }

            item {
                AmountSummaryCard(
                    spentAmount = spentAmount,
                    remainingAmount = remainingAmount,
                    limitAmount = budget.monto,
                    currency = currency
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Movimientos del presupuesto",
                        color = BudgetDetailPalette.TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "${budgetTransactions.size}",
                        color = BudgetDetailPalette.Lavender,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (budgetTransactions.isEmpty()) {
                item { EmptyBudgetMovementsCard() }
            } else {
                items(budgetTransactions, key = { it.id }) { transaction ->
                    BudgetMovementItem(
                        transaction = transaction,
                        accounts = accounts,
                        currency = currency,
                        onClick = { onTransactionClick(transaction) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MissingBudgetState(isLoading: Boolean, onBack: () -> Unit) {
    Scaffold(containerColor = BudgetDetailPalette.Background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BudgetHeader(
                title = "Presupuesto",
                onBack = onBack,
                onSearch = { },
                onEdit = { }
            )
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (isLoading) {
                    CircularProgressIndicator(color = BudgetDetailPalette.Lavender)
                } else {
                    Text("Presupuesto no encontrado", color = BudgetDetailPalette.TextSecondary)
                }
            }
        }
    }
}

@Composable
private fun BudgetHeader(
    title: String,
    onBack: () -> Unit,
    onSearch: () -> Unit,
    onEdit: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CircularActionButton(
            icon = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = "Atrás",
            onClick = onBack
        )
        Text(
            text = title,
            color = BudgetDetailPalette.TextPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        CircularActionButton(
            icon = Icons.Default.Search,
            contentDescription = "Buscar",
            onClick = onSearch
        )
        CircularActionButton(
            icon = Icons.Default.Edit,
            contentDescription = "Editar",
            onClick = onEdit
        )
    }
}

@Composable
private fun CircularActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(44.dp)
            .background(BudgetDetailPalette.ElevatedCard, CircleShape)
            .border(1.dp, BudgetDetailPalette.Border, CircleShape)
    ) {
        Icon(icon, contentDescription = contentDescription, tint = BudgetDetailPalette.TextPrimary)
    }
}

@Composable
private fun BudgetProgressCard(
    categoryIcon: String,
    categoryName: String,
    percentage: Int,
    progress: Float,
    spentAmount: Double,
    limitAmount: Double,
    currency: String,
    status: BudgetStatus
) {
    PremiumCard {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(
                            BudgetDetailPalette.ElevatedCard,
                            BudgetDetailPalette.Card,
                            BudgetDetailPalette.Lavender.copy(alpha = 0.16f)
                        )
                    ),
                    RoundedCornerShape(30.dp)
                )
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    BudgetProgressRing(
                        progress = progress,
                        categoryIcon = categoryIcon,
                        percentage = percentage,
                        color = status.progressColor
                    )
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Progreso del presupuesto",
                            color = BudgetDetailPalette.TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${formatCurrency(currency, spentAmount)} / ${formatCurrency(currency, limitAmount)}",
                            color = BudgetDetailPalette.TextPrimary,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 2
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            InfoChip(text = categoryName, color = BudgetDetailPalette.Lavender)
                            InfoChip(text = status.label, color = status.color)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetProgressRing(
    progress: Float,
    categoryIcon: String,
    percentage: Int,
    color: Color
) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(148.dp)) {
        Canvas(modifier = Modifier.size(140.dp)) {
            drawCircle(
                color = BudgetDetailPalette.Border.copy(alpha = 0.65f),
                style = Stroke(width = 13.dp.toPx(), cap = StrokeCap.Round)
            )
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = 13.dp.toPx(), cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = categoryIcon, fontSize = 32.sp)
            Text(
                text = "$percentage%",
                color = BudgetDetailPalette.TextPrimary,
                fontSize = 23.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun PeriodInfoCard(
    startDate: LocalDate,
    endDate: LocalDate,
    transactionCount: Int
) {
    PremiumCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AccentCircle(text = "📅", color = BudgetDetailPalette.Sky)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Periodo",
                    color = BudgetDetailPalette.TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatDateRange(startDate, endDate),
                    color = BudgetDetailPalette.TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = BudgetDetailPalette.Lavender.copy(alpha = 0.16f),
                border = BorderStroke(1.dp, BudgetDetailPalette.Lavender.copy(alpha = 0.30f))
            ) {
                Text(
                    text = "$transactionCount movs.",
                    color = BudgetDetailPalette.Lavender,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun DailyAvailableCard(
    amount: Double,
    currency: String
) {
    PremiumCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AccentCircle(text = "↗", color = BudgetDetailPalette.Mint)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Disponible diario",
                    color = BudgetDetailPalette.TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = formatCurrency(currency, amount),
                    color = BudgetDetailPalette.Mint,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
            Text(
                text = "restante",
                color = BudgetDetailPalette.TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AmountSummaryCard(
    spentAmount: Double,
    remainingAmount: Double,
    limitAmount: Double,
    currency: String
) {
    PremiumCard {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryAmountColumn(
                label = "Gastado",
                amount = formatCurrency(currency, spentAmount),
                color = BudgetDetailPalette.Coral,
                modifier = Modifier.weight(1f)
            )
            SummaryAmountColumn(
                label = "Restante",
                amount = formatCurrency(currency, remainingAmount),
                color = if (remainingAmount >= 0.0) BudgetDetailPalette.Mint else BudgetDetailPalette.Coral,
                modifier = Modifier.weight(1f)
            )
            SummaryAmountColumn(
                label = "Límite",
                amount = formatCurrency(currency, limitAmount),
                color = BudgetDetailPalette.Lavender,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun SummaryAmountColumn(
    label: String,
    amount: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(BudgetDetailPalette.ElevatedCard.copy(alpha = 0.72f))
            .padding(horizontal = 10.dp, vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            color = BudgetDetailPalette.TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1
        )
        Text(
            text = amount,
            color = color,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun BudgetMovementItem(
    transaction: TransactionResponse,
    accounts: List<AccountResponse>,
    currency: String,
    onClick: () -> Unit
) {
    val amountColor = when (transaction.type) {
        "EXPENSE" -> BudgetDetailPalette.Coral
        "INCOME" -> BudgetDetailPalette.Mint
        else -> BudgetDetailPalette.Sky
    }
    val prefix = when (transaction.type) {
        "EXPENSE" -> "-"
        "INCOME" -> "+"
        else -> ""
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        color = BudgetDetailPalette.Card,
        border = BorderStroke(1.dp, BudgetDetailPalette.Border),
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AccentCircle(text = "•", color = amountColor)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = transaction.description,
                    color = BudgetDetailPalette.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = transactionAccountName(transaction, accounts),
                    color = BudgetDetailPalette.TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatTransactionDate(transaction.date),
                    color = BudgetDetailPalette.TextMuted,
                    fontSize = 11.sp
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "$prefix${formatCurrency(currency, transaction.amount)}",
                    color = amountColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Abrir detalle",
                    tint = BudgetDetailPalette.TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyBudgetMovementsCard() {
    PremiumCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AccentCircle(text = "🦝", color = BudgetDetailPalette.Lavender)
            Text(
                text = "Sin movimientos asociados",
                color = BudgetDetailPalette.TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Agrega una transacción desde este presupuesto para verla aquí.",
                color = BudgetDetailPalette.TextSecondary,
                fontSize = 13.sp
            )
        }
    }
}

@Composable
private fun PremiumCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = BudgetDetailPalette.Card,
        border = BorderStroke(1.dp, BudgetDetailPalette.Border),
        shadowElevation = 8.dp,
        content = content
    )
}

@Composable
private fun InfoChip(text: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = color.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.32f))
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
        )
    }
}

@Composable
private fun AccentCircle(text: String, color: Color) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.16f))
            .border(1.dp, color.copy(alpha = 0.35f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = color, fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
    }
}

private data class BudgetStatus(
    val label: String,
    val color: Color,
    val progressColor: Color
)

private fun budgetStatusFor(progressRaw: Double): BudgetStatus {
    return when {
        progressRaw > 1.0 -> BudgetStatus("Excedido", BudgetDetailPalette.Coral, BudgetDetailPalette.Coral)
        progressRaw >= 0.8 -> BudgetStatus("Cuidado", BudgetDetailPalette.Lavender, BudgetDetailPalette.Lavender)
        else -> BudgetStatus("En control", BudgetDetailPalette.Mint, BudgetDetailPalette.Lavender)
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

private fun transactionAccountName(transaction: TransactionResponse, accounts: List<AccountResponse>): String {
    return transaction.accountName
        ?: transaction.account?.name
        ?: accounts.find { it.id == transaction.accountId }?.name
        ?: "Cuenta desconocida"
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

private fun formatCurrency(currency: String, amount: Double): String {
    return formatCurrencyAmount(amount, currency)
}

private fun formatDateRange(startDate: LocalDate, endDate: LocalDate): String {
    val locale = Locale.forLanguageTag("es")
    val startFormatter = DateTimeFormatter.ofPattern("d MMM", locale)
    val endFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", locale)
    return "${startDate.format(startFormatter)} - ${endDate.format(endFormatter)}"
}

private fun formatTransactionDate(value: String): String {
    return try {
        val date = LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate()
        date.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es")))
    } catch (e: Exception) {
        value
    }
}

private object BudgetDetailPalette {
    val Background = Color(0xFF080B14)
    val BackgroundAlt = Color(0xFF0B1020)
    val Card = Color(0xFF171C2A)
    val ElevatedCard = Color(0xFF202638)
    val Border = Color(0xFF2C3448)
    val Lavender = Color(0xFFA78BFA)
    val Mint = Color(0xFF7EDC8D)
    val Sky = Color(0xFF74C7EC)
    val Coral = Color(0xFFFF7A85)
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFF9CA3AF)
    val TextMuted = Color(0xFF6B7280)
}
