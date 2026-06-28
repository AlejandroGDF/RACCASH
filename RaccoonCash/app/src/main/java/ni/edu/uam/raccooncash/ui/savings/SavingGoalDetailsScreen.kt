package ni.edu.uam.raccooncash.ui.savings

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
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.raccooncash.data.model.AccountResponse
import ni.edu.uam.raccooncash.data.model.SavingGoalResponse
import ni.edu.uam.raccooncash.data.model.TransactionResponse
import ni.edu.uam.raccooncash.ui.accounts.AccountsViewModel
import ni.edu.uam.raccooncash.ui.components.RaccAddFloatingActionButton
import ni.edu.uam.raccooncash.util.formatCurrencyAmount
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

private object GoalDetailsPalette {
    val Background = Color(0xFF080B14)
    val BackgroundAlt = Color(0xFF0B1020)
    val Card = Color(0xFF171C2A)
    val ElevatedCard = Color(0xFF202638)
    val Lavender = Color(0xFFA78BFA)
    val LavenderDeep = Color(0xFF8B5CF6)
    val Mint = Color(0xFF7EDC8D)
    val Sky = Color(0xFF74C7EC)
    val Orange = Color(0xFFFFB84D)
    val Coral = Color(0xFFFF7A85)
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFF9CA3AF)
    val Border = Color.White.copy(alpha = 0.08f)
}

private data class GoalDetailsStatus(
    val label: String,
    val color: Color,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingGoalDetailsScreen(
    goalId: Long,
    viewModel: SavingsViewModel,
    accountsViewModel: AccountsViewModel,
    onAddTransaction: () -> Unit,
    onEditGoal: () -> Unit,
    onTransactionClick: (TransactionResponse) -> Unit,
    onBack: () -> Unit
) {
    val allGoals by viewModel.savingGoals.collectAsState()
    val goal = remember(allGoals, goalId) { allGoals.find { it.id == goalId } }
    val transactions by viewModel.currentGoalTransactions.collectAsState()
    val accounts by accountsViewModel.accounts.collectAsState()

    LaunchedEffect(goalId) {
        viewModel.loadGoalTransactions(goalId)
    }

    if (goal == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(GoalDetailsPalette.Background),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = GoalDetailsPalette.Lavender)
        }
        return
    }

    val goalColor = goalDetailsColor(goal)
    val deadlineDate = parseGoalDeadline(goal.deadline)
    val rawProgress = goalProgress(goal)
    val progress = rawProgress.coerceIn(0f, 1f)
    val percentage = (rawProgress * 100).toInt().coerceAtLeast(0)
    val status = goalDetailsStatus(rawProgress, deadlineDate)
    val remainingAmount = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)
    val daysRemaining = deadlineDate?.let { ChronoUnit.DAYS.between(LocalDate.now(), it).coerceAtLeast(1) } ?: 1
    val dailyNeeded = if (goal.targetAmount > goal.currentAmount) remainingAmount / daysRemaining else 0.0
    val deadlineLabel = deadlineDate?.let { formatFullGoalDate(it) } ?: "Sin fecha objetivo"
    val goalTransactions = remember(transactions, goalId) {
        transactions
            .filter { it.savingGoalId == goalId }
            .sortedByDescending { parseMovementDateTime(it.date) ?: LocalDateTime.MIN }
    }
    val transactionCount = goalTransactions.size

    Scaffold(
        containerColor = GoalDetailsPalette.Background,
        topBar = {
            GoalDetailTopBar(
                goal = goal,
                goalColor = goalColor,
                onBack = onBack,
                onEditGoal = onEditGoal
            )
        },
        floatingActionButton = {
            RaccAddFloatingActionButton(
                onClick = onAddTransaction,
                contentDescription = "Agregar movimiento a la meta"
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            GoalDetailsPalette.Background,
                            GoalDetailsPalette.BackgroundAlt,
                            GoalDetailsPalette.Background
                        )
                    )
                )
                .padding(padding),
            contentPadding = PaddingValues(start = 16.dp, top = 12.dp, end = 16.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                GoalProgressSummaryCard(
                    goal = goal,
                    goalColor = goalColor,
                    progress = progress,
                    percentage = percentage,
                    status = status
                )
            }

            item {
                GoalQuickInfoCard(
                    deadlineLabel = deadlineLabel,
                    transactionCount = transactionCount,
                    goalColor = goalColor
                )
            }

            item {
                DailySavingsCard(
                    amount = dailyNeeded,
                    currency = goal.currency,
                    goalColor = goalColor
                )
            }

            item {
                GoalAmountSummaryCard(
                    currency = goal.currency,
                    savedAmount = goal.currentAmount,
                    remainingAmount = remainingAmount,
                    targetAmount = goal.targetAmount,
                    goalColor = goalColor
                )
            }

            item {
                GoalMovementsHeader(transactionCount = transactionCount)
            }

            if (goalTransactions.isEmpty()) {
                item {
                    EmptyGoalMovementsCard(goalColor = goalColor)
                }
            } else {
                items(goalTransactions, key = { it.id }) { transaction ->
                    GoalMovementItem(
                        transaction = transaction,
                        accounts = accounts,
                        goal = goal,
                        goalColor = goalColor,
                        onClick = { onTransactionClick(transaction) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalDetailTopBar(
    goal: SavingGoalResponse,
    goalColor: Color,
    onBack: () -> Unit,
    onEditGoal: () -> Unit
) {
    TopAppBar(
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Meta de ahorro",
                    color = GoalDetailsPalette.TextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = goal.name,
                    color = GoalDetailsPalette.TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        },
        navigationIcon = {
            CircularHeaderButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Atrás",
                tint = GoalDetailsPalette.TextPrimary,
                borderColor = goalColor.copy(alpha = 0.32f),
                onClick = onBack,
                modifier = Modifier.padding(start = 12.dp)
            )
        },
        actions = {
            CircularHeaderButton(
                icon = Icons.Default.Edit,
                contentDescription = "Editar meta",
                tint = goalColor,
                borderColor = goalColor.copy(alpha = 0.34f),
                onClick = onEditGoal,
                modifier = Modifier.padding(end = 12.dp)
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = GoalDetailsPalette.Background,
            titleContentColor = GoalDetailsPalette.TextPrimary,
            navigationIconContentColor = GoalDetailsPalette.TextPrimary,
            actionIconContentColor = goalColor
        )
    )
}

@Composable
private fun CircularHeaderButton(
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
            .background(GoalDetailsPalette.ElevatedCard)
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
private fun GoalProgressSummaryCard(
    goal: SavingGoalResponse,
    goalColor: Color,
    progress: Float,
    percentage: Int,
    status: GoalDetailsStatus
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = GoalDetailsPalette.ElevatedCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        border = BorderStroke(1.dp, goalColor.copy(alpha = 0.30f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            goalColor.copy(alpha = 0.20f),
                            GoalDetailsPalette.ElevatedCard,
                            GoalDetailsPalette.Card
                        )
                    )
                )
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(3.dp), modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Progreso total",
                        color = GoalDetailsPalette.TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = goal.name,
                        color = GoalDetailsPalette.TextPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                GoalStatusBadge(status = status)
            }

            GoalProgressCircle(
                icon = goal.icon ?: "💰",
                progress = progress,
                percentage = percentage,
                goalColor = goalColor,
                statusColor = status.color
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
                    Text(
                        text = formatCurrencyAmount(goal.currentAmount, goal.currency, 0),
                        color = GoalDetailsPalette.TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1
                    )
                    Text(
                        text = " / ${formatCurrencyAmount(goal.targetAmount, goal.currency, 0)}",
                        color = GoalDetailsPalette.TextSecondary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 3.dp),
                        maxLines = 1
                    )
                }
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape),
                    color = goalColor,
                    trackColor = Color.White.copy(alpha = 0.10f)
                )
            }
        }
    }
}

@Composable
private fun GoalProgressCircle(
    icon: String,
    progress: Float,
    percentage: Int,
    goalColor: Color,
    statusColor: Color
) {
    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(236.dp)) {
        Canvas(modifier = Modifier.size(224.dp)) {
            val strokeWidth = 16.dp.toPx()
            drawCircle(
                color = Color.White.copy(alpha = 0.09f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            drawArc(
                color = goalColor,
                startAngle = -90f,
                sweepAngle = 360f * progress,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            if (progress > 0.02f) {
                drawArc(
                    color = statusColor.copy(alpha = 0.55f),
                    startAngle = -90f + (360f * progress) - 5f,
                    sweepAngle = 5f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(goalColor.copy(alpha = 0.18f))
                    .border(BorderStroke(1.dp, goalColor.copy(alpha = 0.50f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(text = icon, fontSize = 36.sp)
            }
            Text(
                text = "$percentage%",
                color = GoalDetailsPalette.TextPrimary,
                fontSize = 42.sp,
                fontWeight = FontWeight.ExtraBold,
                lineHeight = 42.sp
            )
            Text(
                text = "completado",
                color = GoalDetailsPalette.TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun GoalStatusBadge(status: GoalDetailsStatus) {
    Surface(
        color = status.color.copy(alpha = 0.16f),
        shape = RoundedCornerShape(999.dp),
        border = BorderStroke(1.dp, status.color.copy(alpha = 0.42f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = status.icon,
                contentDescription = null,
                tint = status.color,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = status.label,
                color = status.color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun GoalQuickInfoCard(
    deadlineLabel: String,
    transactionCount: Int,
    goalColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = GoalDetailsPalette.Card),
        border = BorderStroke(1.dp, GoalDetailsPalette.Border)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GoalInfoTile(
                icon = Icons.Default.DateRange,
                title = "Fecha objetivo",
                value = deadlineLabel,
                accentColor = goalColor,
                modifier = Modifier.weight(1f)
            )
            GoalInfoTile(
                icon = Icons.Default.Savings,
                title = "Transacciones",
                value = transactionCountText(transactionCount),
                accentColor = GoalDetailsPalette.Lavender,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun GoalInfoTile(
    icon: ImageVector,
    title: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(GoalDetailsPalette.ElevatedCard.copy(alpha = 0.62f), RoundedCornerShape(20.dp))
            .border(BorderStroke(1.dp, accentColor.copy(alpha = 0.18f)), RoundedCornerShape(20.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(accentColor.copy(alpha = 0.16f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(19.dp))
        }
        Text(
            text = title,
            color = GoalDetailsPalette.TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            color = GoalDetailsPalette.TextPrimary,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun DailySavingsCard(
    amount: Double,
    currency: String,
    goalColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = GoalDetailsPalette.ElevatedCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, goalColor.copy(alpha = 0.24f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(goalColor.copy(alpha = 0.14f), GoalDetailsPalette.ElevatedCard)
                    )
                )
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(goalColor.copy(alpha = 0.18f), CircleShape)
                    .border(BorderStroke(1.dp, goalColor.copy(alpha = 0.45f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.TrendingUp, contentDescription = null, tint = goalColor, modifier = Modifier.size(27.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Ahorro diario necesario",
                    color = GoalDetailsPalette.TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = formatCurrencyAmount(amount, currency),
                    color = GoalDetailsPalette.TextPrimary,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = "por día",
                color = goalColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(goalColor.copy(alpha = 0.14f), RoundedCornerShape(999.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun GoalAmountSummaryCard(
    currency: String,
    savedAmount: Double,
    remainingAmount: Double,
    targetAmount: Double,
    goalColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = GoalDetailsPalette.Card),
        border = BorderStroke(1.dp, GoalDetailsPalette.Border)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Resumen de montos",
                color = GoalDetailsPalette.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                AmountSummaryColumn(
                    label = "Ahorrado",
                    value = formatCurrencyAmount(savedAmount, currency, 0),
                    color = goalColor,
                    modifier = Modifier.weight(1f)
                )
                AmountSummaryColumn(
                    label = "Restante",
                    value = formatCurrencyAmount(remainingAmount, currency, 0),
                    color = GoalDetailsPalette.Mint,
                    modifier = Modifier.weight(1f)
                )
                AmountSummaryColumn(
                    label = "Objetivo",
                    value = formatCurrencyAmount(targetAmount, currency, 0),
                    color = GoalDetailsPalette.Lavender,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AmountSummaryColumn(
    label: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(GoalDetailsPalette.ElevatedCard.copy(alpha = 0.70f), RoundedCornerShape(18.dp))
            .border(BorderStroke(1.dp, color.copy(alpha = 0.16f)), RoundedCornerShape(18.dp))
            .padding(horizontal = 10.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            color = GoalDetailsPalette.TextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            maxLines = 1
        )
        Text(
            text = value,
            color = color,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun GoalMovementsHeader(transactionCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = "Movimientos de la meta",
                color = GoalDetailsPalette.TextPrimary,
                fontSize = 21.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Solo aportes vinculados explícitamente",
                color = GoalDetailsPalette.TextSecondary,
                fontSize = 12.sp
            )
        }
        Text(
            text = transactionCount.toString(),
            color = GoalDetailsPalette.Lavender,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .background(GoalDetailsPalette.Lavender.copy(alpha = 0.14f), RoundedCornerShape(999.dp))
                .border(BorderStroke(1.dp, GoalDetailsPalette.Lavender.copy(alpha = 0.28f)), RoundedCornerShape(999.dp))
                .padding(horizontal = 11.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun EmptyGoalMovementsCard(goalColor: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = GoalDetailsPalette.Card),
        border = BorderStroke(1.dp, GoalDetailsPalette.Border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(goalColor.copy(alpha = 0.14f), CircleShape)
                    .border(BorderStroke(1.dp, goalColor.copy(alpha = 0.36f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Search, contentDescription = null, tint = goalColor, modifier = Modifier.size(30.dp))
            }
            Text(
                text = "Sin movimientos asociados",
                color = GoalDetailsPalette.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Agrega un aporte desde el botón del mapache para vincularlo explícitamente a esta meta.",
                color = GoalDetailsPalette.TextSecondary,
                fontSize = 13.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun GoalMovementItem(
    transaction: TransactionResponse,
    accounts: List<AccountResponse>,
    goal: SavingGoalResponse,
    goalColor: Color,
    onClick: () -> Unit
) {
    val account = transaction.account ?: accounts.find { it.id == transaction.accountId }
    val accountColor = parseColor(account?.color) ?: goalColor
    val accountName = transaction.accountName ?: account?.name ?: "Cuenta desconocida"
    val dateLabel = formatMovementDate(parseMovementDateTime(transaction.date))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = GoalDetailsPalette.ElevatedCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        border = BorderStroke(1.dp, goalColor.copy(alpha = 0.18f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(goalColor.copy(alpha = 0.16f), RoundedCornerShape(18.dp))
                    .border(BorderStroke(1.dp, goalColor.copy(alpha = 0.34f)), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = goal.icon ?: "💰", fontSize = 25.sp)
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                Text(
                    text = transaction.description,
                    color = GoalDetailsPalette.TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    GoalAccountChip(
                        accountName = accountName,
                        accountColor = accountColor,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = dateLabel,
                        color = GoalDetailsPalette.TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                GoalContributionChip(goalColor = goalColor)
            }

            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = formatCurrencyAmount(transaction.amount, goal.currency),
                    color = goalColor,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
                Text(
                    text = "Aporte",
                    color = GoalDetailsPalette.TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Abrir movimiento",
                tint = GoalDetailsPalette.TextSecondary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun GoalAccountChip(
    accountName: String,
    accountColor: Color,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(GoalDetailsPalette.Card.copy(alpha = 0.86f), RoundedCornerShape(999.dp))
            .border(BorderStroke(1.dp, accountColor.copy(alpha = 0.22f)), RoundedCornerShape(999.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(accountColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
            text = accountName,
            color = GoalDetailsPalette.TextSecondary,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun GoalContributionChip(goalColor: Color) {
    Row(
        modifier = Modifier
            .background(goalColor.copy(alpha = 0.12f), RoundedCornerShape(999.dp))
            .border(BorderStroke(1.dp, goalColor.copy(alpha = 0.24f)), RoundedCornerShape(999.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            tint = goalColor,
            modifier = Modifier.size(13.dp)
        )
        Text(
            text = "Aporte a la meta",
            color = goalColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun goalProgress(goal: SavingGoalResponse): Float {
    return if (goal.targetAmount > 0.0) {
        (goal.currentAmount / goal.targetAmount).toFloat()
    } else {
        0f
    }
}

private fun goalDetailsStatus(progress: Float, deadlineDate: LocalDate?): GoalDetailsStatus = when {
    progress >= 1f -> GoalDetailsStatus("Completada", GoalDetailsPalette.Mint, Icons.Default.CheckCircle)
    deadlineDate != null && deadlineDate.isBefore(LocalDate.now()) -> GoalDetailsStatus("Vencida", GoalDetailsPalette.Coral, Icons.Default.Warning)
    progress >= 0.75f -> GoalDetailsStatus("Cerca", GoalDetailsPalette.Orange, Icons.Default.Star)
    else -> GoalDetailsStatus("En progreso", GoalDetailsPalette.Sky, Icons.Default.Flag)
}

private fun goalDetailsColor(goal: SavingGoalResponse): Color {
    return parseColor(goal.color) ?: fallbackGoalColor(goal)
}

private fun fallbackGoalColor(goal: SavingGoalResponse): Color {
    val iconOrName = "${goal.icon.orEmpty()} ${goal.name}".lowercase(Locale.getDefault())
    return when {
        iconOrName.contains("✈") || iconOrName.contains("viaje") || iconOrName.contains("avion") || iconOrName.contains("avión") -> GoalDetailsPalette.Sky
        iconOrName.contains("casa") || iconOrName.contains("hogar") -> GoalDetailsPalette.Mint
        iconOrName.contains("flor") || iconOrName.contains("sam") -> GoalDetailsPalette.Lavender
        iconOrName.contains("regalo") -> GoalDetailsPalette.Orange
        else -> GoalDetailsPalette.LavenderDeep
    }
}

private fun parseColor(value: String?): Color? {
    val rawColor = value?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val normalizedColor = if (rawColor.startsWith("#")) rawColor else "#$rawColor"
    return try {
        Color(android.graphics.Color.parseColor(normalizedColor))
    } catch (e: Exception) {
        null
    }
}

private fun parseGoalDeadline(value: String): LocalDate? = try {
    LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
} catch (e: Exception) {
    null
}

private fun parseMovementDateTime(value: String): LocalDateTime? {
    return try {
        LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    } catch (e: Exception) {
        try {
            LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay()
        } catch (inner: Exception) {
            null
        }
    }
}

private fun formatFullGoalDate(date: LocalDate): String {
    return date.format(DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es")))
}

private fun formatMovementDate(dateTime: LocalDateTime?): String {
    val date = dateTime?.toLocalDate() ?: return "Sin fecha"
    val today = LocalDate.now()
    return when (date) {
        today -> "Hoy"
        today.minusDays(1) -> "Ayer"
        else -> date.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es")))
    }
}

private fun transactionCountText(count: Int): String = if (count == 1) "1 transacción" else "$count transacciones"
