package com.budgetpilot.core.ai.data.di

import com.budgetpilot.core.ai.data.BuildConfig
import com.budgetpilot.core.ai.data.KtorGeminiLlmClient
import com.budgetpilot.core.ai.data.RateLimiter
import com.budgetpilot.core.ai.data.prompt.AndroidAssetPromptFileSource
import com.budgetpilot.core.ai.data.prompt.AssetPromptRepository
import com.budgetpilot.core.ai.data.prompt.PromptFileSource
import com.budgetpilot.core.ai.data.prompt.PromptRepository
import com.budgetpilot.core.ai.domain.LlmClient
import com.budgetpilot.core.data.network.HttpClientFactory
import com.budgetpilot.core.domain.ai.ApiKeyStatusProvider
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreAiDataModule =
    module {
        single { HttpClientFactory.create(OkHttp.create()) }
        single { RateLimiter() }
        single<LlmClient> { KtorGeminiLlmClient(httpClient = get(), rateLimiter = get()) }
        single<PromptFileSource> { AndroidAssetPromptFileSource(androidContext()) }
        single<PromptRepository> { AssetPromptRepository(get()) }
        single<ApiKeyStatusProvider> { ApiKeyStatusProvider { BuildConfig.GEMINI_API_KEY.isNotBlank() } }
    }
