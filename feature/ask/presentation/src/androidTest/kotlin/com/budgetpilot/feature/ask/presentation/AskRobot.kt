package com.budgetpilot.feature.ask.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertDoesNotExist
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme

private const val SEND_CONTENT_DESCRIPTION = "Send"
private const val HOW_I_CALCULATED_THIS = "How I calculated this"

/**
 * Owns a reactive copy of [AskState] so [typeQuestion] and [toggleTrace] recompose the
 * real field/expander state, exactly like the ViewModel would in the running app, while
 * still surfacing every dispatched action for assertion.
 */
class AskRobot(
    private val composeTestRule: ComposeContentTestRule,
) {
    val dispatchedActions = mutableListOf<AskAction>()

    fun setContent(state: AskState) =
        apply {
            composeTestRule.setContent {
                var currentState by remember { mutableStateOf(state) }
                BudgetPilotTheme {
                    AskContent(
                        state = currentState,
                        onAction = { action ->
                            dispatchedActions += action
                            currentState = currentState.reduce(action)
                        },
                    )
                }
            }
        }

    fun typeQuestion(text: String) =
        apply {
            composeTestRule.onNodeWithText(text = "Ask about your spending…").performTextInput(text)
        }

    fun clickSend() =
        apply {
            composeTestRule.onNodeWithContentDescription(SEND_CONTENT_DESCRIPTION).performClick()
        }

    fun toggleTrace() =
        apply {
            composeTestRule.onNodeWithText(HOW_I_CALCULATED_THIS).performClick()
        }

    fun clickRetry() =
        apply {
            composeTestRule.onNodeWithText("Retry").performClick()
        }

    fun clickClear() =
        apply {
            composeTestRule.onNodeWithText("Clear").performClick()
        }

    fun assertAnswerShown(answerText: String) =
        apply {
            composeTestRule.onNodeWithText(answerText, substring = true).assertIsDisplayed()
        }

    fun assertTraceStepVisible(toolName: String) =
        apply {
            composeTestRule.onNodeWithText(toolName).assertIsDisplayed()
        }

    fun assertRetryVisible() =
        apply {
            composeTestRule.onNodeWithText("Retry").assertIsDisplayed()
        }

    fun assertClearNotVisible() =
        apply {
            composeTestRule.onNodeWithText("Clear").assertDoesNotExist()
        }

    private fun AskState.reduce(action: AskAction): AskState =
        when (action) {
            is AskAction.OnQuestionChange -> copy(questionInput = action.text)
            is AskAction.OnToggleTraceExpanded ->
                copy(
                    turns =
                        turns.map { turn ->
                            if (turn.id == action.turnId) turn.copy(isTraceExpanded = !turn.isTraceExpanded) else turn
                        },
                )
            AskAction.OnClearClick -> AskState()
            else -> this
        }
}
