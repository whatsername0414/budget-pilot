package com.budgetpilot.feature.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.ai.ApiKeyStatusProvider
import com.budgetpilot.core.domain.repository.UserPreferencesRepository
import com.budgetpilot.core.presentation.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val userPreferencesRepository: UserPreferencesRepository,
    apiKeyStatusProvider: ApiKeyStatusProvider,
) : ViewModel() {
    private val _state = MutableStateFlow(SettingsState(isApiKeyConfigured = apiKeyStatusProvider.isApiKeyConfigured()))
    val state = _state.asStateFlow()

    private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    init {
        observePreferences()
    }

    fun onAction(action: SettingsAction) {
        when (action) {
            is SettingsAction.OnCloudAiToggle -> toggleCloudAi(action.enabled)
            is SettingsAction.OnPrivateModeToggle -> togglePrivateMode(action.enabled)
            is SettingsAction.OnDemoModeToggle -> toggleDemoMode(action.enabled)
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            combine(
                userPreferencesRepository.cloudAiEnabled,
                userPreferencesRepository.privateModeEnabled,
                userPreferencesRepository.demoModeEnabled,
            ) { cloudAiEnabled, privateModeEnabled, demoModeEnabled ->
                Triple(cloudAiEnabled, privateModeEnabled, demoModeEnabled)
            }.collect { (cloudAiEnabled, privateModeEnabled, demoModeEnabled) ->
                _state.update {
                    it.copy(
                        cloudAiEnabled = cloudAiEnabled,
                        privateModeEnabled = privateModeEnabled,
                        demoModeEnabled = demoModeEnabled,
                        isLoading = false,
                    )
                }
            }
        }
    }

    private fun toggleCloudAi(enabled: Boolean) {
        viewModelScope.launch {
            val result = userPreferencesRepository.setCloudAiEnabled(enabled)
            if (result is Result.Error) {
                _events.send(SettingsEvent.ShowError(result.error.toUiText()))
            }
        }
    }

    private fun togglePrivateMode(enabled: Boolean) {
        viewModelScope.launch {
            val result = userPreferencesRepository.setPrivateModeEnabled(enabled)
            if (result is Result.Error) {
                _events.send(SettingsEvent.ShowError(result.error.toUiText()))
            }
        }
    }

    private fun toggleDemoMode(enabled: Boolean) {
        viewModelScope.launch {
            val result = userPreferencesRepository.setDemoModeEnabled(enabled)
            if (result is Result.Error) {
                _events.send(SettingsEvent.ShowError(result.error.toUiText()))
            }
        }
    }
}
