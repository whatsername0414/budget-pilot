package com.budgetpilot.core.ai.domain.tool

import com.budgetpilot.core.ai.domain.AgentTool
import com.budgetpilot.core.ai.domain.ToolError
import com.budgetpilot.core.ai.domain.model.ToolSchema
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.budget.BudgetMath
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.core.domain.repository.BudgetRepository
import com.budgetpilot.core.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class GetBudgetStatusTool(
    private val budgetRepository: BudgetRepository,
    private val categoryRepository: CategoryRepository,
) : AgentTool {
    override val schema =
        ToolSchema(
            name = "get_budget_status",
            description =
                "For a given month, report each budgeted category's spend vs. budget, percent " +
                    "used, and status (on_track, warning, over_budget).",
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
                                    put("description", JsonPrimitive("Month to check, ISO yyyy-MM."))
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

        val rows =
            budgets.map { budget ->
                val spent = budgetRepository.spentForCategoryInMonth(budget.categoryId, month).valueOrZero()
                buildJsonObject {
                    put("category", JsonPrimitive(categoriesById[budget.categoryId]?.name ?: "Unknown"))
                    put("budgeted_pesos", JsonPrimitive(budget.amount.toPesoString()))
                    put("spent_pesos", JsonPrimitive(spent.toPesoString()))
                    put("percent_used", JsonPrimitive(BudgetMath.percentUsed(spent, budget.amount)))
                    put("status", JsonPrimitive(BudgetMath.statusFor(spent, budget.amount).name.lowercase()))
                }
            }

        return Result.Success(
            buildJsonObject {
                put("month", JsonPrimitive(month))
                put("categories", buildJsonArray { rows.forEach { add(it) } })
            },
        )
    }

    private fun Result<Money, *>.valueOrZero(): Money =
        when (this) {
            is Result.Success -> data
            is Result.Error -> Money.ZERO
        }
}
