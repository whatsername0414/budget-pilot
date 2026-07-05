package com.budgetpilot.core.database.repository

import com.budgetpilot.core.database.dao.ExpenseDao
import com.budgetpilot.core.database.mapper.toEntity
import com.budgetpilot.core.database.mapper.toExpense
import com.budgetpilot.core.domain.DataError
import com.budgetpilot.core.domain.EmptyResult
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.model.CategoryTotal
import com.budgetpilot.core.domain.model.Expense
import com.budgetpilot.core.domain.model.ExpenseFilter
import com.budgetpilot.core.domain.model.MonthTotal
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.core.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class RoomExpenseRepository(
    private val dao: ExpenseDao,
) : ExpenseRepository {
    override fun observeExpenses(filter: ExpenseFilter): Flow<List<Expense>> =
        dao
            .observeExpenses(
                categoryId = filter.categoryId,
                merchant = filter.merchant,
                startDate = filter.startDate,
                endDate = filter.endDate,
            ).map { entities -> entities.map { it.toExpense() } }

    override suspend fun getExpenseById(id: Long): Result<Expense, DataError.Local> =
        runLocalCatching {
            val entity = dao.getExpenseById(id)
            if (entity != null) Result.Success(entity.toExpense()) else Result.Error(DataError.Local.NOT_FOUND)
        }

    override suspend fun addExpense(expense: Expense): Result<Long, DataError.Local> =
        runLocalCatching { Result.Success(dao.insert(expense.toEntity())) }

    override suspend fun updateExpense(expense: Expense): EmptyResult<DataError.Local> =
        runLocalCatching {
            dao.update(expense.toEntity())
            Result.Success(Unit)
        }

    override suspend fun deleteExpense(expense: Expense): EmptyResult<DataError.Local> =
        runLocalCatching {
            dao.delete(expense.toEntity())
            Result.Success(Unit)
        }

    override suspend fun sumByCategory(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Result<List<CategoryTotal>, DataError.Local> =
        runLocalCatching {
            val totals =
                dao.sumByCategory(startDate, endDate).map {
                    CategoryTotal(categoryId = it.categoryId, total = Money.ofCentavos(it.totalCentavos))
                }
            Result.Success(totals)
        }

    override suspend fun sumByMonth(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Result<List<MonthTotal>, DataError.Local> =
        runLocalCatching {
            val totals =
                dao.sumByMonth(startDate, endDate).map {
                    MonthTotal(month = it.month, total = Money.ofCentavos(it.totalCentavos))
                }
            Result.Success(totals)
        }
}
