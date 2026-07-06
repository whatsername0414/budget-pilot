package com.budgetpilot.core.domain.repository

import com.budgetpilot.core.domain.DataError
import com.budgetpilot.core.domain.EmptyResult
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val cloudAiEnabled: Flow<Boolean>

    suspend fun setCloudAiEnabled(enabled: Boolean): EmptyResult<DataError.Local>
}
