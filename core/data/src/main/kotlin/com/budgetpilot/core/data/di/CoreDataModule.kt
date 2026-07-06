package com.budgetpilot.core.data.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import com.budgetpilot.core.data.connectivity.NetworkConnectivityObserver
import com.budgetpilot.core.data.preferences.UserPreferences
import com.budgetpilot.core.domain.repository.UserPreferencesRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private const val USER_PREFERENCES_FILE_NAME = "user_preferences"

val coreDataModule =
    module {
        single { NetworkConnectivityObserver(androidContext()) }
        single<DataStore<Preferences>> {
            PreferenceDataStoreFactory.create(
                produceFile = { androidContext().preferencesDataStoreFile(USER_PREFERENCES_FILE_NAME) },
            )
        }
        single { UserPreferences(get()) }
        single<UserPreferencesRepository> { get<UserPreferences>() }
    }
