package ni.edu.uam.raccooncash.data.model

data class BudgetCategoryLimitRequest(
    val categoryId: Long,
    val amountLimit: Double
)
