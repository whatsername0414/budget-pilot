package com.budgetpilot.feature.settings.presentation

sealed interface SettingsAction {
    data class OnCloudAiToggle(
        val enabled: Boolean,
    ) : SettingsAction

    data class OnPrivateModeToggle(
        val enabled: Boolean,
    ) : SettingsAction

    data class OnDemoModeToggle(
        val enabled: Boolean,
    ) : SettingsAction
}
