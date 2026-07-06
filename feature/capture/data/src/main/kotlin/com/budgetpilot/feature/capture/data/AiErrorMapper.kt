package com.budgetpilot.feature.capture.data

import com.budgetpilot.core.ai.domain.AiError
import com.budgetpilot.feature.capture.domain.ExtractionError

/**
 * Many-to-one reduction from the agent-loop-shaped [AiError] to the single-shot-extraction-shaped
 * [ExtractionError] (CLAUDE.md §10, P2.2/P3.5): [AiError.QuotaExhausted], [AiError.MalformedOutput],
 * [AiError.NoApiKey], [AiError.ToolFailure] and [AiError.MaxIterations] have no meaningful
 * equivalent for a single extraction call, so they all collapse to [ExtractionError.Cloud.Unavailable].
 */
internal fun AiError.toExtractionError(): ExtractionError =
    when (this) {
        AiError.RateLimited -> ExtractionError.Cloud.RateLimited
        AiError.Network -> ExtractionError.Cloud.Network
        AiError.QuotaExhausted,
        AiError.MalformedOutput,
        AiError.NoApiKey,
        AiError.MaxIterations,
        is AiError.ToolFailure,
        -> ExtractionError.Cloud.Unavailable
    }
