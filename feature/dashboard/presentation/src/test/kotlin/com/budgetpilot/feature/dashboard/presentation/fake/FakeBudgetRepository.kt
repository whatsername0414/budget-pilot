package com.budgetpilot.feature.dashboard.presentation.fake

import com.budgetpilot.core.domain.DataError
import com.budgetpilot.core.domain.EmptyResult
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.model.Budget
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.core.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeBudgetRepository(
    seedBudgets: List<Budget> = emptyList(),
) : BudgetRepository {
    private val budgets = MutableStateFlow(seedBudgets)
    private var nextId = (seedBudgets.maxOfOrNull { it.id } ?: 0L) + 1

    override fun observeBudgetsForMonth(month: String): Flow<List<Budget>> = budgets.map { list -> list.filter { it.month == month } }

    override suspend fun getBudget(
        categoryId: Long,
        month: String,
    ): Result<Budget, DataError.Local> =
        budgets.value
            .find { it.categoryId == categoryId && it.month == month }
            ?.let { Result.Success(it) }
            ?: Result.Error(DataError.Local.NOT_FOUND)

    override suspend fun addBudget(budget: Budget): Result<Long, DataError.Local> {
        val id = nextId++
        budgets.update { it + budget.copy(id = id) }
        return Result.Success(id)
    }

    override suspend fun updateBudget(budget: Budget): EmptyResult<DataError.Local> {
        budgets.update { list -> list.map { if (it.id == budget.id) budget else it } }
        return Result.Success(Unit)
    }

    override suspend fun deleteBudget(budget: Budget): EmptyResult<DataError.Local> {
        budgets.update { list -> list.filterNot { it.id == budget.id } }
        return Result.Success(Unit)
    }

    override suspend fun spentForCategoryInMonth(
        categoryId: Long,
        month: String,
    ): Result<Money, DataError.Local> = Result.Success(Money.ZERO)
}
