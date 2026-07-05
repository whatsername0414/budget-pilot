package com.budgetpilot.core.database.repository

import com.budgetpilot.core.database.dao.BudgetDao
import com.budgetpilot.core.database.mapper.toBudget
import com.budgetpilot.core.database.mapper.toEntity
import com.budgetpilot.core.domain.DataError
import com.budgetpilot.core.domain.EmptyResult
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.model.Budget
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.core.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomBudgetRepository(
    private val dao: BudgetDao,
) : BudgetRepository {
    override fun observeBudgetsForMonth(month: String): Flow<List<Budget>> =
        dao.observeBudgetsForMonth(month).map { entities -> entities.map { it.toBudget() } }

    override suspend fun getBudget(
        categoryId: Long,
        month: String,
    ): Result<Budget, DataError.Local> =
        runLocalCatching {
            val entity = dao.getBudget(categoryId, month)
            if (entity != null) Result.Success(entity.toBudget()) else Result.Error(DataError.Local.NOT_FOUND)
        }

    override suspend fun addBudget(budget: Budget): Result<Long, DataError.Local> =
        runLocalCatching { Result.Success(dao.insert(budget.toEntity())) }

    override suspend fun updateBudget(budget: Budget): EmptyResult<DataError.Local> =
        runLocalCatching {
            dao.update(budget.toEntity())
            Result.Success(Unit)
        }

    override suspend fun deleteBudget(budget: Budget): EmptyResult<DataError.Local> =
        runLocalCatching {
            dao.delete(budget.toEntity())
            Result.Success(Unit)
        }

    override suspend fun spentForCategoryInMonth(
        categoryId: Long,
        month: String,
    ): Result<Money, DataError.Local> =
        runLocalCatching {
            Result.Success(Money.ofCentavos(dao.spentForCategoryInMonth(categoryId, month)))
        }
}
