package com.budgetpilot.feature.capture.data

import com.budgetpilot.core.ai.data.prompt.PromptId
import com.budgetpilot.core.ai.data.prompt.PromptRepository
import com.budgetpilot.core.ai.domain.LlmClient
import com.budgetpilot.core.ai.domain.model.ChatMessage
import com.budgetpilot.core.ai.domain.model.ChatRole
import com.budgetpilot.core.ai.domain.model.LlmRequest
import com.budgetpilot.core.ai.domain.model.LlmResponse
import com.budgetpilot.core.ai.domain.model.MessagePart
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.feature.capture.domain.ExtractionCache
import com.budgetpilot.feature.capture.domain.ExtractionError
import com.budgetpilot.feature.capture.domain.ReceiptExtractor
import com.budgetpilot.feature.capture.domain.ReceiptImage
import com.budgetpilot.feature.capture.domain.model.Confidence
import com.budgetpilot.feature.capture.domain.model.ExtractedField
import com.budgetpilot.feature.capture.domain.model.ExtractedReceipt
import com.budgetpilot.feature.capture.domain.model.LineItem
import com.budgetpilot.feature.capture.domain.model.ReceiptType
import kotlinx.coroutines.CancellationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.security.MessageDigest
import java.time.LocalDate
import java.util.Base64

private const val IMAGE_MIME_TYPE = "image/jpeg"
private const val MALFORMED_OUTPUT_PLACEHOLDER = "{{malformed_output}}"

/**
 * Cloud [ReceiptExtractor] (PLAN.md §5.4): sends the receipt photo + `extraction_v1` prompt to
 * Gemini with a strict `responseSchema`, parses the returned JSON into an [ExtractedReceipt], and
 * checks/writes a hash-keyed [ExtractionCache] so re-picking the same photo costs zero requests.
 * A response that fails to parse gets exactly one repair-prompt retry (PLAN.md §5.2) before giving
 * up with [ExtractionError.NothingRecognized].
 */
class VisionLlmExtractor(
    private val llmClient: LlmClient,
    private val promptRepository: PromptRepository,
    private val extractionCache: ExtractionCache,
) : ReceiptExtractor {
    override suspend fun extract(image: ReceiptImage): Result<ExtractedReceipt, ExtractionError> {
        val imageBytes = image.readBytes()
        val imageHash = imageBytes.sha256Hex()

        extractionCache.get(imageHash)?.let { cachedJson ->
            parseReceiptJson(cachedJson)?.let { return Result.Success(it) }
        }

        return when (val result = llmClient.complete(buildExtractionRequest(imageBytes))) {
            is Result.Error -> Result.Error(result.error.toExtractionError())
            is Result.Success -> onExtractionResponse(result.data, imageHash)
        }
    }

    private suspend fun onExtractionResponse(
        response: LlmResponse,
        imageHash: String,
    ): Result<ExtractedReceipt, ExtractionError> {
        val text = response.textOrEmpty()
        parseReceiptJson(text)?.let { receipt ->
            extractionCache.put(imageHash, text)
            return Result.Success(receipt)
        }
        return repair(text, imageHash)
    }

    private suspend fun repair(
        malformedOutput: String,
        imageHash: String,
    ): Result<ExtractedReceipt, ExtractionError> {
        val repairPrompt =
            promptRepository
                .getPrompt(PromptId.REPAIR_V1)
                .replace(MALFORMED_OUTPUT_PLACEHOLDER, malformedOutput)
        val request =
            LlmRequest(
                messages = listOf(ChatMessage(ChatRole.USER, listOf(MessagePart.Text(repairPrompt)))),
                responseSchema = EXTRACTION_RESPONSE_SCHEMA,
            )

        return when (val result = llmClient.complete(request)) {
            is Result.Error -> Result.Error(result.error.toExtractionError())
            is Result.Success -> {
                val repairedText = result.data.textOrEmpty()
                val repaired = parseReceiptJson(repairedText)
                if (repaired != null) {
                    extractionCache.put(imageHash, repairedText)
                    Result.Success(repaired)
                } else {
                    Result.Error(ExtractionError.NothingRecognized)
                }
            }
        }
    }

    private suspend fun buildExtractionRequest(imageBytes: ByteArray): LlmRequest {
        val extractionPrompt = promptRepository.getPrompt(PromptId.EXTRACTION_V1)
        val base64Image = Base64.getEncoder().encodeToString(imageBytes)
        return LlmRequest(
            messages =
                listOf(
                    ChatMessage(ChatRole.SYSTEM, listOf(MessagePart.Text(extractionPrompt))),
                    ChatMessage(ChatRole.USER, listOf(MessagePart.Image(IMAGE_MIME_TYPE, base64Image))),
                ),
            responseSchema = EXTRACTION_RESPONSE_SCHEMA,
        )
    }
}

private fun LlmResponse.textOrEmpty(): String = (this as? LlmResponse.Text)?.content.orEmpty()

private fun ByteArray.sha256Hex(): String = MessageDigest.getInstance("SHA-256").digest(this).joinToString("") { "%02x".format(it) }

@Suppress("TooGenericExceptionCaught", "SwallowedException")
private fun parseReceiptJson(text: String): ExtractedReceipt? =
    try {
        val root = Json.parseToJsonElement(text).jsonObject
        val receiptType = ReceiptType.valueOf(root.getValue("receipt_type").jsonPrimitive.content)
        val merchant = root.parseConfidenceField("merchant") { it.jsonPrimitive.content }
        val date = root.parseConfidenceField("date") { LocalDate.parse(it.jsonPrimitive.content) }
        val lineItems = root.parseConfidenceField("line_items") { it.jsonArray.map(JsonElement::toLineItem) }
        val total = root.parseConfidenceField("total") { Money.fromPesos(it.jsonPrimitive.content) }
        val suggestedCategory =
            root.parseConfidenceField("suggested_category") { value ->
                if (value is JsonNull) null else value.jsonPrimitive.content
            }

        ExtractedReceipt(
            merchant = merchant.clampedIfBlank(),
            date = date,
            lineItems = lineItems.clampedIfEmpty(),
            total = total.clampedIfNotPositive(),
            suggestedCategory = suggestedCategory,
            receiptType = ExtractedField(receiptType, Confidence.HIGH),
        )
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        null
    }

private fun JsonElement.toLineItem(): LineItem {
    val itemObject = jsonObject
    return LineItem(
        description = itemObject.getValue("description").jsonPrimitive.content,
        amount = Money.fromPesos(itemObject.getValue("amount").jsonPrimitive.content),
    )
}

private fun <T> JsonObject.parseConfidenceField(
    key: String,
    parseValue: (JsonElement) -> T,
): ExtractedField<T> {
    val fieldObject = getValue(key).jsonObject
    val value = parseValue(fieldObject.getValue("value"))
    val confidence = Confidence.valueOf(fieldObject.getValue("confidence").jsonPrimitive.content)
    return ExtractedField(value, confidence)
}

private fun ExtractedField<String>.clampedIfBlank(): ExtractedField<String> =
    if (value.isBlank()) copy(confidence = confidence.clampToAtLeast(Confidence.LOW)) else this

private fun ExtractedField<List<LineItem>>.clampedIfEmpty(): ExtractedField<List<LineItem>> =
    if (value.isEmpty()) copy(confidence = confidence.clampToAtLeast(Confidence.LOW)) else this

private fun ExtractedField<Money>.clampedIfNotPositive(): ExtractedField<Money> =
    if (value <= Money.ZERO) copy(confidence = confidence.clampToAtLeast(Confidence.LOW)) else this

private fun Confidence.clampToAtLeast(min: Confidence): Confidence = Confidence.entries[maxOf(ordinal, min.ordinal)]
