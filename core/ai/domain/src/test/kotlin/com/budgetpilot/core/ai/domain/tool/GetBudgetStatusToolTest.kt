package com.budgetpilot.core.ai.domain.tool

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.budgetpilot.core.ai.domain.fake.FakeBudgetRepository
import com.budgetpilot.core.ai.domain.fake.FakeCategoryRepository
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.model.Budget
import com.budgetpilot.core.domain.model.Category
import com.budgetpilot.core.domain.money.Money
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.junit.jupiter.api.Test

class GetBudgetStatusToolTest {
    private val foodCategory = Category(id = 1, name = "Food", iconKey = "restaurant", colorKey = "food", isDefault = true)

    @Test
    fun `reports spend, percent used, and status per budgeted category`() =
        runTest {
            val tool =
                GetBudgetStatusTool(
                    budgetRepository =
                        FakeBudgetRepository(
                            budgets = listOf(Budget(id = 1, categoryId = 1, month = "2026-07", amount = Money.fromPesos("1000"))),
                            spendByCategoryAndMonth = mapOf((1L to "2026-07") to Money.fromPesos("900")),
                        ),
                    categoryRepository = FakeCategoryRepository(listOf(foodCategory)),
                )

            val result = tool.execute(buildJsonObject { put("month", "2026-07") })

            val data = (result as Result.Success).data.jsonObject
            val categories = data["categories"]!!.jsonArray
            assertThat(categories).hasSize(1)
            val row = categories[0].jsonObject
            assertThat(row["category"]!!.jsonPrimitive.content).isEqualTo("Food")
            assertThat(row["spent_pesos"]!!.jsonPrimitive.content).isEqualTo("900.00")
            assertThat(row["budgeted_pesos"]!!.jsonPrimitive.content).isEqualTo("1000.00")
            assertThat(row["status"]!!.jsonPrimitive.content).isEqualTo("warning")
        }

    @Test
    fun `marks over-budget categories`() =
        runTest {
            val tool =
                GetBudgetStatusTool(
                    budgetRepository =
                        FakeBudgetRepository(
                            budgets = listOf(Budget(id = 1, categoryId = 1, month = "2026-07", amount = Money.fromPesos("1000"))),
                            spendByCategoryAndMonth = mapOf((1L to "2026-07") to Money.fromPesos("1200")),
                        ),
                    categoryRepository = FakeCategoryRepository(listOf(foodCategory)),
                )

            val result = tool.execute(buildJsonObject { put("month", "2026-07") })

            val data = (result as Result.Success).data.jsonObject
            val row = data["categories"]!!.jsonArray[0].jsonObject
            assertThat(row["status"]!!.jsonPrimitive.content).isEqualTo("over_budget")
        }

    @Test
    fun `rejects a malformed month`() =
        runTest {
            val tool = GetBudgetStatusTool(FakeBudgetRepository(), FakeCategoryRepository())

            val result = tool.execute(buildJsonObject { put("month", "July 2026") })

            assertThat(result).isInstanceOf<Result.Error<*>>()
        }

    @Test
    fun `rejects a missing month`() =
        runTest {
            val tool = GetBudgetStatusTool(FakeBudgetRepository(), FakeCategoryRepository())

            val result = tool.execute(JsonObject(emptyMap()))

            assertThat(result).isInstanceOf<Result.Error<*>>()
        }
}
