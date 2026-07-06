package com.budgetpilot.feature.insights.data

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.budgetpilot.core.ai.data.FakeLlmClient
import com.budgetpilot.core.ai.data.prompt.PromptId
import com.budgetpilot.core.ai.data.prompt.PromptRepository
import com.budgetpilot.core.ai.domain.AiError
import com.budgetpilot.core.ai.domain.model.LlmResponse
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.model.Budget
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.feature.insights.data.fake.FakeBudgetRepository
import com.budgetpilot.feature.insights.data.fake.FakeCategoryRepository
import com.budgetpilot.feature.insights.data.fake.FakeExpenseRepository
import com.budgetpilot.feature.insights.data.fake.FakeInsightStore
import com.budgetpilot.feature.insights.data.fake.FakeUserPreferencesRepository
import com.budgetpilot.feature.insights.domain.InsightRuleEngine
import com.budgetpilot.feature.insights.domain.InsightThrottlePolicy
import com.budgetpilot.feature.insights.domain.model.InsightType
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

private const val MONTH = "2026-07"
private val NOW: Instant = Instant.parse("2026-07-15T12:00:00Z")
private val CLOCK: Clock = Clock.fixed(NOW, ZoneOffset.UTC)

private const val OVER_BUDGET_TEMPLATE_MESSAGE =
    "You've gone over your Food budget this month — ₱6,500.00 spent against a ₱5,000.00 budget."

private val fakePromptRepository =
    object : PromptRepository {
        override fun getPrompt(id: PromptId) = "SYSTEM PROMPT"
    }

/** "Now" is fixed to 2026-07-15T12:00:00Z; a Food budget of ₱5,000 spent to ₱6,500 always fires BUDGET_EXCEEDED. */
class InsightCheckUseCaseTest {
    private fun useCase(
        insightStore: FakeInsightStore = FakeInsightStore(),
        cloudAiEnabled: Boolean = false,
        llmClient: FakeLlmClient = FakeLlmClient(emptyList()),
    ): Pair<InsightCheckUseCase, FakeLlmClient> {
        val budgetRepository =
            FakeBudgetRepository(
                budgets = listOf(Budget(id = 1, categoryId = 1, month = MONTH, amount = Money.ofCentavos(500_000))),
                spendByCategoryAndMonth = mapOf((1L to MONTH) to Money.ofCentavos(650_000)),
            )
        val useCase =
            InsightCheckUseCase(
                budgetRepository = budgetRepository,
                expenseRepository = FakeExpenseRepository(),
                categoryRepository = FakeCategoryRepository(),
                userPreferencesRepository = FakeUserPreferencesRepository(cloudAiEnabled),
                ruleEngine = InsightRuleEngine(CLOCK),
                throttlePolicy = InsightThrottlePolicy(insightStore, CLOCK),
                messageComposer = InsightMessageComposer(llmClient, fakePromptRepository),
                insightStore = insightStore,
                clock = CLOCK,
            )
        return useCase to llmClient
    }

    @Test
    fun `candidate is phrased by the LLM and stored`() =
        runTest {
            val llmClient = FakeLlmClient(listOf(Result.Success(LlmResponse.Text("You're over budget on Food this month."))))
            val (useCase, _) = useCase(cloudAiEnabled = true, llmClient = llmClient)

            val result = useCase.check()

            assertThat(result).isInstanceOf<InsightCheckResult.Stored>()
            val stored = result as InsightCheckResult.Stored
            assertThat(stored.insight.message).isEqualTo("You're over budget on Food this month.")
            assertThat(stored.insight.type).isEqualTo(InsightType.BUDGET_EXCEEDED)
            assertThat(llmClient.receivedRequests).hasSize(1)
        }

    @Test
    fun `throttled candidate is skipped without saving`() =
        runTest {
            val insightStore = FakeInsightStore(shownTypesByMonth = setOf(InsightType.BUDGET_EXCEEDED to MONTH))
            val (useCase, llmClient) = useCase(insightStore = insightStore)

            val result = useCase.check()

            assertThat(result).isEqualTo(InsightCheckResult.Throttled)
            assertThat(insightStore.saved).isEmpty()
            assertThat(llmClient.receivedRequests).isEmpty()
        }

    @Test
    fun `cloud AI off uses the template fallback without calling the LLM`() =
        runTest {
            val (useCase, llmClient) = useCase(cloudAiEnabled = false)

            val result = useCase.check()

            assertThat(result).isInstanceOf<InsightCheckResult.Stored>()
            val stored = result as InsightCheckResult.Stored
            assertThat(stored.insight.message).isEqualTo(OVER_BUDGET_TEMPLATE_MESSAGE)
            assertThat(llmClient.receivedRequests).isEmpty()
        }

    @Test
    fun `LLM failure falls back to the template`() =
        runTest {
            val llmClient = FakeLlmClient(listOf(Result.Error(AiError.RateLimited)))
            val (useCase, _) = useCase(cloudAiEnabled = true, llmClient = llmClient)

            val result = useCase.check()

            assertThat(result).isInstanceOf<InsightCheckResult.Stored>()
            val stored = result as InsightCheckResult.Stored
            assertThat(stored.insight.message).isEqualTo(OVER_BUDGET_TEMPLATE_MESSAGE)
            assertThat(llmClient.receivedRequests).hasSize(1)
        }
}
