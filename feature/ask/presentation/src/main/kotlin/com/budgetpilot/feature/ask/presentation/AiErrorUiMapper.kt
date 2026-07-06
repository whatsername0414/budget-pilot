package com.budgetpilot.feature.ask.presentation

import com.budgetpilot.core.ai.data.RateLimiter
import com.budgetpilot.core.ai.domain.AiError

private const val MILLIS_PER_SECOND = 1_000L

/**
 * Many-to-one reduction from the agent-loop-shaped [AiError] to the four Ask error cards
 * (DESIGN-SPEC.md §10): [AiError.QuotaExhausted], [AiError.MalformedOutput],
 * [AiError.MaxIterations] and [AiError.ToolFailure] have no dedicated card, so they all collapse
 * to [AskErrorUi.Generic]. [AiError.RateLimited] carries no server-provided retry delay, so the
 * countdown reuses [RateLimiter]'s own minimum retry interval as a reasonable default.
 */
internal fun AiError.toAskErrorUi(): AskErrorUi =
    when (this) {
        AiError.RateLimited ->
            AskErrorUi.RateLimited(retryInSeconds = (RateLimiter.DEFAULT_MIN_INTERVAL_MILLIS / MILLIS_PER_SECOND).toInt())
        AiError.Network -> AskErrorUi.Offline
        AiError.NoApiKey -> AskErrorUi.NoApiKey
        AiError.QuotaExhausted,
        AiError.MalformedOutput,
        AiError.MaxIterations,
        is AiError.ToolFailure,
        -> AskErrorUi.Generic
    }
