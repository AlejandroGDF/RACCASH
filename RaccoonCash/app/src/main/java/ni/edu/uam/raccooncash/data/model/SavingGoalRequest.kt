package ni.edu.uam.raccooncash.data.model

data class SavingGoalRequest(
    val name: String,
    val targetAmount: Double,
    val deadline: String,
    val color: String,
    val icon: String? = null,
    val currency: String = "C$"
)
