package com.budgetpilot.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.budgetpilot.core.domain.DataError
import com.budgetpilot.core.domain.EmptyResult
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.repository.UserPreferencesRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.IOException

private val CLOUD_AI_ENABLED_KEY = booleanPreferencesKey("cloud_ai_enabled")
private const val CLOUD_AI_ENABLED_DEFAULT = true

/**
 * DataStore-backed [UserPreferencesRepository]. Also exposes [isCloudAiAllowedSnapshot], a
 * synchronous cached read of the same value (kept warm via an internal [CoroutineScope]
 * collecting [cloudAiEnabled]) — needed so whichever module owns `:feature:capture:domain`'s
 * non-suspend `CloudAiPolicy` fun interface can SAM-adapt this class from its own Koin wiring,
 * the same way `:core:data`'s `NetworkConnectivityObserver` is adapted into that module's
 * `ConnectivityObserver`, without `:core:data` depending on a feature module directly.
 */
class UserPreferences(
    private val dataStore: DataStore<Preferences>,
) : UserPreferencesRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val cloudAiEnabled: Flow<Boolean> =
        dataStore.data.map { preferences -> preferences[CLOUD_AI_ENABLED_KEY] ?: CLOUD_AI_ENABLED_DEFAULT }

    private val cachedCloudAiEnabled: StateFlow<Boolean> =
        cloudAiEnabled.stateIn(scope, SharingStarted.Eagerly, CLOUD_AI_ENABLED_DEFAULT)

    fun isCloudAiAllowedSnapshot(): Boolean = cachedCloudAiEnabled.value

    @Suppress("SwallowedException")
    override suspend fun setCloudAiEnabled(enabled: Boolean): EmptyResult<DataError.Local> =
        try {
            dataStore.edit { preferences -> preferences[CLOUD_AI_ENABLED_KEY] = enabled }
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            Result.Error(DataError.Local.DISK_FULL)
        } catch (e: Exception) {
            Result.Error(DataError.Local.UNKNOWN)
        }
}
