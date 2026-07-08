package com.budgetpilot.feature.settings.presentation

import androidx.compose.runtime.Stable
import com.budgetpilot.core.presentation.UiText

@Stable
data class SettingsState(
    val privateModeEnabled: Boolean = false,
    val cloudAiEnabled: Boolean = true,
    val isApiKeyConfigured: Boolean = false,
    val demoModeEnabled: Boolean = false,
    val dynamicColorEnabled: Boolean = false,
    val isLoading: Boolean = true,
    val error: UiText? = null,
    val isDemoDataSeedVisible: Boolean = BuildConfig.DEBUG,
    val isSeedingDemoData: Boolean = false,
)
