package com.budgetpilot.feature.insights.presentation

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetpilot.feature.insights.data.InsightCheckUseCase
import com.budgetpilot.feature.insights.domain.InsightStore
import com.budgetpilot.feature.insights.domain.model.Insight
import com.budgetpilot.feature.insights.domain.model.InsightType
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Clock

/**
 * Backs the dashboard's insight slot (DESIGN-SPEC.md §11): runs the throttled
 * [InsightCheckUseCase] once per app-open (this ViewModel's [init], scoped to the Home
 * destination's back stack entry so it doesn't re-fire on every recomposition), then always
 * surfaces whatever the store's latest undismissed insight is — which may predate this session's
 * check.
 */
class InsightViewModel(
    private val insightCheckUseCase: InsightCheckUseCase,
    private val insightStore: InsightStore,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModel() {
    private val _state = MutableStateFlow(InsightState())
    val state = _state.asStateFlow()

    private val _events = Channel<InsightEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var currentInsight: Insight? = null

    init {
        viewModelScope.launch {
            insightCheckUseCase.check()
            refresh()
        }
    }

    fun onAction(action: InsightAction) {
        when (action) {
            InsightAction.OnDismissClick -> dismiss()
            InsightAction.OnAskMoreClick -> askMore()
            InsightAction.OnNotificationPermissionResult -> markNotificationPermissionRequested()
        }
    }

    private fun dismiss() {
        val insight = currentInsight ?: return
        viewModelScope.launch {
            insightStore.dismiss(insight.id, clock.instant())
            currentInsight = null
            _state.update { it.copy(card = null) }
        }
    }

    private fun askMore() {
        val insight = currentInsight ?: return
        viewModelScope.launch {
            _events.send(InsightEvent.NavigateToAsk(insight.followUpQuestion ?: prefillQuestionFor(insight.type)))
        }
    }

    private suspend fun refresh() {
        val insight = insightStore.getLatestUndismissed()
        currentInsight = insight
        _state.update { it.copy(card = insight?.let { found -> InsightCardUi(message = found.message) }) }
        if (insight != null) maybeRequestNotificationPermission()
    }

    private fun markNotificationPermissionRequested() {
        viewModelScope.launch { insightStore.markNotificationPermissionRequested() }
    }

    private suspend fun maybeRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        if (insightStore.hasRequestedNotificationPermission()) return
        _events.send(InsightEvent.RequestNotificationPermission)
    }
}

/**
 * Fallback for insights persisted before [Insight.followUpQuestion] existed (that field is
 * null for them) — a generic per-type prompt with no category/merchant/amount detail. Every
 * newly created insight instead gets a grounded question from
 * [com.budgetpilot.feature.insights.data.InsightFollowUpQuestions].
 */
internal fun prefillQuestionFor(type: InsightType): String =
    when (type) {
        InsightType.BUDGET_EXCEEDED -> "How do I get back under budget this month?"
        InsightType.BUDGET_NEAR_LIMIT -> "How do I stay under my budget this month?"
        InsightType.CATEGORY_SPIKE -> "Why is my spending up this month?"
        InsightType.LARGE_EXPENSE -> "How does this expense affect my budget?"
    }
