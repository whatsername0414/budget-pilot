package com.budgetpilot.core.ai.domain.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
}
