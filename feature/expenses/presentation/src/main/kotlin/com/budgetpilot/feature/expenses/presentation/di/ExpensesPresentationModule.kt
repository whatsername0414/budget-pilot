package com.budgetpilot.feature.expenses.presentation.di

import com.budgetpilot.feature.expenses.presentation.editor.ExpenseEditorViewModel
import com.budgetpilot.feature.expenses.presentation.main.ExpenseListViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val expensesPresentationModule =
    module {
        viewModelOf(::ExpenseListViewModel)
        viewModelOf(::ExpenseEditorViewModel)
    }
