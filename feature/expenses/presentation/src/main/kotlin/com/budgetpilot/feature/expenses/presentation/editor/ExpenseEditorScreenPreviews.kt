package com.budgetpilot.feature.expenses.presentation.editor

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.domain.model.Category
import com.budgetpilot.core.presentation.UiText

@PreviewLightDark
@Composable
private fun ExpenseEditorScreenAddPreview() {
    BudgetPilotTheme {
        ExpenseEditorContent(
            state =
                ExpenseEditorState(
                    mode = ExpenseEditorMode.ADD,
                    amountText = "249.00",
                    merchant = "Jollibee SM North",
                    selectedCategoryId = 1,
                    categories =
                        listOf(
                            Category(1, "Food", "restaurant", "food", true),
                            Category(2, "Transport", "directions_bus", "transport", true),
                        ),
                ),
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun ExpenseEditorScreenEditPreview() {
    BudgetPilotTheme {
        ExpenseEditorContent(
            state =
                ExpenseEditorState(
                    mode = ExpenseEditorMode.EDIT,
                    amountText = "20.00",
                    merchant = "Grab",
                    selectedCategoryId = 2,
                    amountError = UiText.DynamicString("Enter an amount"),
                    categories =
                        listOf(
                            Category(1, "Food", "restaurant", "food", true),
                            Category(2, "Transport", "directions_bus", "transport", true),
                        ),
                ),
            onAction = {},
        )
    }
}
