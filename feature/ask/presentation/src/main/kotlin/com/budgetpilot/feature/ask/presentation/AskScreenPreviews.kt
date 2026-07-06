package com.budgetpilot.feature.ask.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.PreviewLightDark
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme

private const val SAMPLE_TURN_ID = 1L

@PreviewLightDark
@Composable
private fun AskScreenEmptyPreview() {
    BudgetPilotTheme {
        AskContent(state = AskState(), onAction = {})
    }
}

@PreviewLightDark
@Composable
private fun AskScreenLoadingMidTracePreview() {
    BudgetPilotTheme {
        AskContent(
            state =
                AskState(
                    questionInput = "",
                    isSending = true,
                    turns =
                        listOf(
                            AskTurn(
                                id = SAMPLE_TURN_ID,
                                question = "How much did I spend on food last month?",
                                phase = AskTurnPhase.RUNNING,
                                stagedStatus = AskStagedStatus.CHECKING_EXPENSES,
                                trace =
                                    listOf(
                                        AskTraceStepUi(
                                            toolName = "resolve_date_range",
                                            argsSummary = "\"last month\"",
                                            resultSummary = "Jun 1–30",
                                            durationMs = 180,
                                        ),
                                    ),
                            ),
                        ),
                ),
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun AskScreenAnsweredTraceExpandedPreview() {
    BudgetPilotTheme {
        AskContent(
            state =
                AskState(
                    turns =
                        listOf(
                            AskTurn(
                                id = SAMPLE_TURN_ID,
                                question = "How much did I spend on food last month?",
                                phase = AskTurnPhase.ANSWERED,
                                trace =
                                    listOf(
                                        AskTraceStepUi(
                                            toolName = "resolve_date_range",
                                            argsSummary = "\"last month\"",
                                            resultSummary = "Jun 1–30",
                                            durationMs = 180,
                                        ),
                                        AskTraceStepUi(
                                            toolName = "query_expenses",
                                            argsSummary = "category: Food, Jun 1–30",
                                            resultSummary = "23 expenses, ₱5,872.25",
                                            durationMs = 1240,
                                        ),
                                    ),
                                answerText = "You spent ₱5,872.25 on Food last month, across 23 expenses.",
                                dataUsedSummary = "Based on 23 Food expenses, Jun 1–30.",
                                isTraceExpanded = true,
                                modelTurnCount = 2,
                                totalDurationMs = 4100,
                                followUpSuggestions = listOf("Compare to this month?", "Break down by merchant?"),
                            ),
                        ),
                ),
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun AskScreenErrorRateLimitedPreview() {
    BudgetPilotTheme {
        AskContent(
            state = AskState(questionInput = "Am I over budget?", turns = listOf(errorTurn(AskErrorUi.RateLimited(42)))),
            onAction = {},
        )
    }
}

@PreviewLightDark
@Composable
private fun AskScreenErrorOfflinePreview() {
    BudgetPilotTheme {
        AskContent(state = AskState(turns = listOf(errorTurn(AskErrorUi.Offline))), onAction = {})
    }
}

@PreviewLightDark
@Composable
private fun AskScreenErrorNoApiKeyPreview() {
    BudgetPilotTheme {
        AskContent(state = AskState(turns = listOf(errorTurn(AskErrorUi.NoApiKey))), onAction = {})
    }
}

@PreviewLightDark
@Composable
private fun AskScreenErrorGenericPreview() {
    BudgetPilotTheme {
        AskContent(state = AskState(turns = listOf(errorTurn(AskErrorUi.Generic))), onAction = {})
    }
}

private fun errorTurn(error: AskErrorUi) =
    AskTurn(
        id = SAMPLE_TURN_ID,
        question = "How much did I spend on food last month?",
        phase = AskTurnPhase.ERROR,
        error = error,
    )
