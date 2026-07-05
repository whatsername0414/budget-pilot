package com.budgetpilot.core.database

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactlyInAnyOrder
import assertk.assertions.hasSize
import com.budgetpilot.core.database.seed.DefaultCategories
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CategoryDaoTest {
    private lateinit var database: BudgetPilotDatabase

    @Before
    fun createDatabase() {
        database =
            Room
                .inMemoryDatabaseBuilder(
                    ApplicationProvider.getApplicationContext(),
                    BudgetPilotDatabase::class.java,
                ).build()
    }

    @After
    fun closeDatabase() {
        database.close()
    }

    @Test
    fun seedingDefaultCategories_makesAllEightAvailable() =
        runTest {
            database.categoryDao().insertAll(DefaultCategories.all)

            database.categoryDao().observeCategories().test {
                val categories = awaitItem()
                assertThat(categories).hasSize(8)
                assertThat(categories.map { it.name }).containsExactlyInAnyOrder(
                    "Food",
                    "Transport",
                    "Bills",
                    "Groceries",
                    "Shopping",
                    "Health",
                    "Entertainment",
                    "Other",
                )
            }
        }

    @Test
    fun insertAll_ignoresDuplicateNamesOnConflict() =
        runTest {
            database.categoryDao().insertAll(DefaultCategories.all)
            database.categoryDao().insertAll(DefaultCategories.all)

            database.categoryDao().observeCategories().test {
                assertThat(awaitItem()).hasSize(8)
            }
        }
}
