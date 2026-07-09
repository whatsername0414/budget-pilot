package com.budgetpilot.feature.ask.presentation

import com.budgetpilot.core.ai.domain.model.TraceStep
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Flattens a tool call's JSON args and result into "key: value" prose for the reasoning trace UI.
 * Generic rather than per-tool-specific: every read-only tool already returns flat,
 * display-ready field names and plain-decimal peso strings, so no per-tool phrasing is needed.
 * [TraceStep.ToolInvocation.resultSummary] is itself already a stringified JSON element (from
 * [com.budgetpilot.core.ai.domain.AgentLoop]'s trace recording),
 * so it's reparsed here before flattening; a result that isn't JSON (e.g. an "Error: ..." summary)
 * is passed through unchanged.
 */
internal fun TraceStep.ToolInvocation.toAskTraceStepUi(): AskTraceStepUi =
    AskTraceStepUi(
        toolName = name,
        argsSummary = args.humanized(),
        resultSummary = runCatching { Json.parseToJsonElement(resultSummary) }.getOrNull()?.humanized() ?: resultSummary,
        durationMs = durationMs,
    )

private fun JsonElement.humanized(): String =
    when (this) {
        is JsonObject -> entries.joinToString(", ") { (key, value) -> "$key: ${value.humanized()}" }
        is JsonArray -> if (isEmpty()) "none" else joinToString("; ") { it.humanized() }
        is JsonPrimitive -> content
    }
