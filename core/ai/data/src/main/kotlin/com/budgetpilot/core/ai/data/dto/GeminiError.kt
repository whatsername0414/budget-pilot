package com.budgetpilot.core.ai.data.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

@Serializable
data class GeminiErrorEnvelope(
    val error: GeminiErrorBody,
)

@Serializable
data class GeminiErrorBody(
    val code: Int = 0,
    val message: String = "",
    val status: String = "",
    val details: List<JsonObject> = emptyList(),
)

private const val RETRY_DELAY_KEY = "retryDelay"
private const val MILLIS_PER_SECOND = 1_000

/**
 * Google's `google.rpc.RetryInfo` detail carries a `retryDelay` string like `"6s"` — not
 * verbatim-confirmed on a docs.ai.google.dev reference page (see CLAUDE.md §10, P3.1), only
 * corroborated by real request/response examples, so this is parsed defensively.
 */
fun GeminiErrorBody.retryDelayMillis(): Long? {
    val retryDelay =
        details
            .firstNotNullOfOrNull { detail -> detail[RETRY_DELAY_KEY] as? JsonPrimitive }
            ?.takeIf { it.isString }
            ?.content
            ?: return null
    return retryDelay.removeSuffix("s").toDoubleOrNull()?.let { seconds -> (seconds * MILLIS_PER_SECOND).toLong() }
}
