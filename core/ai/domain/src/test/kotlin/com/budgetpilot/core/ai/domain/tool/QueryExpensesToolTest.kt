package com.budgetpilot.core.ai.domain.tool

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.budgetpilot.core.ai.domain.fake.FakeCategoryRepository
import com.budgetpilot.core.ai.domain.fake.FakeExpenseRepository
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.model.Category
import com.budgetpilot.core.domain.model.Expense
import com.budgetpilot.core.domain.model.ExpenseSource
import com.budgetpilot.core.domain.money.Money
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate

class QueryExpensesToolTest {
    private val foodCategory = Category(id = 1, name = "Food", iconKey = "restaurant", colorKey = "food", isDefault = true)
    private val transportCategory = Category(id = 2, name = "Transport", iconKey = "bus", colorKey = "transport", isDefault = true)

    private fun expense(
        id: Long,
        amount: String,
        merchant: String,
        categoryId: Long,
        date: LocalDate,
    ) = Expense(
        id = id,
        amount = Money.fromPesos(amount),
        merchant = merchant,
        categoryId = categoryId,
        date = date,
        note = null,
        source = ExpenseSource.MANUAL,
        imageUri = null,
        createdAt = Instant.EPOCH,
    )

    private fun tool(expenses: List<Expense>) =
        QueryExpensesTool(
            expenseRepository = FakeExpenseRepository(expenses),
            categoryRepository = FakeCategoryRepository(listOf(foodCategory, transportCategory)),
        )

    @Test
    fun `sums matching expenses`() =
        runTest {
            val result =
                tool(
                    listOf(
                        expense(1, "500", "Jollibee", 1, LocalDate.of(2026, 7, 1)),
                        expense(2, "300", "SM", 1, LocalDate.of(2026, 7, 2)),
                        expense(3, "999", "Grab", 2, LocalDate.of(2026, 7, 3)),
                    ),
                ).execute(
                    buildJsonObject {
                        put("category", "Food")
                        put("start_date", "2026-07-01")
                        put("end_date", "2026-07-31")
                        put("aggregation", "sum")
                    },
                )

            val data = (result as Result.Success).data.jsonObject
            assertThat(data["total_pesos"]!!.jsonPrimitive.content).isEqualTo("800.00")
            assertThat(data["count"]!!.jsonPrimitive.content).isEqualTo("2")
        }

    @Test
    fun `counts matching expenses`() =
        runTest {
            val result =
                tool(
                    listOf(
                        expense(1, "500", "Jollibee", 1, LocalDate.of(2026, 7, 1)),
                        expense(2, "300", "SM", 1, LocalDate.of(2026, 7, 2)),
                    ),
                ).execute(
                    buildJsonObject {
                        put("start_date", "2026-07-01")
                        put("end_date", "2026-07-31")
                        put("aggregation", "count")
                    },
                )

            val data = (result as Result.Success).data.jsonObject
            assertThat(data["count"]!!.jsonPrimitive.content).isEqualTo("2")
        }

    @Test
    fun `averages matching expenses`() =
        runTest {
            val result =
                tool(
                    listOf(
                        expense(1, "500", "Jollibee", 1, LocalDate.of(2026, 7, 1)),
                        expense(2, "300", "SM", 1, LocalDate.of(2026, 7, 2)),
                    ),
                ).execute(
                    buildJsonObject {
                        put("start_date", "2026-07-01")
                        put("end_date", "2026-07-31")
                        put("aggregation", "avg")
                    },
                )

            val data = (result as Result.Success).data.jsonObject
            assertThat(data["average_pesos"]!!.jsonPrimitive.content).isEqualTo("400.00")
        }

    @Test
    fun `groups by category`() =
        runTest {
            val result =
                tool(
                    listOf(
                        expense(1, "500", "Jollibee", 1, LocalDate.of(2026, 7, 1)),
                        expense(2, "300", "SM", 1, LocalDate.of(2026, 7, 2)),
                        expense(3, "999", "Grab", 2, LocalDate.of(2026, 7, 3)),
                    ),
                ).execute(
                    buildJsonObject {
                        put("start_date", "2026-07-01")
                        put("end_date", "2026-07-31")
                        put("aggregation", "group_by_category")
                    },
                )

            val groups = (result as Result.Success).data.jsonObject["groups"]!!.jsonArray
            assertThat(groups).hasSize(2)
            val food = groups.first { it.jsonObject["category"]!!.jsonPrimitive.content == "Food" }.jsonObject
            assertThat(food["total_pesos"]!!.jsonPrimitive.content).isEqualTo("800.00")
            assertThat(food["count"]!!.jsonPrimitive.content).isEqualTo("2")
        }

    @Test
    fun `filters by merchant`() =
        runTest {
            val result =
                tool(
                    listOf(
                        expense(1, "500", "Jollibee", 1, LocalDate.of(2026, 7, 1)),
                        expense(2, "300", "SM Supermarket", 1, LocalDate.of(2026, 7, 2)),
                    ),
                ).execute(
                    buildJsonObject {
                        put("merchant", "jollibee")
                        put("start_date", "2026-07-01")
                        put("end_date", "2026-07-31")
                        put("aggregation", "sum")
                    },
                )

            val data = (result as Result.Success).data.jsonObject
            assertThat(data["total_pesos"]!!.jsonPrimitive.content).isEqualTo("500.00")
        }

    @Test
    fun `rejects an unknown category`() =
        runTest {
            val result =
                tool(emptyList()).execute(
                    buildJsonObject {
                        put("category", "Rent")
                        put("start_date", "2026-07-01")
                        put("end_date", "2026-07-31")
                        put("aggregation", "sum")
                    },
                )

            assertThat(result).isInstanceOf<Result.Error<*>>()
        }

    @Test
    fun `rejects a malformed date`() =
        runTest {
            val result =
                tool(emptyList()).execute(
                    buildJsonObject {
                        put("start_date", "not-a-date")
                        put("end_date", "2026-07-31")
                        put("aggregation", "sum")
                    },
                )

            assertThat(result).isInstanceOf<Result.Error<*>>()
        }

    @Test
    fun `rejects an unknown aggregation`() =
        runTest {
            val result =
                tool(emptyList()).execute(
                    buildJsonObject {
                        put("start_date", "2026-07-01")
                        put("end_date", "2026-07-31")
                        put("aggregation", "median")
                    },
                )

            assertThat(result).isInstanceOf<Result.Error<*>>()
        }

    @Test
    fun `rejects missing required args`() =
        runTest {
            val result = tool(emptyList()).execute(JsonObject(emptyMap()))

            assertThat(result).isInstanceOf<Result.Error<*>>()
        }
}
