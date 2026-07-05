package com.budgetpilot.feature.expenses.presentation.main

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme

class ExpenseListRobot(
    private val composeTestRule: ComposeContentTestRule,
) {
    fun setContent(
        state: ExpenseListState,
        onAction: (ExpenseListAction) -> Unit = {},
    ) = apply {
        composeTestRule.setContent {
            BudgetPilotTheme {
                ExpenseListContent(state = state, onAction = onAction)
            }
        }
    }

    fun assertItemVisible(merchant: String) =
        apply {
            composeTestRule.onNodeWithText(merchant).assertIsDisplayed()
        }

    fun applyFilter(categoryName: String) =
        apply {
            composeTestRule.onNodeWithText(categoryName).performClick()
        }

    fun assertEmptyState(title: String) =
        apply {
            composeTestRule.onNodeWithText(title).assertIsDisplayed()
        }
}
