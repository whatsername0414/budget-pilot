package com.budgetpilot.core.ai.domain

import com.budgetpilot.core.ai.domain.model.ToolSchema
import com.budgetpilot.core.domain.Result
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

interface AgentTool {
    val schema: ToolSchema

    suspend fun execute(args: JsonObject): Result<JsonElement, ToolError>
}
