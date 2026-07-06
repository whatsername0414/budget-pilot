package com.budgetpilot.core.ai.domain

import com.budgetpilot.core.ai.domain.model.AgentAnswer
import com.budgetpilot.core.ai.domain.model.ChatMessage
import com.budgetpilot.core.ai.domain.model.ChatRole
import com.budgetpilot.core.ai.domain.model.LlmRequest
import com.budgetpilot.core.ai.domain.model.LlmResponse
import com.budgetpilot.core.ai.domain.model.MessagePart
import com.budgetpilot.core.ai.domain.model.ToolCall
import com.budgetpilot.core.ai.domain.model.TraceStep
import com.budgetpilot.core.domain.Result
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * Tool-calling loop: sends [goal] to [llm] alongside every [tools] schema, executes whichever
 * tools the model asks for, feeds the results back as the next turn, and repeats until the model
 * returns a final text answer or [maxIterations] is exhausted. A tool that fails twice in a row
 * aborts the run with [AiError.ToolFailure] — the model gets exactly one chance to adapt (retry
 * with different args, or a different tool) before that.
 */
class AgentLoop(
    private val llm: LlmClient,
    private val tools: List<AgentTool>,
    private val maxIterations: Int = 6,
    private val now: () -> Long = System::currentTimeMillis,
) {
    private val toolsByName = tools.associateBy { it.schema.name }

    suspend fun run(
        goal: String,
        systemPrompt: String,
        onStep: (TraceStep) -> Unit = {},
    ): Result<AgentAnswer, AiError> {
        val messages =
            mutableListOf(
                ChatMessage(role = ChatRole.SYSTEM, parts = listOf(MessagePart.Text(systemPrompt))),
                ChatMessage(role = ChatRole.USER, parts = listOf(MessagePart.Text(goal))),
            )
        val trace = mutableListOf<TraceStep>()
        val toolFailureCounts = mutableMapOf<String, Int>()

        repeat(maxIterations) {
            currentCoroutineContext().ensureActive()

            val request = LlmRequest(messages = messages, tools = tools.map { it.schema })
            val llmResponse =
                when (val result = llm.complete(request)) {
                    is Result.Error -> return Result.Error(result.error)
                    is Result.Success -> result.data
                }

            when (llmResponse) {
                is LlmResponse.Text -> {
                    val step = TraceStep.FinalAnswer(llmResponse.content)
                    trace += step
                    onStep(step)
                    return Result.Success(AgentAnswer(text = llmResponse.content, trace = trace.toList()))
                }
                is LlmResponse.ToolCalls -> {
                    messages += modelToolCallMessage(llmResponse.calls)
                    for (call in llmResponse.calls) {
                        currentCoroutineContext().ensureActive()
                        val failure = executeTool(call, toolFailureCounts, trace, onStep, messages)
                        if (failure != null) return Result.Error(failure)
                    }
                }
            }
        }

        return Result.Error(AiError.MaxIterations)
    }

    private suspend fun executeTool(
        call: ToolCall,
        toolFailureCounts: MutableMap<String, Int>,
        trace: MutableList<TraceStep>,
        onStep: (TraceStep) -> Unit,
        messages: MutableList<ChatMessage>,
    ): AiError.ToolFailure? {
        val tool = toolsByName[call.name]
        if (tool == null) {
            val summary = "Unknown tool: ${call.name}"
            recordStep(trace, onStep, call, summary, durationMs = 0)
            messages += toolResultMessage(call.name, errorResponse(summary))
            return null
        }

        val startedAt = now()
        val result = tool.execute(call.args)
        val durationMs = now() - startedAt

        return when (result) {
            is Result.Success -> {
                val summary = result.data.toString()
                recordStep(trace, onStep, call, summary, durationMs)
                messages += toolResultMessage(call.name, successResponse(result.data))
                null
            }
            is Result.Error -> {
                val summary = "Error: ${result.error.message}"
                recordStep(trace, onStep, call, summary, durationMs)
                val failures = 1 + (toolFailureCounts[call.name] ?: 0)
                toolFailureCounts[call.name] = failures
                if (failures >= TOOL_FAILURE_LIMIT) {
                    AiError.ToolFailure(call.name)
                } else {
                    messages += toolResultMessage(call.name, errorResponse(summary))
                    null
                }
            }
        }
    }

    private fun recordStep(
        trace: MutableList<TraceStep>,
        onStep: (TraceStep) -> Unit,
        call: ToolCall,
        summary: String,
        durationMs: Long,
    ) {
        val step =
            TraceStep.ToolInvocation(
                name = call.name,
                args = call.args,
                resultSummary = summary,
                durationMs = durationMs,
            )
        trace += step
        onStep(step)
    }

    private fun modelToolCallMessage(calls: List<ToolCall>): ChatMessage =
        ChatMessage(role = ChatRole.MODEL, parts = calls.map { MessagePart.FunctionCall(name = it.name, args = it.args) })

    private fun toolResultMessage(
        name: String,
        response: JsonObject,
    ): ChatMessage = ChatMessage(role = ChatRole.TOOL, parts = listOf(MessagePart.FunctionResponse(name = name, response = response)))

    private fun successResponse(data: JsonElement): JsonObject = buildJsonObject { put("result", data) }

    private fun errorResponse(message: String): JsonObject = buildJsonObject { put("error", JsonPrimitive(message)) }

    private companion object {
        const val TOOL_FAILURE_LIMIT = 2
    }
}
