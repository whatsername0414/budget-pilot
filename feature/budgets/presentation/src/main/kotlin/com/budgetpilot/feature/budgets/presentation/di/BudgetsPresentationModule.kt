package com.budgetpilot.feature.budgets.presentation.di

import com.budgetpilot.feature.budgets.presentation.charts.ChartsViewModel
import com.budgetpilot.feature.budgets.presentation.editor.BudgetEditorViewModel
import com.budgetpilot.feature.budgets.presentation.main.BudgetListViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val budgetsPresentationModule =
    module {
        viewModelOf(::BudgetListViewModel)
        viewModelOf(::ChartsViewModel)
        viewModel { params ->
            BudgetEditorViewModel(
                categoryId = params.get(),
                month = params.get(),
                budgetRepository = get(),
                categoryRepository = get(),
            )
        }
    }
