package com.budgetpilot.feature.capture.domain

import com.budgetpilot.core.domain.Result
import com.budgetpilot.feature.capture.domain.model.Confidence
import com.budgetpilot.feature.capture.domain.model.ExtractedField
import com.budgetpilot.feature.capture.domain.model.ExtractedReceipt
import com.budgetpilot.feature.capture.domain.model.OcrLine
import java.time.Clock
import java.time.LocalDate

/**
 * Turns raw on-device OCR output into an [ExtractedReceipt] using PH-specific heuristics —
 * no LLM involved, so it must work fully offline. Lines below [MIN_TRUSTED_CONFIDENCE] are
 * excluded from keyword/date/merchant matching (garbled OCR shouldn't drive a HIGH-confidence
 * read) but a value is still guessed from them as a last-resort fallback, always at LOW
 * confidence, matching the confirm screen's "highlight low-confidence fields" contract.
 */
class ReceiptParser(
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    fun parse(lines: List<OcrLine>): Result<ExtractedReceipt, ExtractionError> {
        if (lines.none { it.text.isNotBlank() }) return Result.Error(ExtractionError.NothingRecognized)

        val texts = lines.filter { it.confidence >= MIN_TRUSTED_CONFIDENCE }.map { it.text }
        val receiptType = detectReceiptType(texts)
        val total = extractTotal(texts) ?: return Result.Error(ExtractionError.NothingRecognized)
        val merchant = extractMerchant(texts, receiptType)

        return Result.Success(
            ExtractedReceipt(
                merchant = merchant.field,
                date = extractDate(texts),
                lineItems = ExtractedField(emptyList(), Confidence.LOW),
                total = total,
                suggestedCategory = merchant.category,
                receiptType = ExtractedField(receiptType, Confidence.HIGH),
            ),
        )
    }

    private fun extractDate(texts: List<String>): ExtractedField<LocalDate> {
        for (line in texts) {
            val date = parseReceiptDate(line)
            if (date != null) return ExtractedField(date, Confidence.HIGH)
        }
        return ExtractedField(LocalDate.now(clock), Confidence.LOW)
    }

    companion object {
        const val MIN_TRUSTED_CONFIDENCE = 0.4f
    }
}
