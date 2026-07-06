package com.budgetpilot.core.ai.data.prompt

fun interface PromptFileSource {
    fun read(fileName: String): String
}
