package com.budgetpilot.feature.ask.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.budgetpilot.core.designsystem.components.AppTopBar
import com.budgetpilot.core.designsystem.theme.Spacing
import com.budgetpilot.core.presentation.ObserveAsEvents
import com.budgetpilot.feature.ask.presentation.components.AskAnswerCard
import com.budgetpilot.feature.ask.presentation.components.AskDisclaimerBanner
import com.budgetpilot.feature.ask.presentation.components.AskEmptyState
import com.budgetpilot.feature.ask.presentation.components.AskErrorCard
import com.budgetpilot.feature.ask.presentation.components.AskFollowUpChips
import com.budgetpilot.feature.ask.presentation.components.AskInputBar
import com.budgetpilot.feature.ask.presentation.components.AskRunningCard
import com.budgetpilot.feature.ask.presentation.components.QuestionBubble
import org.koin.androidx.compose.koinViewModel

private val TurnItemGap = 14.dp

/** Stateful Ask entry point: owns the ViewModel and forwards its settings-link event. */
@Composable
fun AskScreen(
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AskViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            AskEvent.NavigateToSettings -> onNavigateToSettings()
        }
    }

    AskContent(state = state, onAction = viewModel::onAction, modifier = modifier)
}

/** Stateless Ask screen content (state + onAction only). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AskContent(
    state: AskState,
    onAction: (AskAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            AppTopBar(
                title = stringResource(R.string.ask_top_bar_title),
                actions = {
                    if (state.turns.isNotEmpty()) {
                        TextButton(onClick = { onAction(AskAction.OnClearClick) }) {
                            Text(stringResource(R.string.action_clear))
                        }
                    }
                },
            )
        },
        bottomBar = {
            AskInputBar(
                questionInput = state.questionInput,
                isSending = state.isSending,
                onQuestionChange = { onAction(AskAction.OnQuestionChange(it)) },
                onSendClick = { onAction(AskAction.OnSendClick) },
            )
        },
    ) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            AskDisclaimerBanner(
                modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.medium, vertical = Spacing.small),
            )
            Box(modifier = Modifier.weight(1f).fillMaxSize()) {
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
    Column(verticalArrangement = Arrangement.spacedBy(TurnItemGap)) {
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
