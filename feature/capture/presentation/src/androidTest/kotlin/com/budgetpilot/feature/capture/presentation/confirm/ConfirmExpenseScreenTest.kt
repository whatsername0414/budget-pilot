package com.budgetpilot.feature.capture.presentation.confirm

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.budgetpilot.core.domain.model.Category
import com.budgetpilot.feature.capture.domain.model.Confidence
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConfirmExpenseScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val robot by lazy { ConfirmExpenseRobot(composeTestRule) }

    private val categories =
        listOf(
            Category(id = 1, name = "Food", iconKey = "restaurant", colorKey = "food", isDefault = true),
        )

    @Test
    fun fields_arePrefilled_fromExtractedState() {
        robot
            .setContent(
                state =
                    ConfirmExpenseState(
                        phase = ConfirmExpensePhase.LOADED,
                        merchant = "Jollibee",
                        amountText = "154.00",
                        categories = categories,
                        selectedCategoryId = 1,
                    ),
            ).assertFieldVisible("Jollibee")
            .assertFieldVisible("154.00")
    }

    @Test
    fun lowConfidenceHelperText_isShown_forMediumConfidenceField() {
        robot
            .setContent(
                state =
                    ConfirmExpenseState(
                        phase = ConfirmExpensePhase.LOADED,
                        merchant = "Jollibee",
                        merchantConfidence = Confidence.MEDIUM,
                        amountText = "154.00",
                        categories = categories,
                        selectedCategoryId = 1,
                    ),
            ).assertTextVisible("Hard to read on the receipt — please check")
    }

    @Test
    fun clickingSave_dispatchesSaveAction() {
        var dispatchedAction: ConfirmExpenseAction? = null

        robot
            .setContent(
                state =
                    ConfirmExpenseState(
                        phase = ConfirmExpensePhase.LOADED,
                        merchant = "Jollibee",
                        amountText = "154.00",
                        categories = categories,
                        selectedCategoryId = 1,
                    ),
                onAction = { dispatchedAction = it },
            ).clickSave()

        assertThat(dispatchedAction).isEqualTo(ConfirmExpenseAction.OnSaveClick)
    }
}
