package ni.edu.uam.raccooncash.data.model

data class AccountResponse(
    val id: Long,
    val name: String,
    val type: String,
    val initialBalance: Double,
    val currentBalance: Double,
    val currency: String,
    val color: String?,
    val decimalPrecision: Int?,
    val active: Boolean,
    val createdAt: String?,
    val updatedAt: String?
)
