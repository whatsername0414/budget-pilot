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

class GetBudgetsToolTest {
    private val foodCategory = Category(id = 1, name = "Food", iconKey = "restaurant", colorKey = "food", isDefault = true)

    @Test
    fun `lists budgets for the requested month with category names`() =
        runTest {
            val tool =
                GetBudgetsTool(
                    budgetRepository =
                        FakeBudgetRepository(
                            budgets =
                                listOf(
                                    Budget(id = 1, categoryId = 1, month = "2026-07", amount = Money.fromPesos("3000")),
                                    Budget(id = 2, categoryId = 1, month = "2026-06", amount = Money.fromPesos("2000")),
                                ),
                        ),
                    categoryRepository = FakeCategoryRepository(listOf(foodCategory)),
                )

            val result = tool.execute(buildJsonObject { put("month", "2026-07") })

            val data = (result as Result.Success).data.jsonObject
            val budgets = data["budgets"]!!.jsonArray
            assertThat(budgets).hasSize(1)
            val first = budgets[0].jsonObject
            assertThat(first["category"]!!.jsonPrimitive.content).isEqualTo("Food")
            assertThat(first["amount_pesos"]!!.jsonPrimitive.content).isEqualTo("3000.00")
        }

    @Test
    fun `rejects a malformed month`() =
        runTest {
            val tool = GetBudgetsTool(FakeBudgetRepository(), FakeCategoryRepository())

            val result = tool.execute(buildJsonObject { put("month", "not-a-month") })

            assertThat(result).isInstanceOf<Result.Error<*>>()
        }

    @Test
    fun `rejects a missing month`() =
        runTest {
            val tool = GetBudgetsTool(FakeBudgetRepository(), FakeCategoryRepository())

            val result = tool.execute(JsonObject(emptyMap()))

            assertThat(result).isInstanceOf<Result.Error<*>>()
        }
}
