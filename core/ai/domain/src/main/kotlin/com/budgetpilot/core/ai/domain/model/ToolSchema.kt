package com.budgetpilot.core.ai.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class ToolSchema(
    val name: String,
    val description: String,
    val parameters: JsonObject,
)
