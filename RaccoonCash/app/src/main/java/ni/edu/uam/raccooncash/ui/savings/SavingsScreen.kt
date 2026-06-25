package ni.edu.uam.raccooncash.ui.savings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ni.edu.uam.raccooncash.data.model.SavingGoalResponse
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

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
        topBar = {
            TopAppBar(
                title = { Text("Ahorros", fontSize = 28.sp, fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { viewModel.loadSavingGoals() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Recargar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading && savingGoals.isEmpty()) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (error != null && savingGoals.isEmpty()) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            } else if (savingGoals.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "No hay ninguna meta de ahorro actualmente",
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    AddSavingGoalCard(onClick = onAddGoalClick)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
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
    val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f) else 0f
    val percentage = (progress * 100).toInt()
    
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
    
    val dateLabel = deadlineDate.format(DateTimeFormatter.ofPattern("d MMM", Locale.forLanguageTag("es")))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E222D))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text(
                        text = goal.name,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateLabel,
                        color = Color.Gray,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${goal.transactionCount ?: 0} transacciones",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(CircleShape)
                        .background(goalColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(goal.icon ?: "💰", fontSize = 32.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${goal.currency}${String.format(Locale.getDefault(), "%.0f", goal.currentAmount)}",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = " / ${goal.currency}${String.format(Locale.getDefault(), "%.0f", goal.targetAmount)}",
                    color = Color.Gray,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(CircleShape)
                    .background(Color.Gray.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                // Progress Fill
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceAtLeast(0.01f)) // Show at least a bit if there is progress
                        .fillMaxHeight()
                        .align(Alignment.CenterStart)
                        .background(goalColor.copy(alpha = 0.4f))
                )
                
                Text(
                    text = "$percentage%",
                    color = Color(0xFFD1C4E9),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AddSavingGoalCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .width(120.dp)
            .height(120.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Nueva meta",
                color = Color.Gray,
                fontSize = 14.sp
            )
        }
    }
}
