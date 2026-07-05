package com.budgetpilot

import android.app.Application
import com.budgetpilot.feature.budgets.presentation.di.budgetsPresentationModule
import com.budgetpilot.feature.dashboard.presentation.di.dashboardPresentationModule
import com.budgetpilot.feature.expenses.presentation.di.expensesPresentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class BudgetPilotApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BudgetPilotApp)
            // More feature and core modules are registered here as later phases add them.
            modules(expensesPresentationModule, budgetsPresentationModule, dashboardPresentationModule)
        }
    }
}
