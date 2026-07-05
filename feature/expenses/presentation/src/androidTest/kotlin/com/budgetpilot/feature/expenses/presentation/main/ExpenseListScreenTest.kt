package com.budgetpilot.feature.expenses.presentation.main

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.budgetpilot.core.domain.model.Category
import com.budgetpilot.core.domain.model.ExpenseSource
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.feature.expenses.presentation.main.model.ExpenseDayGroupUi
import com.budgetpilot.feature.expenses.presentation.main.model.ExpenseUi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class ExpenseListScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val robot by lazy { ExpenseListRobot(composeTestRule) }

    @Test
    fun items_areShown_whenDayGroupsPresent() {
        robot
            .setContent(
                state =
                    ExpenseListState(
                        isLoading = false,
                        dayGroups =
                            listOf(
                                ExpenseDayGroupUi(
                                    date = LocalDate.now(),
                                    dateLabel = "Today",
                                    totalAmount = Money.fromPesos("269.00"),
                                    expenses =
                                        listOf(
                                            ExpenseUi(
                                                id = 1,
                                                merchant = "Jollibee SM North",
                                                categoryName = "Food",
                                                categoryIconKey = "restaurant",
                                                categoryColorKey = "food",
                                                amount = Money.fromPesos("249.00"),
                                                formattedTime = "12:34 PM",
                                                source = ExpenseSource.MANUAL,
                                            ),
                                            ExpenseUi(
                                                id = 2,
                                                merchant = "Cash - parking",
                                                categoryName = "Transport",
                                                categoryIconKey = "directions_bus",
                                                categoryColorKey = "transport",
                                                amount = Money.fromPesos("20.00"),
                                                formattedTime = "5:10 PM",
                                                source = ExpenseSource.MANUAL,
                                            ),
                                        ),
                                ),
                            ),
                    ),
            ).assertItemVisible("Jollibee SM North")
            .assertItemVisible("Cash - parking")
    }

    @Test
    fun emptyState_isShown_whenNoExpenses() {
        robot
            .setContent(state = ExpenseListState(isLoading = false, dayGroups = emptyList()))
            .assertEmptyState("No expenses yet")
    }

    @Test
    fun clickingCategoryChip_dispatchesCategoryFilterAction() {
        var dispatchedAction: ExpenseListAction? = null

        robot
            .setContent(
                state =
                    ExpenseListState(
                        isLoading = false,
                        categories = listOf(Category(id = 1, name = "Food", iconKey = "restaurant", colorKey = "food", isDefault = true)),
                        selectedCategoryId = null,
                    ),
                onAction = { dispatchedAction = it },
            ).applyFilter("Food")

        assertThat(dispatchedAction).isEqualTo(ExpenseListAction.OnCategoryFilterSelect(categoryId = 1))
    }
}
