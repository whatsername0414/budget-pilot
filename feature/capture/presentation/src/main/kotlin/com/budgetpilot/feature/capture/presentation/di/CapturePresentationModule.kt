package com.budgetpilot.feature.capture.presentation.di

import com.budgetpilot.feature.capture.presentation.capture.CaptureViewModel
import com.budgetpilot.feature.capture.presentation.confirm.ConfirmExpenseViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val capturePresentationModule =
    module {
        viewModelOf(::CaptureViewModel)
        viewModelOf(::ConfirmExpenseViewModel)
    }
