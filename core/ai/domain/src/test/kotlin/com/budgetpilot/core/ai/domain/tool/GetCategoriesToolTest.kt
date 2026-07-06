package com.budgetpilot.core.ai.domain.tool

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import com.budgetpilot.core.ai.domain.fake.FakeCategoryRepository
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.model.Category
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.jupiter.api.Test

class GetCategoriesToolTest {
    @Test
    fun `lists categories with id and name`() =
        runTest {
            val tool =
                GetCategoriesTool(
                    categoryRepository =
                        FakeCategoryRepository(
                            listOf(
                                Category(id = 1, name = "Food", iconKey = "restaurant", colorKey = "food", isDefault = true),
                            ),
                        ),
                )

            val result = tool.execute(JsonObject(emptyMap()))

            val data = (result as Result.Success).data.jsonObject
            val categories = data["categories"]!!.jsonArray
            assertThat(categories).hasSize(1)
            val first = categories[0].jsonObject
            assertThat(first["id"]!!.jsonPrimitive.content).isEqualTo("1")
            assertThat(first["name"]!!.jsonPrimitive.content).isEqualTo("Food")
        }
}
