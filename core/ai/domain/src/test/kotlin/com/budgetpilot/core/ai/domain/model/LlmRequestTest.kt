package com.budgetpilot.core.ai.domain.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.junit.jupiter.api.Test

class LlmRequestTest {
    @Test
    fun `round-trips a minimal request with defaults`() {
        val request = LlmRequest(messages = listOf(ChatMessage(role = ChatRole.USER, parts = listOf(MessagePart.Text("hi")))))

        val json = Json.encodeToString(request)
        val decoded = Json.decodeFromString<LlmRequest>(json)

        assertThat(decoded).isEqualTo(request)
    }

    @Test
    fun `round-trips a full request with tools, schema, and generation config`() {
        val request =
            LlmRequest(
                messages = listOf(ChatMessage(role = ChatRole.USER, parts = listOf(MessagePart.Text("How much on food?")))),
                tools =
                    listOf(
                        ToolSchema(
                            name = "query_expenses",
                            description = "Query expenses.",
                            parameters = buildJsonObject { put("type", JsonPrimitive("object")) },
                        ),
                    ),
                responseSchema = buildJsonObject { put("type", JsonPrimitive("object")) },
                temperature = 0.2,
                maxOutputTokens = 512,
            )

        val json = Json.encodeToString(request)
        val decoded = Json.decodeFromString<LlmRequest>(json)

        assertThat(decoded).isEqualTo(request)
    }
}
