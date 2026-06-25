package ni.edu.uam.raccooncash.data.model

data class BudgetCategoryLimitResponse(
    val id: Long,
    val budgetId: Long,
    val budgetName: String?,
    val categoryId: Long,
    val categoryName: String?,
    val amountLimit: Double
)
