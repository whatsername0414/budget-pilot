package com.budgetpilot.core.ai.domain.fake

import com.budgetpilot.core.ai.domain.AgentTool
import com.budgetpilot.core.ai.domain.ToolError
import com.budgetpilot.core.ai.domain.model.ToolSchema
import com.budgetpilot.core.domain.Result
import kotlinx.coroutines.delay
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

class FakeAgentTool(
    name: String,
    script: List<Result<JsonElement, ToolError>>,
    private val delayMillis: Long = 0,
) : AgentTool {
    override val schema: ToolSchema = ToolSchema(name = name, description = "fake tool", parameters = JsonObject(emptyMap()))

    private val remainingResults = ArrayDeque(script)
    private val mutableReceivedArgs = mutableListOf<JsonObject>()

    val receivedArgs: List<JsonObject> get() = mutableReceivedArgs
    val callCount: Int get() = mutableReceivedArgs.size

    override suspend fun execute(args: JsonObject): Result<JsonElement, ToolError> {
        mutableReceivedArgs += args
        if (delayMillis > 0) delay(delayMillis)
        return remainingResults.removeFirstOrNull()
            ?: error("FakeAgentTool(${schema.name}) script exhausted: no result queued for call #${mutableReceivedArgs.size}")
    }
}
