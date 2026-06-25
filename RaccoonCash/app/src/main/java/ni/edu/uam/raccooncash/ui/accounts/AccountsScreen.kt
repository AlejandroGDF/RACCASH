package ni.edu.uam.raccooncash.ui.accounts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ni.edu.uam.raccooncash.data.model.AccountResponse
import ni.edu.uam.raccooncash.data.model.PresupuestoRespuesta
import ni.edu.uam.raccooncash.data.model.SavingGoalResponse
import ni.edu.uam.raccooncash.data.model.TransactionResponse
import ni.edu.uam.raccooncash.ui.budgets.BudgetsViewModel
import ni.edu.uam.raccooncash.ui.components.RaccAddFloatingActionButton
import ni.edu.uam.raccooncash.ui.savings.SavingsViewModel
import java.time.LocalDate
import java.time.LocalDateTime

private object HomePalette {
    val Background = Color(0xFF080B14)
    val BackgroundAlt = Color(0xFF0B1020)
    val Card = Color(0xFF171C2A)
    val ElevatedCard = Color(0xFF202638)
    val Border = Color.White.copy(alpha = 0.08f)
    val Lavender = Color(0xFFA78BFA)
    val Mint = Color(0xFF7EDC8D)
    val Sky = Color(0xFF74C7EC)
    val Coral = Color(0xFFFF7A85)
    val Alert = Color(0xFFFFB84D)
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFF9CA3AF)
}

data class AccountVisual(
    val icon: ImageVector,
    val color: Color,
    val backgroundColor: Color
)

fun getAccountVisual(accountName: String): AccountVisual {
    val normalizedName = accountName.trim().lowercase(Locale.getDefault())

    return when {
        listOf("débito", "debito", "tarjeta", "banco", "credit", "crédito", "credito")
            .any { it in normalizedName } -> AccountVisual(
            icon = Icons.Default.CreditCard,
            color = HomePalette.Sky,
            backgroundColor = HomePalette.Sky.copy(alpha = 0.14f)
        )

        listOf("efectivo", "cash", "cartera", "dinero", "bolsillo")
            .any { it in normalizedName } -> AccountVisual(
            icon = Icons.Default.Payments,
            color = HomePalette.Mint,
            backgroundColor = HomePalette.Mint.copy(alpha = 0.14f)
        )

        listOf("ahorro", "meta", "guardado", "alcancía", "alcancia")
            .any { it in normalizedName } -> AccountVisual(
            icon = Icons.Default.Star,
            color = HomePalette.Lavender,
            backgroundColor = HomePalette.Lavender.copy(alpha = 0.16f)
        )

        else -> AccountVisual(
            icon = Icons.Default.AccountBalanceWallet,
            color = Color(0xFFB6C2D9),
            backgroundColor = HomePalette.ElevatedCard.copy(alpha = 0.84f)
        )
    }
}

private fun parseAccountColor(color: String?): Color? {
    val rawColor = color?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val normalizedColor = if (rawColor.startsWith("#")) rawColor else "#$rawColor"

    return try {
        Color(android.graphics.Color.parseColor(normalizedColor))
    } catch (e: IllegalArgumentException) {
        null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    viewModel: AccountsViewModel = viewModel(),
    savingsViewModel: SavingsViewModel = viewModel(),
    budgetsViewModel: BudgetsViewModel = viewModel(),
    onAddAccountClick: () -> Unit,
    onAddTransactionClick: () -> Unit,
    onTransactionClick: (TransactionResponse) -> Unit = {},
    onAccountClick: (AccountResponse) -> Unit = {},
    onSettingsClick: () -> Unit
) {
    val accounts by viewModel.accounts.collectAsState()
    val transactions by viewModel.transactions.collectAsState()
    val allCategories by viewModel.categories.collectAsState()
    val savingGoals by savingsViewModel.savingGoals.collectAsState()
    val budgets by budgetsViewModel.budgets.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    // Agrupar transacciones por fecha
    val groupedTransactions = remember(transactions) {
        transactions.sortedByDescending { it.date }.groupBy {
            try {
                LocalDateTime.parse(it.date, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate()
            } catch (e: Exception) {
                LocalDate.now()
            }
        }
    }

    Scaffold(
        containerColor = HomePalette.Background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Inicio",
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold,
                            color = HomePalette.TextPrimary
                        )
                        Text(
                            "Resumen financiero",
                            fontSize = 13.sp,
                            color = HomePalette.TextSecondary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Configuración",
                            tint = HomePalette.TextSecondary
                        )
                    }
                    IconButton(onClick = { viewModel.loadAccounts() }) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Recargar",
                            tint = HomePalette.Sky
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = HomePalette.Background,
                    titleContentColor = HomePalette.TextPrimary,
                    actionIconContentColor = HomePalette.TextSecondary
                )
            )
        },
        floatingActionButton = {
            RaccAddFloatingActionButton(
                onClick = onAddTransactionClick,
                contentDescription = "Nueva Transacción"
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            HomePalette.Background,
                            HomePalette.BackgroundAlt,
                            HomePalette.Background
                        )
                    )
                )
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                if (isLoading && accounts.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(132.dp)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = HomePalette.Card),
                        shape = RoundedCornerShape(28.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HomePalette.Border)
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = HomePalette.Lavender)
                        }
                    }
                } else if (error != null && accounts.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = HomePalette.Card),
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HomePalette.Coral.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = error!!,
                            color = HomePalette.Coral,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                } else {
                    AccountsOverviewSection(
                        accounts = accounts,
                        transactions = transactions,
                        onAddAccountClick = onAddAccountClick,
                        onAccountClick = onAccountClick
                    )
                }
            }

            item {
                HomeInsightSection(
                    transactions = transactions,
                    savingGoals = savingGoals,
                    budgets = budgets
                )
            }

            item {
                BalanceTrendCard(transactions = transactions)
            }

            item {
                HomeSectionHeader(
                    title = "Transacciones recientes",
                    detail = if (transactions.isNotEmpty()) "${transactions.size} movimientos" else null
                )
            }

            if (transactions.isEmpty() && !isLoading) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = HomePalette.Card),
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, HomePalette.Border)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No hay transacciones registradas.", color = HomePalette.TextSecondary)
                        }
                    }
                }
            } else {
                groupedTransactions.forEach { (date, dailyTransactions) ->
                    val dailySum = dailyTransactions.sumOf { 
                        if (it.type == "INCOME") it.amount else if (it.type == "EXPENSE") -it.amount else 0.0 
                    }
                    
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val dateLabel = when (date) {
                                LocalDate.now() -> "Hoy, ${date.format(DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("es")))}"
                                LocalDate.now().minusDays(1) -> "Ayer, ${date.format(DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("es")))}"
                                else -> date.format(DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale.forLanguageTag("es")))
                            }
                            Text(
                                text = dateLabel.replaceFirstChar { it.uppercase() },
                                color = HomePalette.TextSecondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                             
                            if (dailySum != 0.0) {
                                Text(
                                    text = "${if (dailySum > 0) "+" else ""}C$${String.format(Locale.getDefault(), "%.2f", dailySum)}",
                                    color = HomePalette.TextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    items(dailyTransactions) { transaction ->
                        RecentTransactionItem(
                            transaction = transaction,
                            allCategories = allCategories,
                            allAccounts = accounts,
                            onClick = { onTransactionClick(transaction) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeInsightSection(
    transactions: List<TransactionResponse>,
    savingGoals: List<SavingGoalResponse>,
    budgets: List<PresupuestoRespuesta>
) {
    val today = LocalDate.now()
    val largestExpenseToday = remember(transactions, today) {
        transactions
            .filter { transaction ->
                transaction.type == "EXPENSE" && parseTransactionDate(transaction.date) == today
            }
            .maxByOrNull { it.amount }
    }
    val largestExpenseName = largestExpenseToday?.let { expense ->
        expense.description.takeIf { it.isNotBlank() }
            ?: expense.categoryName?.takeIf { it.isNotBlank() }
            ?: expense.category?.name?.takeIf { it.isNotBlank() }
            ?: "Gasto"
    } ?: "Sin gastos hoy"
    val largestExpenseAmount = largestExpenseToday?.let { formatHomeCurrency(it.amount) } ?: "C$0.00"
    val closestGoal = remember(savingGoals) {
        savingGoals.maxByOrNull { goal ->
            calculateHomeProgress(current = goal.currentAmount, target = goal.targetAmount)
        }
    }
    val closestGoalProgress = closestGoal?.let { goal ->
        calculateHomeProgress(current = goal.currentAmount, target = goal.targetAmount)
    } ?: 0.0
    val closestGoalRemaining = closestGoal?.let { goal ->
        (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)
    }
    val riskiestBudget = remember(budgets) {
        budgets.maxByOrNull { budget ->
            calculateHomeProgress(current = budget.montoActual, target = budget.monto)
        }
    }
    val riskiestBudgetProgress = riskiestBudget?.let { budget ->
        calculateHomeProgress(current = budget.montoActual, target = budget.monto)
    } ?: 0.0
    val riskiestBudgetRemaining = riskiestBudget?.let { budget ->
        (budget.monto - budget.montoActual).coerceAtLeast(0.0)
    }

    Column(modifier = Modifier.padding(top = 8.dp)) {
        HomeSectionHeader(
            title = "Resumen inteligente"
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                InsightCard(
                    title = "Mayor gasto",
                    status = largestExpenseName,
                    amount = largestExpenseAmount,
                    detail = largestExpenseToday?.let { "Hoy" },
                    accentColor = HomePalette.Coral
                )
            }
            item {
                InsightCard(
                    title = "Meta más cercana",
                    status = closestGoal?.name ?: "Sin metas activas",
                    amount = closestGoal?.let { goal ->
                        "Faltan ${formatHomeCurrency(closestGoalRemaining ?: 0.0, goal.currency)}"
                    },
                    detail = closestGoal?.let { "${formatHomePercent(closestGoalProgress)} completado" },
                    accentColor = HomePalette.Lavender
                )
            }
            item {
                InsightCard(
                    title = "Presupuesto en riesgo",
                    status = riskiestBudget?.nombre ?: "Sin presupuestos activos",
                    amount = riskiestBudget?.let { budget ->
                        "Restan ${formatHomeCurrency(riskiestBudgetRemaining ?: 0.0, budget.moneda ?: "C$")}"
                    },
                    detail = riskiestBudget?.let { "${formatHomePercent(riskiestBudgetProgress)} usado" },
                    accentColor = HomePalette.Alert
                )
            }
        }
    }
}

@Composable
private fun InsightCard(
    title: String,
    status: String,
    amount: String? = null,
    detail: String? = null,
    accentColor: Color
) {
    val cardWidth = when {
        amount != null && amount.length > 18 -> 224.dp
        amount != null && amount.length > 14 -> 208.dp
        else -> 196.dp
    }

    Card(
        modifier = Modifier
            .width(cardWidth)
            .height(148.dp),
        colors = CardDefaults.cardColors(containerColor = HomePalette.ElevatedCard),
        shape = RoundedCornerShape(24.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, accentColor.copy(alpha = 0.22f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.18f),
                            HomePalette.Card
                        )
                    )
                )
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .background(accentColor.copy(alpha = 0.16f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(accentColor, CircleShape)
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    color = HomePalette.TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = status,
                    color = accentColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                amount?.let {
                    val amountFontSize = when {
                        it.length > 18 -> 12.sp
                        it.length > 14 -> 14.sp
                        else -> 16.sp
                    }

                    Text(
                        text = it,
                        modifier = Modifier.fillMaxWidth(),
                        color = HomePalette.TextPrimary,
                        fontSize = amountFontSize,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                detail?.let {
                    Text(
                        text = it,
                        color = HomePalette.TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

private fun parseTransactionDate(date: String): LocalDate? {
    return try {
        LocalDateTime.parse(date, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate()
    } catch (ignored: Exception) {
        try {
            LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE)
        } catch (ignored: Exception) {
            null
        }
    }
}

private fun formatHomeCurrency(amount: Double): String {
    return formatHomeCurrency(amount, "C$")
}

private fun formatHomeCurrency(amount: Double, currency: String): String {
    return "$currency${String.format(Locale.US, "%.2f", amount)}"
}

private fun calculateHomeProgress(current: Double, target: Double): Double {
    return if (target > 0.0) {
        (current / target).coerceAtLeast(0.0)
    } else {
        0.0
    }
}

private fun formatHomePercent(progress: Double): String {
    return "${String.format(Locale.US, "%.0f", progress * 100)}%"
}

@Composable
private fun HomeSectionHeader(
    title: String,
    detail: String? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            color = HomePalette.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        detail?.let {
            Text(
                text = it,
                color = HomePalette.TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun AccountsOverviewSection(
    accounts: List<AccountResponse>,
    transactions: List<TransactionResponse>,
    onAddAccountClick: () -> Unit,
    onAccountClick: (AccountResponse) -> Unit
) {
    Column(modifier = Modifier.padding(top = 8.dp)) {
        HomeSectionHeader(
            title = "Cuentas",
            detail = if (accounts.isNotEmpty()) "${accounts.size} activas" else null
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(accounts) { account ->
                val transactionCount = transactions.count { it.accountId == account.id || it.toAccountId == account.id }
                AccountPocketCard(
                    account = account,
                    transactionCount = transactionCount,
                    onClick = { onAccountClick(account) }
                )
            }
            item {
                AddAccountCard(onClick = onAddAccountClick)
            }
        }
    }
}

@Composable
fun BalanceTrendCard(transactions: List<TransactionResponse>) {
    val sortedTransactions = remember(transactions) {
        transactions.sortedBy { it.date }
    }

    val balanceHistory = remember(sortedTransactions) {
        var current = 0.0
        sortedTransactions.map {
            current += if (it.type == "INCOME") it.amount else if (it.type == "EXPENSE") -it.amount else 0.0
            Pair(it.date, current)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(324.dp)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        colors = CardDefaults.cardColors(containerColor = HomePalette.Card),
        shape = RoundedCornerShape(32.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, HomePalette.Lavender.copy(alpha = 0.16f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            HomePalette.ElevatedCard.copy(alpha = 0.94f),
                            HomePalette.Card,
                            HomePalette.BackgroundAlt.copy(alpha = 0.92f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Text(
                        "Tendencia de balance",
                        color = HomePalette.TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "Flujo acumulado de tus movimientos",
                        color = HomePalette.TextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Box(
                    modifier = Modifier
                        .background(HomePalette.Lavender.copy(alpha = 0.14f), RoundedCornerShape(999.dp))
                        .padding(horizontal = 12.dp, vertical = 7.dp)
                ) {
                    Text(
                        "Últimos 14 días",
                        color = HomePalette.Lavender,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
              
            if (balanceHistory.size < 2) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .background(HomePalette.Lavender.copy(alpha = 0.12f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .background(HomePalette.Lavender.copy(alpha = 0.7f), CircleShape)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Sin tendencia disponible",
                            color = HomePalette.TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Agrega al menos dos movimientos",
                            color = HomePalette.TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            } else {
                val balances = balanceHistory.map { it.second }
                val maxBalance = balances.maxOrNull() ?: 1.0
                val minBalance = balances.minOrNull() ?: 0.0
                val range = (maxBalance - minBalance).coerceAtLeast(1.0)

                Canvas(modifier = Modifier.fillMaxSize()) {
                    val labelPadding = 56.dp.toPx()
                    val rightPadding = 28.dp.toPx()
                    val topPadding = 12.dp.toPx()
                    val bottomPadding = 30.dp.toPx()
                    val chartWidth = size.width - labelPadding - rightPadding
                    val chartHeight = size.height - topPadding - bottomPadding
                    val chartBottom = topPadding + chartHeight
                    val spacing = chartWidth / (balanceHistory.size - 1)

                    val gridColor = HomePalette.Border.copy(alpha = 0.55f)
                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.rgb(156, 163, 175)
                        textSize = 10.5f.sp.toPx()
                        textAlign = android.graphics.Paint.Align.RIGHT
                        isAntiAlias = true
                    }

                    val yLabels = 4
                    for (i in 0..yLabels) {
                        val labelValue = minBalance + (range * i / yLabels)
                        val y = chartBottom - (chartHeight * i / yLabels)
                        drawLine(
                            color = gridColor,
                            start = androidx.compose.ui.geometry.Offset(labelPadding, y),
                            end = androidx.compose.ui.geometry.Offset(size.width - rightPadding, y),
                            strokeWidth = 1.dp.toPx()
                        )
                        drawContext.canvas.nativeCanvas.drawText(
                            "C$${String.format(Locale.getDefault(), "%.0f", labelValue)}",
                            labelPadding - 12.dp.toPx(),
                            y + 4.dp.toPx(),
                            textPaint
                        )
                    }

                    val points = balanceHistory.mapIndexed { index, pair ->
                        val balance = pair.second
                        val x = labelPadding + index * spacing
                        val y = chartBottom - ((balance - minBalance) / range * chartHeight).toFloat()
                        androidx.compose.ui.geometry.Offset(x, y)
                    }

                    val strokePath = Path().apply {
                        points.forEachIndexed { index, offset ->
                            if (index == 0) moveTo(offset.x, offset.y)
                            else lineTo(offset.x, offset.y)
                        }
                    }

                    val fillPath = Path().apply {
                        addPath(strokePath)
                        lineTo(points.last().x, chartBottom)
                        lineTo(points.first().x, chartBottom)
                        close()
                    }

                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                HomePalette.Lavender.copy(alpha = 0.34f),
                                HomePalette.Sky.copy(alpha = 0.10f),
                                Color.Transparent
                            ),
                            startY = topPadding,
                            endY = chartBottom
                        )
                    )

                    drawPath(
                        path = strokePath,
                        color = HomePalette.Lavender.copy(alpha = 0.22f),
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )

                    drawPath(
                        path = strokePath,
                        color = HomePalette.Lavender,
                        style = Stroke(width = 3.2f.dp.toPx(), cap = StrokeCap.Round)
                    )

                    points.lastOrNull()?.let { lastPoint ->
                        drawCircle(
                            color = HomePalette.TextPrimary.copy(alpha = 0.94f),
                            radius = 5.5f.dp.toPx(),
                            center = lastPoint
                        )
                        drawCircle(
                            color = HomePalette.Lavender,
                            radius = 3.4f.dp.toPx(),
                            center = lastPoint
                        )
                    }

                    val datePaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.rgb(156, 163, 175)
                        textSize = 10.5f.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }

                    val dateFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("es"))
                    val labelIndexes = if (balanceHistory.size <= 5) {
                        balanceHistory.indices.toList()
                    } else {
                        listOf(
                            0,
                            balanceHistory.lastIndex / 4,
                            balanceHistory.lastIndex / 2,
                            balanceHistory.lastIndex * 3 / 4,
                            balanceHistory.lastIndex
                        ).distinct()
                    }

                    labelIndexes.forEach { index ->
                        parseTransactionDate(balanceHistory[index].first)?.let { date ->
                            val x = labelPadding + index * spacing
                            drawContext.canvas.nativeCanvas.drawText(
                                date.format(dateFormatter),
                                x,
                                size.height - 5.dp.toPx(),
                                datePaint
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AccountPocketCard(
    account: AccountResponse,
    transactionCount: Int,
    onClick: () -> Unit
) {
    val accountVisual = getAccountVisual(account.name)
    val savedColor = parseAccountColor(account.color)
    val accountColor = savedColor ?: accountVisual.color
    val accountBackgroundColor = savedColor?.copy(alpha = 0.16f) ?: accountVisual.backgroundColor

    Card(
        modifier = Modifier
            .width(228.dp)
            .height(136.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = HomePalette.Card),
        border = androidx.compose.foundation.BorderStroke(1.dp, accountColor.copy(alpha = 0.38f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 7.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(HomePalette.ElevatedCard, HomePalette.Card, accountBackgroundColor)
                    )
                )
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .background(accountBackgroundColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = accountVisual.icon,
                                contentDescription = null,
                                tint = accountColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = account.name,
                                color = HomePalette.TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "$transactionCount transacciones",
                                color = HomePalette.TextSecondary,
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = HomePalette.TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Column {
                    Text(
                        text = "${account.currency}${String.format("%.${account.decimalPrecision ?: 2}f", account.currentBalance)}",
                        color = HomePalette.TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(accountColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cuenta activa", color = HomePalette.TextSecondary, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun AddAccountCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(148.dp)
            .height(136.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = HomePalette.Card.copy(alpha = 0.62f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, HomePalette.Lavender.copy(alpha = 0.42f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            HomePalette.Lavender.copy(alpha = 0.14f),
                            HomePalette.ElevatedCard.copy(alpha = 0.36f)
                        )
                    )
                )
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(HomePalette.Lavender.copy(alpha = 0.16f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = HomePalette.Lavender,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Agregar cuenta",
                color = HomePalette.TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "Nuevo bolsillo",
                color = HomePalette.TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun RecentTransactionItem(
    transaction: TransactionResponse,
    allCategories: List<ni.edu.uam.raccooncash.data.model.CategoryResponse> = emptyList(),
    allAccounts: List<AccountResponse> = emptyList(),
    onClick: () -> Unit
) {
    val isSaving = transaction.description.startsWith("Ahorro para", ignoreCase = true)
    val isExpense = transaction.type == "EXPENSE"
    val isTransfer = transaction.type == "TRANSFER" || isSaving

    val category = transaction.category ?: allCategories.find { it.id == transaction.categoryId }
    val parentCategory = if (category?.parentCategoryId != null && category.parentCategoryId != 0L) {
        allCategories.find { it.id == category.parentCategoryId }
    } else null

    val amountColor = when {
        isExpense -> HomePalette.Coral
        isTransfer -> HomePalette.Sky
        else -> HomePalette.Mint
    }

    val prefix = when {
        isExpense -> "-"
        isTransfer -> ""
        else -> "+"
    }

    val accountColor = try {
        val colorHex = transaction.account?.color ?: allAccounts.find { it.id == transaction.accountId }?.color ?: "#7E57C2"
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        HomePalette.TextSecondary
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = HomePalette.Card.copy(alpha = 0.92f)),
        shape = RoundedCornerShape(22.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, HomePalette.Border)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(amountColor.copy(alpha = 0.13f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                val emoji = if (parentCategory != null) {
                    getEmojiForCategory(parentCategory.name, parentCategory.icon)
                } else {
                    getEmojiForCategory(transaction.categoryName ?: category?.name, category?.icon)
                }
                Text(text = emoji, fontSize = 23.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.description,
                    color = HomePalette.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(5.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(HomePalette.ElevatedCard.copy(alpha = 0.72f), RoundedCornerShape(999.dp))
                            .padding(horizontal = 7.dp, vertical = 3.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .background(accountColor, CircleShape)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = transaction.accountName ?: transaction.account?.name ?: "Cuenta desconocida",
                            color = HomePalette.TextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (parentCategory != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${getEmojiForCategory(category?.name, category?.icon)} ${transaction.categoryName ?: category?.name ?: ""}",
                            color = HomePalette.TextPrimary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier
                                .background(amountColor.copy(alpha = 0.14f), RoundedCornerShape(999.dp))
                                .padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$prefix C$ ${String.format(Locale.getDefault(), "%.2f", transaction.amount)}",
                    color = amountColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1
                )
                if (isTransfer) {
                    Text(
                        text = "→ ${transaction.destinationAccountName ?: transaction.toAccountName ?: transaction.toAccount?.name ?: ""}",
                        color = HomePalette.TextSecondary,
                        fontSize = 10.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionItem(
    transaction: TransactionResponse,
    allCategories: List<ni.edu.uam.raccooncash.data.model.CategoryResponse> = emptyList(),
    allAccounts: List<AccountResponse> = emptyList(),
    onClick: () -> Unit
) {
    val isSaving = transaction.description.startsWith("Ahorro para", ignoreCase = true)
    val isExpense = transaction.type == "EXPENSE"
    val isTransfer = transaction.type == "TRANSFER" || isSaving
    
    val category = transaction.category ?: allCategories.find { it.id == transaction.categoryId }
    val parentCategory = if (category?.parentCategoryId != null && category.parentCategoryId != 0L) {
        allCategories.find { it.id == category.parentCategoryId }
    } else null

    val amountColor = when {
        isExpense -> Color(0xFFEF9A9A)
        isTransfer -> Color(0xFF90CAF9)
        else -> Color(0xFFA5D6A7)
    }
    
    val prefix = when {
        isExpense -> "-"
        isTransfer -> ""
        else -> "+"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(amountColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            val emoji = if (parentCategory != null) {
                getEmojiForCategory(parentCategory.name, parentCategory.icon)
            } else {
                getEmojiForCategory(transaction.categoryName ?: category?.name, category?.icon)
            }
            Text(
                text = emoji,
                fontSize = 24.sp
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(transaction.description, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                val accountColor = try {
                    val colorHex = transaction.account?.color ?: allAccounts.find { it.id == transaction.accountId }?.color ?: "#7E57C2"
                    Color(android.graphics.Color.parseColor(colorHex))
                } catch (e: Exception) {
                    Color.Gray
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(accountColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = transaction.accountName ?: transaction.account?.name ?: "Cuenta desconocida",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }
                if (parentCategory != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${getEmojiForCategory(category?.name, category?.icon)} ${transaction.categoryName ?: category?.name ?: ""}",
                        color = Color.White,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .background(Color(0xFFEF9A9A).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$prefix C$ ${String.format(Locale.getDefault(), "%.2f", transaction.amount)}",
                color = amountColor,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            if (isTransfer) {
                Text(
                    text = "→ ${transaction.destinationAccountName ?: transaction.toAccountName ?: transaction.toAccount?.name ?: ""}",
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
        }
    }
}

fun getEmojiForCategory(categoryName: String?, serverIcon: String?): String {
    // Mapeo basado en el identificador de icono del servidor
    val iconMapping = mapOf(
        "utensils" to "🍴",
        "shopping-bag" to "🛍️",
        "car" to "🚕",
        "gamepad" to "🎮",
        "receipt" to "🧾",
        "home" to "🏠",
        "heart-pulse" to "🏥",
        "pill" to "💊",
        "graduation-cap" to "🎓",
        "plane" to "✈️",
        "gift" to "🎁",
        "sparkles" to "✨",
        "briefcase" to "💼",
        "house" to "🏠",
        "laptop" to "💻",
        "money-bill" to "💵",
        "bus" to "🚌",
        "hamburger" to "🍔",
        "shopping-cart" to "🛒",
        "lightbulb" to "💡",
        "wrench" to "🔧"
    )

    val normalizedServerIcon = serverIcon?.trim()
    if (!normalizedServerIcon.isNullOrEmpty()) {
        iconMapping[normalizedServerIcon.lowercase()]?.let { return it }
        if (normalizedServerIcon.isCustomEmojiValue()) return normalizedServerIcon
    }

    return when (categoryName?.lowercase()) {
        "comida", "restaurante" -> "🍴"
        "comestibles", "supermercado" -> "🥦"
        "compras" -> "🛍️"
        "transporte" -> "🚕"
        "entretenimiento" -> "🍿"
        "regalos" -> "🎁"
        "belleza" -> "🌸"
        "viajes" -> "✈️"
        "trabajo" -> "💼"
        "salud", "medicamento", "medicamentos" -> "💊"
        "educación" -> "📚"
        "hogar", "alquiler" -> "🏠"
        "facturas", "servicios" -> "🧾"
        "ahorro" -> "💰"
        "ingreso" -> "💵"
        "tecnología" -> "💻"
        "suscripciones" -> "📺"
        "deudas" -> "💳"
        "mascotas" -> "🐶"
        "emergencias" -> "🚨"
        "impuestos" -> "🏛️"
        "otros" -> "📝"
        "corrección de saldo" -> "⚖️"
        else -> "📝"
    }
}

private fun String.isCustomEmojiValue(): Boolean {
    return any { char ->
        val type = Character.getType(char)
        type == Character.SURROGATE.toInt() ||
            type == Character.OTHER_SYMBOL.toInt() ||
            type == Character.NON_SPACING_MARK.toInt() ||
            char == '\uFE0F' ||
            char == '\u200D'
    }
}

@Composable
fun AccountChip(
    account: ni.edu.uam.raccooncash.data.model.AccountResponse,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val accountColor = try {
        Color(android.graphics.Color.parseColor(account.color ?: "#7E57C2"))
    } catch (e: Exception) {
        Color(0xFF7E57C2)
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) accountColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, accountColor) else null,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(accountColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = account.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
