package com.budgetpilot.feature.dashboard.presentation.di

import com.budgetpilot.feature.dashboard.presentation.HomeViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val homePresentationModule =
    module {
        viewModelOf(::HomeViewModel)
    }
