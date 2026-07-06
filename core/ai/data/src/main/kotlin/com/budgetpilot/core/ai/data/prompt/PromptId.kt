package com.budgetpilot.core.ai.data.prompt

enum class PromptId(
    val fileName: String,
) {
    EXTRACTION_V1("extraction_v1.md"),
    REPAIR_V1("repair_v1.md"),
    AGENT_V1("agent_v1.md"),
    INSIGHT_V1("insight_v1.md"),
}
