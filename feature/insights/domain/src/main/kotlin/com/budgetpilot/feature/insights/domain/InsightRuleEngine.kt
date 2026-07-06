package com.budgetpilot.feature.insights.domain

import com.budgetpilot.core.domain.budget.BudgetMath
import com.budgetpilot.core.domain.budget.BudgetStatus
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.feature.insights.domain.model.BudgetSnapshot
import com.budgetpilot.feature.insights.domain.model.CategorySpendHistory
import com.budgetpilot.feature.insights.domain.model.ExpenseSnapshot
import com.budgetpilot.feature.insights.domain.model.InsightCandidate
import com.budgetpilot.feature.insights.domain.model.InsightData
import com.budgetpilot.feature.insights.domain.model.InsightType
import com.budgetpilot.feature.insights.domain.model.MonthlySpendingSnapshot
import java.time.Clock
import java.time.LocalDate
import kotlin.math.roundToInt

private const val NEAR_LIMIT_DAY_CUTOFF = 20

// Spike threshold 1.5x and large-expense threshold 30% are compared as exact integer
// cross-multiplications (rather than Double division) so boundary values such as
// "exactly 1.5x" or "exactly 30%" never misfire due to floating-point rounding.
private const val SPIKE_RATIO_NUMERATOR = 3L
private const val SPIKE_RATIO_DENOMINATOR = 2L
private const val LARGE_EXPENSE_SHARE_NUMERATOR = 3L
private const val LARGE_EXPENSE_SHARE_DENOMINATOR = 10L

// Priority bands are spaced 1000 apart with each rule's own magnitude contribution
// clamped below that gap, so a type can never outrank a higher-priority type.
private const val PRIORITY_BAND_BUDGET_EXCEEDED = 3000
private const val PRIORITY_BAND_BUDGET_NEAR_LIMIT = 2000
private const val PRIORITY_BAND_CATEGORY_SPIKE = 1000
private const val PRIORITY_BAND_LARGE_EXPENSE = 0
private const val MAX_PRIORITY_WITHIN_BAND = 999

class InsightRuleEngine(
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    fun evaluate(snapshot: MonthlySpendingSnapshot): InsightCandidate? {
        val candidates =
            budgetCandidates(snapshot.budgets, snapshot.month) +
                spikeCandidates(snapshot.categorySpend, snapshot.month) +
                largeExpenseCandidates(snapshot.expenses, snapshot.month)
        return candidates.maxByOrNull { it.priority }
    }

    private fun budgetCandidates(
        budgets: List<BudgetSnapshot>,
        month: String,
    ): List<InsightCandidate> {
        val today = LocalDate.now(clock)
        return budgets.mapNotNull { budget ->
            val percentUsed = BudgetMath.percentUsed(budget.spentAmount, budget.budgetAmount)
            val status = BudgetMath.statusFor(budget.spentAmount, budget.budgetAmount)
            val data =
                InsightData.BudgetStatus(
                    categoryId = budget.categoryId,
                    categoryName = budget.categoryName,
                    month = month,
                    spent = budget.spentAmount,
                    budget = budget.budgetAmount,
                    percentUsed = percentUsed,
                )
            when {
                status == BudgetStatus.OVER_BUDGET ->
                    InsightCandidate(
                        type = InsightType.BUDGET_EXCEEDED,
                        priority = PRIORITY_BAND_BUDGET_EXCEEDED + boundedMagnitude(percentUsed),
                        data = data,
                    )

                status == BudgetStatus.WARNING && today.dayOfMonth < NEAR_LIMIT_DAY_CUTOFF ->
                    InsightCandidate(
                        type = InsightType.BUDGET_NEAR_LIMIT,
                        priority = PRIORITY_BAND_BUDGET_NEAR_LIMIT + boundedMagnitude(percentUsed),
                        data = data,
                    )

                else -> null
            }
        }
    }

    private fun spikeCandidates(
        categorySpend: List<CategorySpendHistory>,
        month: String,
    ): List<InsightCandidate> =
        categorySpend.mapNotNull { history ->
            if (history.priorMonthsSpend.isEmpty()) return@mapNotNull null
            val priorTotalCentavos = history.priorMonthsSpend.sumOf { it.centavos }
            val priorMonthCount = history.priorMonthsSpend.size
            if (priorTotalCentavos <= 0) return@mapNotNull null

            val currentCentavos = history.currentMonthSpend.centavos
            // current > 1.5 * average  <=>  current * count * 2 > priorTotal * 3
            val isSpike = currentCentavos * priorMonthCount * SPIKE_RATIO_DENOMINATOR > priorTotalCentavos * SPIKE_RATIO_NUMERATOR
            if (!isSpike) return@mapNotNull null

            val averageCentavos = priorTotalCentavos.toDouble() / priorMonthCount
            val ratio = currentCentavos / averageCentavos

            InsightCandidate(
                type = InsightType.CATEGORY_SPIKE,
                priority = PRIORITY_BAND_CATEGORY_SPIKE + boundedMagnitude(ratio * PERCENT_SCALE),
                data =
                    InsightData.CategorySpike(
                        categoryId = history.categoryId,
                        categoryName = history.categoryName,
                        month = month,
                        currentSpend = history.currentMonthSpend,
                        averageSpend = Money.ofCentavos(averageCentavos.roundToInt().toLong()),
                        ratio = ratio,
                    ),
            )
        }

    private fun largeExpenseCandidates(
        expenses: List<ExpenseSnapshot>,
        month: String,
    ): List<InsightCandidate> {
        val monthlyTotalCentavos = expenses.sumOf { it.amount.centavos }
        if (monthlyTotalCentavos <= 0) return emptyList()

        return expenses.mapNotNull { expense ->
            val expenseCentavos = expense.amount.centavos
            // expense > 0.3 * total  <=>  expense * 10 > total * 3
            val isLarge = expenseCentavos * LARGE_EXPENSE_SHARE_DENOMINATOR > monthlyTotalCentavos * LARGE_EXPENSE_SHARE_NUMERATOR
            if (!isLarge) return@mapNotNull null

            val percentOfMonthlyTotal = expenseCentavos.toDouble() / monthlyTotalCentavos * PERCENT_SCALE
            InsightCandidate(
                type = InsightType.LARGE_EXPENSE,
                priority = PRIORITY_BAND_LARGE_EXPENSE + boundedMagnitude(percentOfMonthlyTotal),
                data =
                    InsightData.LargeExpense(
                        expenseId = expense.expenseId,
                        merchant = expense.merchant,
                        month = month,
                        amount = expense.amount,
                        monthlyTotal = Money.ofCentavos(monthlyTotalCentavos),
                        percentOfMonthlyTotal = percentOfMonthlyTotal,
                    ),
            )
        }
    }

    private fun boundedMagnitude(value: Double): Int = value.roundToInt().coerceIn(0, MAX_PRIORITY_WITHIN_BAND)

    private companion object {
        const val PERCENT_SCALE = 100.0
    }
}
