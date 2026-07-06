package com.budgetpilot.core.ai.domain.fake

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

class FakeExpenseRepository(
    private val expenses: List<Expense> = emptyList(),
) : ExpenseRepository {
    override fun observeExpenses(filter: ExpenseFilter): Flow<List<Expense>> = flowOf(expenses.filter { it.matches(filter) })

    override suspend fun getExpenseById(id: Long): Result<Expense, DataError.Local> =
        expenses
            .find { it.id == id }
            ?.let { Result.Success(it) }
            ?: Result.Error(DataError.Local.NOT_FOUND)

    override suspend fun addExpense(expense: Expense): Result<Long, DataError.Local> = Result.Success(expense.id)

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

    private fun Expense.matches(filter: ExpenseFilter): Boolean {
        val merchantQuery = filter.merchant
        val inRange = date >= filter.startDate && date <= filter.endDate
        val categoryMatches = filter.categoryId == null || categoryId == filter.categoryId
        val merchantMatches = merchantQuery == null || merchant.contains(merchantQuery, ignoreCase = true)
        return inRange && categoryMatches && merchantMatches
    }
}
