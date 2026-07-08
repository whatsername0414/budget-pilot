package com.budgetpilot.feature.capture.presentation.confirm

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.domain.model.Category
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.core.presentation.UiText
import com.budgetpilot.feature.capture.domain.model.Confidence
import com.budgetpilot.feature.capture.domain.model.LineItem

private val PreviewCategories =
    listOf(
        Category(1, "Food", "restaurant", "food", true),
        Category(2, "Transport", "directions_bus", "transport", true),
    )

@Preview
@Composable
private fun ConfirmExpenseScreenLoadingPreview() {
    BudgetPilotTheme {
        ConfirmExpenseContent(
            state = ConfirmExpenseState(phase = ConfirmExpensePhase.LOADING),
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun ConfirmExpenseScreenLoadedPreview() {
    BudgetPilotTheme {
        ConfirmExpenseContent(
            state =
                ConfirmExpenseState(
                    phase = ConfirmExpensePhase.LOADED,
                    merchant = "Jollibee",
                    merchantConfidence = Confidence.MEDIUM,
                    amountText = "199.00",
                    lineItems =
                        listOf(
                            LineItem("1pc Chickenjoy w/ Rice", Money.fromPesos("89.00")),
                            LineItem("Jollibee Spaghetti", Money.fromPesos("65.00")),
                        ),
                    categories = PreviewCategories,
                    selectedCategoryId = 1,
                    categoryConfidence = Confidence.MEDIUM,
                ),
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun ConfirmExpenseScreenLoadedEmptyLineItemsPreview() {
    BudgetPilotTheme {
        ConfirmExpenseContent(
            state =
                ConfirmExpenseState(
                    phase = ConfirmExpensePhase.LOADED,
                    merchant = "Cash",
                    amountText = "120.00",
                    lineItems = emptyList(),
                    categories = PreviewCategories,
                    selectedCategoryId = 1,
                ),
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun ConfirmExpenseScreenErrorPreview() {
    BudgetPilotTheme {
        ConfirmExpenseContent(
            state =
                ConfirmExpenseState(
                    phase = ConfirmExpensePhase.ERROR,
                    errorMessage = UiText.DynamicString("We couldn't read this receipt. Check your connection and try again."),
                ),
            onAction = {},
        )
    }
}
