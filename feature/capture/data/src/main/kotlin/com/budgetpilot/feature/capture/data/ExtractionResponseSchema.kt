package com.budgetpilot.feature.capture.data

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject

/**
 * Gemini `responseSchema` (JSON mode) mirroring `extraction_v1.md`'s output schema field-for-field
 * (CLAUDE.md §10, P3.1: `type` uses Gemini's own uppercase enum strings, e.g. `"OBJECT"`).
 */
internal val EXTRACTION_RESPONSE_SCHEMA: JsonObject =
    buildJsonObject {
        put("type", JsonPrimitive("OBJECT"))
        put(
            "properties",
            buildJsonObject {
                put(
                    "receipt_type",
                    buildJsonObject {
                        put("type", JsonPrimitive("STRING"))
                        put("enum", stringArray("PAPER", "GCASH", "MAYA"))
                    },
                )
                put("merchant", confidenceFieldSchema(stringSchema()))
                put("date", confidenceFieldSchema(stringSchema()))
                put("line_items", confidenceFieldSchema(lineItemsArraySchema()))
                put("total", confidenceFieldSchema(numberSchema()))
                put("suggested_category", confidenceFieldSchema(stringSchema(nullable = true)))
            },
        )
        put(
            "required",
            stringArray("receipt_type", "merchant", "date", "line_items", "total", "suggested_category"),
        )
    }

private fun confidenceFieldSchema(valueSchema: JsonObject): JsonObject =
    buildJsonObject {
        put("type", JsonPrimitive("OBJECT"))
        put(
            "properties",
            buildJsonObject {
                put("value", valueSchema)
                put(
                    "confidence",
                    buildJsonObject {
                        put("type", JsonPrimitive("STRING"))
                        put("enum", stringArray("HIGH", "MEDIUM", "LOW"))
                    },
                )
            },
        )
        put("required", stringArray("value", "confidence"))
    }

private fun stringSchema(nullable: Boolean = false): JsonObject =
    buildJsonObject {
        put("type", JsonPrimitive("STRING"))
        if (nullable) put("nullable", JsonPrimitive(true))
    }

private fun numberSchema(): JsonObject = buildJsonObject { put("type", JsonPrimitive("NUMBER")) }

private fun lineItemsArraySchema(): JsonObject =
    buildJsonObject {
        put("type", JsonPrimitive("ARRAY"))
        put(
            "items",
            buildJsonObject {
                put("type", JsonPrimitive("OBJECT"))
                put(
                    "properties",
                    buildJsonObject {
                        put("description", stringSchema())
                        put("amount", numberSchema())
                    },
                )
                put("required", stringArray("description", "amount"))
            },
        )
    }

private fun stringArray(vararg values: String) = buildJsonArray { values.forEach { add(JsonPrimitive(it)) } }
