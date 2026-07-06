package com.budgetpilot.feature.ask.presentation.di

import com.budgetpilot.feature.ask.presentation.AskViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val askPresentationModule =
    module {
        viewModelOf(::AskViewModel)
    }
