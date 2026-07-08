package com.budgetpilot.feature.insights.presentation.fake

import com.budgetpilot.core.domain.DataError
import com.budgetpilot.core.domain.EmptyResult
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.model.Budget
import com.budgetpilot.core.domain.model.Category
import com.budgetpilot.core.domain.model.CategoryTotal
import com.budgetpilot.core.domain.model.Expense
import com.budgetpilot.core.domain.model.ExpenseFilter
import com.budgetpilot.core.domain.model.MonthTotal
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.core.domain.repository.BudgetRepository
import com.budgetpilot.core.domain.repository.CategoryRepository
import com.budgetpilot.core.domain.repository.ExpenseRepository
import com.budgetpilot.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate

/**
 * Minimal, always-empty repo fakes — only exist so [com.budgetpilot.feature.insights.data.InsightCheckUseCase]
 * can be constructed in [com.budgetpilot.feature.insights.presentation.InsightViewModelTest]; an
 * empty snapshot means the rule engine never fires, so these tests only exercise the ViewModel's
 * own load/dismiss/ask-more behavior against [FakeInsightStore], not the check itself (already
 * covered by `InsightCheckUseCaseTest` in `:feature:insights:data`).
 */
class FakeBudgetRepository : BudgetRepository {
    override fun observeBudgetsForMonth(month: String): Flow<List<Budget>> = flowOf(emptyList())

    override suspend fun getBudget(
        categoryId: Long,
        month: String,
    ): Result<Budget, DataError.Local> = Result.Error(DataError.Local.NOT_FOUND)

    override suspend fun addBudget(budget: Budget): Result<Long, DataError.Local> = Result.Success(budget.id)

    override suspend fun updateBudget(budget: Budget): EmptyResult<DataError.Local> = Result.Success(Unit)

    override suspend fun deleteBudget(budget: Budget): EmptyResult<DataError.Local> = Result.Success(Unit)

    override suspend fun spentForCategoryInMonth(
        categoryId: Long,
        month: String,
    ): Result<Money, DataError.Local> = Result.Success(Money.ZERO)
}

class FakeExpenseRepository : ExpenseRepository {
    override fun observeExpenses(filter: ExpenseFilter): Flow<List<Expense>> = flowOf(emptyList())

    override suspend fun getExpenseById(id: Long): Result<Expense, DataError.Local> = Result.Error(DataError.Local.NOT_FOUND)

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
}

class FakeCategoryRepository : CategoryRepository {
    override fun observeCategories(): Flow<List<Category>> = flowOf(emptyList())

    override suspend fun getCategoryById(id: Long): Result<Category, DataError.Local> = Result.Error(DataError.Local.NOT_FOUND)
}

class FakeUserPreferencesRepository : UserPreferencesRepository {
    override val cloudAiEnabled = MutableStateFlow(false)
    override val privateModeEnabled = MutableStateFlow(false)
    override val demoModeEnabled = MutableStateFlow(false)
    override val dynamicColorEnabled = MutableStateFlow(false)

    override suspend fun setCloudAiEnabled(enabled: Boolean): EmptyResult<DataError.Local> {
        cloudAiEnabled.value = enabled
        return Result.Success(Unit)
    }

    override suspend fun setPrivateModeEnabled(enabled: Boolean): EmptyResult<DataError.Local> {
        privateModeEnabled.value = enabled
        return Result.Success(Unit)
    }

    override suspend fun setDemoModeEnabled(enabled: Boolean): EmptyResult<DataError.Local> {
        demoModeEnabled.value = enabled
        return Result.Success(Unit)
    }

    override suspend fun setDynamicColorEnabled(enabled: Boolean): EmptyResult<DataError.Local> {
        dynamicColorEnabled.value = enabled
        return Result.Success(Unit)
    }
}
