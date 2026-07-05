package com.budgetpilot.feature.expenses.presentation.fake

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
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class FakeExpenseRepository(
    seed: List<Expense> = emptyList(),
) : ExpenseRepository {
    private val expenses = MutableStateFlow(seed)
    private var nextId = (seed.maxOfOrNull { it.id } ?: 0L) + 1

    var shouldFailObserve: Boolean = false
    var deleteResult: EmptyResult<DataError.Local> = Result.Success(Unit)
    var addResult: ((Expense) -> Result<Long, DataError.Local>)? = null

    override fun observeExpenses(filter: ExpenseFilter): Flow<List<Expense>> =
        if (shouldFailObserve) {
            flow { throw IllegalStateException("Simulated load failure") }
        } else {
            expenses.map { all -> all.filter { it.matches(filter) } }
        }

    override suspend fun getExpenseById(id: Long): Result<Expense, DataError.Local> =
        expenses.value
            .find { it.id == id }
            ?.let { Result.Success(it) }
            ?: Result.Error(DataError.Local.NOT_FOUND)

    override suspend fun addExpense(expense: Expense): Result<Long, DataError.Local> {
        val override = addResult?.invoke(expense)
        if (override != null) return override

        val id = nextId++
        expenses.update { it + expense.copy(id = id) }
        return Result.Success(id)
    }

    override suspend fun updateExpense(expense: Expense): EmptyResult<DataError.Local> {
        expenses.update { list -> list.map { if (it.id == expense.id) expense else it } }
        return Result.Success(Unit)
    }

    override suspend fun deleteExpense(expense: Expense): EmptyResult<DataError.Local> {
        if (deleteResult is Result.Success) {
            expenses.update { list -> list.filterNot { it.id == expense.id } }
        }
        return deleteResult
    }

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
