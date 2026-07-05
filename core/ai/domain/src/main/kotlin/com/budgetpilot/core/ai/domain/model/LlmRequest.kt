package com.budgetpilot.core.ai.domain.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class LlmRequest(
    val messages: List<ChatMessage>,
    val tools: List<ToolSchema> = emptyList(),
    val responseSchema: JsonObject? = null,
    val temperature: Double? = null,
    val maxOutputTokens: Int? = null,
)
