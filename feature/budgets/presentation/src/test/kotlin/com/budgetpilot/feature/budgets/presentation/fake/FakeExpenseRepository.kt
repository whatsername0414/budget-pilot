package com.budgetpilot.feature.budgets.presentation.fake

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
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import java.time.YearMonth

class FakeExpenseRepository(
    seedExpenses: List<Expense> = emptyList(),
) : ExpenseRepository {
    private val expenses = seedExpenses.toMutableList()
    private var nextId = (seedExpenses.maxOfOrNull { it.id } ?: 0L) + 1

    var shouldFailSumByCategory: Boolean = false
    var shouldFailSumByMonth: Boolean = false

    override fun observeExpenses(filter: ExpenseFilter): Flow<List<Expense>> = flowOf(expenses.toList())

    override suspend fun getExpenseById(id: Long): Result<Expense, DataError.Local> =
        expenses
            .find { it.id == id }
            ?.let { Result.Success(it) }
            ?: Result.Error(DataError.Local.NOT_FOUND)

    override suspend fun addExpense(expense: Expense): Result<Long, DataError.Local> {
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
        expenses.removeAll { it.id == expense.id }
        return Result.Success(Unit)
    }

    override suspend fun sumByCategory(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Result<List<CategoryTotal>, DataError.Local> {
        if (shouldFailSumByCategory) return Result.Error(DataError.Local.UNKNOWN)
        val totals =
            expenses
                .filter { it.date in startDate..endDate }
                .groupBy { it.categoryId }
                .map { (categoryId, group) -> CategoryTotal(categoryId, group.fold(Money.ZERO) { acc, e -> acc + e.amount }) }
        return Result.Success(totals)
    }

    override suspend fun sumByMonth(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Result<List<MonthTotal>, DataError.Local> {
        if (shouldFailSumByMonth) return Result.Error(DataError.Local.UNKNOWN)
        val totals =
            expenses
                .filter { it.date in startDate..endDate }
                .groupBy { YearMonth.from(it.date).toString() }
                .map { (month, group) -> MonthTotal(month, group.fold(Money.ZERO) { acc, e -> acc + e.amount }) }
        return Result.Success(totals)
    }
}
