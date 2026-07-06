package com.budgetpilot.core.ai.data.mapper

import com.budgetpilot.core.ai.data.dto.GeminiContent
import com.budgetpilot.core.ai.data.dto.GeminiFunctionDeclaration
import com.budgetpilot.core.ai.data.dto.GeminiGenerateContentRequest
import com.budgetpilot.core.ai.data.dto.GeminiGenerationConfig
import com.budgetpilot.core.ai.data.dto.GeminiInlineData
import com.budgetpilot.core.ai.data.dto.GeminiPart
import com.budgetpilot.core.ai.data.dto.GeminiTool
import com.budgetpilot.core.ai.domain.model.ChatMessage
import com.budgetpilot.core.ai.domain.model.ChatRole
import com.budgetpilot.core.ai.domain.model.LlmRequest
import com.budgetpilot.core.ai.domain.model.MessagePart
import com.budgetpilot.core.ai.domain.model.ToolSchema

private const val GEMINI_ROLE_USER = "user"
private const val GEMINI_ROLE_MODEL = "model"
private const val RESPONSE_MIME_TYPE_JSON = "application/json"

fun LlmRequest.toGeminiRequest(): GeminiGenerateContentRequest {
    val (systemMessages, conversationMessages) = messages.partition { it.role == ChatRole.SYSTEM }

    return GeminiGenerateContentRequest(
        contents = conversationMessages.map { it.toGeminiContent() },
        systemInstruction = systemMessages.toSystemInstruction(),
        tools =
            tools.takeIf { it.isNotEmpty() }?.let { schemas ->
                listOf(GeminiTool(functionDeclarations = schemas.map { schema -> schema.toGeminiFunctionDeclaration() }))
            },
        generationConfig = toGenerationConfig(),
    )
}

private fun List<ChatMessage>.toSystemInstruction(): GeminiContent? =
    flatMap { it.parts }
        .takeIf { it.isNotEmpty() }
        ?.let { parts -> GeminiContent(parts = parts.map { part -> part.toGeminiPart() }) }

private fun LlmRequest.toGenerationConfig(): GeminiGenerationConfig? {
    if (responseSchema == null && temperature == null && maxOutputTokens == null) return null
    return GeminiGenerationConfig(
        responseMimeType = responseSchema?.let { RESPONSE_MIME_TYPE_JSON },
        responseSchema = responseSchema,
        temperature = temperature,
        maxOutputTokens = maxOutputTokens,
    )
}

private fun ChatMessage.toGeminiContent(): GeminiContent =
    GeminiContent(
        role = role.toGeminiRole(),
        parts =
            parts.map {
                it.toGeminiPart()
            },
    )

/**
 * Gemini's `contents` array only accepts "user"/"model" roles. There is no dedicated tool-result
 * role in the wire format we've verified (CLAUDE.md §10, P3.1) and the provider-agnostic
 * [MessagePart] has no function-response variant yet either (tracked as CLAUDE.md open question
 * #5) — so a [ChatRole.TOOL] message is passed straight through as plain "user" text for now.
 * Revisit once Phase 4's `AgentLoop` needs to round-trip real tool results.
 */
private fun ChatRole.toGeminiRole(): String =
    when (this) {
        ChatRole.USER, ChatRole.TOOL -> GEMINI_ROLE_USER
        ChatRole.MODEL -> GEMINI_ROLE_MODEL
        ChatRole.SYSTEM -> error("SYSTEM messages must be extracted into systemInstruction before mapping")
    }

private fun MessagePart.toGeminiPart(): GeminiPart =
    when (this) {
        is MessagePart.Text -> GeminiPart(text = text)
        is MessagePart.Image -> GeminiPart(inlineData = GeminiInlineData(mimeType = mimeType, data = base64Data))
    }

private fun ToolSchema.toGeminiFunctionDeclaration(): GeminiFunctionDeclaration =
    GeminiFunctionDeclaration(name = name, description = description, parameters = parameters)
