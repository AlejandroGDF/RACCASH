package ni.edu.uam.raccooncash

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import ni.edu.uam.raccooncash.data.model.AccountResponse
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
                                val selected = currentScreen == screen
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
    
    val months = listOf("enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "septiembre", "octubre", "noviembre", "diciembre")
    var selectedMonthIndex by remember { mutableIntStateOf(java.time.LocalDate.now().monthValue - 1) }
    var filterState by remember { mutableStateOf(TransactionFilterState()) }
    var sortOption by remember { mutableStateOf(TransactionSortOption.DATE_DESC) }
    var showFilterSheet by remember { mutableStateOf(false) }

    val monthTransactions = transactions.filter { transaction ->
        val date = parseTransactionDate(transaction)
        date?.monthValue == selectedMonthIndex + 1 && date.year == LocalDate.now().year
    }

    val filteredTransactions = monthTransactions.filter { transaction ->
        transaction.matchesTransactionFilters(filterState, allCategories)
    }

    val totalIncome = filteredTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val totalExpense = filteredTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }

    if (showFilterSheet) {
        TransactionFilterSheet(
            filters = filterState,
            categories = allCategories,
            onFiltersChange = { filterState = it },
            onDismiss = { showFilterSheet = false }
        )
    }

    Scaffold(
        floatingActionButton = {
            RaccAddFloatingActionButton(
                onClick = onAddTransactionClick,
                contentDescription = "Nueva transacción"
            )
        }
    ) { paddingValues ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F111A))
            .padding(paddingValues)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Transacciones",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            TransactionToolsMenu(
                activeFilterCount = filterState.activeCount,
                sortOption = sortOption,
                onFilterClick = { showFilterSheet = true },
                onSortSelected = { sortOption = it }
            )
        }

        ScrollableTabRow(
            selectedTabIndex = selectedMonthIndex,
            containerColor = Color.Transparent,
            contentColor = Color.White,
            edgePadding = 16.dp,
            divider = {},
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedMonthIndex]),
                    color = Color.White
                )
            }
        ) {
            months.forEachIndexed { index, month ->
                Tab(
                    selected = selectedMonthIndex == index,
                    onClick = { selectedMonthIndex = index },
                    text = { Text(month, color = if (selectedMonthIndex == index) Color.White else Color.Gray) }
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp).background(Color(0xFF1E222D), RoundedCornerShape(12.dp)).padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Gastos", color = Color.Gray, fontSize = 12.sp)
                Text("C$${String.format(Locale.getDefault(), "%.2f", totalExpense)}", color = Color(0xFFEF9A9A), fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Ingresos", color = Color.Gray, fontSize = 12.sp)
                Text("C$${String.format(Locale.getDefault(), "%.2f", totalIncome)}", color = Color(0xFFA5D6A7), fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Total", color = Color.Gray, fontSize = 12.sp)
                val total = totalIncome - totalExpense
                Text("C$${String.format(Locale.getDefault(), "%.2f", total)}", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            val groupedByDay = buildTransactionGroups(filteredTransactions, sortOption)

            if (filteredTransactions.isEmpty()) {
                item {
                    Text(
                        text = if (filterState.hasActiveFilters) "No hay transacciones con esos filtros." else "No hay transacciones en este mes.",
                        color = Color.Gray,
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }

            groupedByDay.forEach { (date, dailyTrans) ->
                item {
                    val dateLabel = when (date) {
                        java.time.LocalDate.now() -> "Hoy, ${date.format(java.time.format.DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("es")))}"
                        java.time.LocalDate.now().minusDays(1) -> "Ayer, ${date.format(java.time.format.DateTimeFormatter.ofPattern("d 'de' MMMM", Locale.forLanguageTag("es")))}"
                        else -> date.format(java.time.format.DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM", Locale.forLanguageTag("es")))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(dateLabel.replaceFirstChar { it.uppercase(Locale.getDefault()) }, color = Color.Gray, fontSize = 14.sp)
                        val daySum = dailyTrans.sumOf {
                            when (it.type) {
                                "INCOME" -> it.amount
                                "EXPENSE" -> -it.amount
                                else -> 0.0
                            }
                        }
                        Text("C$${String.format(Locale.getDefault(), "%.2f", daySum)}", color = Color.Gray, fontSize = 14.sp)
                    }
                }
                items(dailyTrans) { transaction ->
                    ni.edu.uam.raccooncash.ui.accounts.TransactionItem(
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
}
