package com.budgetpilot.feature.settings.presentation

import androidx.compose.runtime.Stable
import com.budgetpilot.core.presentation.UiText

@Stable
data class SettingsState(
    val cloudAiEnabled: Boolean = true,
    val isApiKeyConfigured: Boolean = false,
    val isLoading: Boolean = true,
    val error: UiText? = null,
)
