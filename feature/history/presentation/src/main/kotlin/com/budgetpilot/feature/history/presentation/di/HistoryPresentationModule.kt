package com.budgetpilot.feature.history.presentation.di

import com.budgetpilot.feature.history.presentation.editor.ExpenseEditorViewModel
import com.budgetpilot.feature.history.presentation.expenses.ExpenseListViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val historyPresentationModule =
    module {
        viewModelOf(::ExpenseListViewModel)
        viewModelOf(::ExpenseEditorViewModel)
    }
