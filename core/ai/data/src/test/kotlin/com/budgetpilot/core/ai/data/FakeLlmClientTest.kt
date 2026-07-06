package com.budgetpilot.core.ai.data

import assertk.assertThat
import assertk.assertions.contains
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.budgetpilot.core.ai.domain.AiError
import com.budgetpilot.core.ai.domain.model.ChatMessage
import com.budgetpilot.core.ai.domain.model.ChatRole
import com.budgetpilot.core.ai.domain.model.LlmRequest
import com.budgetpilot.core.ai.domain.model.LlmResponse
import com.budgetpilot.core.ai.domain.model.MessagePart
import com.budgetpilot.core.ai.domain.model.ToolCall
import com.budgetpilot.core.domain.Result
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.JsonObject
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class FakeLlmClientTest {
    @Test
    fun `default script returns canned extraction JSON`() =
        runTest {
            val fakeLlmClient = FakeLlmClient()

            val result = fakeLlmClient.complete(requestOf("extract this receipt"))

            val response = (result as Result.Success).data as LlmResponse.Text
            assertThat(response.content).contains("\"receipt_type\": \"PAPER\"")
        }

    @Test
    fun `returns scripted responses in order`() =
        runTest {
            val first: Result<LlmResponse, AiError> = Result.Success(LlmResponse.Text("first"))
            val second: Result<LlmResponse, AiError> =
                Result.Success(
                    LlmResponse.ToolCalls(listOf(ToolCall("get_categories", JsonObject(emptyMap())))),
                )
            val fakeLlmClient = FakeLlmClient(script = listOf(first, second))

            val firstResult = fakeLlmClient.complete(requestOf("one"))
            val secondResult = fakeLlmClient.complete(requestOf("two"))

            assertThat(firstResult).isEqualTo(first)
            assertThat(secondResult).isEqualTo(second)
        }

    @Test
    fun `returns a scripted error`() =
        runTest {
            val fakeLlmClient = FakeLlmClient(script = listOf(Result.Error(AiError.RateLimited)))

            val result = fakeLlmClient.complete(requestOf("one"))

            assertThat((result as Result.Error).error).isInstanceOf<AiError.RateLimited>()
        }

    @Test
    fun `records every request it receives`() =
        runTest {
            val fakeLlmClient =
                FakeLlmClient(
                    script =
                        listOf(
                            Result.Success(LlmResponse.Text("first")),
                            Result.Success(LlmResponse.Text("second")),
                        ),
                )

            fakeLlmClient.complete(requestOf("one"))
            fakeLlmClient.complete(requestOf("two"))

            val receivedTexts = fakeLlmClient.receivedRequests.map { it.firstText() }
            assertThat(receivedTexts).containsExactly("one", "two")
        }

    @Test
    fun `throws once the script is exhausted`() =
        runTest {
            val fakeLlmClient = FakeLlmClient(script = listOf(Result.Success(LlmResponse.Text("only one"))))

            fakeLlmClient.complete(requestOf("one"))

            assertThrows<IllegalStateException> {
                fakeLlmClient.complete(requestOf("two"))
            }
        }

    private fun requestOf(text: String): LlmRequest =
        LlmRequest(messages = listOf(ChatMessage(role = ChatRole.USER, parts = listOf(MessagePart.Text(text)))))

    private fun LlmRequest.firstText(): String {
        val firstMessage = messages.first()
        return (firstMessage.parts.first() as MessagePart.Text).text
    }
}
