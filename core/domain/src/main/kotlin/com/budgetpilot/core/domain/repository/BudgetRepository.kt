package com.budgetpilot.core.domain.repository

import com.budgetpilot.core.domain.DataError
import com.budgetpilot.core.domain.EmptyResult
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.model.Budget
import com.budgetpilot.core.domain.money.Money
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun observeBudgetsForMonth(month: String): Flow<List<Budget>>

    suspend fun getBudget(
        categoryId: Long,
        month: String,
    ): Result<Budget, DataError.Local>

    suspend fun addBudget(budget: Budget): Result<Long, DataError.Local>

    suspend fun updateBudget(budget: Budget): EmptyResult<DataError.Local>

    suspend fun deleteBudget(budget: Budget): EmptyResult<DataError.Local>

    suspend fun spentForCategoryInMonth(
        categoryId: Long,
        month: String,
    ): Result<Money, DataError.Local>
}
