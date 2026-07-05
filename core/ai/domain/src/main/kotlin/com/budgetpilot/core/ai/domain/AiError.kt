package com.budgetpilot.core.ai.domain

import com.budgetpilot.core.domain.Error

sealed interface AiError : Error {
    data object RateLimited : AiError

    data object QuotaExhausted : AiError

    data object MalformedOutput : AiError

    data object MaxIterations : AiError

    data class ToolFailure(
        val tool: String,
    ) : AiError

    data object NoApiKey : AiError

    data object Network : AiError
}
