package com.budgetpilot.core.database

import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.budgetpilot.core.database.entity.BudgetEntity
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
class BudgetDaoTest {
    private lateinit var database: BudgetPilotDatabase
    private var foodCategoryId: Long = 0

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
                listOf(CategoryEntity(name = "Food", iconKey = "restaurant", colorKey = "food", isDefault = true)),
            )
            foodCategoryId =
                database
                    .categoryDao()
                    .observeCategories()
                    .first()
                    .single()
                    .id
        }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun getBudget_returnsInsertedBudgetForCategoryAndMonth() =
        runTest {
            val budget = BudgetEntity(categoryId = foodCategoryId, month = "2026-07", amountCentavos = 500_000)
            database.budgetDao().insert(budget)

            val result = database.budgetDao().getBudget(foodCategoryId, "2026-07")

            assertThat(result?.amountCentavos).isEqualTo(500_000L)
        }

    @Test
    fun getBudget_returnsNullForMonthWithNoBudget() =
        runTest {
            assertThat(database.budgetDao().getBudget(foodCategoryId, "2026-07")).isNull()
        }

    @Test
    fun insertingDuplicateCategoryAndMonth_violatesUniqueConstraint() =
        runTest {
            val budget = BudgetEntity(categoryId = foodCategoryId, month = "2026-07", amountCentavos = 500_000)
            database.budgetDao().insert(budget)

            val failure = runCatching { database.budgetDao().insert(budget.copy(amountCentavos = 600_000)) }.exceptionOrNull()

            assertThat(failure).isNotNull().isInstanceOf(SQLiteConstraintException::class)
        }

    @Test
    fun spentForCategoryInMonth_sumsOnlyThatCategoryAndMonth() =
        runTest {
            val expenseDao = database.expenseDao()
            expenseDao.insert(
                ExpenseEntity(
                    amountCentavos = 10_000,
                    merchant = "Jollibee",
                    categoryId = foodCategoryId,
                    date = LocalDate.of(2026, 7, 5),
                    note = null,
                    source = ExpenseSource.MANUAL,
                    imageUri = null,
                    createdAt = Instant.now(),
                ),
            )
            expenseDao.insert(
                ExpenseEntity(
                    amountCentavos = 5_000,
                    merchant = "McDonald's",
                    categoryId = foodCategoryId,
                    date = LocalDate.of(2026, 8, 1),
                    note = null,
                    source = ExpenseSource.MANUAL,
                    imageUri = null,
                    createdAt = Instant.now(),
                ),
            )

            val spent = database.budgetDao().spentForCategoryInMonth(foodCategoryId, "2026-07")

            assertThat(spent).isEqualTo(10_000L)
        }
}
