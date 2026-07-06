package com.budgetpilot.feature.capture.data.fake

import com.budgetpilot.core.ai.data.prompt.PromptId
import com.budgetpilot.core.ai.data.prompt.PromptRepository

class FakePromptRepository(
    private val prompts: Map<PromptId, String> =
        mapOf(
            PromptId.EXTRACTION_V1 to "EXTRACT",
            PromptId.REPAIR_V1 to "REPAIR:{{malformed_output}}",
        ),
) : PromptRepository {
    override fun getPrompt(id: PromptId): String = prompts.getValue(id)
}
