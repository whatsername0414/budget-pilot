package com.budgetpilot.core.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.budgetpilot.core.domain.DataError
import com.budgetpilot.core.domain.EmptyResult
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.preferences.CloudAiAvailability
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
private val PRIVATE_MODE_ENABLED_KEY = booleanPreferencesKey("private_mode_enabled")
private const val PRIVATE_MODE_ENABLED_DEFAULT = false
private val DEMO_MODE_ENABLED_KEY = booleanPreferencesKey("demo_mode_enabled")
private const val DEMO_MODE_ENABLED_DEFAULT = false

/**
 * DataStore-backed [UserPreferencesRepository]. Also exposes [isCloudAiAllowedSnapshot] and
 * [isDemoModeEnabledSnapshot], synchronous cached reads (kept warm via an internal
 * [CoroutineScope] collecting the underlying flows) — needed so whichever module owns
 * `:feature:capture:domain`'s non-suspend `CloudAiPolicy` fun interface, or `:core:ai:data`'s
 * demo-mode `LlmClient` swap, can SAM-adapt this class from its own Koin wiring, the same way
 * `:core:data`'s `NetworkConnectivityObserver` is adapted into that module's `ConnectivityObserver`,
 * without `:core:data` depending on a feature module directly. [isCloudAiAllowedSnapshot] combines
 * [cloudAiEnabled] with [privateModeEnabled] via [CloudAiAvailability] rather than private mode
 * overwriting the stored cloud-AI value, so the cloud-AI preference "remembers" its prior value
 * once private mode is turned back off.
 */
class UserPreferences(
    private val dataStore: DataStore<Preferences>,
) : UserPreferencesRepository {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val cloudAiEnabled: Flow<Boolean> =
        dataStore.data.map { preferences -> preferences[CLOUD_AI_ENABLED_KEY] ?: CLOUD_AI_ENABLED_DEFAULT }

    override val privateModeEnabled: Flow<Boolean> =
        dataStore.data.map { preferences -> preferences[PRIVATE_MODE_ENABLED_KEY] ?: PRIVATE_MODE_ENABLED_DEFAULT }

    override val demoModeEnabled: Flow<Boolean> =
        dataStore.data.map { preferences -> preferences[DEMO_MODE_ENABLED_KEY] ?: DEMO_MODE_ENABLED_DEFAULT }

    private val cachedCloudAiEnabled: StateFlow<Boolean> =
        cloudAiEnabled.stateIn(scope, SharingStarted.Eagerly, CLOUD_AI_ENABLED_DEFAULT)

    private val cachedPrivateModeEnabled: StateFlow<Boolean> =
        privateModeEnabled.stateIn(scope, SharingStarted.Eagerly, PRIVATE_MODE_ENABLED_DEFAULT)

    private val cachedDemoModeEnabled: StateFlow<Boolean> =
        demoModeEnabled.stateIn(scope, SharingStarted.Eagerly, DEMO_MODE_ENABLED_DEFAULT)

    fun isCloudAiAllowedSnapshot(): Boolean =
        CloudAiAvailability.isAllowed(
            cloudAiEnabled = cachedCloudAiEnabled.value,
            privateModeEnabled = cachedPrivateModeEnabled.value,
        )

    fun isDemoModeEnabledSnapshot(): Boolean = cachedDemoModeEnabled.value

    override suspend fun setCloudAiEnabled(enabled: Boolean): EmptyResult<DataError.Local> =
        setBooleanPreference(CLOUD_AI_ENABLED_KEY, enabled)

    override suspend fun setPrivateModeEnabled(enabled: Boolean): EmptyResult<DataError.Local> =
        setBooleanPreference(PRIVATE_MODE_ENABLED_KEY, enabled)

    override suspend fun setDemoModeEnabled(enabled: Boolean): EmptyResult<DataError.Local> =
        setBooleanPreference(DEMO_MODE_ENABLED_KEY, enabled)

    @Suppress("SwallowedException")
    private suspend fun setBooleanPreference(
        key: Preferences.Key<Boolean>,
        value: Boolean,
    ): EmptyResult<DataError.Local> =
        try {
            dataStore.edit { preferences -> preferences[key] = value }
            Result.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: IOException) {
            Result.Error(DataError.Local.DISK_FULL)
        } catch (e: Exception) {
            Result.Error(DataError.Local.UNKNOWN)
        }
}
