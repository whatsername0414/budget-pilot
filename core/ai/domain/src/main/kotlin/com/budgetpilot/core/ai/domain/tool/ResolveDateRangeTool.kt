package com.budgetpilot.core.ai.domain.tool

import com.budgetpilot.core.ai.domain.AgentTool
import com.budgetpilot.core.ai.domain.ToolError
import com.budgetpilot.core.ai.domain.model.ToolSchema
import com.budgetpilot.core.domain.Result
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.YearMonth
import java.time.temporal.TemporalAdjusters

class ResolveDateRangeTool(
    private val clock: Clock = Clock.systemDefaultZone(),
) : AgentTool {
    override val schema =
        ToolSchema(
            name = "resolve_date_range",
            description =
                "Resolve a natural-language date expression (e.g. \"today\", \"last month\", " +
                    "\"this week\", \"last 7 days\", \"June\", \"December 2025\", \"2025\") to an " +
                    "inclusive ISO yyyy-MM-dd start/end date range.",
            parameters =
                buildJsonObject {
                    put("type", JsonPrimitive("object"))
                    put(
                        "properties",
                        buildJsonObject {
                            put(
                                "query",
                                buildJsonObject {
                                    put("type", JsonPrimitive("string"))
                                    put(
                                        "description",
                                        JsonPrimitive(
                                            "Natural-language date range, e.g. \"today\", \"last month\", \"last 7 days\".",
                                        ),
                                    )
                                },
                            )
                        },
                    )
                    put("required", buildJsonArray { add(JsonPrimitive("query")) })
                },
        )

    override suspend fun execute(args: JsonObject): Result<JsonElement, ToolError> {
        val query = args.stringOrNull("query") ?: return Result.Error(ToolError("Missing \"query\"."))
        val range =
            resolve(query.trim().lowercase())
                ?: return Result.Error(ToolError("Could not resolve date range \"$query\"."))

        return Result.Success(
            buildJsonObject {
                put("start_date", JsonPrimitive(range.first.toString()))
                put("end_date", JsonPrimitive(range.second.toString()))
            },
        )
    }

    private fun resolve(query: String): Pair<LocalDate, LocalDate>? {
        val today = LocalDate.now(clock)
        return when {
            query == "today" -> today to today
            query == "yesterday" -> today.minusDays(1).let { it to it }
            query == "this week" -> weekRange(today)
            query == "last week" -> weekRange(today.minusWeeks(1))
            query == "this month" -> monthRange(YearMonth.from(today))
            query == "last month" -> monthRange(YearMonth.from(today).minusMonths(1))
            LAST_N_DAYS.matches(query) -> lastNDays(query, today)
            YEAR_ONLY.matches(query) -> yearRange(query.toInt())
            else -> resolveMonthName(query, today)
        }
    }

    private fun lastNDays(
        query: String,
        today: LocalDate,
    ): Pair<LocalDate, LocalDate>? {
        val n = LAST_N_DAYS.matchEntire(query)!!.groupValues[1].toInt()
        if (n <= 0) return null
        return today.minusDays((n - 1).toLong()) to today
    }

    private fun yearRange(year: Int): Pair<LocalDate, LocalDate> {
        val start = monthRange(YearMonth.of(year, Month.JANUARY)).first
        val end = monthRange(YearMonth.of(year, Month.DECEMBER)).second
        return start to end
    }

    private fun resolveMonthName(
        query: String,
        today: LocalDate,
    ): Pair<LocalDate, LocalDate>? {
        val match = MONTH_NAME.matchEntire(query) ?: return null
        val month = MONTH_NAMES[match.groupValues[1]] ?: return null
        val year = match.groupValues[2].takeIf { it.isNotEmpty() }?.toInt() ?: today.year
        return monthRange(YearMonth.of(year, month))
    }

    private fun weekRange(reference: LocalDate): Pair<LocalDate, LocalDate> {
        val start = reference.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        return start to start.plusWeeks(1).minusDays(1)
    }

    private fun monthRange(yearMonth: YearMonth): Pair<LocalDate, LocalDate> = yearMonth.atDay(1) to yearMonth.atEndOfMonth()

    private companion object {
        val LAST_N_DAYS = Regex("""last (\d+) days?""")
        val YEAR_ONLY = Regex("""\d{4}""")
        val MONTH_NAME =
            Regex("""(january|february|march|april|may|june|july|august|september|october|november|december)\s*(\d{4})?""")
        val MONTH_NAMES =
            mapOf(
                "january" to 1,
                "february" to 2,
                "march" to 3,
                "april" to 4,
                "may" to 5,
                "june" to 6,
                "july" to 7,
                "august" to 8,
                "september" to 9,
                "october" to 10,
                "november" to 11,
                "december" to 12,
            )
    }
}
