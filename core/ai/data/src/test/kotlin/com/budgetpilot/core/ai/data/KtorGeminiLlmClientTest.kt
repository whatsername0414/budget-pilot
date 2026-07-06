package com.budgetpilot.core.ai.data

import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isTrue
import com.budgetpilot.core.ai.data.dto.GeminiGenerateContentRequest
import com.budgetpilot.core.ai.domain.AiError
import com.budgetpilot.core.ai.domain.model.ChatMessage
import com.budgetpilot.core.ai.domain.model.ChatRole
import com.budgetpilot.core.ai.domain.model.LlmRequest
import com.budgetpilot.core.ai.domain.model.LlmResponse
import com.budgetpilot.core.ai.domain.model.MessagePart
import com.budgetpilot.core.ai.domain.model.ToolSchema
import com.budgetpilot.core.data.network.HttpClientFactory
import com.budgetpilot.core.domain.Result
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.headersOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.currentTime
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.junit.jupiter.api.Test

private const val FAKE_API_KEY = "fake-api-key"
private const val API_KEY_HEADER = "x-goog-api-key"
private val testJson = Json { ignoreUnknownKeys = true }

private const val SUCCESS_TEXT_BODY =
    """{"candidates":[{"content":{"role":"model","parts":[{"text":"Hello from Gemini"}]},"finishReason":"STOP"}]}"""

private const val SUCCESS_FUNCTION_CALL_BODY =
    """{"candidates":[{"content":{"role":"model","parts":[
        {"functionCall":{"name":"get_categories","args":{"month":"2026-07"}}}
    ]},"finishReason":"STOP"}]}"""

private const val ERROR_429_BODY =
    """{"error":{"code":429,"message":"Resource exhausted","status":"RESOURCE_EXHAUSTED","details":[]}}"""

private const val ERROR_429_WITH_RETRY_HINT_BODY =
    """{"error":{"code":429,"message":"Resource exhausted","status":"RESOURCE_EXHAUSTED","details":[
        {"@type":"type.googleapis.com/google.rpc.RetryInfo","retryDelay":"3s"}
    ]}}"""

private const val ERROR_500_BODY =
    """{"error":{"code":500,"message":"Internal error","status":"INTERNAL","details":[]}}"""

private const val GARBAGE_BODY = "not-json-at-all"

@OptIn(ExperimentalCoroutinesApi::class)
class KtorGeminiLlmClientTest {
    @Test
    fun `sends auth header, system instruction, image part, tools and response schema`() =
        runTest {
            var capturedRequest: HttpRequestData? = null
            val engine =
                MockEngine { request ->
                    capturedRequest = request
                    jsonResponse(HttpStatusCode.OK, SUCCESS_TEXT_BODY)
                }
            val client = buildClient(engine)

            val request =
                LlmRequest(
                    messages =
                        listOf(
                            ChatMessage(ChatRole.SYSTEM, listOf(MessagePart.Text("Be concise."))),
                            ChatMessage(
                                ChatRole.USER,
                                listOf(
                                    MessagePart.Text("What is this?"),
                                    MessagePart.Image("image/jpeg", "base64data"),
                                ),
                            ),
                        ),
                    tools = listOf(ToolSchema("get_categories", "Lists categories", JsonObject(emptyMap()))),
                    responseSchema = buildJsonObject { put("type", JsonPrimitive("OBJECT")) },
                )

            client.complete(request)

            val sent = requireNotNull(capturedRequest)
            assertThat(sent.headers[API_KEY_HEADER]).isEqualTo(FAKE_API_KEY)

            val body = testJson.decodeFromString<GeminiGenerateContentRequest>(sent.bodyAsText())
            assertThat(
                body.systemInstruction
                    ?.parts
                    ?.first()
                    ?.text,
            ).isEqualTo("Be concise.")
            assertThat(
                body.contents
                    .first()
                    .parts
                    .any { it.inlineData?.mimeType == "image/jpeg" },
            ).isTrue()
            assertThat(
                body.tools
                    ?.first()
                    ?.functionDeclarations
                    ?.first()
                    ?.name,
            ).isEqualTo("get_categories")
            assertThat(body.generationConfig?.responseMimeType).isEqualTo("application/json")
        }

    @Test
    fun `200 text response maps to LlmResponse Text`() =
        runTest {
            val engine = MockEngine { jsonResponse(HttpStatusCode.OK, SUCCESS_TEXT_BODY) }
            val client = buildClient(engine)

            val result = client.complete(minimalRequest())

            assertThat(result).isInstanceOf<Result.Success<LlmResponse>>()
            val response = (result as Result.Success<LlmResponse>).data
            assertThat(response).isInstanceOf<LlmResponse.Text>()
            assertThat((response as LlmResponse.Text).content).isEqualTo("Hello from Gemini")
        }

    @Test
    fun `200 function-call response maps to LlmResponse ToolCalls`() =
        runTest {
            val engine = MockEngine { jsonResponse(HttpStatusCode.OK, SUCCESS_FUNCTION_CALL_BODY) }
            val client = buildClient(engine)

            val result = client.complete(minimalRequest())

            assertThat(result).isInstanceOf<Result.Success<LlmResponse>>()
            val response = (result as Result.Success<LlmResponse>).data
            assertThat(response).isInstanceOf<LlmResponse.ToolCalls>()
            val call = (response as LlmResponse.ToolCalls).calls.first()
            assertThat(call.name).isEqualTo("get_categories")
        }

    @Test
    fun `429 then success waits the computed exponential backoff when no retry hint is present`() =
        runTest {
            var callCount = 0
            val engine =
                MockEngine {
                    callCount++
                    if (callCount == 1) {
                        jsonResponse(HttpStatusCode.TooManyRequests, ERROR_429_BODY)
                    } else {
                        jsonResponse(HttpStatusCode.OK, SUCCESS_TEXT_BODY)
                    }
                }
            val client = buildClient(engine)

            val result = client.complete(minimalRequest())

            assertThat(result).isInstanceOf<Result.Success<LlmResponse>>()
            assertThat(callCount).isEqualTo(2)
            assertThat(currentTime).isEqualTo(1_000L)
        }

    @Test
    fun `429 then success honors the server's retryDelay hint over the computed backoff`() =
        runTest {
            var callCount = 0
            val engine =
                MockEngine {
                    callCount++
                    if (callCount == 1) {
                        jsonResponse(HttpStatusCode.TooManyRequests, ERROR_429_WITH_RETRY_HINT_BODY)
                    } else {
                        jsonResponse(HttpStatusCode.OK, SUCCESS_TEXT_BODY)
                    }
                }
            val client = buildClient(engine)

            val result = client.complete(minimalRequest())

            assertThat(result).isInstanceOf<Result.Success<LlmResponse>>()
            assertThat(currentTime).isEqualTo(3_000L)
        }

    @Test
    fun `429 exhausts all retries and returns RateLimited`() =
        runTest {
            var callCount = 0
            val engine =
                MockEngine {
                    callCount++
                    jsonResponse(HttpStatusCode.TooManyRequests, ERROR_429_BODY)
                }
            val client = buildClient(engine)

            val result = client.complete(minimalRequest())

            assertThat(result).isEqualTo(Result.Error(AiError.RateLimited))
            assertThat(callCount).isEqualTo(1 + GeminiClientConfig.DEFAULT_MAX_RETRIES)
            assertThat(currentTime).isEqualTo(1_000L + 2_000L + 4_000L)
        }

    @Test
    fun `500 is retried the same way as 429`() =
        runTest {
            var callCount = 0
            val engine =
                MockEngine {
                    callCount++
                    if (callCount == 1) {
                        jsonResponse(HttpStatusCode.InternalServerError, ERROR_500_BODY)
                    } else {
                        jsonResponse(HttpStatusCode.OK, SUCCESS_TEXT_BODY)
                    }
                }
            val client = buildClient(engine)

            val result = client.complete(minimalRequest())

            assertThat(result).isInstanceOf<Result.Success<LlmResponse>>()
            assertThat(callCount).isEqualTo(2)
        }

    @Test
    fun `garbage JSON body maps to MalformedOutput without retrying`() =
        runTest {
            var callCount = 0
            val engine =
                MockEngine {
                    callCount++
                    jsonResponse(HttpStatusCode.OK, GARBAGE_BODY)
                }
            val client = buildClient(engine)

            val result = client.complete(minimalRequest())

            assertThat(result).isEqualTo(Result.Error(AiError.MalformedOutput))
            assertThat(callCount).isEqualTo(1)
        }

    @Test
    fun `missing api key returns NoApiKey without making a network call`() =
        runTest {
            val engine = MockEngine { jsonResponse(HttpStatusCode.OK, SUCCESS_TEXT_BODY) }
            val client = buildClient(engine, apiKey = null)

            val result = client.complete(minimalRequest())

            assertThat(result).isEqualTo(Result.Error(AiError.NoApiKey))
            assertThat(engine.requestHistory).isEmpty()
        }
}

private fun MockRequestHandleScope.jsonResponse(
    status: HttpStatusCode,
    body: String,
) = respond(content = body, status = status, headers = headersOf(HttpHeaders.ContentType, "application/json"))

private fun buildClient(
    engine: MockEngine,
    apiKey: String? = FAKE_API_KEY,
): KtorGeminiLlmClient =
    KtorGeminiLlmClient(
        httpClient = HttpClientFactory.create(engine),
        rateLimiter = RateLimiter(minIntervalMillis = 0L),
        apiKey = apiKey,
        jitterMillis = { 0L },
    )

private fun minimalRequest(): LlmRequest = LlmRequest(messages = listOf(ChatMessage(ChatRole.USER, listOf(MessagePart.Text("Hi")))))

private fun HttpRequestData.bodyAsText(): String =
    when (val content = body) {
        is OutgoingContent.ByteArrayContent -> String(content.bytes())
        else -> error("Unsupported body content for test assertions: $content")
    }
