package com.budgetpilot.core.ai.data.dto

import kotlinx.serialization.Serializable

@Serializable
data class GeminiGenerateContentResponse(
    val candidates: List<GeminiCandidate> = emptyList(),
)

@Serializable
data class GeminiCandidate(
    val content: GeminiContent? = null,
    val finishReason: String? = null,
)
