package ni.edu.uam.raccooncash.ui.budgets

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ni.edu.uam.raccooncash.data.model.BudgetCategoryLimitResponse
import ni.edu.uam.raccooncash.data.model.PresupuestoRespuesta
import ni.edu.uam.raccooncash.ui.components.RaccAddFloatingActionButton
import ni.edu.uam.raccooncash.util.formatCurrencyAmount
import java.util.Locale

private object BudgetPalette {
    val Background = Color(0xFF080B14)
    val BackgroundAlt = Color(0xFF0B1020)
    val Card = Color(0xFF171C2A)
    val ElevatedCard = Color(0xFF202638)
    val Border = Color.White.copy(alpha = 0.08f)
    val Lavender = Color(0xFFA78BFA)
    val LavenderStrong = Color(0xFF7C3AED)
    val Mint = Color(0xFF7EDC8D)
    val Orange = Color(0xFFFFB84D)
    val Coral = Color(0xFFFF7A85)
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFF9CA3AF)
}

private data class BudgetStatusVisual(
    val label: String,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    viewModel: BudgetsViewModel = viewModel(),
    onAddBudgetClick: () -> Unit,
    onBudgetClick: (PresupuestoRespuesta) -> Unit
) {
    val budgets by viewModel.budgets.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val categoryLimitsByBudgetId by viewModel.budgetCategoryLimitsByBudgetId.collectAsState()
    val budgetIds = remember(budgets) { budgets.map { it.id } }

    LaunchedEffect(budgetIds) {
        viewModel.loadBudgetCategoryLimitsForList(budgetIds)
    }

    Scaffold(
        containerColor = BudgetPalette.Background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Presupuestos",
                            color = BudgetPalette.TextPrimary,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tus presupuestos y su estado actual",
                            color = BudgetPalette.TextSecondary,
                            fontSize = 13.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BudgetPalette.Background,
                    titleContentColor = BudgetPalette.TextPrimary
                )
            )
        },
        floatingActionButton = {
            RaccAddFloatingActionButton(
                onClick = onAddBudgetClick,
                contentDescription = "Añadir presupuesto"
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            BudgetPalette.Background,
                            BudgetPalette.BackgroundAlt,
                            BudgetPalette.Background
                        )
                    )
                )
                .padding(paddingValues),
            contentPadding = PaddingValues(start = 16.dp, top = 18.dp, end = 16.dp, bottom = 104.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (isLoading && budgets.isEmpty()) {
                item {
                    BudgetLoadingCard()
                }
            } else if (budgets.isEmpty()) {
                item {
                    EmptyBudgetsState()
                }
                item {
                    AddBudgetEmptyCard(onClick = onAddBudgetClick)
                }
            } else {
                items(budgets, key = { it.id }) { budget ->
                    BudgetItem(
                        budget = budget,
                        categoryLimits = categoryLimitsByBudgetId[budget.id].orEmpty(),
                        onClick = { onBudgetClick(budget) }
                    )
                }
                item {
                    AddBudgetEmptyCard(onClick = onAddBudgetClick)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BudgetItem(
    budget: PresupuestoRespuesta,
    categoryLimits: List<BudgetCategoryLimitResponse>,
    onClick: () -> Unit
) {
    val currency = budget.moneda ?: "C$"
    val usageRatio = calculateBudgetUsageRatio(budget)
    val progress = usageRatio.toFloat().coerceIn(0f, 1f)
    val status = getBudgetStatusVisual(usageRatio)
    val remaining = budget.monto - budget.montoActual
    val categoryLabels = remember(budget, categoryLimits) {
        buildBudgetCategoryLabels(budget, categoryLimits)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = BudgetPalette.Card),
        border = BorderStroke(1.dp, status.color.copy(alpha = 0.24f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            BudgetPalette.ElevatedCard.copy(alpha = 0.96f),
                            BudgetPalette.Card,
                            status.color.copy(alpha = 0.07f)
                        )
                    )
                )
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 12.dp)
                ) {
                    Text(
                        text = budget.nombre,
                        color = BudgetPalette.TextPrimary,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categoryLabels.forEach { label ->
                            BudgetCategoryChip(label = label)
                        }
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Límite",
                        color = BudgetPalette.TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = formatBudgetCurrency(budget.monto, currency),
                        color = BudgetPalette.TextPrimary,
                        fontSize = amountFontSize(formatBudgetCurrency(budget.monto, currency)),
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.End
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = BudgetPalette.TextSecondary,
                    modifier = Modifier
                        .padding(top = 14.dp)
                        .size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Usado: ${formatBudgetPercent(usageRatio)}",
                    color = BudgetPalette.TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
                BudgetStatusBadge(status = status)
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(999.dp)),
                color = status.color,
                trackColor = BudgetPalette.ElevatedCard.copy(alpha = 0.72f)
            )

            Spacer(modifier = Modifier.height(14.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                BudgetMetricPill(
                    label = "Gastado",
                    value = formatBudgetCurrency(budget.montoActual, currency),
                    valueColor = BudgetPalette.TextPrimary
                )
                BudgetMetricPill(
                    label = "Restante",
                    value = formatBudgetCurrency(remaining, currency),
                    valueColor = if (remaining < 0) BudgetPalette.Coral else BudgetPalette.Mint
                )
                BudgetMetricPill(
                    label = "Usado",
                    value = formatBudgetPercent(usageRatio),
                    valueColor = status.color
                )
            }
        }
    }
}

@Composable
fun AddBudgetEmptyCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 118.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = BudgetPalette.ElevatedCard),
        border = BorderStroke(1.dp, BudgetPalette.Lavender.copy(alpha = 0.46f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 118.dp)
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            BudgetPalette.ElevatedCard,
                            BudgetPalette.Card,
                            BudgetPalette.Lavender.copy(alpha = 0.12f)
                        )
                    )
                )
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(BudgetPalette.Lavender.copy(alpha = 0.16f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = BudgetPalette.Lavender,
                    modifier = Modifier.size(27.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Nuevo presupuesto",
                    color = BudgetPalette.TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Crea un nuevo presupuesto",
                    color = BudgetPalette.TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = BudgetPalette.Lavender,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun BudgetCategoryChip(label: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = BudgetPalette.ElevatedCard.copy(alpha = 0.86f),
        border = BorderStroke(1.dp, BudgetPalette.Lavender.copy(alpha = 0.24f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(BudgetPalette.Lavender, CircleShape)
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = label,
                color = BudgetPalette.TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 148.dp)
            )
        }
    }
}

@Composable
private fun BudgetStatusBadge(status: BudgetStatusVisual) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = status.color.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, status.color.copy(alpha = 0.36f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(status.color, CircleShape)
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = status.label,
                color = status.color,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun BudgetMetricPill(
    label: String,
    value: String,
    valueColor: Color
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = BudgetPalette.ElevatedCard.copy(alpha = 0.74f),
        border = BorderStroke(1.dp, BudgetPalette.Border)
    ) {
        Column(
            modifier = Modifier
                .widthIn(min = 104.dp, max = 184.dp)
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            Text(
                text = "$label:",
                color = BudgetPalette.TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = value,
                color = valueColor,
                fontSize = amountFontSize(value),
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun EmptyBudgetsState() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = BudgetPalette.Card),
        border = BorderStroke(1.dp, BudgetPalette.Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            BudgetPalette.ElevatedCard.copy(alpha = 0.95f),
                            BudgetPalette.Card
                        )
                    )
                )
                .padding(horizontal = 26.dp, vertical = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .background(BudgetPalette.Lavender.copy(alpha = 0.16f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = BudgetPalette.Lavender,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Aún no tienes presupuestos",
                color = BudgetPalette.TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Crea tu primer presupuesto para controlar tus gastos.",
                color = BudgetPalette.TextSecondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BudgetLoadingCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(138.dp),
        colors = CardDefaults.cardColors(containerColor = BudgetPalette.Card),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(1.dp, BudgetPalette.Border)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BudgetPalette.Lavender)
        }
    }
}

private fun calculateBudgetUsageRatio(budget: PresupuestoRespuesta): Double {
    return if (budget.monto > 0.0) {
        (budget.montoActual / budget.monto).coerceAtLeast(0.0)
    } else {
        0.0
    }
}

private fun getBudgetStatusVisual(usageRatio: Double): BudgetStatusVisual {
    return when {
        usageRatio >= 1.0 -> BudgetStatusVisual("Excedido", BudgetPalette.Coral)
        usageRatio >= 0.7 -> BudgetStatusVisual("En riesgo", BudgetPalette.Orange)
        else -> BudgetStatusVisual("En control", BudgetPalette.Mint)
    }
}

private fun buildBudgetCategoryLabels(
    budget: PresupuestoRespuesta,
    categoryLimits: List<BudgetCategoryLimitResponse>
): List<String> {
    val categoryNames = categoryLimits
        .mapNotNull { it.categoryName?.trim()?.takeIf { name -> name.isNotEmpty() } }
        .distinct()

    if (categoryNames.isEmpty()) {
        return if (budget.esGasto && budget.incluirTodasLasTransacciones) {
            listOf("Todas las categorías")
        } else {
            listOf("Sin categoría")
        }
    }

    val visibleCategories = categoryNames.take(3)
    return if (categoryNames.size > visibleCategories.size) {
        visibleCategories + "+${categoryNames.size - visibleCategories.size}"
    } else {
        visibleCategories
    }
}

private fun formatBudgetCurrency(amount: Double, currency: String): String {
    return formatCurrencyAmount(amount, currency)
}

private fun formatBudgetPercent(usageRatio: Double): String {
    return "${String.format(Locale.US, "%.0f", usageRatio * 100)}%"
}

private fun amountFontSize(value: String) = when {
    value.length > 18 -> 12.sp
    value.length > 14 -> 13.sp
    else -> 15.sp
}
