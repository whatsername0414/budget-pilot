package com.budgetpilot.feature.budgets.presentation.budgets

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.feature.budgets.presentation.budgets.model.BudgetCategoryUi
import com.budgetpilot.feature.budgets.presentation.budgets.model.UnbudgetedCategoryUi

@Preview
@Composable
private fun BudgetListScreenPreview() {
    BudgetPilotTheme {
        BudgetListContent(
            state =
                BudgetListState(
                    isLoading = false,
                    totalBudgeted = Money.fromPesos("12,000.00"),
                    totalSpent = Money.fromPesos("9,612.25"),
                    budgetedCategories =
                        listOf(
                            BudgetCategoryUi(
                                categoryId = 1,
                                name = "Food",
                                iconKey = "restaurant",
                                colorKey = "food",
                                spent = Money.fromPesos("5,872.25"),
                                budget = Money.fromPesos("6,000.00"),
                            ),
                            BudgetCategoryUi(
                                categoryId = 2,
                                name = "Shopping",
                                iconKey = "shopping_bag",
                                colorKey = "shopping",
                                spent = Money.fromPesos("3,412.00"),
                                budget = Money.fromPesos("3,000.00"),
                            ),
                            BudgetCategoryUi(
                                categoryId = 3,
                                name = "Transport",
                                iconKey = "directions_bus",
                                colorKey = "transport",
                                spent = Money.fromPesos("328.00"),
                                budget = Money.fromPesos("3,000.00"),
                            ),
                        ),
                    unbudgetedCategories =
                        listOf(
                            UnbudgetedCategoryUi(
                                categoryId = 4,
                                name = "Entertainment",
                                iconKey = "movie",
                                colorKey = "entertainment",
                            ),
                        ),
                ),
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun BudgetListScreenEmptyPreview() {
    BudgetPilotTheme {
        BudgetListContent(
            state =
                BudgetListState(
                    isLoading = false,
                    unbudgetedCategories =
                        listOf(
                            UnbudgetedCategoryUi(1, "Food", "restaurant", "food"),
                            UnbudgetedCategoryUi(2, "Transport", "directions_bus", "transport"),
                        ),
                ),
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun BudgetListScreenLoadingPreview() {
    BudgetPilotTheme {
        BudgetListContent(
            state = BudgetListState(isLoading = true),
            onAction = {},
        )
    }
}
