package com.budgetpilot.core.ai.data.mapper

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.budgetpilot.core.ai.data.dto.GeminiGenerateContentRequest
import com.budgetpilot.core.ai.domain.model.ChatMessage
import com.budgetpilot.core.ai.domain.model.ChatRole
import com.budgetpilot.core.ai.domain.model.LlmRequest
import com.budgetpilot.core.ai.domain.model.MessagePart
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.Test

private val json = Json { ignoreUnknownKeys = true }

class GeminiRequestMapperTest {
    @Test
    fun `a multi-turn tool exchange maps the model's call and the tool's response to the right roles and parts`() {
        val request =
            LlmRequest(
                messages =
                    listOf(
                        ChatMessage(ChatRole.SYSTEM, listOf(MessagePart.Text("system prompt"))),
                        ChatMessage(ChatRole.USER, listOf(MessagePart.Text("How much did I spend on food?"))),
                        ChatMessage(
                            ChatRole.MODEL,
                            listOf(MessagePart.FunctionCall(name = "get_categories", args = buildJsonObject {})),
                        ),
                        ChatMessage(
                            ChatRole.TOOL,
                            listOf(
                                MessagePart.FunctionResponse(
                                    name = "get_categories",
                                    response = buildJsonObject { put("count", JsonPrimitive(3)) },
                                ),
                            ),
                        ),
                    ),
            )

        val geminiRequest = request.toGeminiRequest()

        val systemInstructionText =
            geminiRequest.systemInstruction
                ?.parts
                ?.first()
                ?.text
        assertThat(systemInstructionText).isEqualTo("system prompt")

        val modelContent = geminiRequest.contents[1]
        assertThat(modelContent.role).isEqualTo("model")
        val functionCallName =
            modelContent.parts
                .first()
                .functionCall
                ?.name
        assertThat(functionCallName).isEqualTo("get_categories")

        val toolContent = geminiRequest.contents[2]
        assertThat(toolContent.role).isEqualTo("user")
        val functionResponse = requireNotNull(toolContent.parts.first().functionResponse)
        assertThat(functionResponse.name).isEqualTo("get_categories")
        assertThat(functionResponse.response["count"]).isEqualTo(JsonPrimitive(3))
    }

    @Test
    fun `a mapped tool exchange round-trips through the wire JSON format unchanged`() {
        val request =
            LlmRequest(
                messages =
                    listOf(
                        ChatMessage(ChatRole.USER, listOf(MessagePart.Text("goal"))),
                        ChatMessage(
                            ChatRole.MODEL,
                            listOf(
                                MessagePart.FunctionCall(
                                    name = "get_budgets",
                                    args = buildJsonObject { put("month", JsonPrimitive("2026-07")) },
                                ),
                            ),
                        ),
                        ChatMessage(
                            ChatRole.TOOL,
                            listOf(
                                MessagePart.FunctionResponse(
                                    name = "get_budgets",
                                    response = buildJsonObject { put("result", JsonPrimitive("ok")) },
                                ),
                            ),
                        ),
                    ),
            )

        val geminiRequest = request.toGeminiRequest()
        val encoded = json.encodeToString(geminiRequest)
        val decoded = json.decodeFromString<GeminiGenerateContentRequest>(encoded)

        assertThat(decoded).isEqualTo(geminiRequest)
    }
}
