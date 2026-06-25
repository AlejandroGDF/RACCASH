package ni.edu.uam.raccooncash.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ni.edu.uam.raccooncash.data.model.*
import ni.edu.uam.raccooncash.data.repository.RaccoonRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class TransactionsViewModel : ViewModel() {
    private val repository = RaccoonRepository()

    private val _accounts = MutableStateFlow<List<AccountResponse>>(emptyList())
    val accounts: StateFlow<List<AccountResponse>> = _accounts.asStateFlow()

    private val _categories = MutableStateFlow<List<CategoryResponse>>(emptyList())
    val categories: StateFlow<List<CategoryResponse>> = _categories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _addTransactionSuccess = MutableStateFlow(false)
    val addTransactionSuccess: StateFlow<Boolean> = _addTransactionSuccess.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            // Cargar cuentas
            try {
                _accounts.value = repository.getAccounts()
            } catch (e: Exception) {
                _error.value = "Error al cargar cuentas."
                e.printStackTrace()
            }

            // Cargar categorías (independiente para que un error 500 aquí no bloquee todo)
            try {
                _categories.value = repository.getCategories()
            } catch (e: Exception) {
                // Solo mostramos error si las cuentas cargaron bien, para no sobreescribir
                if (_error.value == null) {
                    _error.value = "Aviso: Error al cargar categorías (Revisar Backend)."
                }
                e.printStackTrace()
            }

            _isLoading.value = false
        }
    }

    fun createTransaction(
        amount: Double,
        type: String,
        accountId: Long,
        toAccountId: Long? = null,
        categoryId: Long? = null,
        description: String,
        notes: String? = null,
        dateTime: LocalDateTime = LocalDateTime.now()
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _addTransactionSuccess.value = false
            try {
                val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                val formattedDate = dateTime.format(formatter)

                val request = TransactionRequest(
                    amount = amount,
                    type = type,
                    accountId = accountId,
                    toAccountId = toAccountId,
                    categoryId = categoryId,
                    description = description,
                    notes = notes,
                    date = formattedDate
                )
                repository.createTransaction(request)
                
                // Actualizar las cuentas inmediatamente después de la transacción
                _accounts.value = repository.getAccounts()

                _addTransactionSuccess.value = true
            } catch (e: retrofit2.HttpException) {
                // ... (handling as before)
                _error.value = "Error del servidor: ${e.code()}"
                e.printStackTrace()
            } catch (e: Exception) {
                _error.value = "Error al crear la transacción. Revisa tu conexión."
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateTransaction(
        id: Long,
        amount: Double,
        type: String,
        accountId: Long,
        toAccountId: Long? = null,
        categoryId: Long? = null,
        description: String,
        notes: String? = null,
        dateTime: LocalDateTime = LocalDateTime.now()
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _addTransactionSuccess.value = false
            try {
                val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
                val formattedDate = dateTime.format(formatter)

                val request = TransactionRequest(
                    amount = amount,
                    type = type,
                    accountId = accountId,
                    toAccountId = toAccountId,
                    categoryId = categoryId,
                    description = description,
                    notes = notes,
                    date = formattedDate
                )
                repository.updateTransaction(id, request)
                
                // Actualizar las cuentas inmediatamente después de la edición
                _accounts.value = repository.getAccounts()

                _addTransactionSuccess.value = true
            } catch (e: Exception) {
                _error.value = "Error al actualizar la transacción."
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteTransaction(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteTransaction(id)
                
                // Actualizar las cuentas inmediatamente después de eliminar
                _accounts.value = repository.getAccounts()

                _addTransactionSuccess.value = true
            } catch (e: Exception) {
                _error.value = "Error al eliminar la transacción."
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun createCategory(name: String, type: String, emoji: String, parentId: Long? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = CategoryRequest(
                    name = name,
                    type = type,
                    icon = emoji,
                    color = "#7E57C2", // Default color
                    parentCategoryId = parentId
                )
                repository.createCategory(request)
                loadInitialData() // Reload categories
            } catch (e: Exception) {
                _error.value = "Error al crear la categoría."
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateCategory(id: Long, name: String, type: String, emoji: String, color: String = "#7E57C2", parentId: Long? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = CategoryRequest(
                    name = name,
                    type = type,
                    icon = emoji,
                    color = color,
                    parentCategoryId = parentId
                )
                repository.updateCategory(id, request)
                loadInitialData()
            } catch (e: Exception) {
                _error.value = "Error al actualizar la categoría."
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                repository.deleteCategory(id)
                loadInitialData() // Reload categories
            } catch (e: Exception) {
                _error.value = "Error al eliminar la categoría."
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetSuccess() {
        _addTransactionSuccess.value = false
    }
}
