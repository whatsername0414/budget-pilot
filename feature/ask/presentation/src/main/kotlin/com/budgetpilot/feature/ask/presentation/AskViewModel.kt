package com.budgetpilot.feature.ask.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.budgetpilot.core.ai.data.agent.AgentSessionFactory
import com.budgetpilot.core.ai.domain.AiError
import com.budgetpilot.core.ai.domain.model.AgentAnswer
import com.budgetpilot.core.ai.domain.model.TraceStep
import com.budgetpilot.core.domain.Result
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AskViewModel(
    savedStateHandle: SavedStateHandle,
    private val agentSessionFactory: AgentSessionFactory,
) : ViewModel() {
    private val _state =
        MutableStateFlow(
            AskState(questionInput = savedStateHandle.get<String>(KEY_PREFILL_QUESTION) ?: ""),
        )
    val state = _state.asStateFlow()

    private val _events = Channel<AskEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private var nextTurnId = 0L
    private var activeRun: Job? = null

    fun onAction(action: AskAction) {
        when (action) {
            is AskAction.OnQuestionChange -> _state.update { it.copy(questionInput = action.text) }
            AskAction.OnSendClick -> ask(_state.value.questionInput)
            is AskAction.OnSuggestionClick -> ask(action.suggestion)
            is AskAction.OnToggleTraceExpanded -> toggleTraceExpanded(action.turnId)
            is AskAction.OnRetryClick -> retry(action.turnId)
            AskAction.OnOpenSettingsClick -> viewModelScope.launch { _events.send(AskEvent.NavigateToSettings) }
            AskAction.OnClearClick -> clear()
        }
    }

    private fun clear() {
        activeRun?.cancel()
        activeRun = null
        _state.value = AskState()
    }

    private fun ask(question: String) {
        val trimmed = question.trim()
        if (trimmed.isBlank()) return

        val turnId = nextTurnId++
        _state.update {
            it.copy(
                questionInput = "",
                turns = it.turns + AskTurn(id = turnId, question = trimmed),
            )
        }
        runTurn(turnId, trimmed)
    }

    private fun retry(turnId: Long) {
        val question =
            _state.value.turns
                .find { it.id == turnId }
                ?.question ?: return
        _state.update { state ->
            state.copy(
                turns =
                    state.turns.map { turn ->
                        if (turn.id != turnId) {
                            turn
                        } else {
                            turn.copy(
                                phase = AskTurnPhase.RUNNING,
                                stagedStatus = AskStagedStatus.PLANNING,
                                trace = emptyList(),
                                error = null,
                            )
                        }
                    },
            )
        }
        runTurn(turnId, question)
    }

    // New question or retry cancels whatever's still running — only one turn is ever in flight;
    // viewModelScope cancellation on onCleared() covers navigating away mid-run.
    private fun runTurn(
        turnId: Long,
        question: String,
    ) {
        activeRun?.cancel()
        _state.update { it.copy(isSending = true) }
        activeRun =
            viewModelScope.launch {
                val result =
                    agentSessionFactory.createAgentLoop().run(
                        goal = question,
                        systemPrompt = agentSessionFactory.systemPrompt,
                        onStep = { step -> onTraceStep(turnId, step) },
                    )
                when (result) {
                    is Result.Success -> onAnswer(turnId, result.data)
                    is Result.Error -> onError(turnId, result.error)
                }
                _state.update { it.copy(isSending = false) }
            }
    }

    private fun onTraceStep(
        turnId: Long,
        step: TraceStep,
    ) {
        if (step !is TraceStep.ToolInvocation) return
        updateTurn(turnId) { turn ->
            val trace = turn.trace + step.toAskTraceStepUi()
            turn.copy(trace = trace, stagedStatus = stagedStatusFor(trace.size))
        }
    }

    private fun onAnswer(
        turnId: Long,
        answer: AgentAnswer,
    ) {
        updateTurn(turnId) { turn ->
            turn.copy(
                phase = AskTurnPhase.ANSWERED,
                answerText = answer.text,
                dataUsedSummary = turn.trace.lastOrNull()?.let { "Based on ${it.resultSummary}." },
                modelTurnCount = turn.trace.size,
                totalDurationMs = turn.trace.sumOf { it.durationMs },
            )
        }
    }

    private fun onError(
        turnId: Long,
        error: AiError,
    ) {
        updateTurn(turnId) { turn -> turn.copy(phase = AskTurnPhase.ERROR, error = error.toAskErrorUi()) }
    }

    private fun toggleTraceExpanded(turnId: Long) {
        updateTurn(turnId) { turn -> turn.copy(isTraceExpanded = !turn.isTraceExpanded) }
    }

    private fun updateTurn(
        turnId: Long,
        transform: (AskTurn) -> AskTurn,
    ) {
        _state.update { state ->
            state.copy(turns = state.turns.map { turn -> if (turn.id == turnId) transform(turn) else turn })
        }
    }

    private companion object {
        const val KEY_PREFILL_QUESTION = "prefillQuestion"
    }
}

internal fun stagedStatusFor(completedSteps: Int): AskStagedStatus =
    when {
        completedSteps <= 0 -> AskStagedStatus.PLANNING
        completedSteps == 1 -> AskStagedStatus.CHECKING_EXPENSES
        else -> AskStagedStatus.ALMOST_DONE
    }
