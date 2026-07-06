package com.budgetpilot.feature.capture.presentation.fake

import com.budgetpilot.core.domain.DataError
import com.budgetpilot.core.domain.EmptyResult
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.model.CategoryTotal
import com.budgetpilot.core.domain.model.Expense
import com.budgetpilot.core.domain.model.ExpenseFilter
import com.budgetpilot.core.domain.model.MonthTotal
import com.budgetpilot.core.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate

class FakeExpenseRepository : ExpenseRepository {
    val addedExpenses = mutableListOf<Expense>()
    var addResult: Result<Long, DataError.Local>? = null

    override fun observeExpenses(filter: ExpenseFilter): Flow<List<Expense>> = flowOf(emptyList())

    override suspend fun getExpenseById(id: Long): Result<Expense, DataError.Local> = Result.Error(DataError.Local.NOT_FOUND)

    override suspend fun addExpense(expense: Expense): Result<Long, DataError.Local> {
        val override = addResult
        if (override != null) return override
        addedExpenses += expense
        return Result.Success(addedExpenses.size.toLong())
    }

    override suspend fun updateExpense(expense: Expense): EmptyResult<DataError.Local> = Result.Success(Unit)

    override suspend fun deleteExpense(expense: Expense): EmptyResult<DataError.Local> = Result.Success(Unit)

    override suspend fun sumByCategory(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Result<List<CategoryTotal>, DataError.Local> = Result.Success(emptyList())

    override suspend fun sumByMonth(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Result<List<MonthTotal>, DataError.Local> = Result.Success(emptyList())
}
