package com.budgetpilot.feature.insights.data.fake

import com.budgetpilot.core.domain.DataError
import com.budgetpilot.core.domain.EmptyResult
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserPreferencesRepository(
    initialCloudAiEnabled: Boolean = true,
) : UserPreferencesRepository {
    private val _cloudAiEnabled = MutableStateFlow(initialCloudAiEnabled)
    override val cloudAiEnabled = _cloudAiEnabled

    private val _privateModeEnabled = MutableStateFlow(false)
    override val privateModeEnabled = _privateModeEnabled

    private val _demoModeEnabled = MutableStateFlow(false)
    override val demoModeEnabled = _demoModeEnabled

    override suspend fun setCloudAiEnabled(enabled: Boolean): EmptyResult<DataError.Local> {
        _cloudAiEnabled.value = enabled
        return Result.Success(Unit)
    }

    override suspend fun setPrivateModeEnabled(enabled: Boolean): EmptyResult<DataError.Local> {
        _privateModeEnabled.value = enabled
        return Result.Success(Unit)
    }

    override suspend fun setDemoModeEnabled(enabled: Boolean): EmptyResult<DataError.Local> {
        _demoModeEnabled.value = enabled
        return Result.Success(Unit)
    }
}
