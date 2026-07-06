package com.budgetpilot.feature.settings.presentation

sealed interface SettingsAction {
    data class OnCloudAiToggle(
        val enabled: Boolean,
    ) : SettingsAction
}
