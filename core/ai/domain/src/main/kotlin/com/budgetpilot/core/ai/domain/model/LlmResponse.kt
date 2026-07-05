package com.budgetpilot.core.ai.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface LlmResponse {
    @Serializable
    @SerialName("text")
    data class Text(
        val content: String,
    ) : LlmResponse

    @Serializable
    @SerialName("tool_calls")
    data class ToolCalls(
        val calls: List<ToolCall>,
    ) : LlmResponse
}
