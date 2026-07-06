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

    override suspend fun setCloudAiEnabled(enabled: Boolean): EmptyResult<DataError.Local> {
        _cloudAiEnabled.value = enabled
        return Result.Success(Unit)
    }
}
