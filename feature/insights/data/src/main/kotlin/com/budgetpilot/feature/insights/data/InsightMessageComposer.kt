package com.budgetpilot.feature.insights.data

import com.budgetpilot.core.ai.data.prompt.PromptId
import com.budgetpilot.core.ai.data.prompt.PromptRepository
import com.budgetpilot.core.ai.domain.LlmClient
import com.budgetpilot.core.ai.domain.model.ChatMessage
import com.budgetpilot.core.ai.domain.model.ChatRole
import com.budgetpilot.core.ai.domain.model.LlmRequest
import com.budgetpilot.core.ai.domain.model.LlmResponse
import com.budgetpilot.core.ai.domain.model.MessagePart
import com.budgetpilot.core.domain.Result
import com.budgetpilot.feature.insights.domain.model.InsightCandidate
import com.budgetpilot.feature.insights.domain.model.InsightData
import com.budgetpilot.feature.insights.domain.model.InsightType

/**
 * Phrases a fired [InsightCandidate] into the one-sentence message shown to the user. When
 * [useCloudAi] is true, makes a single "lean" (no tool-calling, no loop) [LlmClient] call — any
 * failure or non-text response falls back to [InsightMessageTemplates] so the heuristics keep
 * working offline or rate-limited (PLAN.md §6 Phase 5).
 */
class InsightMessageComposer(
    private val llmClient: LlmClient,
    private val promptRepository: PromptRepository,
) {
    suspend fun compose(
        candidate: InsightCandidate,
        useCloudAi: Boolean,
    ): String {
        if (!useCloudAi) return InsightMessageTemplates.messageFor(candidate)

        val request =
            LlmRequest(
                messages =
                    listOf(
                        ChatMessage(ChatRole.SYSTEM, listOf(MessagePart.Text(promptRepository.getPrompt(PromptId.INSIGHT_V1)))),
                        ChatMessage(ChatRole.USER, listOf(MessagePart.Text(describe(candidate)))),
                    ),
            )

        return when (val result = llmClient.complete(request)) {
            is Result.Error -> InsightMessageTemplates.messageFor(candidate)
            is Result.Success ->
                (result.data as? LlmResponse.Text)
                    ?.content
                    ?.trim()
                    ?.takeIf { it.isNotEmpty() }
                    ?: InsightMessageTemplates.messageFor(candidate)
        }
    }

    private fun describe(candidate: InsightCandidate): String {
        val fields =
            when (val data = candidate.data) {
                is InsightData.BudgetStatus ->
                    "Month: ${data.month}\n" +
                        "Category: ${data.categoryName}\n" +
                        "Spent: ${data.spent.toDisplayPesoString()}\n" +
                        "Budget: ${data.budget.toDisplayPesoString()}\n" +
                        "Percent used: ${data.percentUsed.toInt()}%"

                is InsightData.CategorySpike ->
                    "Month: ${data.month}\n" +
                        "Category: ${data.categoryName}\n" +
                        "Current month spend: ${data.currentSpend.toDisplayPesoString()}\n" +
                        "Recent monthly average: ${data.averageSpend.toDisplayPesoString()}\n" +
                        "Ratio vs. average: ${data.ratio}x"

                is InsightData.LargeExpense ->
                    "Month: ${data.month}\n" +
                        "Merchant: ${data.merchant}\n" +
                        "Expense amount: ${data.amount.toDisplayPesoString()}\n" +
                        "Month's total spending: ${data.monthlyTotal.toDisplayPesoString()}\n" +
                        "Percent of month's total: ${data.percentOfMonthlyTotal.toInt()}%"
            }
        return "Insight type: ${candidate.type.describe()}\n$fields\n\n" +
            "Phrase this as one short sentence for the user."
    }

    private fun InsightType.describe(): String =
        when (this) {
            InsightType.BUDGET_EXCEEDED -> "budget exceeded"
            InsightType.BUDGET_NEAR_LIMIT -> "budget near limit"
            InsightType.CATEGORY_SPIKE -> "category spending spike"
            InsightType.LARGE_EXPENSE -> "unusually large expense"
        }
}
