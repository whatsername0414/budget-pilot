package com.budgetpilot.feature.ask.presentation

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isTrue
import com.budgetpilot.core.ai.data.FakeLlmClient
import com.budgetpilot.core.ai.data.agent.AgentSessionFactory
import com.budgetpilot.core.ai.data.prompt.PromptId
import com.budgetpilot.core.ai.data.prompt.PromptRepository
import com.budgetpilot.core.ai.domain.AgentTool
import com.budgetpilot.core.ai.domain.AiError
import com.budgetpilot.core.ai.domain.LlmClient
import com.budgetpilot.core.ai.domain.ToolError
import com.budgetpilot.core.ai.domain.model.LlmRequest
import com.budgetpilot.core.ai.domain.model.LlmResponse
import com.budgetpilot.core.ai.domain.model.ToolCall
import com.budgetpilot.core.ai.domain.model.ToolSchema
import com.budgetpilot.core.domain.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

private val fakePromptRepository =
    object : PromptRepository {
        override fun getPrompt(id: PromptId): String = "system prompt"
    }

class AskViewModelTest {
    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `staged status escalates as tool steps complete`() {
        assertThat(stagedStatusFor(completedSteps = 0)).isEqualTo(AskStagedStatus.PLANNING)
        assertThat(stagedStatusFor(completedSteps = 1)).isEqualTo(AskStagedStatus.CHECKING_EXPENSES)
        assertThat(stagedStatusFor(completedSteps = 2)).isEqualTo(AskStagedStatus.ALMOST_DONE)
        assertThat(stagedStatusFor(completedSteps = 5)).isEqualTo(AskStagedStatus.ALMOST_DONE)
    }

    @Test
    fun `sending a question queues a turn and reaches the answered phase`() =
        runTest {
            val llm = FakeLlmClient(script = listOf(Result.Success(LlmResponse.Text("Answer"))))
            val viewModel = askViewModel(llm)

            viewModel.onAction(AskAction.OnQuestionChange("How much did I spend on food last month?"))
            viewModel.onAction(AskAction.OnSendClick)

            // Queued synchronously, before the agent loop's coroutine has run at all.
            val queuedTurn =
                viewModel.state.value.turns
                    .single()
            assertThat(queuedTurn.stagedStatus).isEqualTo(AskStagedStatus.PLANNING)
            assertThat(viewModel.state.value.questionInput).isEqualTo("")

            advanceUntilIdle()

            assertThat(
                viewModel.state.value.turns
                    .single()
                    .phase,
            ).isEqualTo(AskTurnPhase.ANSWERED)
        }

    @Test
    fun `successful run stores the answer and trace in state`() =
        runTest {
            val llm =
                FakeLlmClient(
                    script =
                        listOf(
                            Result.Success(
                                toolCallResponse("resolve_date_range", buildJsonObject { put("query", JsonPrimitive("last month")) }),
                            ),
                            Result.Success(toolCallResponse("query_expenses", buildJsonObject { put("category", JsonPrimitive("Food")) })),
                            Result.Success(LlmResponse.Text("You spent ₱5,872.25 on Food last month, across 23 expenses.")),
                        ),
                )
            val tools =
                listOf(
                    fakeTool(
                        "resolve_date_range",
                        Result.Success(
                            buildJsonObject {
                                put("start_date", JsonPrimitive("2026-06-01"))
                                put("end_date", JsonPrimitive("2026-06-30"))
                            },
                        ),
                    ),
                    fakeTool(
                        "query_expenses",
                        Result.Success(
                            buildJsonObject {
                                put("count", JsonPrimitive(23))
                                put("total_pesos", JsonPrimitive("5872.25"))
                            },
                        ),
                    ),
                )
            val viewModel = askViewModel(llm, tools)

            viewModel.onAction(AskAction.OnQuestionChange("How much did I spend on food last month?"))
            viewModel.onAction(AskAction.OnSendClick)
            advanceUntilIdle()

            val turn =
                viewModel.state.value.turns
                    .single()
            assertThat(turn.phase).isEqualTo(AskTurnPhase.ANSWERED)
            assertThat(turn.answerText).isEqualTo("You spent ₱5,872.25 on Food last month, across 23 expenses.")
            assertThat(turn.trace).hasSize(2)
            assertThat(turn.trace[0].argsSummary).isEqualTo("query: last month")
            assertThat(turn.trace[1].resultSummary).isEqualTo("count: 23, total_pesos: 5872.25")
            assertThat(turn.dataUsedSummary).isEqualTo("Based on count: 23, total_pesos: 5872.25.")
            assertThat(turn.modelTurnCount).isEqualTo(2)
        }

    @Test
    fun `rate limited error maps to the rate limited card`() =
        runTest {
            val viewModel = askViewModel(scriptedErrorClient(AiError.RateLimited))

            viewModel.onAction(AskAction.OnQuestionChange("Am I over budget?"))
            viewModel.onAction(AskAction.OnSendClick)
            advanceUntilIdle()

            val turn =
                viewModel.state.value.turns
                    .single()
            assertThat(turn.phase).isEqualTo(AskTurnPhase.ERROR)
            assertThat(turn.error).isEqualTo(AskErrorUi.RateLimited(retryInSeconds = 6))
        }

    @Test
    fun `network error maps to the offline card`() =
        runTest {
            val viewModel = askViewModel(scriptedErrorClient(AiError.Network))

            viewModel.onAction(AskAction.OnQuestionChange("Am I over budget?"))
            viewModel.onAction(AskAction.OnSendClick)
            advanceUntilIdle()

            assertThat(
                viewModel.state.value.turns
                    .single()
                    .error,
            ).isEqualTo(AskErrorUi.Offline)
        }

    @Test
    fun `no api key error maps to the no api key card`() =
        runTest {
            val viewModel = askViewModel(scriptedErrorClient(AiError.NoApiKey))

            viewModel.onAction(AskAction.OnQuestionChange("Am I over budget?"))
            viewModel.onAction(AskAction.OnSendClick)
            advanceUntilIdle()

            assertThat(
                viewModel.state.value.turns
                    .single()
                    .error,
            ).isEqualTo(AskErrorUi.NoApiKey)
        }

    @Test
    fun `unmapped agent errors collapse to the generic card, and retrying recovers`() =
        runTest {
            val llm =
                FakeLlmClient(
                    script =
                        listOf(
                            Result.Error(AiError.MaxIterations),
                            Result.Success(LlmResponse.Text("Yes, Transport is over budget.")),
                        ),
                )
            val viewModel = askViewModel(llm)

            viewModel.onAction(AskAction.OnQuestionChange("Am I over budget?"))
            viewModel.onAction(AskAction.OnSendClick)
            advanceUntilIdle()

            val turnId =
                viewModel.state.value.turns
                    .single()
                    .id
            assertThat(
                viewModel.state.value.turns
                    .single()
                    .error,
            ).isEqualTo(AskErrorUi.Generic)

            viewModel.onAction(AskAction.OnRetryClick(turnId))
            advanceUntilIdle()

            val retried =
                viewModel.state.value.turns
                    .single()
            assertThat(retried.phase).isEqualTo(AskTurnPhase.ANSWERED)
            assertThat(retried.answerText).isEqualTo("Yes, Transport is over budget.")
        }

    @Test
    fun `sending a new question cancels the previous run`() =
        runTest {
            val llm = HangOnFirstCallLlmClient(subsequentResponse = LlmResponse.Text("Second answer"))
            val viewModel = askViewModel(llm)

            viewModel.onAction(AskAction.OnQuestionChange("First?"))
            viewModel.onAction(AskAction.OnSendClick)
            assertThat(
                viewModel.state.value.turns
                    .single()
                    .phase,
            ).isEqualTo(AskTurnPhase.RUNNING)

            viewModel.onAction(AskAction.OnQuestionChange("Second?"))
            viewModel.onAction(AskAction.OnSendClick)
            advanceUntilIdle()

            assertThat(llm.firstCallCancelled).isTrue()
            val turns = viewModel.state.value.turns
            assertThat(turns).hasSize(2)
            assertThat(turns[0].phase).isEqualTo(AskTurnPhase.RUNNING)
            assertThat(turns[1].phase).isEqualTo(AskTurnPhase.ANSWERED)
            assertThat(turns[1].answerText).isEqualTo("Second answer")
        }

    @Test
    fun `suggestion click submits it as a question`() =
        runTest {
            val llm = FakeLlmClient(script = listOf(Result.Success(LlmResponse.Text("Answer"))))
            val viewModel = askViewModel(llm)

            viewModel.onAction(AskAction.OnSuggestionClick("Food this month?"))
            advanceUntilIdle()

            val turn =
                viewModel.state.value.turns
                    .single()
            assertThat(turn.question).isEqualTo("Food this month?")
            assertThat(turn.phase).isEqualTo(AskTurnPhase.ANSWERED)
            assertThat(viewModel.state.value.questionInput).isEqualTo("")
        }

    private fun askViewModel(
        llmClient: LlmClient,
        tools: List<AgentTool> = emptyList(),
    ): AskViewModel {
        val factory = AgentSessionFactory(llmClient = llmClient, tools = tools, promptRepository = fakePromptRepository)
        return AskViewModel(agentSessionFactory = factory)
    }

    private fun scriptedErrorClient(error: AiError): FakeLlmClient = FakeLlmClient(script = listOf(Result.Error(error)))

    private fun toolCallResponse(
        name: String,
        args: JsonObject,
    ): LlmResponse = LlmResponse.ToolCalls(listOf(ToolCall(name = name, args = args)))

    private fun fakeTool(
        name: String,
        result: Result<JsonElement, ToolError>,
    ): AgentTool =
        object : AgentTool {
            override val schema = ToolSchema(name = name, description = "fake", parameters = buildJsonObject {})

            override suspend fun execute(args: JsonObject): Result<JsonElement, ToolError> = result
        }

    private class HangOnFirstCallLlmClient(
        private val subsequentResponse: LlmResponse,
    ) : LlmClient {
        private var callCount = 0
        var firstCallCancelled = false
            private set

        override suspend fun complete(request: LlmRequest): Result<LlmResponse, AiError> {
            callCount++
            if (callCount == 1) {
                try {
                    awaitCancellation()
                } finally {
                    firstCallCancelled = true
                }
            }
            return Result.Success(subsequentResponse)
        }
    }
}
