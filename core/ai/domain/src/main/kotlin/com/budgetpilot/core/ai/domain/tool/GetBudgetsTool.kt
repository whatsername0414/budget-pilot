package com.budgetpilot.core.ai.domain.tool

import com.budgetpilot.core.ai.domain.AgentTool
import com.budgetpilot.core.ai.domain.ToolError
import com.budgetpilot.core.ai.domain.model.ToolSchema
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.repository.BudgetRepository
import com.budgetpilot.core.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class GetBudgetsTool(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
) : AgentTool {
    override val schema =
        ToolSchema(
            name = "get_budgets",
            description = "List the user's monthly budget amount per category for a given month.",
            parameters =
                buildJsonObject {
                    put("type", JsonPrimitive("object"))
                    put(
                        "properties",
                        buildJsonObject {
                            put(
                                "month",
                                buildJsonObject {
                                    put("type", JsonPrimitive("string"))
                                    put("description", JsonPrimitive("Month to list budgets for, ISO yyyy-MM."))
                                },
                            )
                        },
                    )
                    put("required", buildJsonArray { add(JsonPrimitive("month")) })
                },
        )

    override suspend fun execute(args: JsonObject): Result<JsonElement, ToolError> {
        val month = args.stringOrNull("month") ?: return Result.Error(ToolError("Missing \"month\" (expected yyyy-MM)."))
        if (!MONTH_PATTERN.matches(month)) {
            return Result.Error(ToolError("Invalid \"month\" \"$month\" (expected yyyy-MM)."))
        }

        val categoriesById = categoryRepository.observeCategories().first().associateBy { it.id }
        val budgets = budgetRepository.observeBudgetsForMonth(month).first()

        return Result.Success(
            buildJsonObject {
                put("month", JsonPrimitive(month))
                put(
                    "budgets",
                    buildJsonArray {
                        budgets.forEach { budget ->
                            add(
                                buildJsonObject {
                                    put("category", JsonPrimitive(categoriesById[budget.categoryId]?.name ?: "Unknown"))
                                    put("amount_pesos", JsonPrimitive(budget.amount.toPesoString()))
                                },
                            )
                        }
                    },
                )
            },
        )
    }
}
