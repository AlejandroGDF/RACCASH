package ni.edu.uam.raccooncash.ui.accounts

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.raccooncash.data.model.AccountResponse

val accountColors = listOf(
    Color(0xFF7E57C2), // Purple
    Color(0xFF2196F3), // Blue
    Color(0xFF00BCD4), // Cyan
    Color(0xFF4DB6AC), // Teal
    Color(0xFF81C784), // Light Green
    Color(0xFFD4E157), // Lime
    Color(0xFFFFA726), // Orange
    Color(0xFFEF5350), // Red
    Color(0xFFEC407A), // Pink
    Color(0xFFAB47BC)  // Light Purple
)

data class CurrencyInfo(val code: String, val symbol: String, val country: String)

val availableCurrencies = listOf(
    CurrencyInfo("NIO", "C$", "Nicaragua"),
    CurrencyInfo("USD", "$", "United States"),
    CurrencyInfo("EUR", "€", "Euro Member")
)

private object AddAccountPalette {
    val Background = Color(0xFF080B14)
    val BackgroundAlt = Color(0xFF0B1020)
    val Card = Color(0xFF171C2A)
    val ElevatedCard = Color(0xFF202638)
    val Border = Color.White.copy(alpha = 0.08f)
    val Lavender = Color(0xFFA78BFA)
    val LavenderStrong = Color(0xFF7C3AED)
    val Mint = Color(0xFF7EDC8D)
    val Sky = Color(0xFF74C7EC)
    val Cyan = Color(0xFF22D3EE)
    val Orange = Color(0xFFFFB84D)
    val Rose = Color(0xFFFF7A85)
    val Pink = Color(0xFFEC4899)
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFF9CA3AF)
}

private val addAccountColorOptions = listOf(
    AddAccountPalette.Lavender,
    AddAccountPalette.Sky,
    AddAccountPalette.Cyan,
    AddAccountPalette.Mint,
    AddAccountPalette.Orange,
    AddAccountPalette.Rose,
    AddAccountPalette.Pink
)

private data class AccountIconOption(
    val label: String,
    val icon: ImageVector,
    val accent: Color
)

private val accountIconOptions = listOf(
    AccountIconOption("Billetera", Icons.Default.AccountBalanceWallet, AddAccountPalette.Lavender),
    AccountIconOption("Tarjeta", Icons.Default.CreditCard, AddAccountPalette.Sky),
    AccountIconOption("Efectivo", Icons.Default.Payments, AddAccountPalette.Mint),
    AccountIconOption("Banco", Icons.Default.AccountBalance, AddAccountPalette.Cyan),
    AccountIconOption("Alcancía", Icons.Default.Savings, AddAccountPalette.Orange),
    AccountIconOption("Estrella", Icons.Default.Star, AddAccountPalette.Lavender),
    AccountIconOption("Caja fuerte", Icons.Default.Security, AddAccountPalette.Rose),
    AccountIconOption("Meta", Icons.Default.Flag, AddAccountPalette.Mint)
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountScreen(
    viewModel: AccountsViewModel,
    accountToEdit: AccountResponse? = null,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf(accountToEdit?.name ?: "") }
    var balance by remember { mutableStateOf(accountToEdit?.initialBalance?.toString() ?: "") }

    val initialColor = if (accountToEdit?.color != null) {
        try {
            Color(android.graphics.Color.parseColor(accountToEdit.color))
        } catch (e: Exception) {
            addAccountColorOptions[0]
        }
    } else {
        addAccountColorOptions[0]
    }
    var selectedColor by remember { mutableStateOf(initialColor) }

    val initialCurrency = availableCurrencies.find { it.symbol == accountToEdit?.currency }
        ?: availableCurrencies[0]
    var selectedCurrency by remember { mutableStateOf(initialCurrency) }

    var decimalPrecision by remember {
        mutableIntStateOf((accountToEdit?.decimalPrecision ?: 2).coerceIn(0, 2))
    }
    var selectedIcon by remember { mutableStateOf(getInitialAccountIconOption(accountToEdit?.name)) }

    val success by viewModel.addAccountSuccess.collectAsState()
    val canSave = name.isNotEmpty()

    LaunchedEffect(success) {
        if (success) {
            viewModel.resetSuccess()
            onBack()
        }
    }

    fun saveAccount() {
        if (!canSave) return

        val argb = selectedColor.toArgb()
        val colorHex = String.format("#%06X", 0xFFFFFF and argb)
        val safePrecision = decimalPrecision.coerceIn(0, 2)

        if (accountToEdit != null) {
            viewModel.updateAccount(
                id = accountToEdit.id,
                name = name,
                balance = balance.toDoubleOrNull() ?: 0.0,
                currency = selectedCurrency.symbol,
                color = colorHex,
                precision = safePrecision
            )
        } else {
            viewModel.createAccount(
                name = name,
                balance = balance.toDoubleOrNull() ?: 0.0,
                currency = selectedCurrency.symbol,
                color = colorHex,
                precision = safePrecision
            )
        }
    }

    Scaffold(
        containerColor = AddAccountPalette.Background,
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Atrás",
                            tint = AddAccountPalette.TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    navigationIconContentColor = AddAccountPalette.TextPrimary
                )
            )
        },
        bottomBar = {
            SaveAccountBottomBar(
                enabled = canSave,
                onClick = ::saveAccount
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            AddAccountPalette.Background,
                            AddAccountPalette.BackgroundAlt,
                            AddAccountPalette.Background
                        )
                    )
                )
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (accountToEdit != null) "Editar cuenta" else "Añadir cuenta",
                    color = AddAccountPalette.TextPrimary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold
                )

                Text(
                    text = "Personaliza cómo verás esta cuenta en Raccoon Cash.",
                    color = AddAccountPalette.TextSecondary,
                    fontSize = 14.sp
                )

                AccountNameField(
                    name = name,
                    selectedIcon = selectedIcon,
                    selectedColor = selectedColor,
                    onNameChange = { name = it }
                )

                PremiumCard(title = "Icono de cuenta") {
                    AccountIconPicker(
                        selectedIcon = selectedIcon,
                        onSelected = { selectedIcon = it }
                    )
                }

                PremiumCard(title = "Apariencia") {
                    Text(
                        text = "Color de la cuenta",
                        color = AddAccountPalette.TextSecondary,
                        fontSize = 13.sp
                    )
                    AccountColorPicker(
                        selectedColor = selectedColor,
                        onColorSelected = { selectedColor = it }
                    )
                }

                BalanceCard(
                    balance = balance,
                    currency = selectedCurrency,
                    accentColor = selectedColor,
                    onBalanceChange = { value ->
                        if (value.isEmpty() || value.toDoubleOrNull() != null) {
                            balance = value
                        }
                    }
                )

                PremiumCard(title = "Precisión decimal") {
                    DecimalPrecisionSelector(
                        selectedPrecision = decimalPrecision,
                        onPrecisionSelected = { decimalPrecision = it }
                    )
                }

                PremiumCard(title = "Divisa de la cuenta") {
                    CurrencySelector(
                        selectedCurrency = selectedCurrency,
                        onCurrencySelected = { selectedCurrency = it }
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountNameField(
    name: String,
    selectedIcon: AccountIconOption,
    selectedColor: Color,
    onNameChange: (String) -> Unit
) {
    OutlinedTextField(
        value = name,
        onValueChange = onNameChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text("Nombre") },
        placeholder = { Text("Ej. Efectivo, Débito, Ahorro…") },
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(selectedColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    selectedIcon.icon,
                    contentDescription = selectedIcon.label,
                    tint = selectedColor,
                    modifier = Modifier.size(22.dp)
                )
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
        textStyle = LocalTextStyle.current.copy(
            color = AddAccountPalette.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        ),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = AddAccountPalette.TextPrimary,
            unfocusedTextColor = AddAccountPalette.TextPrimary,
            focusedContainerColor = AddAccountPalette.ElevatedCard,
            unfocusedContainerColor = AddAccountPalette.ElevatedCard,
            focusedBorderColor = AddAccountPalette.Lavender,
            unfocusedBorderColor = AddAccountPalette.Border,
            cursorColor = AddAccountPalette.Lavender,
            focusedLabelColor = AddAccountPalette.Lavender,
            unfocusedLabelColor = AddAccountPalette.TextSecondary,
            focusedPlaceholderColor = AddAccountPalette.TextSecondary,
            unfocusedPlaceholderColor = AddAccountPalette.TextSecondary
        )
    )
}

@Composable
private fun PremiumCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        color = AddAccountPalette.Card,
        tonalElevation = 2.dp,
        border = BorderStroke(1.dp, AddAccountPalette.Border)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title,
                color = AddAccountPalette.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

@Composable
private fun AccountIconPicker(
    selectedIcon: AccountIconOption,
    onSelected: (AccountIconOption) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        accountIconOptions.chunked(4).forEach { rowOptions ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowOptions.forEach { option ->
                    AccountIconTile(
                        option = option,
                        selected = selectedIcon == option,
                        onClick = { onSelected(option) },
                        modifier = Modifier.weight(1f)
                    )
                }
                repeat(4 - rowOptions.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AccountIconTile(
    option: AccountIconOption,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(86.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(if (selected) option.accent.copy(alpha = 0.16f) else AddAccountPalette.ElevatedCard)
            .border(
                width = if (selected) 2.dp else 1.dp,
                color = if (selected) AddAccountPalette.Lavender else AddAccountPalette.Border,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(option.accent.copy(alpha = if (selected) 0.24f else 0.14f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                option.icon,
                contentDescription = option.label,
                tint = option.accent,
                modifier = Modifier.size(21.dp)
            )
        }
        Spacer(modifier = Modifier.height(7.dp))
        Text(
            text = option.label,
            color = if (selected) AddAccountPalette.TextPrimary else AddAccountPalette.TextSecondary,
            fontSize = 10.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1
        )
    }
}

@Composable
private fun AccountColorPicker(
    selectedColor: Color,
    onColorSelected: (Color) -> Unit
) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(addAccountColorOptions) { color ->
            val selected = selectedColor == color
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(if (selected) color.copy(alpha = 0.18f) else Color.Transparent)
                    .border(
                        width = if (selected) 2.dp else 1.dp,
                        color = if (selected) AddAccountPalette.Lavender else AddAccountPalette.Border,
                        shape = CircleShape
                    )
                    .clickable { onColorSelected(color) }
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(color),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Color seleccionado",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceCard(
    balance: String,
    currency: CurrencyInfo,
    accentColor: Color,
    onBalanceChange: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = AddAccountPalette.Card,
        tonalElevation = 3.dp,
        border = BorderStroke(1.dp, AddAccountPalette.Border)
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.linearGradient(
                        listOf(
                            AddAccountPalette.ElevatedCard.copy(alpha = 0.92f),
                            AddAccountPalette.Card
                        )
                    )
                )
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "Saldo inicial",
                color = AddAccountPalette.TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "${currency.symbol}${if (balance.isBlank()) "0.00" else balance}",
                color = accentColor,
                fontSize = 34.sp,
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Ingresa el saldo con el que inicia esta cuenta.",
                color = AddAccountPalette.TextSecondary,
                fontSize = 13.sp
            )

            TextField(
                value = balance,
                onValueChange = onBalanceChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Monto") },
                placeholder = { Text("0.00") },
                prefix = { Text(currency.symbol) },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                textStyle = LocalTextStyle.current.copy(
                    color = AddAccountPalette.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                colors = TextFieldDefaults.colors(
                    focusedTextColor = AddAccountPalette.TextPrimary,
                    unfocusedTextColor = AddAccountPalette.TextPrimary,
                    focusedContainerColor = AddAccountPalette.BackgroundAlt.copy(alpha = 0.72f),
                    unfocusedContainerColor = AddAccountPalette.BackgroundAlt.copy(alpha = 0.72f),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = AddAccountPalette.Lavender,
                    focusedLabelColor = AddAccountPalette.Lavender,
                    unfocusedLabelColor = AddAccountPalette.TextSecondary,
                    focusedPlaceholderColor = AddAccountPalette.TextSecondary,
                    unfocusedPlaceholderColor = AddAccountPalette.TextSecondary,
                    focusedPrefixColor = AddAccountPalette.TextPrimary,
                    unfocusedPrefixColor = AddAccountPalette.TextPrimary
                )
            )
        }
    }
}

@Composable
private fun DecimalPrecisionSelector(
    selectedPrecision: Int,
    onPrecisionSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        listOf(0, 1, 2).forEach { precision ->
            val selected = selectedPrecision == precision
            Surface(
                onClick = { onPrecisionSelected(precision) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(18.dp),
                color = if (selected) AddAccountPalette.Lavender.copy(alpha = 0.18f) else AddAccountPalette.ElevatedCard,
                border = BorderStroke(
                    width = if (selected) 2.dp else 1.dp,
                    color = if (selected) AddAccountPalette.Lavender else AddAccountPalette.Border
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = precision.toString(),
                        color = if (selected) AddAccountPalette.Lavender else AddAccountPalette.TextPrimary,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (precision == 1) "decimal" else "decimales",
                        color = if (selected) AddAccountPalette.TextPrimary else AddAccountPalette.TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun CurrencySelector(
    selectedCurrency: CurrencyInfo,
    onCurrencySelected: (CurrencyInfo) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        availableCurrencies.forEach { currency ->
            CurrencyCard(
                currency = currency,
                selected = selectedCurrency == currency,
                onClick = { onCurrencySelected(currency) }
            )
        }
    }
}

@Composable
private fun CurrencyCard(
    currency: CurrencyInfo,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = if (selected) AddAccountPalette.Lavender.copy(alpha = 0.14f) else AddAccountPalette.ElevatedCard,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) AddAccountPalette.Lavender else AddAccountPalette.Border
        )
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(if (selected) AddAccountPalette.Lavender.copy(alpha = 0.24f) else AddAccountPalette.Card),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = currency.symbol,
                    color = if (selected) AddAccountPalette.Lavender else AddAccountPalette.TextPrimary,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${currency.code} — ${currency.symbol}",
                    color = AddAccountPalette.TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = currency.country,
                    color = AddAccountPalette.TextSecondary,
                    fontSize = 13.sp
                )
            }

            if (selected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Divisa seleccionada",
                    tint = AddAccountPalette.Lavender,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

@Composable
private fun SaveAccountBottomBar(
    enabled: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = AddAccountPalette.Background.copy(alpha = 0.96f),
        tonalElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .clip(RoundedCornerShape(999.dp))
                    .background(
                        Brush.horizontalGradient(
                            if (enabled) {
                                listOf(
                                    AddAccountPalette.LavenderStrong,
                                    AddAccountPalette.Lavender,
                                    Color(0xFFC4B5FD)
                                )
                            } else {
                                listOf(
                                    AddAccountPalette.ElevatedCard,
                                    AddAccountPalette.Card
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
                Text(
                    text = "Guardar cuenta",
                    color = if (enabled) AddAccountPalette.TextPrimary else AddAccountPalette.TextSecondary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

private fun getInitialAccountIconOption(accountName: String?): AccountIconOption {
    val normalizedName = accountName.orEmpty().trim().lowercase()
    return when {
        listOf("tarjeta", "débito", "debito", "crédito", "credito").any { it in normalizedName } ->
            accountIconOptions.first { it.label == "Tarjeta" }
        listOf("efectivo", "cash", "dinero").any { it in normalizedName } ->
            accountIconOptions.first { it.label == "Efectivo" }
        listOf("banco", "bank").any { it in normalizedName } ->
            accountIconOptions.first { it.label == "Banco" }
        listOf("alcancía", "alcancia", "ahorro").any { it in normalizedName } ->
            accountIconOptions.first { it.label == "Alcancía" }
        listOf("meta", "objetivo").any { it in normalizedName } ->
            accountIconOptions.first { it.label == "Meta" }
        else -> accountIconOptions.first()
    }
}
