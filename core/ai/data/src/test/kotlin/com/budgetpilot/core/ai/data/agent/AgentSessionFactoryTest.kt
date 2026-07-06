package com.budgetpilot.core.ai.data.agent

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.budgetpilot.core.ai.data.FakeLlmClient
import com.budgetpilot.core.ai.data.prompt.AssetPromptRepository
import com.budgetpilot.core.ai.data.prompt.ClasspathPromptFileSource
import com.budgetpilot.core.ai.domain.model.AgentAnswer
import com.budgetpilot.core.ai.domain.model.LlmResponse
import com.budgetpilot.core.ai.domain.model.ToolCall
import com.budgetpilot.core.ai.domain.model.TraceStep
import com.budgetpilot.core.ai.domain.tool.GetBudgetStatusTool
import com.budgetpilot.core.ai.domain.tool.QueryExpensesTool
import com.budgetpilot.core.ai.domain.tool.ResolveDateRangeTool
import com.budgetpilot.core.domain.DataError
import com.budgetpilot.core.domain.EmptyResult
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.model.Budget
import com.budgetpilot.core.domain.model.Category
import com.budgetpilot.core.domain.model.CategoryTotal
import com.budgetpilot.core.domain.model.Expense
import com.budgetpilot.core.domain.model.ExpenseFilter
import com.budgetpilot.core.domain.model.ExpenseSource
import com.budgetpilot.core.domain.model.MonthTotal
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.core.domain.repository.BudgetRepository
import com.budgetpilot.core.domain.repository.CategoryRepository
import com.budgetpilot.core.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private val FOOD = Category(id = 1, name = "Food", iconKey = "restaurant", colorKey = "food", isDefault = true)
private val TRANSPORT = Category(id = 2, name = "Transport", iconKey = "directions_bus", colorKey = "transport", isDefault = true)
private val FIXED_CLOCK = Clock.fixed(Instant.parse("2026-07-15T00:00:00Z"), ZoneOffset.UTC)
private val promptRepository = AssetPromptRepository(ClasspathPromptFileSource())

class AgentSessionFactoryTest {
    @Test
    fun `food last month scenario resolves a date range then sums matching expenses`() =
        runTest {
            val expenses =
                FakeExpenseRepository(
                    listOf(
                        expense(id = 1, amountPesos = 250.0, categoryId = FOOD.id, date = LocalDate.of(2026, 6, 10)),
                        expense(id = 2, amountPesos = 300.0, categoryId = FOOD.id, date = LocalDate.of(2026, 6, 20)),
                    ),
                )
            val categories = FakeCategoryRepository(listOf(FOOD, TRANSPORT))
            val tools =
                listOf(
                    ResolveDateRangeTool(clock = FIXED_CLOCK),
                    QueryExpensesTool(expenseRepository = expenses, categoryRepository = categories),
                )
            val llm =
                FakeLlmClient(
                    script =
                        listOf(
                            Result.Success(
                                toolCallResponse("resolve_date_range", buildJsonObject { put("query", JsonPrimitive("last month")) }),
                            ),
                            Result.Success(
                                toolCallResponse(
                                    "query_expenses",
                                    buildJsonObject {
                                        put("category", JsonPrimitive("Food"))
                                        put("start_date", JsonPrimitive("2026-06-01"))
                                        put("end_date", JsonPrimitive("2026-06-30"))
                                        put("aggregation", JsonPrimitive("sum"))
                                    },
                                ),
                            ),
                            Result.Success(LlmResponse.Text("You spent ₱550.00 on Food last month, across 2 expenses.")),
                        ),
                )
            val factory = AgentSessionFactory(llmClient = llm, tools = tools, promptRepository = promptRepository)

            val goal = "How much did I spend on food last month?"
            val result = factory.createAgentLoop().run(goal = goal, systemPrompt = factory.systemPrompt)

            val answer = (result as Result.Success<AgentAnswer>).data
            assertThat(answer.text).isEqualTo("You spent ₱550.00 on Food last month, across 2 expenses.")
            assertThat(answer.trace.count { it is TraceStep.ToolInvocation }).isEqualTo(2)
        }

    @Test
    fun `over budget check scenario reports a category's budget status`() =
        runTest {
            val budgets =
                FakeBudgetRepository(
                    budgets = listOf(Budget(id = 1, categoryId = TRANSPORT.id, month = "2026-07", amount = Money.ofCentavos(100_000))),
                    spendByCategoryAndMonth = mapOf((TRANSPORT.id to "2026-07") to Money.ofCentavos(120_000)),
                )
            val categories = FakeCategoryRepository(listOf(FOOD, TRANSPORT))
            val tools = listOf(GetBudgetStatusTool(budgetRepository = budgets, categoryRepository = categories))
            val llm =
                FakeLlmClient(
                    script =
                        listOf(
                            Result.Success(
                                toolCallResponse("get_budget_status", buildJsonObject { put("month", JsonPrimitive("2026-07")) }),
                            ),
                            Result.Success(LlmResponse.Text("Yes — Transport is over budget at ₱1,200.00 of ₱1,000.00 (120%).")),
                        ),
                )
            val factory = AgentSessionFactory(llmClient = llm, tools = tools, promptRepository = promptRepository)

            val result = factory.createAgentLoop().run(goal = "Am I over budget?", systemPrompt = factory.systemPrompt)

            val answer = (result as Result.Success<AgentAnswer>).data
            assertThat(answer.text).contains("over budget")
            assertThat(answer.trace.count { it is TraceStep.ToolInvocation }).isEqualTo(1)
        }

    @Test
    fun `unanswerable question scenario declines without calling any tool`() =
        runTest {
            val llm =
                FakeLlmClient(
                    script =
                        listOf(
                            Result.Success(
                                LlmResponse.Text(
                                    "I can only answer questions about your own expenses, budgets, and categories in Budget Pilot.",
                                ),
                            ),
                        ),
                )
            val factory = AgentSessionFactory(llmClient = llm, tools = emptyList(), promptRepository = promptRepository)

            val result = factory.createAgentLoop().run(goal = "What's the weather today?", systemPrompt = factory.systemPrompt)

            val answer = (result as Result.Success<AgentAnswer>).data
            assertThat(answer.text).contains("only answer questions about your own")
            assertThat(answer.trace.size).isEqualTo(1)
            assertThat(answer.trace.first()).isInstanceOf<TraceStep.FinalAnswer>()
        }

    private fun toolCallResponse(
        name: String,
        args: JsonObject,
    ): LlmResponse = LlmResponse.ToolCalls(listOf(ToolCall(name = name, args = args)))

    private fun expense(
        id: Long,
        amountPesos: Double,
        categoryId: Long,
        date: LocalDate,
    ): Expense =
        Expense(
            id = id,
            amount = Money.ofCentavos((amountPesos * 100).toLong()),
            merchant = "Merchant",
            categoryId = categoryId,
            date = date,
            note = null,
            source = ExpenseSource.MANUAL,
            imageUri = null,
            createdAt = Instant.now(),
        )
}

private class FakeExpenseRepository(
    private val expenses: List<Expense>,
) : ExpenseRepository {
    override fun observeExpenses(filter: ExpenseFilter): Flow<List<Expense>> {
        val merchantQuery = filter.merchant
        return flowOf(
            expenses.filter {
                it.date >= filter.startDate &&
                    it.date <= filter.endDate &&
                    (filter.categoryId == null || it.categoryId == filter.categoryId) &&
                    (merchantQuery == null || it.merchant.contains(merchantQuery, ignoreCase = true))
            },
        )
    }

    override suspend fun getExpenseById(id: Long): Result<Expense, DataError.Local> =
        expenses.find { it.id == id }?.let { Result.Success(it) } ?: Result.Error(DataError.Local.NOT_FOUND)

    override suspend fun addExpense(expense: Expense): Result<Long, DataError.Local> = Result.Success(expense.id)

    override suspend fun updateExpense(expense: Expense): EmptyResult<DataError.Local> = Result.Success(Unit)

    override suspend fun deleteExpense(expense: Expense): EmptyResult<DataError.Local> = Result.Success(Unit)

    override suspend fun sumByCategory(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Result<List<CategoryTotal>, DataError.Local> = Result.Success(emptyList())

    override suspend fun sumByMonth(
        startDate: LocalDate,
        endDate: LocalDate,
    ): Result<List<MonthTotal>, DataError.Local> = Result.Success(emptyList())
}

private class FakeCategoryRepository(
    private val categories: List<Category>,
) : CategoryRepository {
    override fun observeCategories(): Flow<List<Category>> = flowOf(categories)

    override suspend fun getCategoryById(id: Long): Result<Category, DataError.Local> =
        categories.find { it.id == id }?.let { Result.Success(it) } ?: Result.Error(DataError.Local.NOT_FOUND)
}

private class FakeBudgetRepository(
    private val budgets: List<Budget>,
    private val spendByCategoryAndMonth: Map<Pair<Long, String>, Money> = emptyMap(),
) : BudgetRepository {
    override fun observeBudgetsForMonth(month: String): Flow<List<Budget>> = flowOf(budgets.filter { it.month == month })

    override suspend fun getBudget(
        categoryId: Long,
        month: String,
    ): Result<Budget, DataError.Local> =
        budgets.find { it.categoryId == categoryId && it.month == month }?.let { Result.Success(it) }
            ?: Result.Error(DataError.Local.NOT_FOUND)

    override suspend fun addBudget(budget: Budget): Result<Long, DataError.Local> = Result.Success(budget.id)

    override suspend fun updateBudget(budget: Budget): EmptyResult<DataError.Local> = Result.Success(Unit)

    override suspend fun deleteBudget(budget: Budget): EmptyResult<DataError.Local> = Result.Success(Unit)

    override suspend fun spentForCategoryInMonth(
        categoryId: Long,
        month: String,
    ): Result<Money, DataError.Local> = Result.Success(spendByCategoryAndMonth[categoryId to month] ?: Money.ZERO)
}
