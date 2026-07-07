package com.budgetpilot.core.ai.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.budgetpilot.core.ai.domain.model.ChatMessage
import com.budgetpilot.core.ai.domain.model.ChatRole
import com.budgetpilot.core.ai.domain.model.LlmRequest
import com.budgetpilot.core.ai.domain.model.LlmResponse
import com.budgetpilot.core.ai.domain.model.MessagePart
import com.budgetpilot.core.domain.Result
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class DemoAwareLlmClientTest {
    private val realClient = FakeLlmClient(script = listOf(Result.Success(LlmResponse.Text("real answer"))))
    private val demoClient = FakeLlmClient(script = listOf(Result.Success(LlmResponse.Text("demo answer"))))

    @Test
    fun `delegates to the real client when demo mode is off`() =
        runTest {
            val client = DemoAwareLlmClient(realClient, demoClient, isDemoModeEnabled = { false })

            val result = client.complete(requestOf("hello"))

            val text = (result as Result.Success).data as LlmResponse.Text
            assertThat(text.content).isEqualTo("real answer")
        }

    @Test
    fun `delegates to the demo client when demo mode is on`() =
        runTest {
            val client = DemoAwareLlmClient(realClient, demoClient, isDemoModeEnabled = { true })

            val result = client.complete(requestOf("hello"))

            val text = (result as Result.Success).data as LlmResponse.Text
            assertThat(text.content).isEqualTo("demo answer")
        }

    @Test
    fun `re-checks demo mode on every call rather than caching the first decision`() =
        runTest {
            var demoModeEnabled = false
            val client = DemoAwareLlmClient(realClient, demoClient, isDemoModeEnabled = { demoModeEnabled })

            val firstResult = client.complete(requestOf("one"))
            demoModeEnabled = true
            val secondResult = client.complete(requestOf("two"))

            assertThat((firstResult as Result.Success).data).isEqualTo(LlmResponse.Text("real answer"))
            assertThat((secondResult as Result.Success).data).isEqualTo(LlmResponse.Text("demo answer"))
        }

    private fun requestOf(text: String): LlmRequest =
        LlmRequest(messages = listOf(ChatMessage(role = ChatRole.USER, parts = listOf(MessagePart.Text(text)))))
}
