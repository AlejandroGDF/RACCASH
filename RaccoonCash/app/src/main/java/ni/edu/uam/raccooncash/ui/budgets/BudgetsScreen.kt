package ni.edu.uam.raccooncash.ui.budgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import ni.edu.uam.raccooncash.data.model.PresupuestoRespuesta
import ni.edu.uam.raccooncash.ui.components.RaccAddFloatingActionButton
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetsScreen(
    viewModel: BudgetsViewModel = viewModel(),
    onAddBudgetClick: () -> Unit,
    onBudgetClick: (PresupuestoRespuesta) -> Unit
) {
    val budgets by viewModel.budgets.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Scaffold(
        floatingActionButton = {
            RaccAddFloatingActionButton(
                onClick = onAddBudgetClick,
                contentDescription = "Añadir presupuesto"
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Presupuestos",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (isLoading && budgets.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (budgets.isEmpty()) {
                        item {
                            AddBudgetEmptyCard(onClick = onAddBudgetClick)
                        }
                    } else {
                        items(budgets) { budget ->
                            BudgetItem(budget = budget, onClick = { onBudgetClick(budget) })
                        }
                        item {
                            AddBudgetEmptyCard(onClick = onAddBudgetClick)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BudgetItem(budget: PresupuestoRespuesta, onClick: () -> Unit) {
    val progress = if (budget.monto > 0) (budget.montoActual / budget.monto).toFloat().coerceIn(0f, 1f) else 0f
    val budgetColor = try {
        Color(android.graphics.Color.parseColor(budget.color))
    } catch (e: Exception) {
        Color(0xFF7E57C2)
    }

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
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = budget.nombre,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                val moneda = budget.moneda ?: "C$"
                Text(
                    text = "$moneda${String.format(Locale.getDefault(), "%.2f", budget.monto)}",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = budgetColor,
                trackColor = Color.Gray.copy(alpha = 0.2f),
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val moneda = budget.moneda ?: "C$"
                Text(
                    text = "Gastado: $moneda${String.format(Locale.getDefault(), "%.2f", budget.montoActual)}",
                    color = Color.Gray,
                    fontSize = 14.sp
                )
                val remaining = budget.monto - budget.montoActual
                Text(
                    text = "Restan: $moneda${String.format(Locale.getDefault(), "%.2f", remaining)}",
                    color = if (remaining < 0) Color.Red else Color.Gray,
                    fontSize = 14.sp
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
            .height(180.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )
        }
    }
}
