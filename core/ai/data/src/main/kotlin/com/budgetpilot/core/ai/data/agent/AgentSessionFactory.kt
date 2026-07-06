package com.budgetpilot.core.ai.data.agent

import com.budgetpilot.core.ai.data.prompt.PromptId
import com.budgetpilot.core.ai.data.prompt.PromptRepository
import com.budgetpilot.core.ai.domain.AgentLoop
import com.budgetpilot.core.ai.domain.AgentTool
import com.budgetpilot.core.ai.domain.LlmClient

/**
 * Assembles the read-only Q&A [AgentLoop] (PLAN.md §5.3/§6 Phase 4) from an [LlmClient], the
 * read-only tool set, and the versioned agent system prompt. The same factory backs both the real
 * Gemini-wired session (Koin's `coreAiDataModule`) and demo/test sessions — only the [llmClient]
 * and [tools] passed in differ.
 */
class AgentSessionFactory(
    private val llmClient: LlmClient,
    private val tools: List<AgentTool>,
    private val promptRepository: PromptRepository,
    private val maxIterations: Int = 6,
) {
    val systemPrompt: String get() = promptRepository.getPrompt(PromptId.AGENT_V1)

    fun createAgentLoop(): AgentLoop = AgentLoop(llm = llmClient, tools = tools, maxIterations = maxIterations)
}
