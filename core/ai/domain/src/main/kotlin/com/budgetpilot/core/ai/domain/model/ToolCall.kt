package com.budgetpilot.core.ai.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ToolCall(
    val name: String,
    val args: JsonObject,
)
