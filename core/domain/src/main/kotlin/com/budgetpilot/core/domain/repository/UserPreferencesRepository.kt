package com.budgetpilot.core.domain.repository

import com.budgetpilot.core.domain.DataError
import com.budgetpilot.core.domain.EmptyResult
import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val cloudAiEnabled: Flow<Boolean>
    val privateModeEnabled: Flow<Boolean>
    val demoModeEnabled: Flow<Boolean>

    suspend fun setCloudAiEnabled(enabled: Boolean): EmptyResult<DataError.Local>

    suspend fun setPrivateModeEnabled(enabled: Boolean): EmptyResult<DataError.Local>

    suspend fun setDemoModeEnabled(enabled: Boolean): EmptyResult<DataError.Local>
}
