package ni.edu.uam.raccooncash.ui.transactions

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.raccooncash.data.model.AccountResponse
import ni.edu.uam.raccooncash.data.model.CategoryResponse
import ni.edu.uam.raccooncash.data.model.TransactionResponse
import ni.edu.uam.raccooncash.ui.accounts.getEmojiForCategory
import ni.edu.uam.raccooncash.ui.components.EmojiPickerDialog
import ni.edu.uam.raccooncash.util.formatEditableMoney
import ni.edu.uam.raccooncash.util.isPotentialMoneyInput
import ni.edu.uam.raccooncash.util.parseMoneyInput
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private object TransactionPalette {
    val Background = Color(0xFF080B14)
    val BackgroundAlt = Color(0xFF0B1020)
    val Card = Color(0xFF171C2A)
    val ElevatedCard = Color(0xFF202638)
    val Border = Color.White.copy(alpha = 0.08f)
    val Lavender = Color(0xFFA78BFA)
    val LavenderStrong = Color(0xFF7C3AED)
    val Mint = Color(0xFF7EDC8D)
    val Sky = Color(0xFF74C7EC)
    val Orange = Color(0xFFFFB84D)
    val Coral = Color(0xFFFF7A85)
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFF9CA3AF)
}

private data class TransactionAccountVisual(
    val icon: ImageVector,
    val color: Color,
    val backgroundColor: Color
)

private fun parseTransactionColor(color: String?): Color? {
    val rawColor = color?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val normalizedColor = if (rawColor.startsWith("#")) rawColor else "#$rawColor"

    return try {
        Color(android.graphics.Color.parseColor(normalizedColor))
    } catch (e: IllegalArgumentException) {
        null
    }
}

private fun getTransactionAccountVisual(account: AccountResponse): TransactionAccountVisual {
    val normalizedName = account.name.trim().lowercase(Locale.getDefault())
    val fallback = when {
        listOf("efectivo", "cash", "moneda", "monedas", "dinero", "billetera", "cartera")
            .any { it in normalizedName } -> TransactionAccountVisual(
            icon = Icons.Default.Payments,
            color = TransactionPalette.Mint,
            backgroundColor = TransactionPalette.Mint.copy(alpha = 0.14f)
        )

        listOf("débito", "debito", "tarjeta", "banco", "credit", "crédito", "credito")
            .any { it in normalizedName } -> TransactionAccountVisual(
            icon = Icons.Default.CreditCard,
            color = TransactionPalette.Sky,
            backgroundColor = TransactionPalette.Sky.copy(alpha = 0.14f)
        )

        listOf("ahorro", "meta", "guardado", "alcancía", "alcancia", "viaje")
            .any { it in normalizedName } -> TransactionAccountVisual(
            icon = Icons.Default.Star,
            color = TransactionPalette.Orange,
            backgroundColor = TransactionPalette.Orange.copy(alpha = 0.14f)
        )

        else -> TransactionAccountVisual(
            icon = Icons.Default.AccountBalanceWallet,
            color = Color(0xFFB6C2D9),
            backgroundColor = TransactionPalette.ElevatedCard.copy(alpha = 0.84f)
        )
    }

    val savedColor = parseTransactionColor(account.color) ?: fallback.color
    return fallback.copy(
        color = savedColor,
        backgroundColor = savedColor.copy(alpha = 0.16f)
    )
}

private fun getTransactionTypeAccent(selectedTab: Int): Color {
    return when (selectedTab) {
        0 -> TransactionPalette.Coral
        1 -> TransactionPalette.Mint
        else -> TransactionPalette.Sky
    }
}

private fun formatTransactionAccountBalance(account: AccountResponse): String {
    val precision = account.decimalPrecision ?: 2
    return "${account.currency}${String.format(Locale.US, "%.${precision}f", account.currentBalance)}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: TransactionsViewModel,
    transactionToEdit: TransactionResponse? = null,
    initialType: String? = null,
    initialDescription: String = "",
    initialDate: LocalDate? = null,
    initialCategoryId: Long? = null,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val accounts by viewModel.accounts.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val success by viewModel.addTransactionSuccess.collectAsState()

    val initialTab = when (transactionToEdit?.type ?: initialType) {
        "INCOME" -> 1
        "TRANSFER" -> 2
        else -> 0
    }
    var selectedTab by remember { mutableIntStateOf(initialTab) }
    var amount by remember { mutableStateOf(formatEditableMoney(transactionToEdit?.amount)) }
    var title by remember { mutableStateOf(transactionToEdit?.description ?: initialDescription) }
    var notes by remember { mutableStateOf(transactionToEdit?.notes ?: "") }
    var selectedAccountId by remember { mutableStateOf<Long?>(transactionToEdit?.accountId ?: transactionToEdit?.account?.id) }
    var selectedToAccountId by remember { mutableStateOf<Long?>(transactionToEdit?.destinationAccountId ?: transactionToEdit?.toAccountId ?: transactionToEdit?.toAccount?.id) }
    var selectedCategoryId by remember { mutableStateOf<Long?>(transactionToEdit?.categoryId ?: transactionToEdit?.category?.id ?: initialCategoryId) }

    // Date and Time State
    val initialDateTime = if (transactionToEdit?.date != null) {
        try { LocalDateTime.parse(transactionToEdit.date, DateTimeFormatter.ISO_LOCAL_DATE_TIME) } catch (e: Exception) { LocalDateTime.now() }
    } else {
        LocalDateTime.of(initialDate ?: LocalDate.now(), LocalTime.now())
    }
    var selectedDate by remember { mutableStateOf(initialDateTime.toLocalDate()) }
    var selectedTime by remember { mutableStateOf(initialDateTime.toLocalTime()) }

    var showCategorySheet by remember { mutableStateOf(false) }
    var showSubcategorySheet by remember { mutableStateOf(false) }
    var parentCategoryForSub by remember { mutableStateOf<CategoryResponse?>(null) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var initialParentIdForNewCategory by remember { mutableStateOf<Long?>(null) }
    var categoryToEdit by remember { mutableStateOf<CategoryResponse?>(null) }
    val sheetState = rememberModalBottomSheetState()

    val tabs = listOf("Gasto", "Ingreso", "Transferir")

    LaunchedEffect(success) {
        if (success) {
            onBack()
            viewModel.resetSuccess()
        }
    }

    // Date Picker Dialog
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
        },
        selectedDate.year,
        selectedDate.monthValue - 1,
        selectedDate.dayOfMonth
    )

    // Time Picker Dialog
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute ->
            selectedTime = LocalTime.of(hour, minute)
        },
        selectedTime.hour,
        selectedTime.minute,
        true
    )

    val amountDouble = parseMoneyInput(amount)
    val isFormValid = selectedAccountId != null &&
            amountDouble != null &&
            amountDouble > 0.0 &&
            title.isNotBlank() &&
            (if (selectedTab == 2) selectedToAccountId != null else selectedCategoryId != null)

    fun saveTransaction() {
        val type = when (selectedTab) {
            0 -> "EXPENSE"
            1 -> "INCOME"
            else -> "TRANSFER"
        }
        val finalDateTime = LocalDateTime.of(selectedDate, selectedTime)

        if (isFormValid) {
            if (transactionToEdit != null) {
                viewModel.updateTransaction(
                    id = transactionToEdit.id,
                    amount = amountDouble ?: 0.0,
                    type = type,
                    accountId = selectedAccountId!!,
                    toAccountId = if (type == "TRANSFER") selectedToAccountId else null,
                    categoryId = if (type != "TRANSFER") selectedCategoryId else null,
                    description = title,
                    notes = notes,
                    dateTime = finalDateTime
                )
            } else {
                viewModel.createTransaction(
                    amount = amountDouble ?: 0.0,
                    type = type,
                    accountId = selectedAccountId!!,
                    toAccountId = if (type == "TRANSFER") selectedToAccountId else null,
                    categoryId = if (type != "TRANSFER") selectedCategoryId else null,
                    description = title,
                    notes = notes,
                    dateTime = finalDateTime
                )
            }
        }
    }

    Scaffold(
        containerColor = TransactionPalette.Background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (transactionToEdit != null) "Editar transacción" else "Agregar transacción",
                        color = TransactionPalette.TextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = TransactionPalette.TextPrimary
                        )
                    }
                },
                actions = {
                    if (transactionToEdit != null) {
                        IconButton(onClick = { viewModel.deleteTransaction(transactionToEdit.id) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Eliminar",
                                tint = TransactionPalette.Coral
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        bottomBar = {
            SaveTransactionBottomBar(
                error = error,
                isLoading = isLoading,
                enabled = !isLoading && isFormValid,
                onClick = ::saveTransaction
            )
        }
    ) { padding ->
        val selectedAccount = accounts.find { it.id == selectedAccountId }
        val currencySymbol = selectedAccount?.currency ?: "C$"
        val selectedCategory = categories.find { it.id == selectedCategoryId }
        val dateText = when (selectedDate) {
            LocalDate.now() -> "Hoy"
            LocalDate.now().minusDays(1) -> "Ayer"
            else -> selectedDate.format(DateTimeFormatter.ofPattern("d MMM yyyy"))
        }
        val accentColor = getTransactionTypeAccent(selectedTab)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            TransactionPalette.Background,
                            TransactionPalette.BackgroundAlt,
                            TransactionPalette.Background
                        )
                    )
                )
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(modifier = Modifier.height(2.dp))

            TransactionTypeSegmentedControl(
                tabs = tabs,
                selectedTab = selectedTab,
                onTabSelected = { index ->
                    selectedTab = index
                    // Al cambiar de pestaña, resetear la categoría seleccionada si no es transferencia
                    if (index != 2) {
                        selectedCategoryId = null
                    }
                }
            )

            AmountInputCard(
                amount = amount,
                currencySymbol = currencySymbol,
                accentColor = accentColor,
                onAmountChange = { if (isPotentialMoneyInput(it)) amount = it }
            )

            DateTimeSelectorRow(
                dateText = dateText,
                timeText = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                onDateClick = { datePickerDialog.show() },
                onTimeClick = { timePickerDialog.show() }
            )

            AccountSelectorSection(
                title = "Seleccionar cuenta",
                accounts = accounts,
                selectedAccountId = selectedAccountId,
                onAccountSelected = { selectedAccountId = it }
            )

            if (selectedTab == 2) {
                AccountSelectorSection(
                    title = "A la cuenta",
                    accounts = accounts.filter { it.id != selectedAccountId },
                    selectedAccountId = selectedToAccountId,
                    onAccountSelected = { selectedToAccountId = it }
                )
            }

            PremiumTransactionTextField(
                value = title,
                onValueChange = { title = it },
                label = "Título",
                placeholder = "Ej. Cena con amigos",
                minLines = 1
            )

            PremiumTransactionTextField(
                value = notes,
                onValueChange = { notes = it },
                label = "Notas",
                placeholder = "Agrega una nota opcional…",
                minLines = 3
            )

            if (selectedTab != 2) {
                CategorySelectorCard(
                    selectedCategory = selectedCategory,
                    fallbackCategoryName = transactionToEdit?.categoryName,
                    selectedTab = selectedTab,
                    onClick = { showCategorySheet = true }
                )
            }
        }
    }

    if (showCategorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showCategorySheet = false },
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "Selecciona Una Categoría",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )
                
                val filteredCategories = categories.filter {
                    val isCorrectType = if (selectedTab == 0) it.type == "EXPENSE" else it.type == "INCOME"
                    isCorrectType && (it.parentCategoryId == null || it.parentCategoryId == 0L)
                }

                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.heightIn(max = 400.dp)
                ) {
                    items(filteredCategories) { category ->
                        CategoryIconItem(
                            category = category,
                            isSelected = selectedCategoryId == category.id,
                            onClick = {
                                val subs = categories.filter { it.parentCategoryId == category.id }
                                if (subs.isNotEmpty()) {
                                    parentCategoryForSub = category
                                    showSubcategorySheet = true
                                    showCategorySheet = false
                                } else {
                                    selectedCategoryId = category.id
                                    showCategorySheet = false
                                }
                            },
                            onLongClick = {
                                categoryToEdit = category
                                showCategorySheet = false
                            }
                        )
                    }
                    item {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { 
                                     initialParentIdForNewCategory = null
                                    showAddCategoryDialog = true 
                                    showCategorySheet = false
                                }
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(16.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Añadir Categoría")
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Añadir", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showSubcategorySheet && parentCategoryForSub != null) {
        SubcategoryPickerSheet(
            parentCategory = parentCategoryForSub!!,
            subcategories = categories.filter { it.parentCategoryId == parentCategoryForSub!!.id },
            onDismiss = { showSubcategorySheet = false },
            onSubcategorySelected = { subcategoryId ->
                selectedCategoryId = subcategoryId
                showSubcategorySheet = false
            },
            onAddSubcategory = {
                initialParentIdForNewCategory = parentCategoryForSub!!.id
                showAddCategoryDialog = true
                showSubcategorySheet = false
            },
            onEditParent = {
                categoryToEdit = parentCategoryForSub
                showSubcategorySheet = false
            }
        )
    }

    if (showAddCategoryDialog) {
        CategoryEditorDialog(
            categories = categories,
            initialParentId = initialParentIdForNewCategory,
            onDismiss = { 
                showAddCategoryDialog = false
                if (initialParentIdForNewCategory != null) {
                    showSubcategorySheet = true
                } else {
                    showCategorySheet = true
                }
            },
            onConfirm = { name, type, icon, color, parentId ->
                viewModel.createCategory(name, type, icon, parentId)
                showAddCategoryDialog = false
                if (parentId != null) {
                    // Si se creó una subcategoría, refrescamos el padre para que aparezca en el selector
                    parentCategoryForSub = categories.find { it.id == parentId }
                    showSubcategorySheet = true
                } else {
                    showCategorySheet = true
                }
            }
        )
    }

    if (categoryToEdit != null) {
        CategoryEditorDialog(
            categories = categories,
            category = categoryToEdit,
            onDismiss = { 
                categoryToEdit = null
                showCategorySheet = true
            },
            onConfirm = { name, type, icon, color, parentId ->
                viewModel.updateCategory(categoryToEdit!!.id, name, type, icon, color, parentId)
                categoryToEdit = null
                showCategorySheet = true
            },
            onDelete = {
                viewModel.deleteCategory(categoryToEdit!!.id)
                categoryToEdit = null
                showCategorySheet = true
            }
        )
    }
}

@Composable
private fun TransactionTypeSegmentedControl(
    tabs: List<String>,
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp)
            .background(TransactionPalette.ElevatedCard, RoundedCornerShape(24.dp))
            .border(1.dp, TransactionPalette.Border, RoundedCornerShape(24.dp))
            .padding(5.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        tabs.forEachIndexed { index, label ->
            val selected = selectedTab == index
            val accent = getTransactionTypeAccent(index)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clip(RoundedCornerShape(19.dp))
                    .background(
                        if (selected) accent.copy(alpha = 0.16f) else Color.Transparent,
                        RoundedCornerShape(19.dp)
                    )
                    .clickable { onTabSelected(index) }
                    .padding(horizontal = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = label,
                    color = if (selected) TransactionPalette.TextPrimary else TransactionPalette.TextSecondary,
                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(if (selected) 28.dp else 12.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (selected) accent else Color.Transparent)
                )
            }
        }
    }
}

@Composable
private fun AmountInputCard(
    amount: String,
    currencySymbol: String,
    accentColor: Color,
    onAmountChange: (String) -> Unit
) {
    val amountFontSize = when {
        amount.length > 10 -> 30.sp
        amount.length > 7 -> 35.sp
        else -> 42.sp
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = TransactionPalette.Card,
        tonalElevation = 4.dp,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.28f))
    ) {
        Row(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            TransactionPalette.ElevatedCard.copy(alpha = 0.96f),
                            TransactionPalette.Card,
                            accentColor.copy(alpha = 0.13f)
                        )
                    )
                )
                .padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currencySymbol,
                color = accentColor,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(10.dp))
            TextField(
                value = amount,
                onValueChange = onAmountChange,
                placeholder = {
                    Text(
                        "0.00",
                        color = TransactionPalette.TextSecondary,
                        fontSize = amountFontSize,
                        fontWeight = FontWeight.ExtraBold
                    )
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                maxLines = 1,
                textStyle = LocalTextStyle.current.copy(
                    color = TransactionPalette.TextPrimary,
                    fontSize = amountFontSize,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.End
                ),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = accentColor
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun DateTimeSelectorRow(
    dateText: String,
    timeText: String,
    onDateClick: () -> Unit,
    onTimeClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DateTimePill(
            text = dateText,
            icon = Icons.Default.DateRange,
            accentColor = TransactionPalette.Lavender,
            onClick = onDateClick,
            modifier = Modifier.weight(1f)
        )
        DateTimePill(
            text = timeText,
            icon = Icons.Default.AccessTime,
            accentColor = TransactionPalette.Sky,
            onClick = onTimeClick,
            modifier = Modifier.weight(0.78f)
        )
    }
}

@Composable
private fun DateTimePill(
    text: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(20.dp),
        color = TransactionPalette.ElevatedCard,
        border = BorderStroke(1.dp, TransactionPalette.Border)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(18.dp))
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                color = TransactionPalette.TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = TransactionPalette.TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun AccountSelectorSection(
    title: String,
    accounts: List<AccountResponse>,
    selectedAccountId: Long?,
    onAccountSelected: (Long) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            color = TransactionPalette.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(end = 4.dp)
        ) {
            items(accounts) { account ->
                TransactionAccountCard(
                    account = account,
                    selected = selectedAccountId == account.id,
                    onClick = { onAccountSelected(account.id) }
                )
            }
        }
    }
}

@Composable
private fun TransactionAccountCard(
    account: AccountResponse,
    selected: Boolean,
    onClick: () -> Unit
) {
    val visual = getTransactionAccountVisual(account)

    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(224.dp)
            .height(108.dp),
        shape = RoundedCornerShape(24.dp),
        color = if (selected) visual.color.copy(alpha = 0.14f) else TransactionPalette.Card,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) visual.color else TransactionPalette.Border
        ),
        tonalElevation = if (selected) 6.dp else 2.dp
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            TransactionPalette.ElevatedCard.copy(alpha = 0.9f),
                            TransactionPalette.Card,
                            visual.color.copy(alpha = if (selected) 0.18f else 0.08f)
                        )
                    )
                )
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(visual.backgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = visual.icon,
                        contentDescription = null,
                        tint = visual.color,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = account.name,
                        color = TransactionPalette.TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = formatTransactionAccountBalance(account),
                        color = TransactionPalette.TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(visual.color)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (selected) "Seleccionada" else "Cuenta",
                    color = if (selected) TransactionPalette.TextPrimary else TransactionPalette.TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
                if (selected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = "Cuenta seleccionada",
                        tint = visual.color,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun PremiumTransactionTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    minLines: Int
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        shape = RoundedCornerShape(24.dp),
        singleLine = minLines == 1,
        minLines = minLines,
        maxLines = if (minLines == 1) 1 else 5,
        textStyle = LocalTextStyle.current.copy(
            color = TransactionPalette.TextPrimary,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold
        ),
        colors = TextFieldDefaults.colors(
            focusedTextColor = TransactionPalette.TextPrimary,
            unfocusedTextColor = TransactionPalette.TextPrimary,
            focusedContainerColor = TransactionPalette.ElevatedCard,
            unfocusedContainerColor = TransactionPalette.ElevatedCard,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = TransactionPalette.Lavender,
            focusedLabelColor = TransactionPalette.Lavender,
            unfocusedLabelColor = TransactionPalette.TextSecondary,
            focusedPlaceholderColor = TransactionPalette.TextSecondary,
            unfocusedPlaceholderColor = TransactionPalette.TextSecondary
        )
    )
}

@Composable
private fun CategorySelectorCard(
    selectedCategory: CategoryResponse?,
    fallbackCategoryName: String?,
    selectedTab: Int,
    onClick: () -> Unit
) {
    val categoryName = selectedCategory?.name ?: fallbackCategoryName
    val accentColor = parseTransactionColor(selectedCategory?.color) ?: getTransactionTypeAccent(selectedTab)
    val title = categoryName ?: "Selecciona una categoría"
    val subtext = if (categoryName == null) {
        if (selectedTab == 0) "Elige una categoría para tu gasto" else "Elige una categoría para tu ingreso"
    } else {
        if (selectedTab == 0) "Categoría de gasto" else "Categoría de ingreso"
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = TransactionPalette.ElevatedCard,
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.28f)),
        tonalElevation = 3.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getEmojiForCategory(categoryName, selectedCategory?.icon),
                    fontSize = 24.sp
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = TransactionPalette.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtext,
                    color = TransactionPalette.TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = TransactionPalette.TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun SaveTransactionBottomBar(
    error: String?,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = TransactionPalette.Background.copy(alpha = 0.96f),
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            if (error != null) {
                Text(
                    text = error,
                    color = TransactionPalette.Coral,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        Brush.horizontalGradient(
                            if (enabled) {
                                listOf(
                                    TransactionPalette.LavenderStrong,
                                    TransactionPalette.Lavender,
                                    Color(0xFFC4B5FD)
                                )
                            } else {
                                listOf(
                                    TransactionPalette.ElevatedCard,
                                    TransactionPalette.Card
                                )
                            }
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = Color.White.copy(alpha = if (enabled) 0.16f else 0.06f),
                        shape = RoundedCornerShape(999.dp)
                    )
                    .clickable(enabled = enabled, onClick = onClick),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Guardar",
                        color = if (enabled) TransactionPalette.TextPrimary else TransactionPalette.TextSecondary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryEditorDialog(
    categories: List<CategoryResponse>,
    category: CategoryResponse? = null,
    initialParentId: Long? = null,
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, Long?) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var selectedType by remember { mutableStateOf(category?.type ?: "EXPENSE") }
    var selectedEmoji by remember { mutableStateOf(category?.icon ?: "📝") }
    var selectedColor by remember { mutableStateOf(category?.color ?: "#7E57C2") }
    var selectedParentId by remember { mutableStateOf<Long?>(category?.parentCategoryId ?: initialParentId) }
    var isSubcategory by remember { mutableStateOf(category?.parentCategoryId != null || initialParentId != null) }
    
    var showEmojiPicker by remember { mutableStateOf(false) }
    var showParentPicker by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    val colors = listOf("#7E57C2", "#66BB6A", "#26A69A", "#29B6F6", "#42A5F5", "#FFA726", "#EF5350")
    
    if (showDeleteConfirmation) {
        // ... (existing AlertDialog code remains same, I'll just include the start and end)
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            containerColor = Color(0xFF1E222D),
            icon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFF90CAF9), modifier = Modifier.size(48.dp)) },
            title = { 
                Text(
                    "¿Eliminar ${if (category?.parentCategoryId != null) "subcategoría" else "categoría"}?", 
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                ) 
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(category?.name ?: "", color = Color(0xFFE1D5F9), fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Eliminar la etiqueta de esta categoría de todas las transacciones asociadas. Las transacciones no se eliminarán.",
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        onDelete?.invoke()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF90CAF9)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Borrar", color = Color.Black)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteConfirmation = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF9A9A).copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Text("Cancelar", color = Color(0xFFEF9A9A))
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F111A),
        dragHandle = null
    ) {
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp)
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White)
                }
                Text(
                    text = if (category == null) "Añadir categoría" else "Editar categoría",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                if (category != null) {
                    IconButton(onClick = { showDeleteConfirmation = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.White)
                    }
                } else {
                    Spacer(modifier = Modifier.width(48.dp))
                }
            }

            // Type Toggle (Gasto / Ingreso)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(Color(0xFF1E222D), RoundedCornerShape(16.dp))
                    .padding(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            if (selectedType == "EXPENSE") Color(0xFF2C313F) else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedType = "EXPENSE" },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ArrowDropDown, null, tint = Color(0xFFEF9A9A), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Gasto", color = if (selectedType == "EXPENSE") Color.White else Color.Gray)
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            if (selectedType == "INCOME") Color(0xFF2C313F) else Color.Transparent,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedType = "INCOME" },
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.KeyboardArrowUp, null, tint = Color(0xFFA5D6A7), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Ingreso", color = if (selectedType == "INCOME") Color.White else Color.Gray)
                    }
                }
            }

            // Icon and Name
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(Color(0xFF2C313F), CircleShape)
                        .clickable { showEmojiPicker = true },
                    contentAlignment = Alignment.Center
                ) {
                    Text(selectedEmoji, fontSize = 40.sp)
                }
                TextField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = { Text("Nombre", color = Color.Gray, fontSize = 24.sp) },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = Color(0xFFE1D5F9),
                        focusedIndicatorColor = Color(0xFFE1D5F9),
                        unfocusedIndicatorColor = Color.Gray
                    ),
                    textStyle = MaterialTheme.typography.headlineSmall,
                    keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Done)
                )
            }

            // Color Selector
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(Color(0xFF2C313F), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Settings, null, tint = Color.Gray, modifier = Modifier.size(20.dp)) // Palette icon placeholder
                }
                colors.forEach { colorStr ->
                    val color = Color(android.graphics.Color.parseColor(colorStr))
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(color, CircleShape)
                            .clickable { selectedColor = colorStr }
                            .then(
                                if (selectedColor == colorStr) Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape) else Modifier
                            )
                    )
                }
            }

            // Main Category / Subcategory Toggle
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF1E222D), RoundedCornerShape(16.dp))
            ) {
                ListItem(
                    headlineContent = { Text("Categoría principal", color = Color.White, fontWeight = FontWeight.Bold) },
                    leadingContent = { Icon(Icons.Default.Menu, null, tint = Color.Gray) }, // Grid icon placeholder
                    trailingContent = { if (!isSubcategory) Icon(Icons.Default.Check, null, tint = Color(0xFFE1D5F9)) },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable { 
                        isSubcategory = false
                        selectedParentId = null
                    }
                )
                HorizontalDivider(color = Color.Gray.copy(alpha = 0.1f), modifier = Modifier.padding(horizontal = 16.dp))
                ListItem(
                    headlineContent = { Text("Subcategoría", color = Color.White, fontWeight = FontWeight.Bold) },
                    leadingContent = { Icon(Icons.Default.KeyboardArrowDown, null, tint = Color.Gray) }, // Nested icon placeholder
                    trailingContent = { 
                        if (isSubcategory) {
                            Text(
                                categories.find { it.id == selectedParentId }?.name ?: "Seleccionar",
                                color = Color(0xFFE1D5F9)
                            )
                        }
                    },
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    modifier = Modifier.clickable { 
                        isSubcategory = true
                        showParentPicker = true
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onConfirm(name, selectedType, selectedEmoji, selectedColor, selectedParentId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB5A9D4)),
                shape = RoundedCornerShape(28.dp),
                enabled = name.isNotBlank() && (!isSubcategory || selectedParentId != null)
            ) {
                Text(if (category == null) "Asignar nombre" else "Guardar cambios", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showEmojiPicker) {
        EmojiPickerDialog(
            onDismiss = { showEmojiPicker = false },
            title = "Elige un emoji para la categoría",
            onEmojiSelected = { 
                selectedEmoji = it
                showEmojiPicker = false
            }
        )
    }

    if (showParentPicker) {
        ParentCategoryPickerDialog(
            categories = categories.filter { it.type == selectedType && it.parentCategoryId == null && it.id != category?.id },
            onDismiss = { showParentPicker = false },
            onParentSelected = {
                selectedParentId = it.id
                showParentPicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentCategoryPickerDialog(
    categories: List<CategoryResponse>,
    onDismiss: () -> Unit,
    onParentSelected: (CategoryResponse) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F111A)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Selecciona Una Categoría", style = MaterialTheme.typography.headlineSmall, color = Color.White, fontWeight = FontWeight.Bold)
            Text("Seleccione la categoría principal para esta subcategoría", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.heightIn(max = 400.dp).padding(top = 16.dp)
            ) {
                items(categories) { category ->
                    CategoryGridItem(category, onClick = { onParentSelected(category) })
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubcategoryPickerSheet(
    parentCategory: CategoryResponse,
    subcategories: List<CategoryResponse>,
    onDismiss: () -> Unit,
    onSubcategorySelected: (Long) -> Unit,
    onAddSubcategory: () -> Unit,
    onEditParent: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F111A)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Seleccionar Subcategoría",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Surface(
                    onClick = onEditParent,
                    color = Color(0xFF2C313F),
                    shape = CircleShape,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                // "Ninguno" Item
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onSubcategorySelected(parentCategory.id) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFF2C313F), RoundedCornerShape(16.dp))
                                .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Clear, contentDescription = null, tint = Color.Red, modifier = Modifier.size(32.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("Ninguno", color = Color.White, fontSize = 12.sp)
                    }
                }

                // Subcategories
                items(subcategories) { sub ->
                    val subColor = try {
                        Color(android.graphics.Color.parseColor(sub.color ?: "#7E57C2"))
                    } catch (e: Exception) {
                        Color(0xFF7E57C2)
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onSubcategorySelected(sub.id) }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(subColor, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(getEmojiForCategory(sub.name, sub.icon), fontSize = 32.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(sub.name, color = Color.White, fontSize = 12.sp)
                    }
                }

                // "+" Add Button
                item {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onAddSubcategory() }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color.Transparent, RoundedCornerShape(16.dp))
                                .border(1.dp, Color.Gray.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, tint = Color.Gray)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun CategoryGridItem(category: CategoryResponse, onClick: () -> Unit) {
    val categoryColor = try {
        Color(android.graphics.Color.parseColor(category.color ?: "#7E57C2"))
    } catch (e: Exception) {
        Color(0xFF7E57C2)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(categoryColor, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(getEmojiForCategory(category.name, category.icon), fontSize = 32.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(category.name, color = Color.White, fontSize = 12.sp, textAlign = TextAlign.Center, maxLines = 1)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CategoryIconItem(
    category: CategoryResponse,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val categoryColor = try {
        Color(android.graphics.Color.parseColor(category.color ?: "#7E57C2"))
    } catch (e: Exception) {
        Color(0xFF7E57C2)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .background(
                    categoryColor.copy(alpha = if (isSelected) 1f else 0.8f),
                    RoundedCornerShape(16.dp)
                )
                .then(
                    if (isSelected) Modifier.background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(16.dp)) else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(getEmojiForCategory(category.name, category.icon), fontSize = 28.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = category.name,
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            maxLines = 1
        )
    }
}
