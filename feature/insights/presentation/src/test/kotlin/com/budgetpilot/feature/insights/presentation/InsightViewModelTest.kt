package com.budgetpilot.feature.insights.presentation

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.budgetpilot.core.ai.data.prompt.PromptId
import com.budgetpilot.core.ai.data.prompt.PromptRepository
import com.budgetpilot.core.ai.domain.AiError
import com.budgetpilot.core.ai.domain.LlmClient
import com.budgetpilot.core.ai.domain.model.LlmRequest
import com.budgetpilot.core.ai.domain.model.LlmResponse
import com.budgetpilot.core.domain.Result
import com.budgetpilot.feature.insights.data.InsightCheckUseCase
import com.budgetpilot.feature.insights.data.InsightMessageComposer
import com.budgetpilot.feature.insights.domain.InsightRuleEngine
import com.budgetpilot.feature.insights.domain.InsightThrottlePolicy
import com.budgetpilot.feature.insights.domain.model.Insight
import com.budgetpilot.feature.insights.domain.model.InsightType
import com.budgetpilot.feature.insights.presentation.fake.FakeBudgetRepository
import com.budgetpilot.feature.insights.presentation.fake.FakeCategoryRepository
import com.budgetpilot.feature.insights.presentation.fake.FakeExpenseRepository
import com.budgetpilot.feature.insights.presentation.fake.FakeInsightStore
import com.budgetpilot.feature.insights.presentation.fake.FakeUserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

private val fixedClock: Clock = Clock.fixed(Instant.parse("2026-07-15T00:00:00Z"), ZoneOffset.UTC)

private val fakePromptRepository =
    object : PromptRepository {
        override fun getPrompt(id: PromptId): String = "unused"
    }

private val fakeLlmClient =
    object : LlmClient {
        override suspend fun complete(request: LlmRequest): Result<LlmResponse, AiError> = Result.Success(LlmResponse.Text("unused"))
    }

class InsightViewModelTest {
    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `shows the store's latest undismissed insight`() =
        runTest {
            val insight = insightOf(id = 1, type = InsightType.BUDGET_NEAR_LIMIT, message = "80% of Food used")
            val store = FakeInsightStore(latestUndismissed = insight)

            val viewModel = insightViewModel(store)

            assertThat(viewModel.state.value.card).isEqualTo(InsightCardUi(message = insight.message))
        }

    @Test
    fun `dismiss persists dismissedAt and hides the card`() =
        runTest {
            val insight = insightOf(id = 7, type = InsightType.LARGE_EXPENSE, message = "Big Jollibee run")
            val store = FakeInsightStore(latestUndismissed = insight)
            val viewModel = insightViewModel(store)

            viewModel.onAction(InsightAction.OnDismissClick)

            assertThat(viewModel.state.value.card).isNull()
            assertThat(store.dismissed.map { it.first }).isEqualTo(listOf(7L))
        }

    @Test
    fun `ask more emits a navigate event carrying the insight's stored follow-up question`() =
        runTest {
            val insight =
                insightOf(
                    id = 3,
                    type = InsightType.CATEGORY_SPIKE,
                    message = "Transport spend up",
                    followUpQuestion = "Why is my Transport spending up this month compared to recent months?",
                )
            val store = FakeInsightStore(latestUndismissed = insight)
            val viewModel = insightViewModel(store)

            viewModel.events.test {
                viewModel.onAction(InsightAction.OnAskMoreClick)
                assertThat(awaitItem()).isEqualTo(InsightEvent.NavigateToAsk(insight.followUpQuestion!!))
            }
        }

    @Test
    fun `ask more falls back to a generic per-type question when no follow-up question was persisted`() =
        runTest {
            val insight = insightOf(id = 3, type = InsightType.CATEGORY_SPIKE, message = "Transport spend up")
            val store = FakeInsightStore(latestUndismissed = insight)
            val viewModel = insightViewModel(store)

            viewModel.events.test {
                viewModel.onAction(InsightAction.OnAskMoreClick)
                assertThat(awaitItem()).isEqualTo(InsightEvent.NavigateToAsk(prefillQuestionFor(InsightType.CATEGORY_SPIKE)))
            }
        }

    private fun insightOf(
        id: Long,
        type: InsightType,
        message: String,
        followUpQuestion: String? = null,
    ): Insight =
        Insight(
            id = id,
            type = type,
            message = message,
            month = "2026-07",
            createdAt = fixedClock.instant(),
            followUpQuestion = followUpQuestion,
        )

    private fun insightViewModel(store: FakeInsightStore): InsightViewModel {
        val useCase =
            InsightCheckUseCase(
                budgetRepository = FakeBudgetRepository(),
                expenseRepository = FakeExpenseRepository(),
                categoryRepository = FakeCategoryRepository(),
                userPreferencesRepository = FakeUserPreferencesRepository(),
                ruleEngine = InsightRuleEngine(clock = fixedClock),
                throttlePolicy = InsightThrottlePolicy(historyStore = store, clock = fixedClock),
                messageComposer = InsightMessageComposer(fakeLlmClient, fakePromptRepository),
                insightStore = store,
                clock = fixedClock,
            )
        return InsightViewModel(insightCheckUseCase = useCase, insightStore = store, clock = fixedClock)
    }
}
