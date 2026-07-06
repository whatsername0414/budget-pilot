package com.budgetpilot

import android.app.Application
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.WorkManager
import com.budgetpilot.core.ai.data.di.coreAiDataModule
import com.budgetpilot.core.data.di.coreDataModule
import com.budgetpilot.core.database.di.coreDatabaseModule
import com.budgetpilot.feature.ask.presentation.di.askPresentationModule
import com.budgetpilot.feature.budgets.presentation.di.budgetsPresentationModule
import com.budgetpilot.feature.capture.data.di.captureDataModule
import com.budgetpilot.feature.capture.presentation.di.capturePresentationModule
import com.budgetpilot.feature.expenses.presentation.di.expensesPresentationModule
import com.budgetpilot.feature.home.presentation.di.homePresentationModule
import com.budgetpilot.feature.insights.data.di.insightsDataModule
import com.budgetpilot.feature.insights.data.worker.InsightCheckWorker
import com.budgetpilot.feature.settings.presentation.di.settingsPresentationModule
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

class BudgetPilotApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@BudgetPilotApp)
            workManagerFactory()
            // More feature and core modules are registered here as later phases add them.
            modules(
                coreDataModule,
                coreDatabaseModule,
                coreAiDataModule,
                expensesPresentationModule,
                budgetsPresentationModule,
                homePresentationModule,
                captureDataModule,
                capturePresentationModule,
                settingsPresentationModule,
                askPresentationModule,
                insightsDataModule,
            )
        }
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            InsightCheckWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            InsightCheckWorker.periodicRequest(),
        )
    }
}
