package com.budgetpilot.feature.expenses.presentation.di

import com.budgetpilot.feature.expenses.presentation.ExpenseEditorViewModel
import com.budgetpilot.feature.expenses.presentation.ExpenseListViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val expensesPresentationModule =
    module {
        viewModelOf(::ExpenseListViewModel)
        viewModelOf(::ExpenseEditorViewModel)
    }
