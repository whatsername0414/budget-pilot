package com.budgetpilot.feature.insights.presentation

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

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is InsightEvent.NavigateToAsk -> onNavigateToAsk(event.prefillQuestion)
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
