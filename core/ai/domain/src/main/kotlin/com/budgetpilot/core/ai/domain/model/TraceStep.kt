package com.budgetpilot.core.ai.domain.model

import kotlinx.serialization.json.JsonObject

sealed interface TraceStep {
    data class ToolInvocation(
        val name: String,
        val args: JsonObject,
        val resultSummary: String,
        val durationMs: Long,
    ) : TraceStep

    data class FinalAnswer(
        val text: String,
    ) : TraceStep
}
