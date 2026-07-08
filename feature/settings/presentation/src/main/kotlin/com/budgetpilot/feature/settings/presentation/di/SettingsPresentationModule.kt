package com.budgetpilot.feature.settings.presentation.di

import com.budgetpilot.feature.settings.presentation.SettingsViewModel
import com.budgetpilot.feature.settings.presentation.demo.DemoDataSeeder
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingsPresentationModule =
    module {
        singleOf(::DemoDataSeeder)
        viewModelOf(::SettingsViewModel)
    }
