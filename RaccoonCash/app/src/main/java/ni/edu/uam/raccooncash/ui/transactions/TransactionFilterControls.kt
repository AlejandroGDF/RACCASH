package ni.edu.uam.raccooncash.ui.transactions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.raccooncash.data.model.CategoryResponse
import ni.edu.uam.raccooncash.data.model.TransactionResponse
import ni.edu.uam.raccooncash.ui.accounts.getEmojiForCategory
import ni.edu.uam.raccooncash.util.formatCurrencyAmount
import ni.edu.uam.raccooncash.util.isPotentialMoneyInput
import ni.edu.uam.raccooncash.util.parseMoneyInput
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

data class CategoryFilterSelection(
    val categoryId: Long,
    val includeSubcategories: Boolean
)

data class TransactionFilterState(
    val selectedTypes: Set<String> = emptySet(),
    val categoryFilters: List<CategoryFilterSelection> = emptyList(),
    val titleQuery: String = "",
    val minAmount: String = "",
    val maxAmount: String = ""
) {
    val activeCount: Int
        get() = selectedTypes.size +
            categoryFilters.size +
            (if (titleQuery.isNotBlank()) 1 else 0) +
            (if (minAmount.isNotBlank() || maxAmount.isNotBlank()) 1 else 0)

    val hasActiveFilters: Boolean
        get() = activeCount > 0
}

enum class TransactionSortOption(val label: String) {
    DATE_DESC("Mas recientes"),
    DATE_ASC("Mas antiguas"),
    AMOUNT_DESC("Mayor monto"),
    AMOUNT_ASC("Menor monto")
}

private object FilterSheetPalette {
    val Background = Color(0xFF080B14)
    val BackgroundAlt = Color(0xFF0B1020)
    val Card = Color(0xFF171C2A)
    val ElevatedCard = Color(0xFF202638)
    val Border = Color.White.copy(alpha = 0.09f)
    val Lavender = Color(0xFFA78BFA)
    val LavenderDeep = Color(0xFF31254B)
    val Mint = Color(0xFF7EDC8D)
    val Sky = Color(0xFF74C7EC)
    val Coral = Color(0xFFFF7A85)
    val TextPrimary = Color.White
    val TextSecondary = Color(0xFF9CA3AF)
}

@Composable
fun TransactionToolsMenu(
    activeFilterCount: Int,
    sortOption: TransactionSortOption,
    onFilterClick: () -> Unit,
    onSortSelected: (TransactionSortOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Surface(
            onClick = { expanded = true },
            color = Color(0xFF202638),
            shape = RoundedCornerShape(999.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (activeFilterCount > 0) Color(0xFFA78BFA) else Color(0xFFA78BFA).copy(alpha = 0.28f)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Herramientas", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                if (activeFilterCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(Color(0xFFA78BFA), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(activeFilterCount.toString(), color = Color(0xFF080B14), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color(0xFFA78BFA), modifier = Modifier.size(18.dp))
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = Color(0xFF1E222D)
        ) {
            DropdownMenuItem(
                text = { Text("Filtro", color = Color.White) },
                leadingIcon = { Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.White) },
                onClick = {
                    expanded = false
                    onFilterClick()
                }
            )

            HorizontalDivider(color = Color.Gray.copy(alpha = 0.2f))
            Text(
                "Ordenar",
                color = Color.Gray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            TransactionSortOption.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label, color = Color.White) },
                    trailingIcon = {
                        if (sortOption == option) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFFB5A9D4))
                        }
                    },
                    onClick = {
                        expanded = false
                        onSortSelected(option)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TransactionFilterSheet(
    filters: TransactionFilterState,
    categories: List<CategoryResponse>,
    onFiltersChange: (TransactionFilterState) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = FilterSheetPalette.Background,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 2.dp)
                    .width(46.dp)
                    .height(5.dp)
                    .background(FilterSheetPalette.Lavender.copy(alpha = 0.45f), RoundedCornerShape(999.dp))
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .heightIn(max = 680.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            FilterSheetPalette.Background,
                            FilterSheetPalette.BackgroundAlt,
                            FilterSheetPalette.Background
                        )
                    )
                )
                .padding(horizontal = 14.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(top = 8.dp, bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilterSheetHeader(
                    hasActiveFilters = filters.hasActiveFilters,
                    onClearAll = { onFiltersChange(TransactionFilterState()) }
                )

                SelectedFiltersList(
                    filters = filters,
                    onFiltersChange = onFiltersChange
                )

                FilterSection(title = "Tipo de transacción") {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TransactionTypeChip("Gasto", filters.selectedTypes.contains("EXPENSE"), FilterSheetPalette.Coral, Icons.Default.ArrowDownward) {
                            onFiltersChange(filters.toggleType("EXPENSE"))
                        }
                        TransactionTypeChip("Ingreso", filters.selectedTypes.contains("INCOME"), FilterSheetPalette.Mint, Icons.Default.ArrowUpward) {
                            onFiltersChange(filters.toggleType("INCOME"))
                        }
                        TransactionTypeChip("Transferencia", filters.selectedTypes.contains("TRANSFER"), FilterSheetPalette.Sky, Icons.Default.SwapHoriz) {
                            onFiltersChange(filters.toggleType("TRANSFER"))
                        }
                    }
                }

                FilterSection(title = "Título") {
                    OutlinedTextField(
                        value = filters.titleQuery,
                        onValueChange = { onFiltersChange(filters.copy(titleQuery = it)) },
                        label = { Text("Título") },
                        placeholder = { Text("Buscar por título") },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = FilterSheetPalette.TextSecondary)
                        },
                        singleLine = true,
                        colors = filterTextFieldColors(),
                        shape = RoundedCornerShape(16.dp),
                        textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 52.dp)
                    )
                }

                FilterSection(title = "Monto") {
                    Text("Déjalo vacío si no quieres filtrar por monto.", color = FilterSheetPalette.TextSecondary, fontSize = 12.sp)
                    AmountFields(filters = filters, onFiltersChange = onFiltersChange)
                }

                FilterSection(title = "Categorías y subcategorías") {
                    Text(
                        "Elige una categoría completa o subcategorías específicas.",
                        color = FilterSheetPalette.TextSecondary,
                        fontSize = 12.sp
                    )
                    CategoryFilterList(
                        categories = categories,
                        filters = filters,
                        onFiltersChange = onFiltersChange
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(top = 8.dp, bottom = 12.dp)
            ) {
                ApplyFiltersButton(onClick = onDismiss)
            }
        }
    }
}

fun TransactionResponse.matchesTransactionFilters(
    filters: TransactionFilterState,
    categories: List<CategoryResponse>
): Boolean {
    if (filters.selectedTypes.isNotEmpty() && type !in filters.selectedTypes) return false

    if (filters.titleQuery.isNotBlank() && !description.contains(filters.titleQuery.trim(), ignoreCase = true)) {
        return false
    }

    val minAmount = filters.minAmount.toAmountOrNull()
    val maxAmount = filters.maxAmount.toAmountOrNull()
    if (minAmount != null && amount < minAmount) return false
    if (maxAmount != null && amount > maxAmount) return false

    if (filters.categoryFilters.isNotEmpty()) {
        val transactionCategoryId = categoryId ?: category?.id ?: return false
        val transactionCategory = categories.find { it.id == transactionCategoryId } ?: category
        val parentId = transactionCategory?.parentCategoryId?.takeIf { it != 0L }

        return filters.categoryFilters.any { filter ->
            if (filter.includeSubcategories) {
                transactionCategoryId == filter.categoryId || parentId == filter.categoryId
            } else {
                transactionCategoryId == filter.categoryId
            }
        }
    }

    return true
}

fun buildTransactionGroups(
    transactions: List<TransactionResponse>,
    sortOption: TransactionSortOption
): List<Pair<LocalDate, List<TransactionResponse>>> {
    val grouped = transactions.groupBy { parseTransactionDate(it)?.toLocalDate() ?: LocalDate.now() }

    return when (sortOption) {
        TransactionSortOption.DATE_DESC -> grouped.toList()
            .sortedByDescending { it.first }
            .map { (date, items) -> date to items.sortedByDescending { parseTransactionDate(it) } }
        TransactionSortOption.DATE_ASC -> grouped.toList()
            .sortedBy { it.first }
            .map { (date, items) -> date to items.sortedBy { parseTransactionDate(it) } }
        TransactionSortOption.AMOUNT_DESC -> grouped.toList()
            .sortedByDescending { (_, items) -> items.maxOfOrNull { it.amount } ?: 0.0 }
            .map { (date, items) -> date to items.sortedByDescending { it.amount } }
        TransactionSortOption.AMOUNT_ASC -> grouped.toList()
            .sortedBy { (_, items) -> items.minOfOrNull { it.amount } ?: 0.0 }
            .map { (date, items) -> date to items.sortedBy { it.amount } }
    }
}

fun parseTransactionDate(transaction: TransactionResponse): LocalDateTime? {
    return try {
        LocalDateTime.parse(transaction.date, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    } catch (e: Exception) {
        null
    }
}

@Composable
private fun FilterSheetHeader(
    hasActiveFilters: Boolean,
    onClearAll: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Text("Filtros", color = FilterSheetPalette.TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
            Text(
                "Aplica varias condiciones para afinar la búsqueda.",
                color = FilterSheetPalette.TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )
        }

        TextButton(
            onClick = onClearAll,
            enabled = hasActiveFilters,
            colors = ButtonDefaults.textButtonColors(
                contentColor = FilterSheetPalette.Lavender,
                disabledContentColor = FilterSheetPalette.TextSecondary.copy(alpha = 0.45f)
            ),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text("Limpiar todo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SelectedFiltersList(
    filters: TransactionFilterState,
    onFiltersChange: (TransactionFilterState) -> Unit
) {
    val hasGeneralFilters = filters.selectedTypes.isNotEmpty() ||
        filters.titleQuery.isNotBlank() ||
        filters.minAmount.isNotBlank() ||
        filters.maxAmount.isNotBlank()

    if (!hasGeneralFilters) return

    FilterSection(title = "Filtros activos") {
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            filters.selectedTypes.forEach { type ->
                AppliedFilterChip("Tipo: ${type.toTransactionTypeLabel()}") {
                    onFiltersChange(filters.copy(selectedTypes = filters.selectedTypes - type))
                }
            }
            if (filters.titleQuery.isNotBlank()) {
                AppliedFilterChip("Título: ${filters.titleQuery}") {
                    onFiltersChange(filters.copy(titleQuery = ""))
                }
            }
            if (filters.minAmount.isNotBlank() || filters.maxAmount.isNotBlank()) {
                val min = formatAmountFilterLabel(filters.minAmount, "sin mínimo")
                val max = formatAmountFilterLabel(filters.maxAmount, "sin máximo")
                AppliedFilterChip("Monto: $min - $max") {
                    onFiltersChange(filters.copy(minAmount = "", maxAmount = ""))
                }
            }
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = FilterSheetPalette.Card,
        border = BorderStroke(1.dp, FilterSheetPalette.Border),
        shadowElevation = 6.dp
    ) {
        Column(
            modifier = Modifier.padding(13.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(title, color = FilterSheetPalette.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            content()
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AmountFields(
    filters: TransactionFilterState,
    onFiltersChange: (TransactionFilterState) -> Unit
) {
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val stackFields = maxWidth < 280.dp
        val fieldSpacing = if (stackFields) 8.dp else 10.dp

        if (stackFields) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(fieldSpacing)
            ) {
                AmountTextField(
                    value = filters.minAmount,
                    onValueChange = { if (isPotentialMoneyInput(it)) onFiltersChange(filters.copy(minAmount = it)) },
                    label = "Mínimo"
                )
                AmountTextField(
                    value = filters.maxAmount,
                    onValueChange = { if (isPotentialMoneyInput(it)) onFiltersChange(filters.copy(maxAmount = it)) },
                    label = "Máximo"
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(fieldSpacing)
            ) {
                AmountTextField(
                    value = filters.minAmount,
                    onValueChange = { if (isPotentialMoneyInput(it)) onFiltersChange(filters.copy(minAmount = it)) },
                    label = "Mínimo",
                    modifier = Modifier.weight(1f)
                )
                AmountTextField(
                    value = filters.maxAmount,
                    onValueChange = { if (isPotentialMoneyInput(it)) onFiltersChange(filters.copy(maxAmount = it)) },
                    label = "Máximo",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AmountTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier.fillMaxWidth()
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text("0.00") },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        colors = filterTextFieldColors(),
        shape = RoundedCornerShape(16.dp),
        textStyle = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
        modifier = modifier.heightIn(min = 52.dp)
    )
}

@Composable
private fun TransactionTypeChip(
    text: String,
    selected: Boolean,
    accent: Color,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (selected) FilterSheetPalette.Lavender.copy(alpha = 0.16f) else FilterSheetPalette.ElevatedCard,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) FilterSheetPalette.Lavender else FilterSheetPalette.Border
        ),
        shadowElevation = if (selected) 8.dp else 0.dp,
        modifier = Modifier.widthIn(min = 102.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(accent, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = FilterSheetPalette.Background, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                color = FilterSheetPalette.TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CategoryFilterList(
    categories: List<CategoryResponse>,
    filters: TransactionFilterState,
    onFiltersChange: (TransactionFilterState) -> Unit
) {
    val rootCategories = categories
        .filter { it.parentCategoryId == null || it.parentCategoryId == 0L }
        .sortedWith(compareBy<CategoryResponse> { it.type }.thenBy { it.name.lowercase(Locale.getDefault()) })

    if (rootCategories.isEmpty()) {
        Text("No hay categorías disponibles.", color = FilterSheetPalette.TextSecondary, fontSize = 13.sp)
        return
    }

    var focusedCategoryId by remember(rootCategories.map { it.id }) { mutableLongStateOf(rootCategories.first().id) }
    val focusedCategory = rootCategories.find { it.id == focusedCategoryId } ?: rootCategories.first()
    val focusedSubcategories = categories
        .filter { it.parentCategoryId == focusedCategory.id }
        .sortedBy { it.name.lowercase(Locale.getDefault()) }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(rootCategories, key = { it.id }) { category ->
                val wholeCategorySelection = CategoryFilterSelection(category.id, includeSubcategories = true)
                CategoryIconFilterItem(
                    category = category,
                    isFocused = focusedCategory.id == category.id,
                    isActive = wholeCategorySelection in filters.categoryFilters || filters.hasSelectedSubcategory(category, categories),
                    onClick = {
                        focusedCategoryId = category.id
                        onFiltersChange(filters.toggleCategorySelection(wholeCategorySelection, categories))
                    }
                )
            }
        }

        CategoryScopeChooser(
            category = focusedCategory,
            subcategories = focusedSubcategories,
            filters = filters,
            categories = categories,
            onFiltersChange = onFiltersChange
        )

        AppliedCategoryFiltersPanel(
            filters = filters,
            categories = categories,
            onFiltersChange = onFiltersChange
        )
    }
}

@Composable
private fun CategoryScopeChooser(
    category: CategoryResponse,
    subcategories: List<CategoryResponse>,
    filters: TransactionFilterState,
    categories: List<CategoryResponse>,
    onFiltersChange: (TransactionFilterState) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = FilterSheetPalette.BackgroundAlt,
        border = BorderStroke(1.dp, FilterSheetPalette.Border)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(category.name, color = FilterSheetPalette.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(category.type.toTransactionTypeLabel(), color = FilterSheetPalette.TextSecondary, fontSize = 12.sp)
                }
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(category.type.toTransactionTypeColor(), CircleShape)
                )
            }

            val wholeCategorySelection = CategoryFilterSelection(category.id, includeSubcategories = true)
            SelectableChip(
                text = if (subcategories.isEmpty()) "Seleccionar categoría" else "Seleccionar categoría completa",
                selected = wholeCategorySelection in filters.categoryFilters,
                accent = FilterSheetPalette.Lavender,
                onClick = {
                    onFiltersChange(filters.toggleCategorySelection(wholeCategorySelection, categories))
                }
            )

            if (subcategories.isNotEmpty()) {
                Text("Subcategorías disponibles", color = FilterSheetPalette.TextSecondary, fontSize = 12.sp)
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(subcategories, key = { it.id }) { subcategory ->
                        val subcategorySelection = CategoryFilterSelection(subcategory.id, includeSubcategories = false)
                        CategoryIconFilterItem(
                            category = subcategory,
                            isFocused = false,
                            isActive = subcategorySelection in filters.categoryFilters,
                            selected = subcategorySelection in filters.categoryFilters,
                            onClick = {
                                onFiltersChange(filters.toggleCategorySelection(subcategorySelection, categories))
                            }
                        )
                    }
                }
            } else {
                Text("Esta categoría no tiene subcategorías.", color = FilterSheetPalette.TextSecondary, fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AppliedCategoryFiltersPanel(
    filters: TransactionFilterState,
    categories: List<CategoryResponse>,
    onFiltersChange: (TransactionFilterState) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = FilterSheetPalette.ElevatedCard,
        border = BorderStroke(
            1.dp,
            if (filters.categoryFilters.isEmpty()) FilterSheetPalette.Border else FilterSheetPalette.Lavender.copy(alpha = 0.62f)
        ),
        shadowElevation = if (filters.categoryFilters.isEmpty()) 0.dp else 10.dp
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Filtros de categoría aplicados", color = FilterSheetPalette.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)

            if (filters.categoryFilters.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = FilterSheetPalette.BackgroundAlt,
                    border = BorderStroke(1.dp, FilterSheetPalette.Border)
                ) {
                    Text(
                        "Aún no seleccionaste categorías.",
                        color = FilterSheetPalette.TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)
                    )
                }
            } else {
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filters.categoryFilters.forEach { filter ->
                        AppliedFilterChip(filter.toCategoryFilterLabel(categories)) {
                            onFiltersChange(filters.copy(categoryFilters = filters.categoryFilters - filter))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryIconFilterItem(
    category: CategoryResponse,
    isFocused: Boolean,
    isActive: Boolean,
    onClick: () -> Unit,
    selected: Boolean = isActive
) {
    val borderColor = when {
        selected -> FilterSheetPalette.Lavender
        isFocused -> FilterSheetPalette.Lavender.copy(alpha = 0.48f)
        else -> FilterSheetPalette.Border
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(76.dp)
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(18.dp),
            color = when {
                selected -> FilterSheetPalette.Lavender.copy(alpha = 0.18f)
                isFocused -> FilterSheetPalette.ElevatedCard
                else -> FilterSheetPalette.BackgroundAlt
            },
            border = BorderStroke(width = if (selected || isFocused) 2.dp else 1.dp, color = borderColor),
            shadowElevation = if (selected) 8.dp else 0.dp
        ) {
            Box(
                modifier = Modifier.size(58.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(getEmojiForCategory(category.name, category.icon), fontSize = 26.sp)
            }
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = category.name,
            color = if (isActive) FilterSheetPalette.TextPrimary else FilterSheetPalette.TextSecondary,
            fontSize = 10.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 11.sp,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SelectableChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    accent: Color = FilterSheetPalette.Lavender,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = if (selected) FilterSheetPalette.Lavender.copy(alpha = 0.2f) else FilterSheetPalette.ElevatedCard,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) FilterSheetPalette.Lavender else FilterSheetPalette.Border
        ),
        shadowElevation = if (selected) 5.dp else 0.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = FilterSheetPalette.Lavender, modifier = Modifier.size(14.dp))
            } else {
                Box(modifier = Modifier.size(7.dp).background(accent.copy(alpha = 0.82f), CircleShape))
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(text, color = FilterSheetPalette.TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun AppliedFilterChip(
    text: String,
    onRemove: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = FilterSheetPalette.BackgroundAlt,
        border = BorderStroke(1.dp, FilterSheetPalette.Lavender.copy(alpha = 0.46f))
    ) {
        Row(
            modifier = Modifier.padding(start = 10.dp, end = 3.dp, top = 5.dp, bottom = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                color = FilterSheetPalette.TextPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.widthIn(max = 190.dp)
            )
            IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Quitar filtro", tint = FilterSheetPalette.Lavender, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun ApplyFiltersButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(999.dp),
        color = Color.Transparent,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(FilterSheetPalette.LavenderDeep, FilterSheetPalette.Lavender)
                    ),
                    RoundedCornerShape(999.dp)
                )
                .padding(vertical = 13.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Check, contentDescription = null, tint = FilterSheetPalette.TextPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Aplicar filtros", color = FilterSheetPalette.TextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
        }
    }
}

@Composable
private fun filterTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = FilterSheetPalette.TextPrimary,
    unfocusedTextColor = FilterSheetPalette.TextPrimary,
    focusedContainerColor = FilterSheetPalette.ElevatedCard,
    unfocusedContainerColor = FilterSheetPalette.ElevatedCard,
    disabledContainerColor = FilterSheetPalette.ElevatedCard,
    focusedBorderColor = FilterSheetPalette.Lavender,
    unfocusedBorderColor = FilterSheetPalette.Border,
    focusedLabelColor = FilterSheetPalette.Lavender,
    unfocusedLabelColor = FilterSheetPalette.TextSecondary,
    focusedPlaceholderColor = FilterSheetPalette.TextSecondary,
    unfocusedPlaceholderColor = FilterSheetPalette.TextSecondary,
    cursorColor = FilterSheetPalette.Lavender
)

private fun TransactionFilterState.toggleType(type: String): TransactionFilterState {
    return copy(
        selectedTypes = if (type in selectedTypes) selectedTypes - type else selectedTypes + type
    )
}

private fun TransactionFilterState.hasSelectedSubcategory(
    category: CategoryResponse,
    categories: List<CategoryResponse>
): Boolean {
    return categoryFilters.any { filter ->
        val selectedCategory = categories.find { it.id == filter.categoryId }
        !filter.includeSubcategories && selectedCategory?.parentCategoryId == category.id
    }
}

private fun TransactionFilterState.toggleCategorySelection(
    selection: CategoryFilterSelection,
    categories: List<CategoryResponse>
): TransactionFilterState {
    if (selection in categoryFilters) {
        return copy(categoryFilters = categoryFilters - selection)
    }

    val selectedCategory = categories.find { it.id == selection.categoryId }
    val parentId = selectedCategory?.parentCategoryId?.takeIf { it != 0L }
    val updatedFilters = if (selection.includeSubcategories) {
        categoryFilters.filterNot { existing ->
            val existingCategory = categories.find { it.id == existing.categoryId }
            existing.categoryId == selection.categoryId || existingCategory?.parentCategoryId == selection.categoryId
        }
    } else {
        categoryFilters.filterNot { existing ->
            existing.includeSubcategories && existing.categoryId == parentId
        }
    }

    return copy(categoryFilters = updatedFilters + selection)
}

private fun CategoryFilterSelection.toCategoryFilterLabel(categories: List<CategoryResponse>): String {
    val category = categories.find { it.id == categoryId }
    val parent = category?.parentCategoryId?.takeIf { it != 0L }?.let { parentId -> categories.find { it.id == parentId } }

    return when {
        includeSubcategories -> "${category?.name ?: "Categoría desconocida"} completo"
        parent != null -> "${parent.name} > ${category?.name ?: "Subcategoría desconocida"}"
        else -> category?.name ?: "Categoría desconocida"
    }
}

private fun String.toTransactionTypeLabel(): String {
    return when (this) {
        "EXPENSE" -> "Gasto"
        "INCOME" -> "Ingreso"
        "TRANSFER" -> "Transferencia"
        else -> this.lowercase().replaceFirstChar { it.uppercase() }
    }
}

private fun String.toTransactionTypeColor(): Color {
    return when (this) {
        "EXPENSE" -> FilterSheetPalette.Coral
        "INCOME" -> FilterSheetPalette.Mint
        "TRANSFER" -> FilterSheetPalette.Sky
        else -> FilterSheetPalette.Lavender
    }
}

private fun formatAmountFilterLabel(value: String, emptyLabel: String): String {
    if (value.isBlank()) return emptyLabel
    return parseMoneyInput(value)?.let { formatCurrencyAmount(it) } ?: value
}

private fun String.toAmountOrNull(): Double? {
    return parseMoneyInput(this)
}
