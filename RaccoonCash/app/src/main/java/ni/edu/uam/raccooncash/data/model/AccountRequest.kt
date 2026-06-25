package ni.edu.uam.raccooncash.data.model

data class AccountRequest(
    val name: String,
    val type: String,
    val initialBalance: Double,
    val currency: String,
    val color: String?,
    val decimalPrecision: Int = 2
)
