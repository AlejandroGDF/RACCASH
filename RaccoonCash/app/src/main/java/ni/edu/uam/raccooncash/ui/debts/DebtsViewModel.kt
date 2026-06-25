package ni.edu.uam.raccooncash.ui.debts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ni.edu.uam.raccooncash.data.model.AccountResponse
import ni.edu.uam.raccooncash.data.model.DebtPaymentRequest
import ni.edu.uam.raccooncash.data.model.DebtPaymentResponse
import ni.edu.uam.raccooncash.data.model.DebtRequest
import ni.edu.uam.raccooncash.data.model.DebtResponse
import ni.edu.uam.raccooncash.data.repository.RaccoonRepository

class DebtsViewModel : ViewModel() {
    private val repository = RaccoonRepository()

    private val _debts = MutableStateFlow<List<DebtResponse>>(emptyList())
    val debts: StateFlow<List<DebtResponse>> = _debts.asStateFlow()

    private val _selectedDebt = MutableStateFlow<DebtResponse?>(null)
    val selectedDebt: StateFlow<DebtResponse?> = _selectedDebt.asStateFlow()

    private val _payments = MutableStateFlow<List<DebtPaymentResponse>>(emptyList())
    val payments: StateFlow<List<DebtPaymentResponse>> = _payments.asStateFlow()

    private val _accounts = MutableStateFlow<List<AccountResponse>>(emptyList())
    val accounts: StateFlow<List<AccountResponse>> = _accounts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _operationSuccess = MutableStateFlow(false)
    val operationSuccess: StateFlow<Boolean> = _operationSuccess.asStateFlow()

    init {
        loadDebts()
        loadAccounts()
    }

    fun loadAccounts() {
        viewModelScope.launch {
            try {
                _accounts.value = repository.getAccounts()
            } catch (e: Exception) {
                _error.value = "Error al cargar cuentas."
                e.printStackTrace()
            }
        }
    }

    fun loadDebts(
        type: String? = null,
        status: String? = null,
        accountId: Long? = null,
        overdue: Boolean? = null,
        search: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _debts.value = repository.getDebts(
                    type = type,
                    status = status,
                    accountId = accountId,
                    overdue = overdue,
                    search = search?.takeIf { it.isNotBlank() }
                )
            } catch (e: Exception) {
                _error.value = "Error al cargar deudas."
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadDebtDetails(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                _selectedDebt.value = repository.getDebtById(id)
                _payments.value = repository.getDebtPayments(id)
                _accounts.value = repository.getAccounts()
            } catch (e: Exception) {
                _error.value = "Error al cargar la deuda."
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createDebt(
        personName: String,
        description: String?,
        totalAmount: Double,
        type: String,
        dueDate: String?,
        accountId: Long,
        reminderEnabled: Boolean,
        reminderAt: String?
    ) {
        saveDebt(null, personName, description, totalAmount, type, dueDate, accountId, reminderEnabled, reminderAt)
    }

    fun updateDebt(
        id: Long,
        personName: String,
        description: String?,
        totalAmount: Double,
        type: String,
        dueDate: String?,
        accountId: Long,
        reminderEnabled: Boolean,
        reminderAt: String?
    ) {
        saveDebt(id, personName, description, totalAmount, type, dueDate, accountId, reminderEnabled, reminderAt)
    }

    private fun saveDebt(
        id: Long?,
        personName: String,
        description: String?,
        totalAmount: Double,
        type: String,
        dueDate: String?,
        accountId: Long,
        reminderEnabled: Boolean,
        reminderAt: String?
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _operationSuccess.value = false
            _error.value = null
            try {
                val request = DebtRequest(
                    personName = personName,
                    description = description,
                    totalAmount = totalAmount,
                    type = type,
                    dueDate = dueDate,
                    accountId = accountId,
                    reminderEnabled = reminderEnabled,
                    reminderAt = reminderAt
                )
                val savedDebt = if (id == null) {
                    repository.createDebt(request)
                } else {
                    repository.updateDebt(id, request)
                }
                _selectedDebt.value = savedDebt
                _operationSuccess.value = true
                loadDebts()
            } catch (e: Exception) {
                _error.value = "Error al guardar la deuda."
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteDebt(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _operationSuccess.value = false
            _error.value = null
            try {
                repository.deleteDebt(id)
                _selectedDebt.value = null
                _payments.value = emptyList()
                _operationSuccess.value = true
                loadDebts()
            } catch (e: Exception) {
                _error.value = "Error al eliminar la deuda."
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addPayment(
        debtId: Long,
        amount: Double,
        paymentDate: String?,
        accountId: Long,
        notes: String?,
        onCompleted: (() -> Unit)? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _operationSuccess.value = false
            _error.value = null
            try {
                repository.createDebtPayment(
                    debtId,
                    DebtPaymentRequest(
                        amount = amount,
                        paymentDate = paymentDate,
                        accountId = accountId,
                        notes = notes
                    )
                )
                _operationSuccess.value = true
                refreshAfterPayment(debtId)
                onCompleted?.invoke()
            } catch (e: retrofit2.HttpException) {
                _error.value = if (e.code() == 400) {
                    "No se pudo registrar el pago. Revisa saldo y monto pendiente."
                } else {
                    "Error del servidor al registrar el pago."
                }
                e.printStackTrace()
            } catch (e: Exception) {
                _error.value = "Error al registrar el pago."
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deletePayment(debtId: Long, paymentId: Long, onCompleted: (() -> Unit)? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _operationSuccess.value = false
            _error.value = null
            try {
                repository.deleteDebtPayment(debtId, paymentId)
                _operationSuccess.value = true
                refreshAfterPayment(debtId)
                onCompleted?.invoke()
            } catch (e: Exception) {
                _error.value = "Error al anular el pago."
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun refreshAfterPayment(debtId: Long) {
        _selectedDebt.value = repository.getDebtById(debtId)
        _payments.value = repository.getDebtPayments(debtId)
        _accounts.value = repository.getAccounts()
        _debts.value = repository.getDebts()
    }

    fun resetSuccess() {
        _operationSuccess.value = false
    }

    fun clearError() {
        _error.value = null
    }
}
