package com.budgetpilot.core.ai.data

import com.budgetpilot.core.ai.domain.AiError
import com.budgetpilot.core.ai.domain.LlmClient
import com.budgetpilot.core.ai.domain.model.LlmRequest
import com.budgetpilot.core.ai.domain.model.LlmResponse
import com.budgetpilot.core.domain.Result

/**
 * Scripted [LlmClient] for tests and offline demo mode. Each [complete] call returns the next
 * queued result in order (success or a scripted [AiError]) and records the request it was
 * given, so tests can assert both on the responses handed back and on what the caller sent.
 * Defaults to [DEFAULT_SCRIPT] — one canned extraction result — so it also works as a
 * zero-setup demo-mode [LlmClient].
 */
class FakeLlmClient(
    script: List<Result<LlmResponse, AiError>> = DEFAULT_SCRIPT,
) : LlmClient {
    private val remainingResponses = ArrayDeque(script)
    private val mutableReceivedRequests = mutableListOf<LlmRequest>()

    val receivedRequests: List<LlmRequest> get() = mutableReceivedRequests

    override suspend fun complete(request: LlmRequest): Result<LlmResponse, AiError> {
        mutableReceivedRequests += request
        return remainingResponses.removeFirstOrNull()
            ?: error("FakeLlmClient script exhausted: no response queued for call #${mutableReceivedRequests.size}")
    }

    companion object {
        // language=JSON
        private val cannedExtractionJson =
            """
            {
              "receipt_type": "PAPER",
              "merchant": { "value": "Jollibee", "confidence": "HIGH" },
              "date": { "value": "2026-06-15", "confidence": "HIGH" },
              "line_items": {
                "value": [
                  { "description": "1pc Chickenjoy w/ Rice", "amount": 89.00 },
                  { "description": "Jolly Spaghetti", "amount": 65.00 },
                  { "description": "Coke Float", "amount": 45.00 }
                ],
                "confidence": "HIGH"
              },
              "total": { "value": 199.00, "confidence": "HIGH" },
              "suggested_category": { "value": "Food", "confidence": "HIGH" }
            }
            """.trimIndent()

        val DEFAULT_SCRIPT: List<Result<LlmResponse, AiError>> =
            listOf(Result.Success(LlmResponse.Text(cannedExtractionJson)))
    }
}
