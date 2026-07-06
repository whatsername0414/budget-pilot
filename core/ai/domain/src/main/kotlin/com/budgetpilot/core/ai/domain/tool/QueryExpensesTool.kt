package com.budgetpilot.core.ai.domain.tool

import com.budgetpilot.core.ai.domain.AgentTool
import com.budgetpilot.core.ai.domain.ToolError
import com.budgetpilot.core.ai.domain.model.ToolSchema
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.model.Category
import com.budgetpilot.core.domain.model.Expense
import com.budgetpilot.core.domain.model.ExpenseFilter
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.core.domain.repository.CategoryRepository
import com.budgetpilot.core.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.LocalDate
import java.time.format.DateTimeParseException

class QueryExpensesTool(
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository,
) : AgentTool {
    override val schema =
        ToolSchema(
            name = "query_expenses",
            description =
                "Query the user's expenses within a date range, optionally filtered by category " +
                    "or merchant, and aggregate the results. Dates must already be resolved to " +
                    "ISO yyyy-MM-dd (use resolve_date_range first for natural-language ranges " +
                    "like \"last month\").",
            parameters =
                buildJsonObject {
                    put("type", JsonPrimitive("object"))
                    put(
                        "properties",
                        buildJsonObject {
                            put(
                                "category",
                                buildJsonObject {
                                    put("type", JsonPrimitive("string"))
                                    put(
                                        "description",
                                        JsonPrimitive("Category name, e.g. \"Food\". Omit to include all categories."),
                                    )
                                },
                            )
                            put(
                                "merchant",
                                buildJsonObject {
                                    put("type", JsonPrimitive("string"))
                                    put(
                                        "description",
                                        JsonPrimitive("Partial, case-insensitive merchant match. Omit to include all merchants."),
                                    )
                                },
                            )
                            put(
                                "start_date",
                                buildJsonObject {
                                    put("type", JsonPrimitive("string"))
                                    put("description", JsonPrimitive("Inclusive start date, ISO yyyy-MM-dd."))
                                },
                            )
                            put(
                                "end_date",
                                buildJsonObject {
                                    put("type", JsonPrimitive("string"))
                                    put("description", JsonPrimitive("Inclusive end date, ISO yyyy-MM-dd."))
                                },
                            )
                            put(
                                "aggregation",
                                buildJsonObject {
                                    put("type", JsonPrimitive("string"))
                                    put(
                                        "enum",
                                        buildJsonArray {
                                            add(JsonPrimitive("sum"))
                                            add(JsonPrimitive("count"))
                                            add(JsonPrimitive("avg"))
                                            add(JsonPrimitive("group_by_category"))
                                        },
                                    )
                                    put("description", JsonPrimitive("How to aggregate the matching expenses."))
                                },
                            )
                        },
                    )
                    put(
                        "required",
                        buildJsonArray {
                            add(JsonPrimitive("start_date"))
                            add(JsonPrimitive("end_date"))
                            add(JsonPrimitive("aggregation"))
                        },
                    )
                },
        )

    override suspend fun execute(args: JsonObject): Result<JsonElement, ToolError> {
        val categories = categoryRepository.observeCategories().first()
        val parsed =
            when (val result = parseArgs(args, categories)) {
                is Result.Error -> return Result.Error(result.error)
                is Result.Success -> result.data
            }

        val filter =
            ExpenseFilter(
                startDate = parsed.startDate,
                endDate = parsed.endDate,
                categoryId = parsed.categoryId,
                merchant = parsed.merchant,
            )
        val expenses = expenseRepository.observeExpenses(filter).first()

        return when (parsed.aggregation) {
            "sum" -> Result.Success(sumResult(expenses))
            "count" -> Result.Success(countResult(expenses))
            "avg" -> Result.Success(avgResult(expenses))
            "group_by_category" -> Result.Success(groupByCategoryResult(expenses, categories.associateBy { it.id }))
            else ->
                Result.Error(
                    ToolError("Unknown aggregation \"${parsed.aggregation}\". Expected one of: sum, count, avg, group_by_category."),
                )
        }
    }

    private fun parseArgs(
        args: JsonObject,
        categories: List<Category>,
    ): Result<ParsedArgs, ToolError> {
        val startDate = args.parseDate("start_date")
        val endDate = args.parseDate("end_date")
        val aggregation = args.stringOrNull("aggregation")
        if (startDate == null || endDate == null || aggregation == null) {
            val missing =
                buildList {
                    if (startDate == null) add("start_date")
                    if (endDate == null) add("end_date")
                    if (aggregation == null) add("aggregation")
                }
            return Result.Error(
                ToolError("Missing or invalid: ${missing.joinToString()}. Dates must be yyyy-MM-dd."),
            )
        }

        val categoryName = args.stringOrNull("category")
        val categoryId =
            if (categoryName == null) {
                null
            } else {
                categories.find { it.name.equals(categoryName, ignoreCase = true) }?.id
                    ?: return Result.Error(
                        ToolError(
                            "Unknown category \"$categoryName\". Known categories: " +
                                categories.joinToString { it.name } + ".",
                        ),
                    )
            }

        return Result.Success(
            ParsedArgs(
                startDate = startDate,
                endDate = endDate,
                aggregation = aggregation,
                categoryId = categoryId,
                merchant = args.stringOrNull("merchant"),
            ),
        )
    }

    private data class ParsedArgs(
        val startDate: LocalDate,
        val endDate: LocalDate,
        val aggregation: String,
        val categoryId: Long?,
        val merchant: String?,
    )

    private fun sumResult(expenses: List<Expense>): JsonObject =
        buildJsonObject {
            put("aggregation", JsonPrimitive("sum"))
            put("total_pesos", JsonPrimitive(expenses.total().toPesoString()))
            put("count", JsonPrimitive(expenses.size))
        }

    private fun countResult(expenses: List<Expense>): JsonObject =
        buildJsonObject {
            put("aggregation", JsonPrimitive("count"))
            put("count", JsonPrimitive(expenses.size))
        }

    private fun avgResult(expenses: List<Expense>): JsonObject {
        val average =
            if (expenses.isEmpty()) Money.ZERO else Money.ofCentavos(expenses.total().centavos / expenses.size)
        return buildJsonObject {
            put("aggregation", JsonPrimitive("avg"))
            put("average_pesos", JsonPrimitive(average.toPesoString()))
            put("count", JsonPrimitive(expenses.size))
        }
    }

    private fun groupByCategoryResult(
        expenses: List<Expense>,
        categoriesById: Map<Long, Category>,
    ): JsonObject =
        buildJsonObject {
            put("aggregation", JsonPrimitive("group_by_category"))
            put(
                "groups",
                buildJsonArray {
                    expenses.groupBy { it.categoryId }.forEach { (categoryId, group) ->
                        add(
                            buildJsonObject {
                                put("category", JsonPrimitive(categoriesById[categoryId]?.name ?: "Unknown"))
                                put("total_pesos", JsonPrimitive(group.total().toPesoString()))
                                put("count", JsonPrimitive(group.size))
                            },
                        )
                    }
                },
            )
        }

    private fun List<Expense>.total(): Money = fold(Money.ZERO) { acc, expense -> acc + expense.amount }

    private fun JsonObject.parseDate(key: String): LocalDate? {
        val text = stringOrNull(key) ?: return null
        return try {
            LocalDate.parse(text)
        } catch (e: DateTimeParseException) {
            null
        }
    }
}
