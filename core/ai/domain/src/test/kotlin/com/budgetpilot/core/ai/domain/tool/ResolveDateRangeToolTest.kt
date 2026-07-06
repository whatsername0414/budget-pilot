package com.budgetpilot.core.ai.domain.tool

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.budgetpilot.core.domain.Result
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

/** "Today" is fixed to Wednesday 2026-07-15 for every case here. */
class ResolveDateRangeToolTest {
    private val clock =
        Clock.fixed(Instant.parse("2026-07-15T12:00:00Z"), ZoneOffset.UTC)
    private val tool = ResolveDateRangeTool(clock)

    private suspend fun resolve(query: String): JsonObject {
        val result = tool.execute(buildJsonObject { put("query", query) })
        return (result as Result.Success).data.jsonObject
    }

    @Test
    fun `today`() =
        runTest {
            val range = resolve("today")
            assertThat(range["start_date"]!!.jsonPrimitive.content).isEqualTo("2026-07-15")
            assertThat(range["end_date"]!!.jsonPrimitive.content).isEqualTo("2026-07-15")
        }

    @Test
    fun `yesterday`() =
        runTest {
            val range = resolve("yesterday")
            assertThat(range["start_date"]!!.jsonPrimitive.content).isEqualTo("2026-07-14")
            assertThat(range["end_date"]!!.jsonPrimitive.content).isEqualTo("2026-07-14")
        }

    @Test
    fun `this week is monday through sunday of the current week`() =
        runTest {
            val range = resolve("this week")
            assertThat(range["start_date"]!!.jsonPrimitive.content).isEqualTo("2026-07-13")
            assertThat(range["end_date"]!!.jsonPrimitive.content).isEqualTo("2026-07-19")
        }

    @Test
    fun `last week is the prior monday through sunday`() =
        runTest {
            val range = resolve("last week")
            assertThat(range["start_date"]!!.jsonPrimitive.content).isEqualTo("2026-07-06")
            assertThat(range["end_date"]!!.jsonPrimitive.content).isEqualTo("2026-07-12")
        }

    @Test
    fun `this month`() =
        runTest {
            val range = resolve("this month")
            assertThat(range["start_date"]!!.jsonPrimitive.content).isEqualTo("2026-07-01")
            assertThat(range["end_date"]!!.jsonPrimitive.content).isEqualTo("2026-07-31")
        }

    @Test
    fun `last month crosses a year boundary correctly in January`() =
        runTest {
            val januaryClock = Clock.fixed(Instant.parse("2026-01-15T12:00:00Z"), ZoneOffset.UTC)
            val januaryTool = ResolveDateRangeTool(januaryClock)
            val result = januaryTool.execute(buildJsonObject { put("query", "last month") })
            val range = (result as Result.Success).data.jsonObject

            assertThat(range["start_date"]!!.jsonPrimitive.content).isEqualTo("2025-12-01")
            assertThat(range["end_date"]!!.jsonPrimitive.content).isEqualTo("2025-12-31")
        }

    @Test
    fun `last month in a normal month`() =
        runTest {
            val range = resolve("last month")
            assertThat(range["start_date"]!!.jsonPrimitive.content).isEqualTo("2026-06-01")
            assertThat(range["end_date"]!!.jsonPrimitive.content).isEqualTo("2026-06-30")
        }

    @Test
    fun `last N days includes today`() =
        runTest {
            val range = resolve("last 7 days")
            assertThat(range["start_date"]!!.jsonPrimitive.content).isEqualTo("2026-07-09")
            assertThat(range["end_date"]!!.jsonPrimitive.content).isEqualTo("2026-07-15")
        }

    @Test
    fun `month name without year assumes the current year`() =
        runTest {
            val range = resolve("June")
            assertThat(range["start_date"]!!.jsonPrimitive.content).isEqualTo("2026-06-01")
            assertThat(range["end_date"]!!.jsonPrimitive.content).isEqualTo("2026-06-30")
        }

    @Test
    fun `month name with an explicit year`() =
        runTest {
            val range = resolve("December 2025")
            assertThat(range["start_date"]!!.jsonPrimitive.content).isEqualTo("2025-12-01")
            assertThat(range["end_date"]!!.jsonPrimitive.content).isEqualTo("2025-12-31")
        }

    @Test
    fun `year only`() =
        runTest {
            val range = resolve("2025")
            assertThat(range["start_date"]!!.jsonPrimitive.content).isEqualTo("2025-01-01")
            assertThat(range["end_date"]!!.jsonPrimitive.content).isEqualTo("2025-12-31")
        }

    @Test
    fun `is case-insensitive`() =
        runTest {
            val range = resolve("THIS MONTH")
            assertThat(range["start_date"]!!.jsonPrimitive.content).isEqualTo("2026-07-01")
        }

    @Test
    fun `unresolvable query returns an error`() =
        runTest {
            val result = tool.execute(buildJsonObject { put("query", "since payday") })
            assertThat(result).isInstanceOf<Result.Error<*>>()
        }

    @Test
    fun `missing query returns an error`() =
        runTest {
            val result = tool.execute(JsonObject(emptyMap()))
            assertThat(result).isInstanceOf<Result.Error<*>>()
        }
}
