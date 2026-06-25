package ni.edu.uam.raccooncash.ui.transactions

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ni.edu.uam.raccooncash.data.model.CategoryResponse
import ni.edu.uam.raccooncash.data.model.TransactionResponse
import ni.edu.uam.raccooncash.ui.accounts.getEmojiForCategory
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
            color = Color(0xFF1E222D),
            shape = RoundedCornerShape(20.dp),
            border = if (activeFilterCount > 0) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB5A9D4)) else null
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text("Herramientas", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                if (activeFilterCount > 0) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .background(Color(0xFFB5A9D4), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(activeFilterCount.toString(), color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFilterSheet(
    filters: TransactionFilterState,
    categories: List<CategoryResponse>,
    onFiltersChange: (TransactionFilterState) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF0F111A)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 660.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Filtros", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                    Text("Apila varias condiciones para afinar la busqueda.", color = Color.Gray, fontSize = 13.sp)
                }
                if (filters.hasActiveFilters) {
                    Button(
                        onClick = { onFiltersChange(TransactionFilterState()) },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C313F), contentColor = Color.White)
                    ) {
                        Text("Limpiar")
                    }
                }
            }

            SelectedFiltersList(
                filters = filters,
                categories = categories,
                onFiltersChange = onFiltersChange
            )

            FilterSection(title = "Tipo de transaccion") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    TransactionTypeChip("Gasto", filters.selectedTypes.contains("EXPENSE")) {
                        onFiltersChange(filters.toggleType("EXPENSE"))
                    }
                    TransactionTypeChip("Ingreso", filters.selectedTypes.contains("INCOME")) {
                        onFiltersChange(filters.toggleType("INCOME"))
                    }
                    TransactionTypeChip("Transferencia", filters.selectedTypes.contains("TRANSFER")) {
                        onFiltersChange(filters.toggleType("TRANSFER"))
                    }
                }
            }

            FilterSection(title = "Titulo") {
                OutlinedTextField(
                    value = filters.titleQuery,
                    onValueChange = { onFiltersChange(filters.copy(titleQuery = it)) },
                    placeholder = { Text("Buscar por titulo") },
                    singleLine = true,
                    colors = filterTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            FilterSection(title = "Monto") {
                Text("Dejalo vacio si no quieres filtrar por monto.", color = Color.Gray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = filters.minAmount,
                        onValueChange = { onFiltersChange(filters.copy(minAmount = sanitizeAmountInput(it))) },
                        placeholder = { Text("Min") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = filterTextFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = filters.maxAmount,
                        onValueChange = { onFiltersChange(filters.copy(maxAmount = sanitizeAmountInput(it))) },
                        placeholder = { Text("Max") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = filterTextFieldColors(),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            FilterSection(title = "Categorias y subcategorias") {
                Text(
                    "Elige una categoria completa o solo subcategorias especificas. Puedes escoger varias.",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                CategoryFilterList(
                    categories = categories,
                    filters = filters,
                    onFiltersChange = onFiltersChange
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
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
private fun SelectedFiltersList(
    filters: TransactionFilterState,
    categories: List<CategoryResponse>,
    onFiltersChange: (TransactionFilterState) -> Unit
) {
    if (!filters.hasActiveFilters) {
        Text("No hay filtros aplicados.", color = Color.Gray, fontSize = 13.sp)
        return
    }

    FilterSection(title = "Filtros aplicados") {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            filters.selectedTypes.forEach { type ->
                AppliedFilterRow("Tipo: ${type.toTransactionTypeLabel()}") {
                    onFiltersChange(filters.copy(selectedTypes = filters.selectedTypes - type))
                }
            }
            filters.categoryFilters.forEach { filter ->
                AppliedFilterRow(filter.toCategoryFilterLabel(categories)) {
                    onFiltersChange(filters.copy(categoryFilters = filters.categoryFilters - filter))
                }
            }
            if (filters.titleQuery.isNotBlank()) {
                AppliedFilterRow("Titulo: ${filters.titleQuery}") {
                    onFiltersChange(filters.copy(titleQuery = ""))
                }
            }
            if (filters.minAmount.isNotBlank() || filters.maxAmount.isNotBlank()) {
                val min = filters.minAmount.ifBlank { "sin minimo" }
                val max = filters.maxAmount.ifBlank { "sin maximo" }
                AppliedFilterRow("Monto: C$$min - C$$max") {
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF1E222D), RoundedCornerShape(18.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(title, color = Color.White, fontWeight = FontWeight.Bold)
        content()
    }
}

@Composable
private fun RowScope.TransactionTypeChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    SelectableChip(
        text = text,
        selected = selected,
        modifier = Modifier.weight(1f),
        onClick = onClick
    )
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
        Text("No hay categorias disponibles.", color = Color.Gray, fontSize = 13.sp)
        return
    }

    var focusedCategoryId by remember(rootCategories.map { it.id }) { mutableLongStateOf(rootCategories.first().id) }
    val focusedCategory = rootCategories.find { it.id == focusedCategoryId } ?: rootCategories.first()
    val focusedSubcategories = categories
        .filter { it.parentCategoryId == focusedCategory.id }
        .sortedBy { it.name.lowercase(Locale.getDefault()) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F111A), RoundedCornerShape(16.dp))
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Aplicar en ${category.name}", color = Color.White, fontWeight = FontWeight.Bold)
        Text(category.type.toTransactionTypeLabel(), color = Color.Gray, fontSize = 12.sp)

        val wholeCategorySelection = CategoryFilterSelection(category.id, includeSubcategories = true)
        SelectableChip(
            text = if (subcategories.isEmpty()) "Solo esta categoria" else "Toda la categoria",
            selected = wholeCategorySelection in filters.categoryFilters,
            onClick = {
                onFiltersChange(filters.toggleCategorySelection(wholeCategorySelection, categories))
            }
        )

        if (subcategories.isNotEmpty()) {
            Text("O solo una subcategoria", color = Color.Gray, fontSize = 12.sp)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
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
            Text("Esta categoria no tiene subcategorias.", color = Color.Gray, fontSize = 12.sp)
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(82.dp)
    ) {
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(20.dp),
            color = when {
                selected -> Color(0xFFB5A9D4).copy(alpha = 0.25f)
                isFocused -> Color(0xFF2C313F)
                else -> Color(0xFF0F111A)
            },
            border = androidx.compose.foundation.BorderStroke(
                width = if (selected || isFocused) 2.dp else 1.dp,
                color = when {
                    selected -> Color(0xFFB5A9D4)
                    isFocused -> Color.White.copy(alpha = 0.7f)
                    else -> Color.Gray.copy(alpha = 0.25f)
                }
            )
        ) {
            Box(
                modifier = Modifier.size(64.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(getEmojiForCategory(category.name, category.icon), fontSize = 30.sp)
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = category.name,
            color = if (isActive) Color.White else Color.Gray,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 12.sp
        )
    }
}

@Composable
private fun SelectableChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (selected) Color(0xFFB5A9D4).copy(alpha = 0.25f) else Color(0xFF2C313F),
        border = androidx.compose.foundation.BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) Color(0xFFB5A9D4) else Color.Gray.copy(alpha = 0.25f)
        ),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFFB5A9D4), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(text, color = Color.White, fontSize = 13.sp, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun AppliedFilterRow(
    text: String,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0F111A), RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text, color = Color.White, fontSize = 13.sp, modifier = Modifier.weight(1f))
        IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Quitar filtro", tint = Color.Gray, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun filterTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    focusedBorderColor = Color(0xFFB5A9D4),
    unfocusedBorderColor = Color.Gray,
    focusedLabelColor = Color(0xFFB5A9D4),
    cursorColor = Color(0xFFB5A9D4)
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
        includeSubcategories -> "Categoria: ${category?.name ?: "desconocida"} completa"
        parent != null -> "Subcategoria: ${parent.name} > ${category?.name ?: "desconocida"}"
        else -> "Categoria: ${category?.name ?: "desconocida"}"
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

private fun sanitizeAmountInput(value: String): String {
    return value.filter { it.isDigit() || it == '.' || it == ',' }.take(12)
}

private fun String.toAmountOrNull(): Double? {
    return replace(',', '.').toDoubleOrNull()
}
