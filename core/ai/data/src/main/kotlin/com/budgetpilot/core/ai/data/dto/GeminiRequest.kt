package com.budgetpilot.core.ai.data.dto

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class GeminiGenerateContentRequest(
    val contents: List<GeminiContent>,
    val systemInstruction: GeminiContent? = null,
    val tools: List<GeminiTool>? = null,
    val generationConfig: GeminiGenerationConfig? = null,
)

@Serializable
data class GeminiTool(
    val functionDeclarations: List<GeminiFunctionDeclaration>,
)

@Serializable
data class GeminiFunctionDeclaration(
    val name: String,
    val description: String,
    val parameters: JsonObject,
)

@Serializable
data class GeminiGenerationConfig(
    val responseMimeType: String? = null,
    val responseSchema: JsonObject? = null,
    val temperature: Double? = null,
    val maxOutputTokens: Int? = null,
)
