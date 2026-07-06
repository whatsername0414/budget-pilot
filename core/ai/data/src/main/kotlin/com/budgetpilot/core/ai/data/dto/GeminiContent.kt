package com.budgetpilot.core.ai.data.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class GeminiContent(
    val role: String? = null,
    val parts: List<GeminiPart>,
)

@Serializable
data class GeminiPart(
    val text: String? = null,
    val inlineData: GeminiInlineData? = null,
    val functionCall: GeminiFunctionCall? = null,
    val functionResponse: GeminiFunctionResponse? = null,
)

@Serializable
data class GeminiInlineData(
    val mimeType: String,
    val data: String,
)

@Serializable
data class GeminiFunctionCall(
    val name: String,
    val args: JsonObject = JsonObject(emptyMap()),
)

@Serializable
data class GeminiFunctionResponse(
    val name: String,
    val response: JsonObject,
)
