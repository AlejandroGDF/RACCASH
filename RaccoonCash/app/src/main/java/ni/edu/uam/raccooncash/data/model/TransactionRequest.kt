package ni.edu.uam.raccooncash.data.model

data class TransactionRequest(
    val amount: Double,
    val type: String, // INCOME, EXPENSE, TRANSFER
    val accountId: Long,
    val toAccountId: Long? = null,
    val categoryId: Long? = null,
    val description: String,
    val notes: String? = null,
    val date: String,
    val savingGoalId: Long? = null
)
