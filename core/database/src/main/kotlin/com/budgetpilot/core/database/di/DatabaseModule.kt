package com.budgetpilot.core.database.di

import com.budgetpilot.core.database.BudgetPilotDatabase
import com.budgetpilot.core.database.buildBudgetPilotDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreDatabaseModule =
    module {
        single { buildBudgetPilotDatabase(androidContext()) }
        single { get<BudgetPilotDatabase>().categoryDao() }
        single { get<BudgetPilotDatabase>().expenseDao() }
        single { get<BudgetPilotDatabase>().budgetDao() }
    }
