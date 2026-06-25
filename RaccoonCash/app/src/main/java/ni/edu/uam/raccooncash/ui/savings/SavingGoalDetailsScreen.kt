package ni.edu.uam.raccooncash.ui.savings

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.raccooncash.data.model.SavingGoalResponse
import ni.edu.uam.raccooncash.ui.accounts.TransactionItem
import ni.edu.uam.raccooncash.ui.accounts.AccountsViewModel
import ni.edu.uam.raccooncash.ui.components.RaccAddFloatingActionButton
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingGoalDetailsScreen(
    goalId: Long,
    viewModel: SavingsViewModel,
    accountsViewModel: AccountsViewModel,
    onAddTransaction: () -> Unit,
    onEditGoal: () -> Unit,
    onTransactionClick: (ni.edu.uam.raccooncash.data.model.TransactionResponse) -> Unit,
    onBack: () -> Unit
) {
    val allGoals by viewModel.savingGoals.collectAsState()
    val goal = remember(allGoals, goalId) { allGoals.find { it.id == goalId } }
    
    val transactions by viewModel.currentGoalTransactions.collectAsState()
    val accounts by accountsViewModel.accounts.collectAsState()
    val categories by accountsViewModel.categories.collectAsState()

    LaunchedEffect(goalId) {
        viewModel.loadGoalTransactions(goalId)
    }

    if (goal == null) {
        // Goal was probably deleted or hasn't loaded yet
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val goalColor = try {
        Color(android.graphics.Color.parseColor(goal.color))
    } catch (e: Exception) {
        Color(0xFF7E57C2)
    }

    val deadlineDate = try {
        LocalDate.parse(goal.deadline, DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (e: Exception) {
        LocalDate.now()
    }

    val daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(), deadlineDate).coerceAtLeast(1)
    val dailyNeeded = if (goal.targetAmount > goal.currentAmount) {
        (goal.targetAmount - goal.currentAmount) / daysRemaining
    } else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(goal.name, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    IconButton(onClick = onEditGoal) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = goalColor.copy(alpha = 0.2f),
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            RaccAddFloatingActionButton(
                onClick = onAddTransaction,
                contentDescription = "Agregar Ahorro"
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
                        .background(goalColor.copy(alpha = 0.2f))
                        .padding(bottom = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    GoalProgressCircle(goal = goal, color = goalColor)
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
                        text = deadlineDate.format(DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es"))),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "${goal.transactionCount ?: transactions.size} transacciones",
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Daily Needed Card
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Gray.copy(alpha = 0.1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = goalColor)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Ahorro diario necesario", color = Color.Gray, fontSize = 12.sp)
                                Text(
                                    "${goal.currency}${String.format(Locale.getDefault(), "%.2f", dailyNeeded)}",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            if (transactions.isEmpty()) {
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
                            Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(100.dp), tint = Color.Gray.copy(alpha = 0.2f))
                        }
                        Text("No se encontraron transacciones", color = Color.Gray)
                    }
                }
            } else {
                items(transactions) { transaction ->
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
fun GoalProgressCircle(goal: SavingGoalResponse, color: Color) {
    val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
    val percentage = (progress * 100).toInt()

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(240.dp)) {
        Canvas(modifier = Modifier.size(220.dp)) {
            // Background Circle
            drawCircle(
                color = Color.Gray.copy(alpha = 0.2f),
                style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
            )
            // Progress Arc
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
                Text(goal.icon ?: "💰", fontSize = 40.sp)
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
                    text = "${goal.currency}${String.format(Locale.getDefault(), "%.0f", goal.currentAmount)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = " / ${goal.currency}${String.format(Locale.getDefault(), "%.0f", goal.targetAmount)}",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}
