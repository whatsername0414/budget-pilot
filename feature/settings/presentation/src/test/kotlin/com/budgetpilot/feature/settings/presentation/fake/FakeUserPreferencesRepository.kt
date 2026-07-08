package com.budgetpilot.feature.settings.presentation.fake

import com.budgetpilot.core.domain.DataError
import com.budgetpilot.core.domain.EmptyResult
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserPreferencesRepository(
    initialCloudAiEnabled: Boolean = true,
    initialPrivateModeEnabled: Boolean = false,
    initialDemoModeEnabled: Boolean = false,
    initialDynamicColorEnabled: Boolean = false,
    private val failWith: DataError.Local? = null,
) : UserPreferencesRepository {
    private val _cloudAiEnabled = MutableStateFlow(initialCloudAiEnabled)
    override val cloudAiEnabled = _cloudAiEnabled

    private val _privateModeEnabled = MutableStateFlow(initialPrivateModeEnabled)
    override val privateModeEnabled = _privateModeEnabled

    private val _demoModeEnabled = MutableStateFlow(initialDemoModeEnabled)
    override val demoModeEnabled = _demoModeEnabled

    private val _dynamicColorEnabled = MutableStateFlow(initialDynamicColorEnabled)
    override val dynamicColorEnabled = _dynamicColorEnabled

    override suspend fun setCloudAiEnabled(enabled: Boolean): EmptyResult<DataError.Local> {
        if (failWith != null) return Result.Error(failWith)
        _cloudAiEnabled.value = enabled
        return Result.Success(Unit)
    }

    override suspend fun setPrivateModeEnabled(enabled: Boolean): EmptyResult<DataError.Local> {
        if (failWith != null) return Result.Error(failWith)
        _privateModeEnabled.value = enabled
        return Result.Success(Unit)
    }

    override suspend fun setDemoModeEnabled(enabled: Boolean): EmptyResult<DataError.Local> {
        if (failWith != null) return Result.Error(failWith)
        _demoModeEnabled.value = enabled
        return Result.Success(Unit)
    }

    override suspend fun setDynamicColorEnabled(enabled: Boolean): EmptyResult<DataError.Local> {
        if (failWith != null) return Result.Error(failWith)
        _dynamicColorEnabled.value = enabled
        return Result.Success(Unit)
    }
}
