package ni.edu.uam.raccooncash.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ni.edu.uam.raccooncash.data.model.AccountRequest
import ni.edu.uam.raccooncash.data.model.AccountResponse
import ni.edu.uam.raccooncash.data.model.CategoryResponse
import ni.edu.uam.raccooncash.data.model.TransactionResponse
import ni.edu.uam.raccooncash.data.repository.RaccoonRepository

class AccountsViewModel : ViewModel() {
    private val repository = RaccoonRepository()

    private val _accounts = MutableStateFlow<List<AccountResponse>>(emptyList())
    val accounts: StateFlow<List<AccountResponse>> = _accounts.asStateFlow()

    private val _transactions = MutableStateFlow<List<TransactionResponse>>(emptyList())
    val transactions: StateFlow<List<TransactionResponse>> = _transactions.asStateFlow()

    private val _categories = MutableStateFlow<List<CategoryResponse>>(emptyList())
    val categories: StateFlow<List<CategoryResponse>> = _categories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _addAccountSuccess = MutableStateFlow(false)
    val addAccountSuccess: StateFlow<Boolean> = _addAccountSuccess.asStateFlow()

    init {
        loadAccounts()
    }

    fun loadAccounts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            
            // Cargar cuentas
            try {
                _accounts.value = repository.getAccounts()
            } catch (e: Exception) {
                _error.value = "Error al conectar (${e.message ?: "Sin mensaje"}). IP: 10.196.117.24"
                e.printStackTrace()
            }

            // Cargar transacciones (Independiente para que un error 500 aquí no bloquee las cuentas)
            try {
                _transactions.value = repository.getTransactions()
            } catch (e: Exception) {
                // No bloqueamos la UI principal, pero registramos el error
                e.printStackTrace()
            }

            // Cargar categorías
            try {
                _categories.value = repository.getCategories()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createAccount(name: String, balance: Double, currency: String, color: String, precision: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _addAccountSuccess.value = false
            try {
                val request = AccountRequest(
                    name = name,
                    type = "BANK", // Defaulting to BANK for now as per logic
                    initialBalance = balance,
                    currency = currency,
                    color = color,
                    decimalPrecision = precision
                )
                repository.createAccount(request)
                _addAccountSuccess.value = true
                loadAccounts()
            } catch (e: Exception) {
                _error.value = "Error al crear la cuenta."
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateAccount(id: Long, name: String, balance: Double, currency: String, color: String, precision: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _addAccountSuccess.value = false
            try {
                val request = AccountRequest(
                    name = name,
                    type = "BANK",
                    initialBalance = balance,
                    currency = currency,
                    color = color,
                    decimalPrecision = precision
                )
                repository.updateAccount(id, request)
                _addAccountSuccess.value = true
                loadAccounts()
            } catch (e: Exception) {
                _error.value = "Error al actualizar la cuenta."
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetSuccess() {
        _addAccountSuccess.value = false
    }

    fun deleteAccount(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Primero intentamos borrar las transacciones asociadas localmente para agilizar la UI
                // aunque el backend debería manejar la cascada si es posible.
                // Si el backend no tiene cascada, borramos las transacciones una a una.
                val accountTransactions = _transactions.value.filter { it.accountId == id || it.toAccountId == id }
                accountTransactions.forEach {
                    try {
                        repository.deleteTransaction(it.id)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                repository.deleteAccount(id)
                loadAccounts()
            } catch (e: Exception) {
                _error.value = "Error al eliminar la cuenta."
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
