package ni.edu.uam.raccooncash.ui.savings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ni.edu.uam.raccooncash.data.model.SavingGoalResponse
import ni.edu.uam.raccooncash.util.formatCurrencyAmount
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private object SavingsPalette {
    val Background = Color(0xFF080B14)
    val Card = Color(0xFF171C2A)
    val ElevatedCard = Color(0xFF202638)
    val Lavender = Color(0xFFA78BFA)
    val Mint = Color(0xFF7EDC8D)
    val Sky = Color(0xFF74C7EC)
    val Orange = Color(0xFFFFB84D)
    val Coral = Color(0xFFFF7A85)
    val TextSecondary = Color(0xFF9CA3AF)
}

private data class GoalVisualState(
    val label: String,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsScreen(
    viewModel: SavingsViewModel = viewModel(),
    onAddGoalClick: () -> Unit,
    onGoalClick: (SavingGoalResponse) -> Unit
) {
    val savingGoals by viewModel.savingGoals.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()

    Scaffold(
        containerColor = SavingsPalette.Background,
        topBar = {
            TopAppBar(
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Ahorros", fontSize = 28.sp, fontWeight = FontWeight.Bold)
                        Text(
                            text = "Haz seguimiento a tus objetivos de ahorro",
                            color = SavingsPalette.TextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SavingsPalette.Background,
                    titleContentColor = Color.White,
                    actionIconContentColor = SavingsPalette.Lavender
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(SavingsPalette.Background)
                .padding(paddingValues)
        ) {
            if (isLoading && savingGoals.isEmpty()) {
                CircularProgressIndicator(
                    color = SavingsPalette.Lavender,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (error != null && savingGoals.isEmpty()) {
                EmptySavingsState(
                    title = "No pudimos cargar tus metas",
                    text = error!!,
                    onAddGoalClick = onAddGoalClick,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else if (savingGoals.isEmpty()) {
                EmptySavingsState(
                    title = "Aún no tienes metas",
                    text = "Crea tu primera meta de ahorro para comenzar a avanzar hacia tus objetivos.",
                    onAddGoalClick = onAddGoalClick,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(savingGoals) { goal ->
                        SavingGoalItem(goal = goal, onClick = { onGoalClick(goal) })
                    }
                    item {
                        AddSavingGoalCard(onClick = onAddGoalClick)
                    }
                }
            }
        }
    }
}

@Composable
fun SavingGoalItem(goal: SavingGoalResponse, onClick: () -> Unit) {
    val rawProgress = goalProgress(goal)
    val progress = rawProgress.coerceIn(0f, 1f)
    val percentage = (rawProgress * 100).toInt().coerceAtLeast(0)
    val remainingAmount = (goal.targetAmount - goal.currentAmount).coerceAtLeast(0.0)
    val goalColor = goalColor(goal)
    val deadlineDate = parseGoalDate(goal.deadline)
    val visualState = goalVisualState(rawProgress, deadlineDate)
    val dateLabel = deadlineDate?.let { formatGoalDate(it) } ?: "Sin fecha objetivo"
    val transactionLabel = transactionCountText(goal.transactionCount ?: 0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SavingsPalette.ElevatedCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, goalColor.copy(alpha = 0.24f))
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(goalColor.copy(alpha = 0.18f))
                        .border(BorderStroke(1.dp, goalColor.copy(alpha = 0.42f)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(goal.icon ?: "💰", fontSize = 30.sp)
                }

                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = goal.name,
                        color = Color.White,
                        fontSize = 21.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    GoalMetaLine(text = "Objetivo: $dateLabel")
                    GoalMetaLine(text = transactionLabel)
                }

                GoalStatusBadge(label = visualState.label, color = visualState.color)
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = SavingsPalette.Card,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Monto actual", color = SavingsPalette.TextSecondary, fontSize = 12.sp)
                            Text(
                                text = formatGoalMoney(goal.currency, goal.currentAmount),
                                color = Color.White,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 28.sp
                            )
                        }
                        Surface(
                            color = goalColor.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(50),
                            border = BorderStroke(1.dp, goalColor.copy(alpha = 0.38f))
                        ) {
                            Text(
                                text = "$percentage%",
                                color = goalColor,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    AmountDetailLine("Objetivo", formatGoalMoney(goal.currency, goal.targetAmount), SavingsPalette.TextSecondary)
                    AmountDetailLine("Faltan", formatGoalMoney(goal.currency, remainingAmount), visualState.color)
                }
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
                        tint = SavingsPalette.TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = dateLabel,
                        color = SavingsPalette.TextSecondary,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Abrir detalle",
                    tint = SavingsPalette.TextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun GoalMetaLine(text: String) {
    Text(
        text = text,
        color = SavingsPalette.TextSecondary,
        fontSize = 13.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun AmountDetailLine(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            color = SavingsPalette.TextSecondary,
            fontSize = 13.sp,
            modifier = Modifier.width(78.dp)
        )
        Text(
            text = value,
            color = color,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun GoalStatusBadge(label: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.16f),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.dp, color.copy(alpha = 0.42f))
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
private fun EmptySavingsState(
    title: String,
    text: String,
    onAddGoalClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = SavingsPalette.Card),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            border = BorderStroke(1.dp, SavingsPalette.Lavender.copy(alpha = 0.28f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .background(SavingsPalette.Lavender.copy(alpha = 0.16f), CircleShape)
                        .border(BorderStroke(1.dp, SavingsPalette.Lavender.copy(alpha = 0.45f)), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = SavingsPalette.Lavender, modifier = Modifier.size(34.dp))
                }
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = text,
                    color = SavingsPalette.TextSecondary,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        AddSavingGoalCard(onClick = onAddGoalClick)
    }
}

@Composable
fun AddSavingGoalCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SavingsPalette.Card),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, SavingsPalette.Lavender.copy(alpha = 0.46f))
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
                    .background(SavingsPalette.Lavender.copy(alpha = 0.18f), CircleShape)
                    .border(BorderStroke(1.dp, SavingsPalette.Lavender.copy(alpha = 0.42f)), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = null, tint = SavingsPalette.Lavender, modifier = Modifier.size(28.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Nueva meta", color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Text(
                    "Crea un nuevo objetivo de ahorro",
                    color = SavingsPalette.TextSecondary,
                    fontSize = 13.sp
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = SavingsPalette.Lavender,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private fun goalProgress(goal: SavingGoalResponse): Float {
    return if (goal.targetAmount > 0) {
        (goal.currentAmount / goal.targetAmount).toFloat()
    } else {
        0f
    }
}

private fun goalVisualState(progress: Float, deadlineDate: LocalDate?): GoalVisualState = when {
    progress >= 1f -> GoalVisualState("Completada", SavingsPalette.Mint)
    deadlineDate != null && deadlineDate.isBefore(LocalDate.now()) -> GoalVisualState("Vencida", SavingsPalette.Coral)
    progress >= 0.75f -> GoalVisualState("Cerca", SavingsPalette.Orange)
    else -> GoalVisualState("En progreso", SavingsPalette.Lavender)
}

private fun goalColor(goal: SavingGoalResponse): Color {
    return parseGoalColor(goal.color) ?: fallbackGoalColor(goal)
}

private fun parseGoalColor(value: String): Color? = try {
    Color(android.graphics.Color.parseColor(value))
} catch (e: Exception) {
    null
}

private fun fallbackGoalColor(goal: SavingGoalResponse): Color {
    val iconOrName = "${goal.icon.orEmpty()} ${goal.name}".lowercase(Locale.getDefault())
    return when {
        iconOrName.contains("✈") || iconOrName.contains("viaje") || iconOrName.contains("avion") || iconOrName.contains("avión") -> SavingsPalette.Sky
        iconOrName.contains("casa") || iconOrName.contains("hogar") -> SavingsPalette.Mint
        iconOrName.contains("carro") || iconOrName.contains("auto") -> SavingsPalette.Lavender
        iconOrName.contains("regalo") -> SavingsPalette.Orange
        else -> SavingsPalette.Lavender
    }
}

private fun parseGoalDate(value: String): LocalDate? = try {
    LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
} catch (e: Exception) {
    null
}

private fun formatGoalDate(date: LocalDate): String {
    return date.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es")))
}

private fun transactionCountText(count: Int): String = if (count == 1) "1 transacción" else "$count transacciones"

private fun formatGoalMoney(currency: String, value: Double): String {
    return formatCurrencyAmount(value, currency, 0)
}
