package com.budgetpilot.core.ai.data

import com.budgetpilot.core.ai.data.dto.GeminiErrorEnvelope
import com.budgetpilot.core.ai.data.dto.GeminiGenerateContentRequest
import com.budgetpilot.core.ai.data.dto.GeminiGenerateContentResponse
import com.budgetpilot.core.ai.data.dto.retryDelayMillis
import com.budgetpilot.core.ai.data.mapper.toGeminiRequest
import com.budgetpilot.core.ai.data.mapper.toLlmResponse
import com.budgetpilot.core.ai.domain.AiError
import com.budgetpilot.core.ai.domain.LlmClient
import com.budgetpilot.core.ai.domain.model.LlmRequest
import com.budgetpilot.core.ai.domain.model.LlmResponse
import com.budgetpilot.core.domain.Result
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * Fixed per-call knobs, grouped into their own type so [KtorGeminiLlmClient]'s constructor stays
 * under the project's `LongParameterList` threshold.
 */
data class GeminiClientConfig(
    val modelId: String = DEFAULT_MODEL_ID,
    val baseUrl: String = DEFAULT_BASE_URL,
    val maxRetries: Int = DEFAULT_MAX_RETRIES,
) {
    companion object {
        // Verified free-tier-eligible Flash model as of the 2026-07-06 doc check (CLAUDE.md §10,
        // P3.1) — re-verify against a live AI Studio project before relying on this in production.
        const val DEFAULT_MODEL_ID = "gemini-2.5-flash"
        const val DEFAULT_BASE_URL = "https://generativelanguage.googleapis.com/v1beta"
        const val DEFAULT_MAX_RETRIES = 3
    }
}

/**
 * `v1beta generateContent` REST client (PLAN.md §5.2/§5.3). Rate-gates every attempt (including
 * retries) through [rateLimiter], and retries 429/5xx responses with exponential backoff + jitter
 * (1s, 2s, 4s for the default [GeminiClientConfig.maxRetries] of 3), honoring the server's
 * `RetryInfo.retryDelay` hint when present, before giving up with [AiError.RateLimited].
 */
class KtorGeminiLlmClient(
    private val httpClient: HttpClient,
    private val rateLimiter: RateLimiter,
    private val apiKey: String? = BuildConfig.GEMINI_API_KEY.ifBlank { null },
    private val config: GeminiClientConfig = GeminiClientConfig(),
    private val delayFn: suspend (Long) -> Unit = ::delay,
    private val jitterMillis: () -> Long = { Random.nextLong(JITTER_RANGE_MILLIS) },
) : LlmClient {
    override suspend fun complete(request: LlmRequest): Result<LlmResponse, AiError> {
        val key = apiKey
        if (key.isNullOrBlank()) return Result.Error(AiError.NoApiKey)

        val body = request.toGeminiRequest()
        var retryCount = 0
        while (true) {
            when (val outcome = rateLimiter.withRateLimit { attemptCall(key, body) }) {
                is CallOutcome.Success -> return outcome.response.toLlmResponse()
                is CallOutcome.Failure -> return Result.Error(outcome.error)
                is CallOutcome.Retryable -> {
                    if (retryCount >= config.maxRetries) return Result.Error(AiError.RateLimited)
                    delayFn(outcome.retryDelayMillis ?: (backoffBaseMillis(retryCount) + jitterMillis()))
                    retryCount++
                }
            }
        }
    }

    @Suppress("SwallowedException")
    private suspend fun attemptCall(
        apiKey: String,
        body: GeminiGenerateContentRequest,
    ): CallOutcome {
        val response =
            try {
                httpClient.post("${config.baseUrl}/models/${config.modelId}:generateContent") {
                    contentType(ContentType.Application.Json)
                    header(API_KEY_HEADER, apiKey)
                    setBody(body)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                return CallOutcome.Failure(AiError.Network)
            }

        return when (response.status.value) {
            in HTTP_SUCCESS_RANGE -> parseSuccess(response)
            HTTP_TOO_MANY_REQUESTS, in HTTP_SERVER_ERROR_RANGE -> CallOutcome.Retryable(parseRetryDelay(response))
            else -> CallOutcome.Failure(AiError.Network)
        }
    }

    @Suppress("SwallowedException")
    private suspend fun parseSuccess(response: HttpResponse): CallOutcome =
        try {
            CallOutcome.Success(response.body<GeminiGenerateContentResponse>())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            CallOutcome.Failure(AiError.MalformedOutput)
        }

    @Suppress("SwallowedException")
    private suspend fun parseRetryDelay(response: HttpResponse): Long? =
        try {
            response.body<GeminiErrorEnvelope>().error.retryDelayMillis()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }

    private fun backoffBaseMillis(retryCount: Int): Long = BASE_BACKOFF_MILLIS * (1L shl retryCount)

    private sealed interface CallOutcome {
        data class Success(
            val response: GeminiGenerateContentResponse,
        ) : CallOutcome

        data class Failure(
            val error: AiError,
        ) : CallOutcome

        data class Retryable(
            val retryDelayMillis: Long?,
        ) : CallOutcome
    }

    companion object {
        private const val API_KEY_HEADER = "x-goog-api-key"
        private const val HTTP_TOO_MANY_REQUESTS = 429
        private val HTTP_SUCCESS_RANGE = 200..299
        private val HTTP_SERVER_ERROR_RANGE = 500..599
        private const val BASE_BACKOFF_MILLIS = 1_000L
        private const val JITTER_RANGE_MILLIS = 250L
    }
}
