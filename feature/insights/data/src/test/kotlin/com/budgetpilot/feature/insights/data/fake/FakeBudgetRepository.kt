package com.budgetpilot.feature.insights.data.fake

import com.budgetpilot.core.domain.DataError
import com.budgetpilot.core.domain.EmptyResult
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.model.Budget
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.core.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeBudgetRepository(
    private val budgets: List<Budget> = emptyList(),
    private val spendByCategoryAndMonth: Map<Pair<Long, String>, Money> = emptyMap(),
) : BudgetRepository {
    override fun observeBudgetsForMonth(month: String): Flow<List<Budget>> = flowOf(budgets.filter { it.month == month })

    override suspend fun getBudget(
        categoryId: Long,
        month: String,
    ): Result<Budget, DataError.Local> =
        budgets
            .find { it.categoryId == categoryId && it.month == month }
            ?.let { Result.Success(it) }
            ?: Result.Error(DataError.Local.NOT_FOUND)

    override suspend fun addBudget(budget: Budget): Result<Long, DataError.Local> = Result.Success(budget.id)

    override suspend fun updateBudget(budget: Budget): EmptyResult<DataError.Local> = Result.Success(Unit)

    override suspend fun deleteBudget(budget: Budget): EmptyResult<DataError.Local> = Result.Success(Unit)

    override suspend fun spentForCategoryInMonth(
        categoryId: Long,
        month: String,
    ): Result<Money, DataError.Local> = Result.Success(spendByCategoryAndMonth[categoryId to month] ?: Money.ZERO)
}
