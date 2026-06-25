package ni.edu.uam.raccooncash.data.model

data class TransactionResponse(
    val id: Long,
    val amount: Double,
    val type: String, // INCOME, EXPENSE, TRANSFER
    val description: String,
    val notes: String?,
    val date: String,
    
    // IDs y Nombres directos (Basado en la respuesta plana detectada en logs)
    val accountId: Long,
    val accountName: String?,
    val destinationAccountId: Long?,
    val destinationAccountName: String?,
    val toAccountId: Long?,
    val toAccountName: String?,
    val categoryId: Long?,
    val categoryName: String?,
    val savingGoalId: Long? = null,
    
    // Mantener los objetos anidados por si el GET los devuelve así (u opcionales)
    val account: AccountResponse? = null,
    val toAccount: AccountResponse? = null,
    val category: CategoryResponse? = null,
    
    val active: Boolean,
    val generatedByDebtPayment: Boolean = false,
    val createdAt: String?,
    val updatedAt: String?
)
