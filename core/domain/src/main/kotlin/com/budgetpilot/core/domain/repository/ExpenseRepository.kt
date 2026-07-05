package com.budgetpilot.core.domain.repository

import com.budgetpilot.core.domain.DataError
import com.budgetpilot.core.domain.EmptyResult
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.model.CategoryTotal
import com.budgetpilot.core.domain.model.Expense
import com.budgetpilot.core.domain.model.ExpenseFilter
import com.budgetpilot.core.domain.model.MonthTotal
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface ExpenseRepository {
    fun observeExpenses(filter: ExpenseFilter): Flow<List<Expense>>

    suspend fun getExpenseById(id: Long): Result<Expense, DataError.Local>

    suspend fun addExpense(expense: Expense): Result<Long, DataError.Local>

    suspend fun updateExpense(expense: Expense): EmptyResult<DataError.Local>

    suspend fun deleteExpense(expense: Expense): EmptyResult<DataError.Local>

    suspend fun sumByCategory(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Result<List<CategoryTotal>, DataError.Local>

    suspend fun sumByMonth(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Result<List<MonthTotal>, DataError.Local>
}
