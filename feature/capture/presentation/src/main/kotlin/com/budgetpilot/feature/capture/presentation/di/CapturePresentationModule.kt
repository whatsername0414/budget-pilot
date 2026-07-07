package com.budgetpilot.feature.capture.presentation.di

import com.budgetpilot.feature.capture.presentation.capture.CaptureViewModel
import com.budgetpilot.feature.capture.presentation.confirm.ConfirmExpenseViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

private val ON_DEVICE_EXTRACTOR = named("onDeviceExtractor")

val capturePresentationModule =
    module {
        viewModelOf(::CaptureViewModel)
        viewModel {
            ConfirmExpenseViewModel(
                savedStateHandle = get(),
                receiptExtractor = get(),
                onDeviceExtractor = get(ON_DEVICE_EXTRACTOR),
                expenseRepository = get(),
                categoryRepository = get(),
            )
        }
    }
