package com.budgetpilot.feature.ask.presentation

sealed interface AskAction {
    data class OnQuestionChange(
        val text: String,
    ) : AskAction

    data object OnSendClick : AskAction

    data class OnSuggestionClick(
        val suggestion: String,
    ) : AskAction

    data class OnToggleTraceExpanded(
        val turnId: Long,
    ) : AskAction

    data class OnRetryClick(
        val turnId: Long,
    ) : AskAction

    data object OnOpenSettingsClick : AskAction
}
