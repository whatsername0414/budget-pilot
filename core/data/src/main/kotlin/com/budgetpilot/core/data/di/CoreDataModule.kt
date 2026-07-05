package com.budgetpilot.core.data.di

import com.budgetpilot.core.data.connectivity.NetworkConnectivityObserver
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreDataModule =
    module {
        single { NetworkConnectivityObserver(androidContext()) }
    }
