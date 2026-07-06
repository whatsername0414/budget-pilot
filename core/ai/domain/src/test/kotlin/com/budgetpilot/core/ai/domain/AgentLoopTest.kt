package com.budgetpilot.core.ai.domain

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import com.budgetpilot.core.ai.domain.fake.FakeAgentTool
import com.budgetpilot.core.ai.domain.fake.FakeLlmClient
import com.budgetpilot.core.ai.domain.model.AgentAnswer
import com.budgetpilot.core.ai.domain.model.ChatRole
import com.budgetpilot.core.ai.domain.model.LlmResponse
import com.budgetpilot.core.ai.domain.model.MessagePart
import com.budgetpilot.core.ai.domain.model.ToolCall
import com.budgetpilot.core.ai.domain.model.ToolSchema
import com.budgetpilot.core.ai.domain.model.TraceStep
import com.budgetpilot.core.domain.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AgentLoopTest {
    @Test
    fun `happy path resolves after two tool calls with an ordered trace and measured durations`() =
        runTest {
            val queryTool = FakeAgentTool(name = "query_expenses", script = listOf(Result.Success(JsonPrimitive(1500))))
            val budgetTool = FakeAgentTool(name = "get_budget_status", script = listOf(Result.Success(JsonPrimitive("on_track"))))
            val times = ArrayDeque(listOf(1_000L, 1_250L, 2_000L, 2_350L))
            val llm =
                FakeLlmClient(
                    script =
                        listOf(
                            Result.Success(toolCallResponse("query_expenses")),
                            Result.Success(toolCallResponse("get_budget_status")),
                            Result.Success(LlmResponse.Text("You spent ₱1,500 on food and are on track.")),
                        ),
                )
            val loop = AgentLoop(llm = llm, tools = listOf(queryTool, budgetTool), now = { times.removeFirst() })

            val result = loop.run(goal = "How much did I spend on food?", systemPrompt = "system")

            val answer = (result as Result.Success<AgentAnswer>).data
            assertThat(answer.text).isEqualTo("You spent ₱1,500 on food and are on track.")
            assertThat(answer.trace).hasSize(3)
            val firstStep = answer.trace[0] as TraceStep.ToolInvocation
            assertThat(firstStep.name).isEqualTo("query_expenses")
            assertThat(firstStep.durationMs).isEqualTo(250L)
            val secondStep = answer.trace[1] as TraceStep.ToolInvocation
            assertThat(secondStep.name).isEqualTo("get_budget_status")
            assertThat(secondStep.durationMs).isEqualTo(350L)
            assertThat(answer.trace[2]).isInstanceOf<TraceStep.FinalAnswer>()
        }

    @Test
    fun `executes every tool call in a single response before calling the model again`() =
        runTest {
            val toolA = FakeAgentTool(name = "tool_a", script = listOf(Result.Success(JsonPrimitive("a"))))
            val toolB = FakeAgentTool(name = "tool_b", script = listOf(Result.Success(JsonPrimitive("b"))))
            val llm =
                FakeLlmClient(
                    script =
                        listOf(
                            Result.Success(
                                LlmResponse.ToolCalls(
                                    listOf(
                                        ToolCall(name = "tool_a", args = JsonObject(emptyMap())),
                                        ToolCall(name = "tool_b", args = JsonObject(emptyMap())),
                                    ),
                                ),
                            ),
                            Result.Success(LlmResponse.Text("done")),
                        ),
                )
            val loop = AgentLoop(llm = llm, tools = listOf(toolA, toolB))

            val result = loop.run(goal = "goal", systemPrompt = "system")

            assertThat(result).isInstanceOf<Result.Success<AgentAnswer>>()
            assertThat(llm.receivedRequests).hasSize(2)
            val toolResultRoles =
                llm.receivedRequests[1]
                    .messages
                    .takeLast(2)
                    .map { it.role }
            assertThat(toolResultRoles).isEqualTo(listOf(ChatRole.TOOL, ChatRole.TOOL))
        }

    @Test
    fun `the model's tool call turn is echoed back before the tool's function response`() =
        runTest {
            val tool = FakeAgentTool(name = "get_categories", script = listOf(Result.Success(JsonPrimitive("categories"))))
            val llm =
                FakeLlmClient(
                    script =
                        listOf(
                            Result.Success(toolCallResponse("get_categories")),
                            Result.Success(LlmResponse.Text("done")),
                        ),
                )
            val loop = AgentLoop(llm = llm, tools = listOf(tool))

            loop.run(goal = "goal", systemPrompt = "system")

            val secondRequestMessages = llm.receivedRequests[1].messages
            val modelTurn = secondRequestMessages[secondRequestMessages.size - 2]
            val toolTurn = secondRequestMessages.last()

            assertThat(modelTurn.role).isEqualTo(ChatRole.MODEL)
            val functionCall = modelTurn.parts.single() as MessagePart.FunctionCall
            assertThat(functionCall.name).isEqualTo("get_categories")

            assertThat(toolTurn.role).isEqualTo(ChatRole.TOOL)
            val functionResponse = toolTurn.parts.single() as MessagePart.FunctionResponse
            assertThat(functionResponse.name).isEqualTo("get_categories")
            assertThat(functionResponse.response["result"]).isEqualTo(JsonPrimitive("categories"))
        }

    @Test
    fun `a tool error is fed back and the model adapts on its next call`() =
        runTest {
            val tool =
                FakeAgentTool(
                    name = "query_expenses",
                    script =
                        listOf(
                            Result.Error(ToolError("bad filter")),
                            Result.Success(JsonPrimitive(500)),
                        ),
                )
            val llm =
                FakeLlmClient(
                    script =
                        listOf(
                            Result.Success(toolCallResponse("query_expenses")),
                            Result.Success(toolCallResponse("query_expenses")),
                            Result.Success(LlmResponse.Text("You spent ₱500.")),
                        ),
                )
            val loop = AgentLoop(llm = llm, tools = listOf(tool))

            val result = loop.run(goal = "goal", systemPrompt = "system")

            val trace = (result as Result.Success<AgentAnswer>).data.trace
            assertThat(trace).hasSize(3)
            assertThat((trace[0] as TraceStep.ToolInvocation).resultSummary).isEqualTo("Error: bad filter")
            assertThat(tool.callCount).isEqualTo(2)
        }

    @Test
    fun `the same tool failing twice in a row aborts with ToolFailure`() =
        runTest {
            val tool =
                FakeAgentTool(
                    name = "query_expenses",
                    script =
                        listOf(
                            Result.Error(ToolError("bad filter")),
                            Result.Error(ToolError("still bad")),
                        ),
                )
            val llm =
                FakeLlmClient(
                    script =
                        listOf(
                            Result.Success(toolCallResponse("query_expenses")),
                            Result.Success(toolCallResponse("query_expenses")),
                        ),
                )
            val loop = AgentLoop(llm = llm, tools = listOf(tool))

            val result = loop.run(goal = "goal", systemPrompt = "system")

            assertThat(result).isEqualTo(Result.Error(AiError.ToolFailure("query_expenses")))
        }

    @Test
    fun `an unknown tool name is fed back as an error result without aborting`() =
        runTest {
            val llm =
                FakeLlmClient(
                    script =
                        listOf(
                            Result.Success(toolCallResponse("does_not_exist")),
                            Result.Success(LlmResponse.Text("Sorry, I could not find that data.")),
                        ),
                )
            val loop = AgentLoop(llm = llm, tools = emptyList())

            val result = loop.run(goal = "goal", systemPrompt = "system")

            val trace = (result as Result.Success<AgentAnswer>).data.trace
            assertThat((trace[0] as TraceStep.ToolInvocation).resultSummary).isEqualTo("Unknown tool: does_not_exist")
        }

    @Test
    fun `exceeding max iterations bails with MaxIterations`() =
        runTest {
            val tool =
                FakeAgentTool(
                    name = "query_expenses",
                    script = List(5) { Result.Success<JsonElement>(JsonPrimitive(1)) },
                )
            val llm = FakeLlmClient(script = List(5) { Result.Success(toolCallResponse("query_expenses")) })
            val loop = AgentLoop(llm = llm, tools = listOf(tool), maxIterations = 3)

            val result = loop.run(goal = "goal", systemPrompt = "system")

            assertThat(result).isEqualTo(Result.Error(AiError.MaxIterations))
            assertThat(llm.receivedRequests).hasSize(3)
        }

    @Test
    fun `malformed tool args produce a tool error the loop feeds back for one adaptation`() =
        runTest {
            val validatingTool =
                object : AgentTool {
                    override val schema =
                        ToolSchema(name = "query_expenses", description = "", parameters = JsonObject(emptyMap()))
                    var callCount = 0
                        private set

                    override suspend fun execute(args: JsonObject): Result<JsonElement, ToolError> {
                        callCount++
                        return if (!args.containsKey("category")) {
                            Result.Error(ToolError("missing required field: category"))
                        } else {
                            Result.Success(JsonPrimitive(200))
                        }
                    }
                }
            val llm =
                FakeLlmClient(
                    script =
                        listOf(
                            Result.Success(toolCallResponse("query_expenses", JsonObject(emptyMap()))),
                            Result.Success(
                                toolCallResponse(
                                    "query_expenses",
                                    buildJsonObject { put("category", JsonPrimitive("Food")) },
                                ),
                            ),
                            Result.Success(LlmResponse.Text("₱200 on food.")),
                        ),
                )
            val loop = AgentLoop(llm = llm, tools = listOf(validatingTool))

            val result = loop.run(goal = "goal", systemPrompt = "system")

            assertThat(result).isInstanceOf<Result.Success<AgentAnswer>>()
            assertThat(validatingTool.callCount).isEqualTo(2)
        }

    @Test
    fun `an llm error propagates without being wrapped`() =
        runTest {
            val llm = FakeLlmClient(script = listOf(Result.Error(AiError.RateLimited)))
            val loop = AgentLoop(llm = llm, tools = emptyList())

            val result = loop.run(goal = "goal", systemPrompt = "system")

            assertThat(result).isEqualTo(Result.Error(AiError.RateLimited))
        }

    @Test
    fun `cancellation stops the loop promptly instead of waiting for a slow tool`() =
        runTest {
            val slowTool =
                FakeAgentTool(
                    name = "slow_tool",
                    script = listOf(Result.Success(JsonPrimitive("done"))),
                    delayMillis = 60_000,
                )
            val llm = FakeLlmClient(script = listOf(Result.Success(toolCallResponse("slow_tool"))))
            val loop = AgentLoop(llm = llm, tools = listOf(slowTool))

            val job = launch { loop.run(goal = "goal", systemPrompt = "system") }
            runCurrent()
            job.cancel()
            job.join()

            assertThat(job.isCancelled).isTrue()
        }

    private fun toolCallResponse(
        name: String,
        args: JsonObject = JsonObject(emptyMap()),
    ): LlmResponse = LlmResponse.ToolCalls(listOf(ToolCall(name = name, args = args)))
}
