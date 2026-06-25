package ni.edu.uam.raccooncash.data.model

data class DebtRequest(
    val personName: String,
    val description: String?,
    val totalAmount: Double,
    val type: String,
    val dueDate: String?,
    val accountId: Long,
    val reminderEnabled: Boolean,
    val reminderAt: String?
)

data class DebtResponse(
    val id: Long,
    val personName: String,
    val description: String?,
    val totalAmount: Double,
    val paidAmount: Double,
    val remainingAmount: Double,
    val type: String,
    val status: String,
    val dueDate: String?,
    val overdue: Boolean,
    val accountId: Long,
    val accountName: String?,
    val reminderEnabled: Boolean,
    val reminderAt: String?,
    val active: Boolean,
    val createdAt: String?,
    val updatedAt: String?
)

data class DebtPaymentRequest(
    val amount: Double,
    val paymentDate: String?,
    val accountId: Long,
    val notes: String?
)

data class DebtPaymentResponse(
    val id: Long,
    val debtId: Long,
    val accountId: Long?,
    val accountName: String?,
    val transactionId: Long?,
    val amount: Double,
    val paymentDate: String?,
    val notes: String?,
    val active: Boolean,
    val createdAt: String?
)
