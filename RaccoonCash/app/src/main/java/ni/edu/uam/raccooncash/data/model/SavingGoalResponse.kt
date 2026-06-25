package ni.edu.uam.raccooncash.data.model

data class SavingGoalResponse(
    val id: Long,
    val name: String,
    val targetAmount: Double,
    val currentAmount: Double,
    val deadline: String,
    val color: String,
    val icon: String?,
    val currency: String,
    val transactionCount: Int? = 0
)
