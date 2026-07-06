package com.budgetpilot.core.ai.data.prompt

interface PromptRepository {
    fun getPrompt(id: PromptId): String
}

class AssetPromptRepository(
    private val fileSource: PromptFileSource,
) : PromptRepository {
    private val cache = mutableMapOf<PromptId, String>()

    override fun getPrompt(id: PromptId): String = cache.getOrPut(id) { fileSource.read(id.fileName) }
}
