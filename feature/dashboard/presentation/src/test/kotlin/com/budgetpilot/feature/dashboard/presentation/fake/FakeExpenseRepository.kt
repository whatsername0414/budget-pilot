package com.budgetpilot.feature.dashboard.presentation.fake

import com.budgetpilot.core.domain.DataError
import com.budgetpilot.core.domain.EmptyResult
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.model.CategoryTotal
import com.budgetpilot.core.domain.model.Expense
import com.budgetpilot.core.domain.model.ExpenseFilter
import com.budgetpilot.core.domain.model.MonthTotal
import com.budgetpilot.core.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class FakeExpenseRepository(
    seedExpenses: List<Expense> = emptyList(),
) : ExpenseRepository {
    private val expenses = MutableStateFlow(seedExpenses)
    private var nextId = (seedExpenses.maxOfOrNull { it.id } ?: 0L) + 1

    override fun observeExpenses(filter: ExpenseFilter): Flow<List<Expense>> =
        expenses.map { list ->
            val merchant = filter.merchant
            list.filter { expense ->
                expense.date in filter.startDate..filter.endDate &&
                    (filter.categoryId == null || expense.categoryId == filter.categoryId) &&
                    (merchant == null || expense.merchant.contains(merchant, ignoreCase = true))
            }
        }

    override suspend fun getExpenseById(id: Long): Result<Expense, DataError.Local> =
        expenses.value
            .find { it.id == id }
            ?.let { Result.Success(it) }
            ?: Result.Error(DataError.Local.NOT_FOUND)

    override suspend fun addExpense(expense: Expense): Result<Long, DataError.Local> {
        val id = nextId++
        expenses.update { it + expense.copy(id = id) }
        return Result.Success(id)
    }

    override suspend fun updateExpense(expense: Expense): EmptyResult<DataError.Local> {
        expenses.update { list -> list.map { if (it.id == expense.id) expense else it } }
        return Result.Success(Unit)
    }

    override suspend fun deleteExpense(expense: Expense): EmptyResult<DataError.Local> {
        expenses.update { list -> list.filterNot { it.id == expense.id } }
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
