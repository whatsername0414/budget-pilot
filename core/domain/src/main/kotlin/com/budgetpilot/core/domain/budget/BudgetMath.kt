package com.budgetpilot.core.domain.budget

import com.budgetpilot.core.domain.money.Money
import kotlin.math.roundToLong

private const val WARNING_THRESHOLD_PERCENT = 80.0
private const val OVER_BUDGET_THRESHOLD_PERCENT = 100.0

object BudgetMath {
    fun percentUsed(
        spent: Money,
        budget: Money,
    ): Double = spent.percentOf(budget)

    fun statusFor(
        spent: Money,
        budget: Money,
    ): BudgetStatus {
        val percent = percentUsed(spent, budget)
        return when {
            percent >= OVER_BUDGET_THRESHOLD_PERCENT -> BudgetStatus.OVER_BUDGET
            percent >= WARNING_THRESHOLD_PERCENT -> BudgetStatus.WARNING
            else -> BudgetStatus.ON_TRACK
        }
    }

    /**
     * Extrapolates [spent]'s day-of-month pace to a full-month projection and
     * returns how far over [budget] that projection lands (zero if it doesn't).
     */
    fun projectedOverspend(
        spent: Money,
        budget: Money,
        dayOfMonth: Int,
        daysInMonth: Int,
    ): Money {
        require(dayOfMonth in 1..daysInMonth) {
            "dayOfMonth ($dayOfMonth) must be within 1..daysInMonth ($daysInMonth)"
        }

        val projectedCentavos = (spent.centavos.toDouble() / dayOfMonth * daysInMonth).roundToLong()
        val overspendCentavos = projectedCentavos - budget.centavos
        return if (overspendCentavos > 0) Money.ofCentavos(overspendCentavos) else Money.ZERO
    }
}
