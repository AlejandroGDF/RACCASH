package ni.edu.uam.raccooncash.ui.savings

import android.app.DatePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import ni.edu.uam.raccooncash.ui.accounts.accountColors
import ni.edu.uam.raccooncash.ui.components.EmojiPickerDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSavingGoalScreen(
    viewModel: SavingsViewModel,
    goalToEdit: ni.edu.uam.raccooncash.data.model.SavingGoalResponse? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(goalToEdit?.name ?: "") }
    var targetAmount by remember { mutableStateOf(goalToEdit?.targetAmount?.toString() ?: "") }
    
    val initialColor = if (goalToEdit != null) {
        try { Color(android.graphics.Color.parseColor(goalToEdit.color)) } catch (e: Exception) { accountColors[0] }
    } else accountColors[0]
    var selectedColor by remember { mutableStateOf(initialColor) }
    
    var selectedEmoji by remember { mutableStateOf(goalToEdit?.icon ?: "💰") }
    
    val initialDeadline = if (goalToEdit != null) {
        try { LocalDate.parse(goalToEdit.deadline, DateTimeFormatter.ISO_LOCAL_DATE) } catch (e: Exception) { LocalDate.now().plusYears(1) }
    } else LocalDate.now().plusYears(1)
    var deadline by remember { mutableStateOf(initialDeadline) }
    
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    val success by viewModel.addGoalSuccess.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            deadline = LocalDate.of(year, month + 1, dayOfMonth)
        },
        deadline.year,
        deadline.monthValue - 1,
        deadline.dayOfMonth
    )
    
    LaunchedEffect(success) {
        if (success) {
            onBack()
            viewModel.resetSuccess()
        }
    }

    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("¿Eliminar objetivo?") },
            text = { Text("Esta acción no se puede deshacer y se perderán todos los datos asociados a este ahorro.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (goalToEdit != null) {
                            viewModel.deleteSavingGoal(goalToEdit.id)
                            showDeleteConfirmation = false
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Atrás")
                    }
                },
                actions = {
                    if (goalToEdit != null) {
                        IconButton(onClick = { 
                            showDeleteConfirmation = true
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                color = Color(0xFFD1C4E9),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                onClick = {
                    if (name.isNotEmpty() && targetAmount.isNotEmpty() && !isLoading) {
                        val argb = selectedColor.toArgb()
                        val colorHex = String.format("#%06X", 0xFFFFFF and argb)
                        if (goalToEdit != null) {
                            viewModel.updateSavingGoal(
                                id = goalToEdit.id,
                                name = name,
                                targetAmount = targetAmount.toDoubleOrNull() ?: 0.0,
                                deadline = deadline.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                color = colorHex,
                                icon = selectedEmoji
                            )
                        } else {
                            viewModel.addSavingGoal(
                                name = name,
                                targetAmount = targetAmount.toDoubleOrNull() ?: 0.0,
                                deadline = deadline.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                color = colorHex,
                                icon = selectedEmoji
                            )
                        }
                    }
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.Black)
                    } else {
                        Text(
                            if (name.isEmpty()) "Asignar nombre" else if (goalToEdit != null) "Guardar cambios" else "Guardar meta",
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Text(
                text = if (goalToEdit != null) "Editar objetivo" else "Agregar objetivo",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Goal Type Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f)
                        .background(Color(0xFF454B57), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(Color(0xFF81C784), CircleShape))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Meta De Ahorro", color = Color.White, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Icon and Name
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(selectedColor.copy(alpha = 0.3f))
                        .clickable { showEmojiPicker = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(selectedEmoji, fontSize = 48.sp)
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Nombre", fontSize = 32.sp, color = Color.Gray) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Gray,
                        unfocusedIndicatorColor = Color.Gray,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 32.sp, fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Color Palette
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF9FA8DA).copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = null, tint = Color.White)
                    }
                }
                items(accountColors) { color ->
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (selectedColor == color) 4.dp else 0.dp,
                                color = Color.White,
                                shape = CircleShape
                            )
                            .clickable { selectedColor = color }
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Target Amount and Date Section
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = Color.Gray.copy(alpha = 0.05f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.Gray.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text("Objetivo", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.White)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            "C$${if (targetAmount.isEmpty()) "0" else targetAmount}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            color = Color.Gray
                        )
                        // Input invisible mapping to text for style
                        TextField(
                            value = targetAmount,
                            onValueChange = { if (it.isEmpty() || it.replace(",", ".").toDoubleOrNull() != null) targetAmount = it },
                            modifier = Modifier.width(1.dp).height(1.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            )
                        )
                    }
                    
                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp), color = Color.Gray.copy(alpha = 0.2f))
                    
                    Text(
                        text = deadline.format(DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es"))),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.clickable { datePickerDialog.show() }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Para siempre",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                }
            }
        }
    }

    if (showEmojiPicker) {
        EmojiPickerDialog(
            onDismiss = { showEmojiPicker = false },
            title = "Elige un emoji para la meta",
            onEmojiSelected = { 
                selectedEmoji = it
                showEmojiPicker = false
            }
        )
    }
}
