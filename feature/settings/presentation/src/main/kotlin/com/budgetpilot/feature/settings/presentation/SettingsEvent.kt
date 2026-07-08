package com.budgetpilot.feature.settings.presentation

import com.budgetpilot.core.presentation.UiText

sealed interface SettingsEvent {
    data class ShowError(
        val message: UiText,
    ) : SettingsEvent

    data object DemoDataLoaded : SettingsEvent
}
