package com.budgetpilot.core.database.di

import com.budgetpilot.core.database.BudgetPilotDatabase
import com.budgetpilot.core.database.buildBudgetPilotDatabase
import com.budgetpilot.core.database.repository.RoomBudgetRepository
import com.budgetpilot.core.database.repository.RoomCategoryRepository
import com.budgetpilot.core.database.repository.RoomExpenseRepository
import com.budgetpilot.core.domain.repository.BudgetRepository
import com.budgetpilot.core.domain.repository.CategoryRepository
import com.budgetpilot.core.domain.repository.ExpenseRepository
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreDatabaseModule =
    module {
        single { buildBudgetPilotDatabase(androidContext()) }
        single { get<BudgetPilotDatabase>().categoryDao() }
        single { get<BudgetPilotDatabase>().expenseDao() }
        single { get<BudgetPilotDatabase>().budgetDao() }
        single { get<BudgetPilotDatabase>().extractionCacheDao() }
        single<CategoryRepository> { RoomCategoryRepository(get()) }
        single<ExpenseRepository> { RoomExpenseRepository(get()) }
        single<BudgetRepository> { RoomBudgetRepository(get()) }
    }
