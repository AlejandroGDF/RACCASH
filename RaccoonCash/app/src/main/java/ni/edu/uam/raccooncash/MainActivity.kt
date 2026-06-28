package ni.edu.uam.raccooncash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ni.edu.uam.raccooncash.data.model.AccountResponse
import ni.edu.uam.raccooncash.data.model.CategoryResponse
import ni.edu.uam.raccooncash.data.model.DebtResponse
import ni.edu.uam.raccooncash.data.model.PresupuestoRespuesta
import ni.edu.uam.raccooncash.data.model.TipoPeriodoPresupuesto
import ni.edu.uam.raccooncash.data.model.TransactionResponse
import ni.edu.uam.raccooncash.data.model.SavingGoalResponse
import ni.edu.uam.raccooncash.data.security.PinSecurityStore
import ni.edu.uam.raccooncash.ui.account_details.AccountDetailsScreen
import ni.edu.uam.raccooncash.ui.accounts.AccountsScreen
import ni.edu.uam.raccooncash.ui.accounts.AccountsViewModel
import ni.edu.uam.raccooncash.ui.accounts.AddAccountScreen
import ni.edu.uam.raccooncash.ui.accounts.getAccountVisual
import ni.edu.uam.raccooncash.ui.accounts.getEmojiForCategory
import ni.edu.uam.raccooncash.ui.budgets.AddBudgetScreen
import ni.edu.uam.raccooncash.ui.budgets.BudgetDetailsScreen
import ni.edu.uam.raccooncash.ui.budgets.BudgetsScreen
import ni.edu.uam.raccooncash.ui.budgets.BudgetsViewModel
import ni.edu.uam.raccooncash.ui.components.RaccAddFloatingActionButton
import ni.edu.uam.raccooncash.ui.debts.AddDebtPaymentScreen
import ni.edu.uam.raccooncash.ui.debts.AddDebtScreen
import ni.edu.uam.raccooncash.ui.debts.DebtDetailsScreen
import ni.edu.uam.raccooncash.ui.debts.DebtsScreen
import ni.edu.uam.raccooncash.ui.debts.DebtsViewModel
import ni.edu.uam.raccooncash.ui.savings.AddGoalTransactionScreen
import ni.edu.uam.raccooncash.ui.savings.AddSavingGoalScreen
import ni.edu.uam.raccooncash.ui.savings.SavingGoalDetailsScreen
import ni.edu.uam.raccooncash.ui.savings.SavingsScreen
import ni.edu.uam.raccooncash.ui.savings.SavingsViewModel
import ni.edu.uam.raccooncash.ui.security.PinLockScreen
import ni.edu.uam.raccooncash.ui.security.SecurityScreen
import ni.edu.uam.raccooncash.ui.settings.SettingsScreen
import ni.edu.uam.raccooncash.ui.theme.RaccoonCashTheme
import ni.edu.uam.raccooncash.ui.transactions.AddTransactionScreen
import ni.edu.uam.raccooncash.ui.transactions.TransactionFilterSheet
import ni.edu.uam.raccooncash.ui.transactions.TransactionFilterState
import ni.edu.uam.raccooncash.ui.transactions.TransactionSortOption
import ni.edu.uam.raccooncash.ui.transactions.TransactionToolsMenu
import ni.edu.uam.raccooncash.ui.transactions.TransactionsViewModel
import ni.edu.uam.raccooncash.ui.transactions.buildTransactionGroups
import ni.edu.uam.raccooncash.ui.transactions.matchesTransactionFilters
import ni.edu.uam.raccooncash.ui.transactions.parseTransactionDate
import ni.edu.uam.raccooncash.util.formatCurrencyAmount
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var pinSecurityStore: PinSecurityStore
    private val isAppLocked = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        pinSecurityStore = PinSecurityStore(this)
        isAppLocked.value = pinSecurityStore.isPinEnabled()

        setContent {
            RaccoonCashTheme {
                var isPinEnabled by remember { mutableStateOf(pinSecurityStore.isPinEnabled()) }

                if (isPinEnabled && isAppLocked.value) {
                    PinLockScreen(
                        onValidatePin = { pin -> pinSecurityStore.verifyPin(pin) },
                        onUnlocked = { isAppLocked.value = false }
                    )
                    return@RaccoonCashTheme
                }

                val accountsViewModel: AccountsViewModel = viewModel()
                val transactionsViewModel: TransactionsViewModel = viewModel()
                val savingsViewModel: SavingsViewModel = viewModel()
                val budgetsViewModel: BudgetsViewModel = viewModel()
                val debtsViewModel: DebtsViewModel = viewModel()
                val categoryTransactions by accountsViewModel.transactions.collectAsState()
                var currentScreen by remember { mutableStateOf("inicio") }
                var editingTransaction by remember { mutableStateOf<TransactionResponse?>(null) }
                var editingAccount by remember { mutableStateOf<AccountResponse?>(null) }
                var selectedAccountDetails by remember { mutableStateOf<AccountResponse?>(null) }
                var selectedDebt by remember { mutableStateOf<DebtResponse?>(null) }
                var editingDebt by remember { mutableStateOf<DebtResponse?>(null) }
                var selectedSavingGoal by remember { mutableStateOf<SavingGoalResponse?>(null) }
                var editingSavingGoal by remember { mutableStateOf<SavingGoalResponse?>(null) }
                var editingGoalTransaction by remember { mutableStateOf<TransactionResponse?>(null) }
                var selectedBudget by remember { mutableStateOf<PresupuestoRespuesta?>(null) }
                var editingBudget by remember { mutableStateOf<PresupuestoRespuesta?>(null) }
                var budgetTransactionInitialType by remember { mutableStateOf<String?>(null) }
                var budgetTransactionInitialDescription by remember { mutableStateOf("") }
                var budgetTransactionInitialDate by remember { mutableStateOf<LocalDate?>(null) }
                var budgetTransactionInitialCategoryId by remember { mutableStateOf<Long?>(null) }

                Scaffold(
                    bottomBar = {
                        NavigationBar(
                            containerColor = Color(0xFF080B14),
                            contentColor = Color.White
                        ) {
                            val items = listOf(
                                Triple("inicio", "Inicio", Icons.Default.Home),
                                Triple("transacciones", "Movs.", Icons.AutoMirrored.Filled.List),
                                Triple("deudas", "Deudas", Icons.Default.AccountBalanceWallet),
                                Triple("presupuestos", "Planes", Icons.Default.AccountBalanceWallet),
                                Triple("ahorro", "Metas", Icons.Default.Star)
                            )
                            items.forEach { (screen, label, icon) ->
                                val selected = currentScreen == screen ||
                                    (screen == "deudas" && currentScreen in listOf("debt_details", "add_debt", "add_debt_payment")) ||
                                    (screen == "presupuestos" && currentScreen in listOf("budget_details", "add_budget", "add_budget_transaction")) ||
                                    (screen == "ahorro" && currentScreen in listOf("saving_goal_details", "add_goal_transaction", "add_saving_goal"))
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = {
                                        currentScreen = screen
                                        when (screen) {
                                            "inicio", "transacciones" -> accountsViewModel.loadAccounts()
                                            "deudas" -> debtsViewModel.loadDebts()
                                            "presupuestos" -> budgetsViewModel.loadBudgets()
                                            "ahorro" -> savingsViewModel.loadSavingGoals()
                                        }
                                    },
                                    icon = {
                                        Icon(
                                            icon,
                                            contentDescription = label,
                                            modifier = Modifier.size(if (selected) 24.dp else 22.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = label,
                                            maxLines = 1,
                                            softWrap = false,
                                            fontSize = 11.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = Color(0xFFA78BFA),
                                        unselectedIconColor = Color(0xFF9CA3AF),
                                        selectedTextColor = Color.White,
                                        unselectedTextColor = Color(0xFF9CA3AF),
                                        indicatorColor = Color(0xFFA78BFA).copy(alpha = 0.20f)
                                    )
                                )
                            }
                        }
                    }
                ) { padding ->
                    Box(modifier = Modifier.padding(padding)) {
                        when (currentScreen) {
                            "inicio" -> AccountsScreen(
                                viewModel = accountsViewModel,
                                onAddAccountClick = { 
                                    editingAccount = null
                                    currentScreen = "add_account" 
                                },
                                onAddTransactionClick = { 
                                    editingTransaction = null
                                    currentScreen = "add_transaction" 
                                },
                                onTransactionClick = { transaction ->
                                    editingTransaction = transaction
                                    currentScreen = "add_transaction"
                                },
                                onAccountClick = { account ->
                                    selectedAccountDetails = account
                                    currentScreen = "account_details"
                                },
                                onSettingsClick = { currentScreen = "settings" }
                            )
                            "transacciones" -> TransactionsTabScreen(
                                viewModel = accountsViewModel,
                                onAddTransactionClick = {
                                    editingTransaction = null
                                    currentScreen = "add_transaction"
                                },
                                onTransactionClick = { transaction ->
                                    editingTransaction = transaction
                                    currentScreen = "add_transaction"
                                }
                            )
                            "deudas" -> DebtsScreen(
                                viewModel = debtsViewModel,
                                onAddDebtClick = {
                                    editingDebt = null
                                    currentScreen = "add_debt"
                                },
                                onDebtClick = { debt ->
                                    selectedDebt = debt
                                    debtsViewModel.loadDebtDetails(debt.id)
                                    currentScreen = "debt_details"
                                }
                            )
                            "debt_details" -> selectedDebt?.let { debt ->
                                DebtDetailsScreen(
                                    debtId = debt.id,
                                    viewModel = debtsViewModel,
                                    onEditDebt = { currentDebt ->
                                        editingDebt = currentDebt
                                        currentScreen = "add_debt"
                                    },
                                    onAddPayment = { currentDebt ->
                                        selectedDebt = currentDebt
                                        currentScreen = "add_debt_payment"
                                    },
                                    onPaymentChanged = {
                                        accountsViewModel.loadAccounts()
                                        debtsViewModel.loadDebts()
                                    },
                                    onBack = {
                                        currentScreen = "deudas"
                                        debtsViewModel.loadDebts()
                                        accountsViewModel.loadAccounts()
                                    }
                                )
                            }
                            "add_debt_payment" -> selectedDebt?.let { debt ->
                                AddDebtPaymentScreen(
                                    debt = debt,
                                    viewModel = debtsViewModel,
                                    onPaymentSaved = {
                                        accountsViewModel.loadAccounts()
                                        debtsViewModel.loadDebtDetails(debt.id)
                                        currentScreen = "debt_details"
                                    },
                                    onBack = {
                                        currentScreen = "debt_details"
                                        debtsViewModel.loadDebtDetails(debt.id)
                                    }
                                )
                            }
                            "add_debt" -> AddDebtScreen(
                                viewModel = debtsViewModel,
                                debtToEdit = editingDebt,
                                onSaved = {
                                    accountsViewModel.loadAccounts()
                                },
                                onBack = {
                                    currentScreen = if (editingDebt != null && selectedDebt?.id == editingDebt?.id) {
                                        "debt_details"
                                    } else {
                                        "deudas"
                                    }
                                    debtsViewModel.loadDebts()
                                    accountsViewModel.loadAccounts()
                                }
                            )
                            "ahorro" -> SavingsScreen(
                                viewModel = savingsViewModel,
                                onAddGoalClick = { 
                                    editingSavingGoal = null
                                    currentScreen = "add_saving_goal" 
                                },
                                onGoalClick = { goal ->
                                    selectedSavingGoal = goal
                                    currentScreen = "saving_goal_details"
                                }
                            )
                            "presupuestos" -> BudgetsScreen(
                                viewModel = budgetsViewModel,
                                onAddBudgetClick = {
                                    editingBudget = null
                                    currentScreen = "add_budget"
                                },
                                onBudgetClick = { budget ->
                                    selectedBudget = budget
                                    currentScreen = "budget_details"
                                }
                            )
                            "budget_details" -> selectedBudget?.let { budget ->
                                BudgetDetailsScreen(
                                    budgetId = budget.id,
                                    viewModel = budgetsViewModel,
                                    accountsViewModel = accountsViewModel,
                                    onAddTransaction = { currentBudget, categoryId ->
                                        editingTransaction = null
                                        budgetTransactionInitialType = if (currentBudget.esGasto) "EXPENSE" else "INCOME"
                                        budgetTransactionInitialDescription = currentBudget.nombre
                                        budgetTransactionInitialDate = getDefaultTransactionDateForBudget(currentBudget)
                                        budgetTransactionInitialCategoryId = categoryId
                                        currentScreen = "add_budget_transaction"
                                    },
                                    onEditBudget = { currentBudget ->
                                        editingBudget = currentBudget
                                        currentScreen = "add_budget"
                                    },
                                    onTransactionClick = { transaction ->
                                        editingTransaction = transaction
                                        budgetTransactionInitialType = null
                                        budgetTransactionInitialDescription = ""
                                        budgetTransactionInitialDate = null
                                        budgetTransactionInitialCategoryId = null
                                        currentScreen = "add_budget_transaction"
                                    },
                                    onBack = { currentScreen = "presupuestos" }
                                )
                            }
                            "add_budget" -> AddBudgetScreen(
                                viewModel = budgetsViewModel,
                                accountsViewModel = accountsViewModel,
                                budgetToEdit = editingBudget,
                                onBack = {
                                    currentScreen = if (editingBudget != null && selectedBudget?.id == editingBudget?.id) {
                                        "budget_details"
                                    } else {
                                        "presupuestos"
                                    }
                                    budgetsViewModel.loadBudgets()
                                }
                            )
                            "saving_goal_details" -> selectedSavingGoal?.let { goal ->
                                SavingGoalDetailsScreen(
                                    goalId = goal.id,
                                    viewModel = savingsViewModel,
                                    accountsViewModel = accountsViewModel,
                                    onAddTransaction = { 
                                        editingGoalTransaction = null
                                        currentScreen = "add_goal_transaction" 
                                    },
                                    onEditGoal = { 
                                        editingSavingGoal = goal
                                        currentScreen = "add_saving_goal" 
                                    },
                                    onTransactionClick = { transaction ->
                                        editingGoalTransaction = transaction
                                        currentScreen = "add_goal_transaction"
                                    },
                                    onBack = { currentScreen = "ahorro" }
                                )
                            }
                            "add_goal_transaction" -> selectedSavingGoal?.let { goal ->
                                AddGoalTransactionScreen(
                                    goal = goal,
                                    savingsViewModel = savingsViewModel,
                                    accountsViewModel = accountsViewModel,
                                    transactionToEdit = editingGoalTransaction,
                                    onBack = { currentScreen = "saving_goal_details" }
                                )
                            }
                            "add_saving_goal" -> AddSavingGoalScreen(
                                viewModel = savingsViewModel,
                                goalToEdit = editingSavingGoal,
                                onBack = { 
                                    if (editingSavingGoal != null) {
                                        // Si estábamos editando, regresamos a los detalles de esa meta específica
                                        selectedSavingGoal = editingSavingGoal
                                        currentScreen = "saving_goal_details"
                                        savingsViewModel.loadSavingGoals()
                                        savingsViewModel.loadGoalTransactions(editingSavingGoal!!.id)
                                    } else {
                                        // Si era una meta nueva, regresamos a la lista general
                                        currentScreen = "ahorro"
                                        savingsViewModel.loadSavingGoals()
                                    }
                                }
                            )
                            "account_details" -> selectedAccountDetails?.let { account ->
                                AccountDetailsScreen(
                                    account = account,
                                    viewModel = accountsViewModel,
                                    onEditAccount = {
                                        editingAccount = account
                                        currentScreen = "add_account"
                                    },
                                    onTransactionClick = { transaction ->
                                        editingTransaction = transaction
                                        currentScreen = "add_transaction"
                                    },
                                    onBack = { currentScreen = "inicio" }
                                )
                            }
                            "add_account" -> AddAccountScreen(
                                viewModel = accountsViewModel,
                                accountToEdit = editingAccount,
                                onBack = { currentScreen = "inicio" }
                            )
                            "add_transaction" -> AddTransactionScreen(
                                viewModel = transactionsViewModel,
                                transactionToEdit = editingTransaction,
                                categoryTransactions = categoryTransactions,
                                onBack = { 
                                    currentScreen = "inicio"
                                    accountsViewModel.loadAccounts()
                                }
                            )
                            "add_budget_transaction" -> selectedBudget?.let {
                                AddTransactionScreen(
                                    viewModel = transactionsViewModel,
                                    transactionToEdit = editingTransaction,
                                    initialType = budgetTransactionInitialType,
                                    initialDescription = budgetTransactionInitialDescription,
                                    initialDate = budgetTransactionInitialDate,
                                    initialCategoryId = budgetTransactionInitialCategoryId,
                                    initialBudgetId = it.id,
                                    categoryTransactions = categoryTransactions,
                                    onBack = {
                                        currentScreen = "budget_details"
                                        accountsViewModel.loadAccounts()
                                        budgetsViewModel.loadBudgets()
                                    }
                                )
                            }
                            "settings" -> SettingsScreen(
                                onBack = { currentScreen = "inicio" },
                                onSecurityClick = { currentScreen = "security" }
                            )
                            "security" -> SecurityScreen(
                                pinSecurityStore = pinSecurityStore,
                                onBack = { currentScreen = "settings" },
                                onPinStateChanged = { enabled ->
                                    isPinEnabled = enabled
                                    isAppLocked.value = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (::pinSecurityStore.isInitialized && pinSecurityStore.isPinEnabled()) {
            isAppLocked.value = true
        }
    }
}

private fun getDefaultTransactionDateForBudget(budget: PresupuestoRespuesta): LocalDate {
    val startDate = try {
        LocalDate.parse(budget.fechaInicio, DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (e: Exception) {
        return LocalDate.now()
    }
    val endDate = calculateBudgetEndDate(startDate, budget.tipoPeriodo, budget.valorPeriodo)
    val today = LocalDate.now()

    return if (today.isBefore(startDate) || today.isAfter(endDate)) startDate else today
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

private object TransactionsPalette {
    val Background = Color(0xFF080B14)
    val BackgroundAlt = Color(0xFF0B1020)
    val Card = Color(0xFF171C2A)
    val ElevatedCard = Color(0xFF202638)
    val Border = Color.White.copy(alpha = 0.08f)
    val Lavender = Color(0xFFA78BFA)
    val LavenderDeep = Color(0xFF31254B)
    val Mint = Color(0xFF7EDC8D)
    val Sky = Color(0xFF74C7EC)
    val Coral = Color(0xFFFF7A85)
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFF9CA3AF)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionsTabScreen(
    viewModel: AccountsViewModel,
    onAddTransactionClick: () -> Unit,
    onTransactionClick: (TransactionResponse) -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()
    val allCategories by viewModel.categories.collectAsState()
    val allAccounts by viewModel.accounts.collectAsState()

    val today = LocalDate.now()
    val months = listOf("enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre")
    val currentMonthIndex = today.monthValue - 1
    val visibleMonthIndices = remember(currentMonthIndex) {
        (-2..2).map { offset -> (currentMonthIndex + offset + months.size) % months.size }
    }
    var selectedMonthIndex by remember { mutableIntStateOf(currentMonthIndex) }
    var filterState by remember { mutableStateOf(TransactionFilterState()) }
    var sortOption by remember { mutableStateOf(TransactionSortOption.DATE_DESC) }
    var showFilterSheet by remember { mutableStateOf(false) }

    val monthTransactions = transactions.filter { transaction ->
        val date = parseTransactionDate(transaction)
        date?.monthValue == selectedMonthIndex + 1 && date.year == today.year
    }

    val filteredTransactions = monthTransactions.filter { transaction ->
        transaction.matchesTransactionFilters(filterState, allCategories)
    }

    val totalIncome = filteredTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val totalExpense = filteredTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val total = totalIncome - totalExpense

    if (showFilterSheet) {
        TransactionFilterSheet(
            filters = filterState,
            categories = allCategories,
            onFiltersChange = { filterState = it },
            onDismiss = { showFilterSheet = false }
        )
    }

    Scaffold(
        containerColor = TransactionsPalette.Background,
        floatingActionButton = {
            RaccAddFloatingActionButton(
                onClick = onAddTransactionClick,
                contentDescription = "Nueva transacción"
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            TransactionsPalette.Background,
                            TransactionsPalette.BackgroundAlt,
                            TransactionsPalette.Background
                        )
                    )
                )
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            item {
                TransactionsHeader(
                    activeFilterCount = filterState.activeCount,
                    sortOption = sortOption,
                    onFilterClick = { showFilterSheet = true },
                    onSortSelected = { sortOption = it }
                )
            }

            item {
                TransactionsMonthSelector(
                    months = months,
                    visibleMonthIndices = visibleMonthIndices,
                    selectedMonthIndex = selectedMonthIndex,
                    onMonthSelected = { selectedMonthIndex = it }
                )
            }

            item {
                MonthlySummaryCard(
                    totalExpense = totalExpense,
                    totalIncome = totalIncome,
                    total = total
                )
            }

            val groupedByDay = buildTransactionGroups(filteredTransactions, sortOption)

            if (filteredTransactions.isEmpty()) {
                item {
                    EmptyTransactionsState(
                        message = if (filterState.hasActiveFilters) {
                            "No hay transacciones con esos filtros."
                        } else {
                            "No hay transacciones en este mes."
                        }
                    )
                }
            }

            groupedByDay.forEach { (date, dailyTrans) ->
                item {
                    TransactionDateHeader(
                        date = date,
                        dailyTransactions = dailyTrans
                    )
                }
                items(dailyTrans) { transaction ->
                    PremiumTransactionItem(
                        transaction = transaction,
                        allCategories = allCategories,
                        allAccounts = allAccounts,
                        onClick = { onTransactionClick(transaction) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionsHeader(
    activeFilterCount: Int,
    sortOption: TransactionSortOption,
    onFilterClick: () -> Unit,
    onSortSelected: (TransactionSortOption) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Transacciones",
                color = TransactionsPalette.TextPrimary,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Movimientos y filtros del mes",
                color = TransactionsPalette.TextSecondary,
                fontSize = 13.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        TransactionToolsMenu(
            activeFilterCount = activeFilterCount,
            sortOption = sortOption,
            onFilterClick = onFilterClick,
            onSortSelected = onSortSelected
        )
    }
}

@Composable
private fun TransactionsMonthSelector(
    months: List<String>,
    visibleMonthIndices: List<Int>,
    selectedMonthIndex: Int,
    onMonthSelected: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(visibleMonthIndices) { monthIndex ->
            MonthPill(
                text = months[monthIndex],
                selected = selectedMonthIndex == monthIndex,
                onClick = { onMonthSelected(monthIndex) }
            )
        }
    }
}

@Composable
private fun MonthPill(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(999.dp),
            color = if (selected) TransactionsPalette.LavenderDeep else TransactionsPalette.Card.copy(alpha = 0.72f),
            border = BorderStroke(
                width = 1.dp,
                color = if (selected) TransactionsPalette.Lavender.copy(alpha = 0.64f) else TransactionsPalette.Border
            )
        ) {
            Text(
                text = text,
                color = if (selected) TransactionsPalette.TextPrimary else TransactionsPalette.TextSecondary,
                fontSize = 14.sp,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        if (selected) {
            Box(
                modifier = Modifier
                    .width(28.dp)
                    .height(3.dp)
                    .background(TransactionsPalette.Lavender, RoundedCornerShape(999.dp))
            )
        } else {
            Spacer(modifier = Modifier.height(3.dp))
        }
    }
}

@Composable
private fun MonthlySummaryCard(
    totalExpense: Double,
    totalIncome: Double,
    total: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        colors = CardDefaults.cardColors(containerColor = TransactionsPalette.Card),
        shape = RoundedCornerShape(30.dp),
        border = BorderStroke(1.dp, TransactionsPalette.Lavender.copy(alpha = 0.18f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            TransactionsPalette.ElevatedCard,
                            TransactionsPalette.Card,
                            TransactionsPalette.LavenderDeep.copy(alpha = 0.32f)
                        )
                    )
                )
                .padding(horizontal = 16.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SummaryMetric(
                label = "Gastos",
                amount = formatTransactionsCurrency(totalExpense),
                color = TransactionsPalette.Coral
            )
            SummaryDivider()
            SummaryMetric(
                label = "Ingresos",
                amount = formatTransactionsCurrency(totalIncome),
                color = TransactionsPalette.Mint
            )
            SummaryDivider()
            SummaryMetric(
                label = "Total",
                amount = formatTransactionsCurrency(total),
                color = TransactionsPalette.Lavender
            )
        }
    }
}

@Composable
private fun RowScope.SummaryMetric(
    label: String,
    amount: String,
    color: Color
) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            color = TransactionsPalette.TextSecondary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = amount,
            color = color,
            fontSize = if (amount.length > 12) 13.sp else 16.sp,
            fontWeight = FontWeight.ExtraBold,
            maxLines = 1,
            softWrap = false,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SummaryDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(46.dp)
            .background(TransactionsPalette.Border, RoundedCornerShape(999.dp))
    )
}

@Composable
private fun TransactionDateHeader(
    date: LocalDate,
    dailyTransactions: List<TransactionResponse>
) {
    val daySum = dailyTransactions.sumOf {
        when (it.type) {
            "INCOME" -> it.amount
            "EXPENSE" -> -it.amount
            else -> 0.0
        }
    }
    val subtotalColor = when {
        daySum > 0.0 -> TransactionsPalette.Mint
        daySum < 0.0 -> TransactionsPalette.Coral
        else -> TransactionsPalette.TextSecondary
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 9.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = formatTransactionsDateLabel(date),
            color = TransactionsPalette.TextSecondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .weight(1f)
                .padding(end = 12.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = formatTransactionsCurrency(daySum, showPositiveSign = true),
            color = subtotalColor,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier
                .background(TransactionsPalette.ElevatedCard.copy(alpha = 0.84f), RoundedCornerShape(999.dp))
                .padding(horizontal = 10.dp, vertical = 5.dp)
        )
    }
}

@Composable
private fun PremiumTransactionItem(
    transaction: TransactionResponse,
    allCategories: List<CategoryResponse>,
    allAccounts: List<AccountResponse>,
    onClick: () -> Unit
) {
    val isSaving = transaction.description.startsWith("Ahorro para", ignoreCase = true)
    val isExpense = transaction.type == "EXPENSE"
    val isTransfer = transaction.type == "TRANSFER" || isSaving
    val amountColor = when {
        isExpense -> TransactionsPalette.Coral
        isSaving -> TransactionsPalette.Lavender
        isTransfer -> TransactionsPalette.Sky
        else -> TransactionsPalette.Mint
    }
    val amountText = when {
        isExpense -> formatTransactionsCurrency(-transaction.amount)
        isTransfer -> formatTransactionsCurrency(transaction.amount)
        else -> formatTransactionsCurrency(transaction.amount, showPositiveSign = true)
    }
    val category = transaction.category ?: allCategories.find { it.id == transaction.categoryId }
    val parentCategory = if (category?.parentCategoryId != null && category.parentCategoryId != 0L) {
        allCategories.find { it.id == category.parentCategoryId }
    } else {
        null
    }
    val categoryEmoji = when {
        parentCategory != null -> getEmojiForCategory(parentCategory.name, parentCategory.icon)
        category != null -> getEmojiForCategory(transaction.categoryName ?: category.name, category.icon)
        !transaction.categoryName.isNullOrBlank() -> getEmojiForCategory(transaction.categoryName, null)
        else -> null
    }
    val account = transaction.account ?: allAccounts.find { it.id == transaction.accountId }
    val accountName = transaction.accountName ?: account?.name ?: "Cuenta desconocida"
    val accountColor = parseTransactionAccountColor(transaction.account?.color ?: account?.color)
        ?: getAccountVisual(accountName).color
    val destinationId = transaction.toAccountId ?: transaction.destinationAccountId
    val destinationName = transaction.destinationAccountName
        ?: transaction.toAccountName
        ?: transaction.toAccount?.name
        ?: allAccounts.find { it.id == destinationId }?.name
    val title = transaction.description.ifBlank { transaction.categoryName ?: "Movimiento" }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = TransactionsPalette.Card),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, TransactionsPalette.Border),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            TransactionsPalette.ElevatedCard.copy(alpha = 0.72f),
                            TransactionsPalette.Card
                        )
                    )
                )
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(amountColor.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                if (categoryEmoji != null) {
                    Text(text = categoryEmoji, fontSize = 24.sp)
                } else {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.List,
                        contentDescription = null,
                        tint = amountColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(7.dp)
            ) {
                Text(
                    text = title,
                    color = TransactionsPalette.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TransactionAccountChip(
                        accountName = accountName,
                        accountColor = accountColor,
                        modifier = Modifier.widthIn(max = 168.dp)
                    )
                    if (isTransfer && !destinationName.isNullOrBlank()) {
                        Text(
                            text = "→ $destinationName",
                            color = TransactionsPalette.TextSecondary,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = amountText,
                color = amountColor,
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (amountText.length > 13) 13.sp else 15.sp,
                maxLines = 1,
                softWrap = false
            )

            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TransactionsPalette.TextSecondary.copy(alpha = 0.62f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun TransactionAccountChip(
    accountName: String,
    accountColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(999.dp),
        color = TransactionsPalette.ElevatedCard.copy(alpha = 0.92f),
        border = BorderStroke(1.dp, accountColor.copy(alpha = 0.34f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(accountColor, RoundedCornerShape(999.dp))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = accountName,
                color = TransactionsPalette.TextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun EmptyTransactionsState(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = TransactionsPalette.Card),
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, TransactionsPalette.Border)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(TransactionsPalette.Lavender.copy(alpha = 0.14f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    tint = TransactionsPalette.Lavender,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = message,
                color = TransactionsPalette.TextSecondary,
                fontSize = 14.sp,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

private fun formatTransactionsDateLabel(date: LocalDate): String {
    val shortFormatter = DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("es"))
    val fullFormatter = DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale.forLanguageTag("es"))
    val today = LocalDate.now()
    val label = when (date) {
        today -> "Hoy, ${date.format(shortFormatter)}"
        today.minusDays(1) -> "Ayer, ${date.format(shortFormatter)}"
        else -> date.format(fullFormatter)
    }

    return label.replaceFirstChar { it.uppercase(Locale.getDefault()) }
}

private fun formatTransactionsCurrency(
    amount: Double,
    showPositiveSign: Boolean = false
): String {
    val sign = when {
        amount < 0.0 -> "-"
        showPositiveSign && amount > 0.0 -> "+"
        else -> ""
    }
    val normalizedAmount = if (amount < 0.0) -amount else amount

    return "$sign${formatCurrencyAmount(normalizedAmount)}"
}

private fun parseTransactionAccountColor(color: String?): Color? {
    val rawColor = color?.trim()?.takeIf { it.isNotEmpty() } ?: return null
    val normalizedColor = if (rawColor.startsWith("#")) rawColor else "#$rawColor"

    return try {
        Color(android.graphics.Color.parseColor(normalizedColor))
    } catch (e: IllegalArgumentException) {
        null
    }
}
