package com.budgetpilot

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BudgetPilotApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BudgetPilotApp)
            // Feature and core modules are registered here as later phases add them.
            modules(emptyList())
        }
    }
}
