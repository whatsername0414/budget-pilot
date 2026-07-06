package com.budgetpilot.core.ai.domain.tool

import com.budgetpilot.core.ai.domain.AgentTool
import com.budgetpilot.core.ai.domain.ToolError
import com.budgetpilot.core.ai.domain.model.ToolSchema
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class GetCategoriesTool(
    private val categoryRepository: CategoryRepository,
) : AgentTool {
    override val schema =
        ToolSchema(
            name = "get_categories",
            description = "List all expense categories the user can assign expenses and budgets to.",
            parameters =
                buildJsonObject {
                    put("type", JsonPrimitive("object"))
                    put("properties", buildJsonObject {})
                },
        )

    override suspend fun execute(args: JsonObject): Result<JsonElement, ToolError> {
        val categories = categoryRepository.observeCategories().first()
        return Result.Success(
            buildJsonObject {
                put(
                    "categories",
                    buildJsonArray {
                        categories.forEach { category ->
                            add(
                                buildJsonObject {
                                    put("id", JsonPrimitive(category.id))
                                    put("name", JsonPrimitive(category.name))
                                },
                            )
                        }
                    },
                )
            },
        )
    }
}
