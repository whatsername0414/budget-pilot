package com.budgetpilot.core.ai.domain.model

data class AgentAnswer(
    val text: String,
    val trace: List<TraceStep>,
)
