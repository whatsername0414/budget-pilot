package com.budgetpilot.feature.insights.data

import com.budgetpilot.core.domain.DataError
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.model.CategoryTotal
import com.budgetpilot.core.domain.model.ExpenseFilter
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.core.domain.repository.BudgetRepository
import com.budgetpilot.core.domain.repository.CategoryRepository
import com.budgetpilot.core.domain.repository.ExpenseRepository
import com.budgetpilot.core.domain.repository.UserPreferencesRepository
import com.budgetpilot.feature.insights.domain.InsightRuleEngine
import com.budgetpilot.feature.insights.domain.InsightStore
import com.budgetpilot.feature.insights.domain.InsightThrottlePolicy
import com.budgetpilot.feature.insights.domain.model.BudgetSnapshot
import com.budgetpilot.feature.insights.domain.model.CategorySpendHistory
import com.budgetpilot.feature.insights.domain.model.ExpenseSnapshot
import com.budgetpilot.feature.insights.domain.model.Insight
import com.budgetpilot.feature.insights.domain.model.MonthlySpendingSnapshot
import kotlinx.coroutines.flow.first
import java.time.Clock
import java.time.YearMonth

private const val PRIOR_MONTH_COUNT = 3L

/**
 * Runs on a schedule (via [com.budgetpilot.feature.insights.data.worker.InsightCheckWorker]):
 * assembles the current month's [MonthlySpendingSnapshot] from the repos, lets [InsightRuleEngine]
 * decide whether anything is worth surfacing, gates it through [InsightThrottlePolicy], phrases it,
 * and persists it. No Android dependency — fully unit-testable against fakes.
 */
class InsightCheckUseCase(
    private val budgetRepository: BudgetRepository,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val ruleEngine: InsightRuleEngine,
    private val throttlePolicy: InsightThrottlePolicy,
    private val messageComposer: InsightMessageComposer,
    private val insightStore: InsightStore,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    suspend fun check(): InsightCheckResult {
        val snapshot = buildSnapshot(YearMonth.now(clock))
        val candidate = ruleEngine.evaluate(snapshot) ?: return InsightCheckResult.NoCandidate
        if (!throttlePolicy.canShow(candidate.type, snapshot.month)) return InsightCheckResult.Throttled

        val useCloudAi = userPreferencesRepository.cloudAiEnabled.first()
        val message = messageComposer.compose(candidate, useCloudAi)
        val insight =
            Insight(
                id = 0,
                type = candidate.type,
                message = message,
                month = snapshot.month,
                createdAt = clock.instant(),
                followUpQuestion = InsightFollowUpQuestions.questionFor(candidate),
            )
        val savedId = insightStore.save(insight)
        return InsightCheckResult.Stored(insight.copy(id = savedId))
    }

    private suspend fun buildSnapshot(currentMonth: YearMonth): MonthlySpendingSnapshot {
        val monthStr = currentMonth.toString()
        val categoryNamesById = categoryRepository.observeCategories().first().associate { it.id to it.name }

        val budgets = budgetRepository.observeBudgetsForMonth(monthStr).first()
        val budgetSnapshots =
            budgets.map { budget ->
                BudgetSnapshot(
                    categoryId = budget.categoryId,
                    categoryName = categoryNamesById[budget.categoryId] ?: UNKNOWN_CATEGORY,
                    budgetAmount = budget.amount,
                    spentAmount = budgetRepository.spentForCategoryInMonth(budget.categoryId, monthStr).valueOrZero(),
                )
            }

        val currentTotalsByCategory = expenseRepository.monthlyCategoryTotals(currentMonth)
        val priorTotalsByCategory =
            (1..PRIOR_MONTH_COUNT).map { offset -> expenseRepository.monthlyCategoryTotals(currentMonth.minusMonths(offset)) }
        val categoryIds = currentTotalsByCategory.keys + priorTotalsByCategory.flatMap { it.keys }
        val categorySpend =
            categoryIds.map { categoryId ->
                CategorySpendHistory(
                    categoryId = categoryId,
                    categoryName = categoryNamesById[categoryId] ?: UNKNOWN_CATEGORY,
                    currentMonthSpend = currentTotalsByCategory[categoryId] ?: Money.ZERO,
                    priorMonthsSpend = priorTotalsByCategory.map { it[categoryId] ?: Money.ZERO },
                )
            }

        val expenseFilter = ExpenseFilter(startDate = currentMonth.atDay(1), endDate = currentMonth.atEndOfMonth())
        val expenses =
            expenseRepository.observeExpenses(expenseFilter).first().map { expense ->
                ExpenseSnapshot(expenseId = expense.id, merchant = expense.merchant, amount = expense.amount)
            }

        return MonthlySpendingSnapshot(month = monthStr, budgets = budgetSnapshots, categorySpend = categorySpend, expenses = expenses)
    }

    private suspend fun ExpenseRepository.monthlyCategoryTotals(month: YearMonth): Map<Long, Money> =
        sumByCategory(month.atDay(1), month.atEndOfMonth()).valueOrEmpty().associate { it.categoryId to it.total }

    private fun Result<Money, DataError.Local>.valueOrZero(): Money =
        when (this) {
            is Result.Success -> data
            is Result.Error -> Money.ZERO
        }

    private fun Result<List<CategoryTotal>, DataError.Local>.valueOrEmpty(): List<CategoryTotal> =
        when (this) {
            is Result.Success -> data
            is Result.Error -> emptyList()
        }

    private companion object {
        const val UNKNOWN_CATEGORY = "Unknown"
    }
}
