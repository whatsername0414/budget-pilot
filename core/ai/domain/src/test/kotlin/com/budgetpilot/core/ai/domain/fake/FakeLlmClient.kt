package com.budgetpilot.core.ai.domain.fake

import com.budgetpilot.core.ai.domain.AiError
import com.budgetpilot.core.ai.domain.LlmClient
import com.budgetpilot.core.ai.domain.model.LlmRequest
import com.budgetpilot.core.ai.domain.model.LlmResponse
import com.budgetpilot.core.domain.Result

class FakeLlmClient(
    script: List<Result<LlmResponse, AiError>>,
) : LlmClient {
    private val remainingResponses = ArrayDeque(script)
    private val mutableReceivedRequests = mutableListOf<LlmRequest>()

    val receivedRequests: List<LlmRequest> get() = mutableReceivedRequests

    override suspend fun complete(request: LlmRequest): Result<LlmResponse, AiError> {
        mutableReceivedRequests += request
        return remainingResponses.removeFirstOrNull()
            ?: error("FakeLlmClient script exhausted: no response queued for call #${mutableReceivedRequests.size}")
    }
}
