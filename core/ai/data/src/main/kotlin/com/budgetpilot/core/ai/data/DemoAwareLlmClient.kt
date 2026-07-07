package com.budgetpilot.core.ai.data

import com.budgetpilot.core.ai.domain.AiError
import com.budgetpilot.core.ai.domain.LlmClient
import com.budgetpilot.core.ai.domain.model.LlmRequest
import com.budgetpilot.core.ai.domain.model.LlmResponse
import com.budgetpilot.core.domain.Result

/**
 * Restart-free demo-mode swap for [LlmClient] (PLAN.md §6 Phase 6 / DESIGN-SPEC.md §12): checks
 * [isDemoModeEnabled] on every call rather than picking a client once at Koin graph construction,
 * so toggling demo mode in Settings takes effect on the very next capture/Ask call with no app
 * restart, mirroring how `:feature:capture:domain`'s `ExtractionRouter` re-reads its `CloudAiPolicy`
 * on every extraction instead of caching a routing decision.
 */
class DemoAwareLlmClient(
    private val realClient: LlmClient,
    private val demoClient: LlmClient,
    private val isDemoModeEnabled: () -> Boolean,
) : LlmClient {
    override suspend fun complete(request: LlmRequest): Result<LlmResponse, AiError> =
        if (isDemoModeEnabled()) demoClient.complete(request) else realClient.complete(request)
}
