package com.budgetpilot.feature.insights.data

import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.feature.insights.domain.model.InsightCandidate
import com.budgetpilot.feature.insights.domain.model.InsightData
import com.budgetpilot.feature.insights.domain.model.InsightType
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Deterministic Kotlin phrasing used when cloud AI is off or [InsightMessageComposer]'s lean LLM
 * call fails — the rule engine must keep working without AI (PLAN.md §6 Phase 5).
 */
internal object InsightMessageTemplates {
    fun messageFor(candidate: InsightCandidate): String =
        when (val data = candidate.data) {
            is InsightData.BudgetStatus -> budgetMessage(candidate.type, data)
            is InsightData.CategorySpike -> categorySpikeMessage(data)
            is InsightData.LargeExpense -> largeExpenseMessage(data)
        }

    private fun budgetMessage(
        type: InsightType,
        data: InsightData.BudgetStatus,
    ): String =
        if (type == InsightType.BUDGET_EXCEEDED) {
            "You've gone over your ${data.categoryName} budget this month " +
                "— ${data.spent.toDisplayPesoString()} spent against a ${data.budget.toDisplayPesoString()} budget."
        } else {
            "You've used ${data.percentUsed.roundToInt()}% of your ${data.categoryName} budget this month " +
                "(${data.spent.toDisplayPesoString()} of ${data.budget.toDisplayPesoString()})."
        }

    private fun categorySpikeMessage(data: InsightData.CategorySpike): String =
        "Your ${data.categoryName} spending this month (${data.currentSpend.toDisplayPesoString()}) is running about " +
            "${formatRatio(data.ratio)}x your recent average (${data.averageSpend.toDisplayPesoString()})."

    private fun largeExpenseMessage(data: InsightData.LargeExpense): String =
        "Your ${data.merchant} expense of ${data.amount.toDisplayPesoString()} is " +
            "${data.percentOfMonthlyTotal.roundToInt()}% of this month's total spending."

    private fun formatRatio(ratio: Double): String = RATIO_FORMAT.format(ratio)

    private val RATIO_FORMAT = DecimalFormat("0.0", DecimalFormatSymbols(Locale.US))
}

internal fun Money.toDisplayPesoString(): String {
    val pesos = centavos / CENTAVOS_PER_PESO
    return "₱${PESO_FORMAT.format(pesos)}"
}

private const val CENTAVOS_PER_PESO = 100.0
private val PESO_FORMAT = DecimalFormat("#,##0.00", DecimalFormatSymbols(Locale.US))
