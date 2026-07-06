package com.budgetpilot.core.domain.ai

fun interface ApiKeyStatusProvider {
    fun isApiKeyConfigured(): Boolean
}
