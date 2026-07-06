package com.budgetpilot.core.ai.data.mapper

import com.budgetpilot.core.ai.data.dto.GeminiGenerateContentResponse
import com.budgetpilot.core.ai.domain.AiError
import com.budgetpilot.core.ai.domain.model.LlmResponse
import com.budgetpilot.core.ai.domain.model.ToolCall
import com.budgetpilot.core.domain.Result

fun GeminiGenerateContentResponse.toLlmResponse(): Result<LlmResponse, AiError> {
    val parts =
        candidates
            .firstOrNull()
            ?.content
            ?.parts
            .orEmpty()
    if (parts.isEmpty()) return Result.Error(AiError.MalformedOutput)

    val toolCalls = parts.mapNotNull { it.functionCall }.map { ToolCall(name = it.name, args = it.args) }
    if (toolCalls.isNotEmpty()) return Result.Success(LlmResponse.ToolCalls(toolCalls))

    val text = parts.mapNotNull { it.text }.joinToString(separator = "")
    if (text.isBlank()) return Result.Error(AiError.MalformedOutput)
    return Result.Success(LlmResponse.Text(text))
}
