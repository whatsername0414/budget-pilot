package com.budgetpilot.core.ai.data.di

import com.budgetpilot.core.ai.data.BuildConfig
import com.budgetpilot.core.ai.data.DemoAwareLlmClient
import com.budgetpilot.core.ai.data.FakeLlmClient
import com.budgetpilot.core.ai.data.KtorGeminiLlmClient
import com.budgetpilot.core.ai.data.RateLimiter
import com.budgetpilot.core.ai.data.agent.AgentSessionFactory
import com.budgetpilot.core.ai.data.prompt.AndroidAssetPromptFileSource
import com.budgetpilot.core.ai.data.prompt.AssetPromptRepository
import com.budgetpilot.core.ai.data.prompt.PromptFileSource
import com.budgetpilot.core.ai.data.prompt.PromptRepository
import com.budgetpilot.core.ai.domain.AgentTool
import com.budgetpilot.core.ai.domain.LlmClient
import com.budgetpilot.core.ai.domain.tool.GetBudgetStatusTool
import com.budgetpilot.core.ai.domain.tool.GetBudgetsTool
import com.budgetpilot.core.ai.domain.tool.GetCategoriesTool
import com.budgetpilot.core.ai.domain.tool.QueryExpensesTool
import com.budgetpilot.core.ai.domain.tool.ResolveDateRangeTool
import com.budgetpilot.core.data.network.HttpClientFactory
import com.budgetpilot.core.data.preferences.UserPreferences
import com.budgetpilot.core.domain.ai.ApiKeyStatusProvider
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.time.Clock

private val REAL_LLM_CLIENT = named("realLlmClient")
private val DEMO_LLM_CLIENT = named("demoLlmClient")

val coreAiDataModule =
    module {
        single { HttpClientFactory.create(OkHttp.create()) }
        single { RateLimiter() }
        single<LlmClient>(REAL_LLM_CLIENT) { KtorGeminiLlmClient(httpClient = get(), rateLimiter = get()) }
        single<LlmClient>(DEMO_LLM_CLIENT) { FakeLlmClient() }
        single<LlmClient> {
            val preferences = get<UserPreferences>()
            DemoAwareLlmClient(
                realClient = get(REAL_LLM_CLIENT),
                demoClient = get(DEMO_LLM_CLIENT),
                isDemoModeEnabled = { preferences.isDemoModeEnabledSnapshot() },
            )
        }
        single<PromptFileSource> { AndroidAssetPromptFileSource(androidContext()) }
        single<PromptRepository> { AssetPromptRepository(get()) }
        single<ApiKeyStatusProvider> { ApiKeyStatusProvider { BuildConfig.GEMINI_API_KEY.isNotBlank() } }

        single { Clock.systemDefaultZone() }
        singleOf(::QueryExpensesTool)
        singleOf(::GetBudgetsTool)
        singleOf(::GetBudgetStatusTool)
        singleOf(::GetCategoriesTool)
        singleOf(::ResolveDateRangeTool)
        single<List<AgentTool>> {
            listOf(
                get<QueryExpensesTool>(),
                get<GetBudgetsTool>(),
                get<GetBudgetStatusTool>(),
                get<GetCategoriesTool>(),
                get<ResolveDateRangeTool>(),
            )
        }
        single { AgentSessionFactory(llmClient = get(), tools = get(), promptRepository = get()) }
    }
