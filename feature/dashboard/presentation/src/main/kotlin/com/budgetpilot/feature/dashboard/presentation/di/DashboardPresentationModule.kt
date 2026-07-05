package com.budgetpilot.feature.dashboard.presentation.di

import com.budgetpilot.feature.dashboard.presentation.DashboardViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val dashboardPresentationModule =
    module {
        viewModelOf(::DashboardViewModel)
    }
