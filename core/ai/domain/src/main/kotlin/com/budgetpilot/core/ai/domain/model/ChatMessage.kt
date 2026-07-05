package com.budgetpilot.core.ai.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ChatMessage(
    val role: ChatRole,
    val parts: List<MessagePart>,
)
