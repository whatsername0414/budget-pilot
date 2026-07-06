package com.budgetpilot.feature.ask.presentation

import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import assertk.assertThat
import assertk.assertions.contains
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AskScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val robot by lazy { AskRobot(composeTestRule) }

    @Test
    fun typingQuestionAndSending_dispatchesActions_withExistingAnswerShown() {
        val answeredTurn =
            AskTurn(
                id = 1L,
                question = "How much did I spend on food last month?",
                phase = AskTurnPhase.ANSWERED,
                answerText = "You spent ₱1,200.00 on Food last month.",
            )

        robot
            .setContent(state = AskState(turns = listOf(answeredTurn)))
            .typeQuestion("Any GCash expenses?")
            .clickSend()
            .assertAnswerShown("You spent ₱1,200.00 on Food last month.")

        assertThat(robot.dispatchedActions).contains(AskAction.OnQuestionChange(text = "Any GCash expenses?"))
        assertThat(robot.dispatchedActions).contains(AskAction.OnSendClick)
    }

    @Test
    fun traceExpander_toggles_andShowsSteps() {
        val answeredTurn =
            AskTurn(
                id = 2L,
                question = "How much did I spend on food this month?",
                phase = AskTurnPhase.ANSWERED,
                answerText = "You've spent ₱5,872.25 on Food this month.",
                dataUsedSummary = "Based on 23 Food expenses, Jun 1–30.",
                trace =
                    listOf(
                        AskTraceStepUi("resolve_date_range", "\"this month\"", "Jun 1–30", 210),
                        AskTraceStepUi("query_expenses", "category: Food, Jun 1–30", "23 expenses, ₱5,872.25", 1240),
                    ),
                isTraceExpanded = false,
            )

        robot
            .setContent(state = AskState(turns = listOf(answeredTurn)))
            .toggleTrace()
            .assertTraceStepVisible("resolve_date_range")
            .assertTraceStepVisible("query_expenses")

        assertThat(robot.dispatchedActions).contains(AskAction.OnToggleTraceExpanded(turnId = 2L))
    }

    @Test
    fun errorCard_showsRetry_andDispatchesRetryAction() {
        val erroredTurn =
            AskTurn(
                id = 3L,
                question = "How much did I spend on transport?",
                phase = AskTurnPhase.ERROR,
                error = AskErrorUi.Generic,
            )

        robot
            .setContent(state = AskState(turns = listOf(erroredTurn)))
            .assertRetryVisible()
            .clickRetry()

        assertThat(robot.dispatchedActions).contains(AskAction.OnRetryClick(turnId = 3L))
    }
}
