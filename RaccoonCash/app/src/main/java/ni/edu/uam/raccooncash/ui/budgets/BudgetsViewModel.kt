package ni.edu.uam.raccooncash.ui.budgets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ni.edu.uam.raccooncash.data.model.BudgetCategoryLimitResponse
import ni.edu.uam.raccooncash.data.model.BudgetCategoryLimitRequest
import ni.edu.uam.raccooncash.data.model.TipoPeriodoPresupuesto
import ni.edu.uam.raccooncash.data.model.PresupuestoSolicitud
import ni.edu.uam.raccooncash.data.model.PresupuestoRespuesta
import ni.edu.uam.raccooncash.data.repository.RaccoonRepository

class BudgetsViewModel : ViewModel() {
    private val repository = RaccoonRepository()

    private val _budgets = MutableStateFlow<List<PresupuestoRespuesta>>(emptyList())
    val budgets: StateFlow<List<PresupuestoRespuesta>> = _budgets.asStateFlow()

    private val _currentBudgetCategoryLimits = MutableStateFlow<List<BudgetCategoryLimitResponse>>(emptyList())
    val currentBudgetCategoryLimits: StateFlow<List<BudgetCategoryLimitResponse>> = _currentBudgetCategoryLimits.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _operationSuccess = MutableStateFlow(false)
    val operationSuccess: StateFlow<Boolean> = _operationSuccess.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadBudgets()
    }

    fun loadBudgets() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                _budgets.value = repository.getBudgets()
            } catch (e: Exception) {
                _error.value = "Error al cargar presupuestos: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadBudgetCategoryLimits(budgetId: Long) {
        viewModelScope.launch {
            try {
                _currentBudgetCategoryLimits.value = repository.getBudgetCategoryLimits(budgetId)
            } catch (e: Exception) {
                _currentBudgetCategoryLimits.value = emptyList()
                _error.value = "Error al cargar categorías del presupuesto: ${e.message}"
            }
        }
    }

    fun clearBudgetCategoryLimits() {
        _currentBudgetCategoryLimits.value = emptyList()
    }

    fun createBudget(
        nombre: String,
        monto: Double,
        tipoPeriodo: TipoPeriodoPresupuesto,
        valorPeriodo: Int,
        fechaInicio: String,
        color: String,
        esGasto: Boolean,
        incluirTodasLasTransacciones: Boolean,
        categoryId: Long? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = PresupuestoSolicitud(
                    nombre = nombre,
                    monto = monto,
                    tipoPeriodo = tipoPeriodo,
                    valorPeriodo = valorPeriodo,
                    fechaInicio = fechaInicio,
                    color = color,
                    esGasto = esGasto,
                    incluirTodasLasTransacciones = if (esGasto && categoryId != null) false else incluirTodasLasTransacciones
                )
                val budget = repository.createBudget(request)
                syncBudgetCategoryLimit(budget.id, if (esGasto) categoryId else null, monto)
                _operationSuccess.value = true
                loadBudgets()
            } catch (e: Exception) {
                _error.value = "Error al crear presupuesto: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateBudget(
        id: Long,
        nombre: String,
        monto: Double,
        tipoPeriodo: TipoPeriodoPresupuesto,
        valorPeriodo: Int,
        fechaInicio: String,
        color: String,
        esGasto: Boolean,
        incluirTodasLasTransacciones: Boolean,
        categoryId: Long? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val request = PresupuestoSolicitud(
                    nombre = nombre,
                    monto = monto,
                    tipoPeriodo = tipoPeriodo,
                    valorPeriodo = valorPeriodo,
                    fechaInicio = fechaInicio,
                    color = color,
                    esGasto = esGasto,
                    incluirTodasLasTransacciones = if (esGasto && categoryId != null) false else incluirTodasLasTransacciones
                )
                repository.updateBudget(id, request)
                syncBudgetCategoryLimit(id, if (esGasto) categoryId else null, monto)
                _operationSuccess.value = true
                loadBudgets()
            } catch (e: Exception) {
                _error.value = "Error al actualizar presupuesto: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteBudget(id: Long) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = repository.deleteBudget(id)
                if (response.isSuccessful) {
                    _operationSuccess.value = true
                    loadBudgets()
                } else {
                    _error.value = "Error al eliminar: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Error al eliminar presupuesto: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun syncBudgetCategoryLimit(budgetId: Long, categoryId: Long?, amountLimit: Double) {
        val currentLimits = repository.getBudgetCategoryLimits(budgetId)

        if (categoryId == null) {
            currentLimits.forEach { repository.deleteBudgetCategoryLimit(budgetId, it.id) }
            _currentBudgetCategoryLimits.value = emptyList()
            return
        }

        val request = BudgetCategoryLimitRequest(
            categoryId = categoryId,
            amountLimit = amountLimit
        )
        val firstLimit = currentLimits.firstOrNull()

        if (firstLimit == null) {
            repository.createBudgetCategoryLimit(budgetId, request)
        } else {
            repository.updateBudgetCategoryLimit(budgetId, firstLimit.id, request)
            currentLimits.drop(1).forEach { repository.deleteBudgetCategoryLimit(budgetId, it.id) }
        }

        _currentBudgetCategoryLimits.value = repository.getBudgetCategoryLimits(budgetId)
    }

    fun resetSuccess() {
        _operationSuccess.value = false
    }
}
