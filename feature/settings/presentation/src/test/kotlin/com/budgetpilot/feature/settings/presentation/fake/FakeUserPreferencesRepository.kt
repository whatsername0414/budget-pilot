package com.budgetpilot.feature.settings.presentation.fake

import com.budgetpilot.core.domain.DataError
import com.budgetpilot.core.domain.EmptyResult
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserPreferencesRepository(
    initialCloudAiEnabled: Boolean = true,
    private val failWith: DataError.Local? = null,
) : UserPreferencesRepository {
    private val _cloudAiEnabled = MutableStateFlow(initialCloudAiEnabled)
    override val cloudAiEnabled = _cloudAiEnabled

    override suspend fun setCloudAiEnabled(enabled: Boolean): EmptyResult<DataError.Local> {
        if (failWith != null) return Result.Error(failWith)
        _cloudAiEnabled.value = enabled
        return Result.Success(Unit)
    }
}
