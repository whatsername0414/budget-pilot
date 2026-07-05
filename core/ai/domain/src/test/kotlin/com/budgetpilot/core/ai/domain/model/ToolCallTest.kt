package com.budgetpilot.core.ai.domain.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.junit.jupiter.api.Test

class ToolCallTest {
    @Test
    fun `round-trips a tool call with args`() {
        val call =
            ToolCall(
                name = "get_budget_status",
                args = buildJsonObject { put("month", JsonPrimitive("2026-07")) },
            )

        val json = Json.encodeToString(call)
        val decoded = Json.decodeFromString<ToolCall>(json)

        assertThat(decoded).isEqualTo(call)
    }

    @Test
    fun `round-trips a tool call with empty args`() {
        val call = ToolCall(name = "get_categories", args = buildJsonObject { })

        val json = Json.encodeToString(call)
        val decoded = Json.decodeFromString<ToolCall>(json)

        assertThat(decoded).isEqualTo(call)
    }
}
