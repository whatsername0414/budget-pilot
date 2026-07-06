package com.budgetpilot.feature.insights.data.di

import com.budgetpilot.feature.insights.data.InsightCheckUseCase
import com.budgetpilot.feature.insights.data.InsightMessageComposer
import com.budgetpilot.feature.insights.data.RoomInsightStore
import com.budgetpilot.feature.insights.data.notification.InsightNotifier
import com.budgetpilot.feature.insights.data.worker.InsightCheckWorker
import com.budgetpilot.feature.insights.domain.InsightHistoryStore
import com.budgetpilot.feature.insights.domain.InsightRuleEngine
import com.budgetpilot.feature.insights.domain.InsightStore
import com.budgetpilot.feature.insights.domain.InsightThrottlePolicy
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.dsl.worker
import org.koin.dsl.module

val insightsDataModule =
    module {
        single { InsightRuleEngine(clock = get()) }
        single { InsightThrottlePolicy(historyStore = get(), clock = get()) }
        single { RoomInsightStore(dao = get(), dataStore = get()) }
        single<InsightStore> { get<RoomInsightStore>() }
        single<InsightHistoryStore> { get<RoomInsightStore>() }
        single { InsightMessageComposer(llmClient = get(), promptRepository = get()) }
        single {
            InsightCheckUseCase(
                budgetRepository = get(),
                expenseRepository = get(),
                categoryRepository = get(),
                userPreferencesRepository = get(),
                ruleEngine = get(),
                throttlePolicy = get(),
                messageComposer = get(),
                insightStore = get(),
                clock = get(),
            )
        }
        single { InsightNotifier(androidContext()) }
        worker { params ->
            InsightCheckWorker(
                context = params.get(),
                workerParams = params.get(),
                useCase = get(),
                notifier = get(),
            )
        }
    }
