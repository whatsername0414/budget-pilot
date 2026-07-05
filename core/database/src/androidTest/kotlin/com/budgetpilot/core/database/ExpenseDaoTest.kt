package com.budgetpilot.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.hasSize
import assertk.assertions.isEqualTo
import com.budgetpilot.core.database.dao.CategorySpend
import com.budgetpilot.core.database.dao.MonthSpend
import com.budgetpilot.core.database.entity.CategoryEntity
import com.budgetpilot.core.database.entity.ExpenseEntity
import com.budgetpilot.core.database.entity.ExpenseSource
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class ExpenseDaoTest {
    private lateinit var database: BudgetPilotDatabase
    private var foodCategoryId: Long = 0
    private var transportCategoryId: Long = 0

    @Before
    fun createDatabase() =
        runTest {
            database =
                Room
                    .inMemoryDatabaseBuilder(
                        ApplicationProvider.getApplicationContext(),
                        BudgetPilotDatabase::class.java,
                    ).build()

            database.categoryDao().insertAll(
                listOf(
                    CategoryEntity(name = "Food", iconKey = "restaurant", colorKey = "food", isDefault = true),
                    CategoryEntity(
                        name = "Transport",
                        iconKey = "directions_bus",
                        colorKey = "transport",
                        isDefault = true,
                    ),
                ),
            )
            val categories = database.categoryDao().observeCategories().first()
            foodCategoryId = categories.first { it.name == "Food" }.id
            transportCategoryId = categories.first { it.name == "Transport" }.id
        }

    @After
    fun closeDatabase() {
        database.close()
    }

    private fun expense(
        amountCentavos: Long,
        categoryId: Long,
        date: LocalDate,
        merchant: String = "Jollibee",
    ) = ExpenseEntity(
        amountCentavos = amountCentavos,
        merchant = merchant,
        categoryId = categoryId,
        date = date,
        note = null,
        source = ExpenseSource.MANUAL,
        imageUri = null,
        createdAt = Instant.now(),
    )

    @Test
    fun sumByCategory_aggregatesWithinRange() =
        runTest {
            val dao = database.expenseDao()
            dao.insert(expense(10_000, foodCategoryId, LocalDate.of(2026, 7, 5)))
            dao.insert(expense(5_000, foodCategoryId, LocalDate.of(2026, 7, 10)))
            dao.insert(expense(2_000, transportCategoryId, LocalDate.of(2026, 7, 12)))
            // Outside the queried range - must not be counted.
            dao.insert(expense(99_999, foodCategoryId, LocalDate.of(2026, 6, 30)))

            val result =
                dao.sumByCategory(
                    startDate = LocalDate.of(2026, 7, 1),
                    endDate = LocalDate.of(2026, 7, 31),
                )

            assertThat(result).containsExactlyInAnyOrder(
                CategorySpend(categoryId = foodCategoryId, totalCentavos = 15_000),
                CategorySpend(categoryId = transportCategoryId, totalCentavos = 2_000),
            )
        }

    @Test
    fun sumByMonth_respectsMonthBoundaries() =
        runTest {
            val dao = database.expenseDao()
            dao.insert(expense(1_000, foodCategoryId, LocalDate.of(2026, 6, 30)))
            dao.insert(expense(2_000, foodCategoryId, LocalDate.of(2026, 7, 1)))
            dao.insert(expense(3_000, foodCategoryId, LocalDate.of(2026, 7, 31)))
            dao.insert(expense(4_000, foodCategoryId, LocalDate.of(2026, 8, 1)))

            val result =
                dao.sumByMonth(
                    startDate = LocalDate.of(2026, 6, 1),
                    endDate = LocalDate.of(2026, 8, 31),
                )

            assertThat(result).containsExactlyInAnyOrder(
                MonthSpend(month = "2026-06", totalCentavos = 1_000),
                MonthSpend(month = "2026-07", totalCentavos = 5_000),
                MonthSpend(month = "2026-08", totalCentavos = 4_000),
            )
        }

    @Test
    fun observeExpenses_filtersByCategoryMerchantAndDateRange() =
        runTest {
            val dao = database.expenseDao()
            dao.insert(expense(1_000, foodCategoryId, LocalDate.of(2026, 7, 1), merchant = "Jollibee"))
            dao.insert(expense(2_000, foodCategoryId, LocalDate.of(2026, 7, 2), merchant = "McDonald's"))
            dao.insert(expense(3_000, transportCategoryId, LocalDate.of(2026, 7, 2), merchant = "Grab"))
            dao.insert(expense(4_000, foodCategoryId, LocalDate.of(2026, 8, 1), merchant = "Jollibee"))

            dao
                .observeExpenses(
                    categoryId = foodCategoryId,
                    merchant = "Jollibee",
                    startDate = LocalDate.of(2026, 7, 1),
                    endDate = LocalDate.of(2026, 7, 31),
                ).test {
                    val expenses = awaitItem()
                    assertThat(expenses).hasSize(1)
                    assertThat(expenses.single().amountCentavos).isEqualTo(1_000L)
                }
        }
}
