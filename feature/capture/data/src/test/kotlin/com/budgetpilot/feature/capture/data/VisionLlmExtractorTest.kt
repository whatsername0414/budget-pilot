package com.budgetpilot.feature.capture.data

import assertk.assertThat
import assertk.assertions.hasSize
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import assertk.assertions.isNull
import com.budgetpilot.core.ai.data.FakeLlmClient
import com.budgetpilot.core.ai.domain.AiError
import com.budgetpilot.core.ai.domain.model.ChatRole
import com.budgetpilot.core.ai.domain.model.LlmResponse
import com.budgetpilot.core.ai.domain.model.MessagePart
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.feature.capture.data.fake.FakeExtractionCache
import com.budgetpilot.feature.capture.data.fake.FakePromptRepository
import com.budgetpilot.feature.capture.domain.ExtractionError
import com.budgetpilot.feature.capture.domain.ReceiptImage
import com.budgetpilot.feature.capture.domain.model.Confidence
import com.budgetpilot.feature.capture.domain.model.ReceiptType
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.security.MessageDigest

// language=JSON
private const val JOLLIBEE_JSON =
    """
    {
      "receipt_type": "PAPER",
      "merchant": { "value": "Jollibee", "confidence": "HIGH" },
      "date": { "value": "2026-06-15", "confidence": "HIGH" },
      "line_items": {
        "value": [
          { "description": "1pc Chickenjoy w/ Rice", "amount": 89.00 },
          { "description": "Jolly Spaghetti", "amount": 65.00 },
          { "description": "Coke Float", "amount": 45.00 }
        ],
        "confidence": "HIGH"
      },
      "total": { "value": 199.00, "confidence": "HIGH" },
      "suggested_category": { "value": "Food", "confidence": "HIGH" }
    }
    """

// language=JSON
private const val GCASH_JSON =
    """
    {
      "receipt_type": "GCASH",
      "merchant": { "value": "Juan Dela Cruz", "confidence": "HIGH" },
      "date": { "value": "2026-06-15", "confidence": "HIGH" },
      "line_items": {
        "value": [ { "description": "Send Money to Juan Dela Cruz", "amount": 500.00 } ],
        "confidence": "HIGH"
      },
      "total": { "value": 500.00, "confidence": "HIGH" },
      "suggested_category": { "value": null, "confidence": "MEDIUM" }
    }
    """

// language=JSON
private const val BLANK_MERCHANT_JSON =
    """
    {
      "receipt_type": "PAPER",
      "merchant": { "value": "", "confidence": "HIGH" },
      "date": { "value": "2026-06-15", "confidence": "HIGH" },
      "line_items": { "value": [], "confidence": "HIGH" },
      "total": { "value": 0, "confidence": "HIGH" },
      "suggested_category": { "value": null, "confidence": "HIGH" }
    }
    """

private const val MALFORMED_JSON = "not json at all"
private val IMAGE_BYTES = byteArrayOf(1, 2, 3, 4)

class VisionLlmExtractorTest {
    private val cache = FakeExtractionCache()
    private val prompts = FakePromptRepository()

    private fun extractor(llmClient: FakeLlmClient) = VisionLlmExtractor(llmClient, prompts, cache)

    private fun image() = ReceiptImage(path = "unused") { IMAGE_BYTES }

    private fun imageHash() = MessageDigest.getInstance("SHA-256").digest(IMAGE_BYTES).joinToString("") { "%02x".format(it) }

    @Test
    fun `happy path parses the receipt and writes the cache`() =
        runTest {
            val llmClient = FakeLlmClient(listOf(Result.Success(LlmResponse.Text(JOLLIBEE_JSON))))

            val result = extractor(llmClient).extract(image())

            assertThat(result).isInstanceOf(Result.Success::class)
            val receipt = (result as Result.Success).data
            assertThat(receipt.merchant.value).isEqualTo("Jollibee")
            assertThat(receipt.total.value).isEqualTo(Money.fromPesos("199.00"))
            assertThat(receipt.receiptType.value).isEqualTo(ReceiptType.PAPER)
            assertThat(cache.get(imageHash())).isEqualTo(JOLLIBEE_JSON)

            val sentMessages = llmClient.receivedRequests.single().messages
            assertThat(sentMessages.first { it.role == ChatRole.SYSTEM }.parts.first()).isEqualTo(MessagePart.Text("EXTRACT"))
        }

    @Test
    fun `cache hit makes zero LLM calls`() =
        runTest {
            cache.put(imageHash(), JOLLIBEE_JSON)
            val llmClient = FakeLlmClient(emptyList())

            val result = extractor(llmClient).extract(image())

            assertThat(result).isInstanceOf(Result.Success::class)
            assertThat(llmClient.receivedRequests).isEmpty()
        }

    @Test
    fun `malformed output is repaired then succeeds`() =
        runTest {
            val llmClient =
                FakeLlmClient(
                    listOf(
                        Result.Success(LlmResponse.Text(MALFORMED_JSON)),
                        Result.Success(LlmResponse.Text(JOLLIBEE_JSON)),
                    ),
                )

            val result = extractor(llmClient).extract(image())

            assertThat(result).isInstanceOf(Result.Success::class)
            assertThat((result as Result.Success).data.merchant.value).isEqualTo("Jollibee")
            assertThat(cache.get(imageHash())).isEqualTo(JOLLIBEE_JSON)

            val repairMessage =
                llmClient.receivedRequests[1]
                    .messages
                    .single()
                    .parts
                    .single()
            assertThat(repairMessage).isEqualTo(MessagePart.Text("REPAIR:$MALFORMED_JSON"))
        }

    @Test
    fun `malformed output that fails repair gives up`() =
        runTest {
            val llmClient =
                FakeLlmClient(
                    listOf(
                        Result.Success(LlmResponse.Text(MALFORMED_JSON)),
                        Result.Success(LlmResponse.Text(MALFORMED_JSON)),
                    ),
                )

            val result = extractor(llmClient).extract(image())

            assertThat(result).isEqualTo(Result.Error(ExtractionError.NothingRecognized))
            assertThat(llmClient.receivedRequests).hasSize(2)
            assertThat(cache.get(imageHash())).isNull()
        }

    @Test
    fun `GCash subtype maps to ReceiptType GCASH with a null suggested category`() =
        runTest {
            val llmClient = FakeLlmClient(listOf(Result.Success(LlmResponse.Text(GCASH_JSON))))

            val result = extractor(llmClient).extract(image())

            assertThat(result).isInstanceOf(Result.Success::class)
            val receipt = (result as Result.Success).data
            assertThat(receipt.receiptType.value).isEqualTo(ReceiptType.GCASH)
            assertThat(receipt.merchant.value).isEqualTo("Juan Dela Cruz")
            assertThat(receipt.suggestedCategory.value).isNull()
        }

    @Test
    fun `rate limit surfaces as ExtractionError Cloud RateLimited without attempting repair`() =
        runTest {
            val llmClient = FakeLlmClient(listOf(Result.Error(AiError.RateLimited)))

            val result = extractor(llmClient).extract(image())

            assertThat(result).isEqualTo(Result.Error(ExtractionError.Cloud.RateLimited))
            assertThat(llmClient.receivedRequests).hasSize(1)
        }

    @Test
    fun `blank merchant, zero total and empty line items are clamped to LOW confidence`() =
        runTest {
            val llmClient = FakeLlmClient(listOf(Result.Success(LlmResponse.Text(BLANK_MERCHANT_JSON))))

            val result = extractor(llmClient).extract(image())

            assertThat(result).isInstanceOf(Result.Success::class)
            val receipt = (result as Result.Success).data
            assertThat(receipt.merchant.confidence).isEqualTo(Confidence.LOW)
            assertThat(receipt.total.confidence).isEqualTo(Confidence.LOW)
            assertThat(receipt.lineItems.confidence).isEqualTo(Confidence.LOW)
        }
}
