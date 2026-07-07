package com.budgetpilot.feature.insights.presentation.di

import com.budgetpilot.feature.insights.presentation.InsightViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val insightsPresentationModule =
    module {
        viewModelOf(::InsightViewModel)
    }
