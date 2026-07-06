package com.budgetpilot.feature.settings.presentation

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import com.budgetpilot.core.domain.DataError
import com.budgetpilot.feature.settings.presentation.fake.FakeUserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state reflects the current cloud AI preference and API key status`() =
        runTest {
            val viewModel =
                viewModel(
                    preferences = FakeUserPreferencesRepository(initialCloudAiEnabled = false),
                    apiKeyConfigured = true,
                )

            assertThat(viewModel.state.value.cloudAiEnabled).isFalse()
            assertThat(viewModel.state.value.isApiKeyConfigured).isTrue()
            assertThat(viewModel.state.value.isLoading).isFalse()
        }

    @Test
    fun `toggling cloud AI persists the new value via the repository`() =
        runTest {
            val preferences = FakeUserPreferencesRepository(initialCloudAiEnabled = true)
            val viewModel = viewModel(preferences = preferences)

            viewModel.onAction(SettingsAction.OnCloudAiToggle(false))

            assertThat(viewModel.state.value.cloudAiEnabled).isFalse()
            assertThat(preferences.cloudAiEnabled.value).isFalse()
        }

    @Test
    fun `a failed persist emits ShowError and leaves the toggle unchanged`() =
        runTest {
            val preferences =
                FakeUserPreferencesRepository(initialCloudAiEnabled = true, failWith = DataError.Local.DISK_FULL)
            val viewModel = viewModel(preferences = preferences)

            viewModel.events.test {
                viewModel.onAction(SettingsAction.OnCloudAiToggle(false))

                assertThat(awaitItem()).isInstanceOf(SettingsEvent.ShowError::class)
            }
            assertThat(viewModel.state.value.cloudAiEnabled).isTrue()
        }

    private fun viewModel(
        preferences: FakeUserPreferencesRepository = FakeUserPreferencesRepository(),
        apiKeyConfigured: Boolean = false,
    ): SettingsViewModel =
        SettingsViewModel(
            userPreferencesRepository = preferences,
            apiKeyStatusProvider = { apiKeyConfigured },
        )
}
