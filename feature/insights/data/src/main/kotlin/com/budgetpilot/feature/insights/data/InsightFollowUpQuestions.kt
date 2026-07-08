package com.budgetpilot.feature.insights.data

import com.budgetpilot.feature.insights.domain.model.InsightCandidate
import com.budgetpilot.feature.insights.domain.model.InsightData
import com.budgetpilot.feature.insights.domain.model.InsightType

/**
 * Builds the "Ask more" prefill question from the candidate's actual [InsightData] — merchant,
 * category, amount — rather than a generic per-[InsightType] template (CLAUDE.md §10's 2026-07-07
 * "accepted deviation" entry), so the Ask agent's tool calls (`query_expenses`/`get_budget_status`)
 * have something concrete to filter on instead of an unresolvable "this expense".
 */
internal object InsightFollowUpQuestions {
    fun questionFor(candidate: InsightCandidate): String =
        when (val data = candidate.data) {
            is InsightData.BudgetStatus -> budgetQuestion(candidate.type, data)
            is InsightData.CategorySpike ->
                "Why is my ${data.categoryName} spending up this month compared to recent months?"

            is InsightData.LargeExpense ->
                "How does my ${data.amount.toDisplayPesoString()} expense at ${data.merchant} affect my budget this month?"
        }

    private fun budgetQuestion(
        type: InsightType,
        data: InsightData.BudgetStatus,
    ): String =
        if (type == InsightType.BUDGET_EXCEEDED) {
            "How do I get back under budget for ${data.categoryName} this month?"
        } else {
            "How do I stay under my ${data.categoryName} budget this month?"
        }
}
