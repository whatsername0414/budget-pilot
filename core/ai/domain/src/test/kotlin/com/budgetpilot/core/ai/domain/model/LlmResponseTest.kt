package com.budgetpilot.core.ai.domain.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.junit.jupiter.api.Test

class LlmResponseTest {
    @Test
    fun `round-trips a text response`() {
        val response: LlmResponse = LlmResponse.Text(content = "You spent ₱1,234.56 on food.")

        val json = Json.encodeToString(response)
        val decoded = Json.decodeFromString<LlmResponse>(json)

        assertThat(decoded).isEqualTo(response)
    }

    @Test
    fun `round-trips a tool-calls response`() {
        val response: LlmResponse =
            LlmResponse.ToolCalls(
                calls =
                    listOf(
                        ToolCall(
                            name = "query_expenses",
                            args = buildJsonObject { put("category", JsonPrimitive("Food")) },
                        ),
                    ),
            )

        val json = Json.encodeToString(response)
        val decoded = Json.decodeFromString<LlmResponse>(json)

        assertThat(decoded).isEqualTo(response)
    }
}
