package com.budgetpilot.feature.ask.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.budgetpilot.core.designsystem.components.AppTopBar
import com.budgetpilot.core.designsystem.theme.Spacing
import com.budgetpilot.feature.ask.presentation.components.AskAnswerCard
import com.budgetpilot.feature.ask.presentation.components.AskEmptyState
import com.budgetpilot.feature.ask.presentation.components.AskErrorCard
import com.budgetpilot.feature.ask.presentation.components.AskFollowUpChips
import com.budgetpilot.feature.ask.presentation.components.AskInputBar
import com.budgetpilot.feature.ask.presentation.components.AskRunningCard
import com.budgetpilot.feature.ask.presentation.components.QuestionBubble

/**
 * Stateless Ask screen content (state + onAction only). [AskViewModel] and the
 * `AskScreen` wrapper wiring it via `koinViewModel()` arrive in P4.5.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskContent(
    state: AskState,
    onAction: (AskAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = { AppTopBar(title = stringResource(R.string.ask_top_bar_title)) },
        bottomBar = {
            AskInputBar(
                questionInput = state.questionInput,
                isSending = state.isSending,
                onQuestionChange = { onAction(AskAction.OnQuestionChange(it)) },
                onSendClick = { onAction(AskAction.OnSendClick) },
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (state.turns.isEmpty()) {
                AskEmptyState(
                    onSuggestionClick = { onAction(AskAction.OnSuggestionClick(it)) },
                    modifier = Modifier.align(Alignment.Center),
                )
            } else {
                AskConversationList(state = state, onAction = onAction)
            }
        }
    }
}

@Composable
private fun AskConversationList(
    state: AskState,
    onAction: (AskAction) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        reverseLayout = true,
        contentPadding = PaddingValues(Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.medium),
    ) {
        items(items = state.turns.reversed(), key = { it.id }) { turn ->
            AskTurnItem(turn = turn, onAction = onAction)
        }
    }
}

@Composable
private fun AskTurnItem(
    turn: AskTurn,
    onAction: (AskAction) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.small)) {
        QuestionBubble(question = turn.question)
        when (turn.phase) {
            AskTurnPhase.RUNNING -> AskRunningCard(stagedStatus = turn.stagedStatus, trace = turn.trace)
            AskTurnPhase.ANSWERED -> {
                AskAnswerCard(
                    turn = turn,
                    onToggleTraceExpand = { onAction(AskAction.OnToggleTraceExpanded(turn.id)) },
                )
                if (turn.followUpSuggestions.isNotEmpty()) {
                    AskFollowUpChips(
                        suggestions = turn.followUpSuggestions,
                        onSuggestionClick = { onAction(AskAction.OnSuggestionClick(it)) },
                    )
                }
            }
            AskTurnPhase.ERROR ->
                turn.error?.let { error ->
                    AskErrorCard(
                        error = error,
                        onRetryClick = { onAction(AskAction.OnRetryClick(turn.id)) },
                        onOpenSettingsClick = { onAction(AskAction.OnOpenSettingsClick) },
                    )
                }
        }
    }
}
