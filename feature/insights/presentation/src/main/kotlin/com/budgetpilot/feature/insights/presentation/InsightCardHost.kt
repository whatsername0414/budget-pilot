package com.budgetpilot.feature.insights.presentation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgetpilot.core.presentation.ObserveAsEvents
import com.budgetpilot.feature.insights.presentation.components.InsightCard
import org.koin.androidx.compose.koinViewModel

/**
 * Stateful entry point for the dashboard's insight slot (DESIGN-SPEC.md §11). Kept in
 * `:feature:insights:presentation` and wired into `:feature:home:presentation`'s slot only via a
 * callback lambda supplied at `:app` level, per PLAN.md §3's "features never depend on each
 * other" rule. Renders nothing when there is no undismissed insight to show.
 */
@Composable
fun InsightCardHost(
    onNavigateToAsk: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: InsightViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    val notificationPermissionLauncher =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
                viewModel.onAction(InsightAction.OnNotificationPermissionResult)
            }
        } else {
            null
        }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is InsightEvent.NavigateToAsk -> onNavigateToAsk(event.prefillQuestion)
            InsightEvent.RequestNotificationPermission ->
                notificationPermissionLauncher?.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    val card = state.card
    if (card != null) {
        InsightCard(
            message = card.message,
            onDismiss = { viewModel.onAction(InsightAction.OnDismissClick) },
            onAskMore = { viewModel.onAction(InsightAction.OnAskMoreClick) },
            modifier = modifier,
        )
    }
}
