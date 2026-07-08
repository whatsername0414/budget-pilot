package com.budgetpilot.feature.settings.presentation.fake

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
    seedExpenses: List<Expense> = emptyList(),
    private val failWith: DataError.Local? = null,
) : ExpenseRepository {
    private val expenses = seedExpenses.toMutableList()
    private var nextId = (seedExpenses.maxOfOrNull { it.id } ?: 0L) + 1

    val allExpenses: List<Expense> get() = expenses.toList()

    override fun observeExpenses(filter: ExpenseFilter): Flow<List<Expense>> =
        flowOf(
            expenses.filter {
                it.date >= filter.startDate &&
                    it.date <= filter.endDate &&
                    (filter.categoryId == null || it.categoryId == filter.categoryId) &&
                    (filter.merchant == null || it.merchant == filter.merchant)
            },
        )

    override suspend fun getExpenseById(id: Long): Result<Expense, DataError.Local> =
        expenses
            .find { it.id == id }
            ?.let { Result.Success(it) }
            ?: Result.Error(DataError.Local.NOT_FOUND)

    override suspend fun addExpense(expense: Expense): Result<Long, DataError.Local> {
        if (failWith != null) return Result.Error(failWith)
        val id = nextId++
        expenses.add(expense.copy(id = id))
        return Result.Success(id)
    }

    override suspend fun updateExpense(expense: Expense): EmptyResult<DataError.Local> {
        val index = expenses.indexOfFirst { it.id == expense.id }
        if (index < 0) return Result.Error(DataError.Local.NOT_FOUND)
        expenses[index] = expense
        return Result.Success(Unit)
    }

    override suspend fun deleteExpense(expense: Expense): EmptyResult<DataError.Local> {
        if (failWith != null) return Result.Error(failWith)
        expenses.removeAll { it.id == expense.id }
        return Result.Success(Unit)
    }

    override suspend fun sumByCategory(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Result<List<CategoryTotal>, DataError.Local> = Result.Success(emptyList())

    override suspend fun sumByMonth(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Result<List<MonthTotal>, DataError.Local> = Result.Success(emptyList())
}
