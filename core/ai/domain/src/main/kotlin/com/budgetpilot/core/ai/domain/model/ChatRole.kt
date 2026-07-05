package com.budgetpilot.core.ai.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class ChatRole {
    SYSTEM,
    USER,
    MODEL,
    TOOL,
}
