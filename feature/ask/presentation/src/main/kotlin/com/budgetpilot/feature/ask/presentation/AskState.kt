package com.budgetpilot.feature.ask.presentation

import androidx.compose.runtime.Stable

enum class AskStagedStatus {
    PLANNING,
    CHECKING_EXPENSES,
    ALMOST_DONE,
}

enum class AskTurnPhase {
    RUNNING,
    ANSWERED,
    ERROR,
}

/** One step of the agent's reasoning trace, already humanized for display by the ViewModel. */
@Stable
data class AskTraceStepUi(
    val toolName: String,
    val argsSummary: String,
    val resultSummary: String,
    val durationMs: Long,
)

sealed interface AskErrorUi {
    data class RateLimited(
        val retryInSeconds: Int,
    ) : AskErrorUi

    data object Offline : AskErrorUi

    data object NoApiKey : AskErrorUi

    data object Generic : AskErrorUi
}

/** One question-answer round trip in the conversation. */
@Stable
data class AskTurn(
    val id: Long,
    val question: String,
    val phase: AskTurnPhase = AskTurnPhase.RUNNING,
    val stagedStatus: AskStagedStatus = AskStagedStatus.PLANNING,
    val trace: List<AskTraceStepUi> = emptyList(),
    val answerText: String = "",
    val dataUsedSummary: String? = null,
    val isTraceExpanded: Boolean = false,
    val modelTurnCount: Int = 0,
    val totalDurationMs: Long = 0,
    val followUpSuggestions: List<String> = emptyList(),
    val error: AskErrorUi? = null,
)

@Stable
data class AskState(
    val questionInput: String = "",
    val isSending: Boolean = false,
    val turns: List<AskTurn> = emptyList(),
)
