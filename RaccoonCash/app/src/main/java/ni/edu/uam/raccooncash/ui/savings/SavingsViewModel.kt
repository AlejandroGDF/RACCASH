package ni.edu.uam.raccooncash.ui.savings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ni.edu.uam.raccooncash.data.model.SavingGoalRequest
import ni.edu.uam.raccooncash.data.model.SavingGoalResponse
import ni.edu.uam.raccooncash.data.model.TransactionRequest
import ni.edu.uam.raccooncash.data.model.TransactionResponse
import ni.edu.uam.raccooncash.data.repository.RaccoonRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class SavingsViewModel : ViewModel() {
    private val repository = RaccoonRepository()

    private val _savingGoals = MutableStateFlow<List<SavingGoalResponse>>(emptyList())
    val savingGoals: StateFlow<List<SavingGoalResponse>> = _savingGoals.asStateFlow()

    private val _currentGoalTransactions = MutableStateFlow<List<TransactionResponse>>(emptyList())
    val currentGoalTransactions: StateFlow<List<TransactionResponse>> = _currentGoalTransactions.asStateFlow()

    private val _addGoalSuccess = MutableStateFlow(false)
    val addGoalSuccess: StateFlow<Boolean> = _addGoalSuccess.asStateFlow()
    
    private val _addTransactionSuccess = MutableStateFlow(false)
    val addTransactionSuccess: StateFlow<Boolean> = _addTransactionSuccess.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadSavingGoals()
    }

    fun loadSavingGoals() {
        viewModelScope.launch {
            loadSavingGoalsSuspend()
        }
    }

    private suspend fun loadSavingGoalsSuspend() {
        _isLoading.value = true
        try {
            _savingGoals.value = repository.getSavingGoals()
            _error.value = null
        } catch (e: Exception) {
            _error.value = "Error al cargar metas: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun loadGoalTransactions(goalId: Long) {
        viewModelScope.launch {
            loadGoalTransactionsSuspend(goalId)
        }
    }

    private suspend fun loadGoalTransactionsSuspend(goalId: Long) {
        _isLoading.value = true
        try {
            _currentGoalTransactions.value = repository.getSavingGoalTransactions(goalId)
            _error.value = null
        } catch (e: Exception) {
            _error.value = "Error al cargar transacciones: ${e.message}"
        } finally {
            _isLoading.value = false
        }
    }

    fun addSavingGoal(name: String, targetAmount: Double, deadline: String, color: String, icon: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = SavingGoalRequest(
                    name = name,
                    targetAmount = targetAmount,
                    deadline = deadline,
                    color = color,
                    icon = icon
                )
                repository.createSavingGoal(request)
                _addGoalSuccess.value = true
                loadSavingGoalsSuspend()
            } catch (e: Exception) {
                _error.value = "Error al crear meta: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateSavingGoal(id: Long, name: String, targetAmount: Double, deadline: String, color: String, icon: String? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            _addGoalSuccess.value = false // Reset before start
            try {
                val request = SavingGoalRequest(
                    name = name,
                    targetAmount = targetAmount,
                    deadline = deadline,
                    color = color,
                    icon = icon
                )
                repository.updateSavingGoal(id, request)
                loadSavingGoalsSuspend()
                _addGoalSuccess.value = true // Trigger navigation
            } catch (e: Exception) {
                _error.value = "Error al actualizar meta: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteSavingGoal(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteSavingGoal(id)
                _addGoalSuccess.value = true
                loadSavingGoalsSuspend()
            } catch (e: Exception) {
                _error.value = "Error al eliminar meta: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun addTransactionToGoal(
        goalId: Long,
        accountId: Long,
        amount: Double,
        description: String,
        notes: String?,
        dateTime: LocalDateTime
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _addTransactionSuccess.value = false
            _error.value = null
            try {
                val request = TransactionRequest(
                    amount = amount,
                    type = "TRANSFER", // Changed from INCOME to TRANSFER
                    accountId = accountId,
                    description = description,
                    notes = notes,
                    date = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    savingGoalId = goalId
                )
                repository.createTransaction(request)
                loadGoalTransactionsSuspend(goalId)
                loadSavingGoalsSuspend()
                _addTransactionSuccess.value = true
            } catch (e: Exception) {
                _error.value = "Error al registrar ahorro: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateGoalTransaction(
        transactionId: Long,
        goalId: Long,
        accountId: Long,
        amount: Double,
        description: String,
        notes: String?,
        dateTime: LocalDateTime
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _addTransactionSuccess.value = false
            _error.value = null
            try {
                val request = TransactionRequest(
                    amount = amount,
                    type = "TRANSFER", // Changed from INCOME to TRANSFER
                    accountId = accountId,
                    description = description,
                    notes = notes,
                    date = dateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    savingGoalId = goalId
                )
                repository.updateTransaction(transactionId, request)
                loadGoalTransactionsSuspend(goalId)
                loadSavingGoalsSuspend()
                _addTransactionSuccess.value = true
            } catch (e: Exception) {
                _error.value = "Error al actualizar ahorro: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTransaction(transactionId: Long, goalId: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            _addTransactionSuccess.value = false
            _error.value = null
            try {
                repository.deleteTransaction(transactionId)
                // Clear the current list immediately to give instant feedback
                _currentGoalTransactions.value = _currentGoalTransactions.value.filter { it.id != transactionId }
                
                // Then fetch from server to be sure
                loadGoalTransactionsSuspend(goalId)
                loadSavingGoalsSuspend()
                _addTransactionSuccess.value = true
            } catch (e: Exception) {
                _error.value = "Error al eliminar transacción: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun resetSuccess() {
        _addGoalSuccess.value = false
        _addTransactionSuccess.value = false
    }
}
