package com.budgetpilot.feature.capture.presentation.confirm

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme

class ConfirmExpenseRobot(
    private val composeTestRule: ComposeContentTestRule,
) {
    fun setContent(
        state: ConfirmExpenseState,
        onAction: (ConfirmExpenseAction) -> Unit = {},
    ) = apply {
        composeTestRule.setContent {
            BudgetPilotTheme {
                ConfirmExpenseContent(state = state, onAction = onAction)
            }
        }
    }

    fun assertFieldVisible(value: String) =
        apply {
            composeTestRule.onNodeWithText(value).assertIsDisplayed()
        }

    fun assertTextVisible(text: String) =
        apply {
            composeTestRule.onNodeWithText(text).assertIsDisplayed()
        }

    fun clickSave() =
        apply {
            composeTestRule.onNodeWithText("Save expense").performClick()
        }
}
