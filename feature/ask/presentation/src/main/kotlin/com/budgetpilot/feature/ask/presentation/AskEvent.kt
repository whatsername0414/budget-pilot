package com.budgetpilot.feature.ask.presentation

sealed interface AskEvent {
    data object NavigateToSettings : AskEvent
}
