package com.budgetpilot.core.ai.domain

import com.budgetpilot.core.ai.domain.model.LlmRequest
import com.budgetpilot.core.ai.domain.model.LlmResponse
import com.budgetpilot.core.domain.Result

interface LlmClient {
    suspend fun complete(request: LlmRequest): Result<LlmResponse, AiError>
}
