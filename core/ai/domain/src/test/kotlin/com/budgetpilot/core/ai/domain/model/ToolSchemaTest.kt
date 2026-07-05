package com.budgetpilot.core.ai.domain.model

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.junit.jupiter.api.Test

class ToolSchemaTest {
    @Test
    fun `round-trips a tool schema with JSON-schema parameters`() {
        val schema =
            ToolSchema(
                name = "query_expenses",
                description = "Query expenses by category, merchant, and date range.",
                parameters =
                    buildJsonObject {
                        put("type", JsonPrimitive("object"))
                        put(
                            "properties",
                            buildJsonObject {
                                put(
                                    "category",
                                    buildJsonObject { put("type", JsonPrimitive("string")) },
                                )
                            },
                        )
                    },
            )

        val json = Json.encodeToString(schema)
        val decoded = Json.decodeFromString<ToolSchema>(json)

        assertThat(decoded).isEqualTo(schema)
    }
}
