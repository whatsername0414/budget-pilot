package com.budgetpilot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgetpilot.core.designsystem.theme.BudgetPilotTheme
import com.budgetpilot.core.domain.repository.UserPreferencesRepository
import com.budgetpilot.ui.AppShell
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val userPreferencesRepository: UserPreferencesRepository by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val dynamicColorEnabled by userPreferencesRepository.dynamicColorEnabled.collectAsStateWithLifecycle(false)
            BudgetPilotTheme(dynamicColorEnabled = dynamicColorEnabled) {
                AppShell()
            }
        }
    }
}
