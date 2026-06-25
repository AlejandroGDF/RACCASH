package ni.edu.uam.raccooncash.data.repository

import ni.edu.uam.raccooncash.data.model.*
import ni.edu.uam.raccooncash.data.remote.RetrofitClient

class RaccoonRepository {
    private val apiService = RetrofitClient.apiService

    suspend fun getAccounts() = apiService.getAccounts()

    suspend fun createAccount(request: AccountRequest) = apiService.createAccount(request)

    suspend fun updateAccount(id: Long, request: AccountRequest) = apiService.updateAccount(id, request)

    suspend fun deleteAccount(id: Long) = apiService.deleteAccount(id)

    // Transactions
    suspend fun getTransactions() = apiService.getTransactions()

    suspend fun createTransaction(request: TransactionRequest) = apiService.createTransaction(request)

    suspend fun updateTransaction(id: Long, request: TransactionRequest) = apiService.updateTransaction(id, request)

    suspend fun deleteTransaction(id: Long) = apiService.deleteTransaction(id)

    // Debts
    suspend fun getDebts(
        type: String? = null,
        status: String? = null,
        accountId: Long? = null,
        dueFrom: String? = null,
        dueTo: String? = null,
        overdue: Boolean? = null,
        search: String? = null
    ) = apiService.getDebts(type, status, accountId, dueFrom, dueTo, overdue, search)

    suspend fun getDebtById(id: Long) = apiService.getDebtById(id)

    suspend fun createDebt(request: DebtRequest) = apiService.createDebt(request)

    suspend fun updateDebt(id: Long, request: DebtRequest) = apiService.updateDebt(id, request)

    suspend fun deleteDebt(id: Long) = apiService.deleteDebt(id)

    suspend fun getDebtPayments(id: Long) = apiService.getDebtPayments(id)

    suspend fun createDebtPayment(id: Long, request: DebtPaymentRequest) = apiService.createDebtPayment(id, request)

    suspend fun deleteDebtPayment(id: Long, paymentId: Long) = apiService.deleteDebtPayment(id, paymentId)

    // Categories
    suspend fun getCategories() = apiService.getCategories()

    suspend fun createCategory(request: ni.edu.uam.raccooncash.data.model.CategoryRequest) = apiService.createCategory(request)

    suspend fun updateCategory(id: Long, request: ni.edu.uam.raccooncash.data.model.CategoryRequest) = apiService.updateCategory(id, request)

    suspend fun deleteCategory(id: Long) = apiService.deleteCategory(id)

    // Saving Goals
    suspend fun getSavingGoals() = apiService.getSavingGoals()

    suspend fun createSavingGoal(request: SavingGoalRequest) = apiService.createSavingGoal(request)

    suspend fun updateSavingGoal(id: Long, request: SavingGoalRequest) = apiService.updateSavingGoal(id, request)

    suspend fun deleteSavingGoal(id: Long) = apiService.deleteSavingGoal(id)

    suspend fun getSavingGoalTransactions(id: Long) = apiService.getSavingGoalTransactions(id)

    // Budgets
    suspend fun getBudgets() = apiService.getBudgets()

    suspend fun getBudgetCategoryLimits(id: Long) = apiService.getBudgetCategoryLimits(id)

    suspend fun createBudgetCategoryLimit(id: Long, request: BudgetCategoryLimitRequest) = apiService.createBudgetCategoryLimit(id, request)

    suspend fun updateBudgetCategoryLimit(budgetId: Long, limitId: Long, request: BudgetCategoryLimitRequest) = apiService.updateBudgetCategoryLimit(budgetId, limitId, request)

    suspend fun deleteBudgetCategoryLimit(budgetId: Long, limitId: Long) = apiService.deleteBudgetCategoryLimit(budgetId, limitId)

    suspend fun createBudget(request: PresupuestoSolicitud) = apiService.createBudget(request)

    suspend fun updateBudget(id: Long, request: PresupuestoSolicitud) = apiService.updateBudget(id, request)

    suspend fun deleteBudget(id: Long) = apiService.deleteBudget(id)
}
