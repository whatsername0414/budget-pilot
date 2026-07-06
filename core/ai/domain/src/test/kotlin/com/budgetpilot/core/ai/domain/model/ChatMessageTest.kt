package com.budgetpilot.core.ai.domain.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.Test

class ChatMessageTest {
    @Test
    fun `round-trips a message with a text part`() {
        val message = ChatMessage(role = ChatRole.USER, parts = listOf(MessagePart.Text("How much did I spend?")))

        val json = Json.encodeToString(message)
        val decoded = Json.decodeFromString<ChatMessage>(json)

        assertThat(decoded).isEqualTo(message)
    }

    @Test
    fun `round-trips a message with an image part`() {
        val message =
            ChatMessage(
                role = ChatRole.USER,
                parts = listOf(MessagePart.Image(mimeType = "image/jpeg", base64Data = "AAAA")),
            )

        val json = Json.encodeToString(message)
        val decoded = Json.decodeFromString<ChatMessage>(json)

        assertThat(decoded).isEqualTo(message)
    }

    @Test
    fun `round-trips a message with mixed parts`() {
        val message =
            ChatMessage(
                role = ChatRole.MODEL,
                parts =
                    listOf(
                        MessagePart.Text("Here is the receipt:"),
                        MessagePart.Image(mimeType = "image/png", base64Data = "BBBB"),
                    ),
            )

        val json = Json.encodeToString(message)
        val decoded = Json.decodeFromString<ChatMessage>(json)

        assertThat(decoded).isEqualTo(message)
    }

    @Test
    fun `round-trips a model message with a function call part`() {
        val message =
            ChatMessage(
                role = ChatRole.MODEL,
                parts = listOf(MessagePart.FunctionCall(name = "get_categories", args = buildJsonObject {})),
            )

        val json = Json.encodeToString(message)
        val decoded = Json.decodeFromString<ChatMessage>(json)

        assertThat(decoded).isEqualTo(message)
    }

    @Test
    fun `round-trips a tool message with a function response part`() {
        val message =
            ChatMessage(
                role = ChatRole.TOOL,
                parts =
                    listOf(
                        MessagePart.FunctionResponse(
                            name = "get_categories",
                            response = buildJsonObject { put("count", JsonPrimitive(3)) },
                        ),
                    ),
            )

        val json = Json.encodeToString(message)
        val decoded = Json.decodeFromString<ChatMessage>(json)

        assertThat(decoded).isEqualTo(message)
    }
}
