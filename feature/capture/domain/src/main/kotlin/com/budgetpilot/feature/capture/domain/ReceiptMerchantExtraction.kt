package com.budgetpilot.feature.capture.domain

import com.budgetpilot.core.domain.merchant.MerchantCatalog
import com.budgetpilot.feature.capture.domain.model.Confidence
import com.budgetpilot.feature.capture.domain.model.ExtractedField
import com.budgetpilot.feature.capture.domain.model.ReceiptType
import java.util.Locale

private const val TOP_LINES_FOR_MERCHANT = 5
private val RECIPIENT_PREFIXES = listOf("TO:", "TO ", "RECIPIENT:", "BILLER:", "MERCHANT:")

internal data class MerchantResult(
    val field: ExtractedField<String>,
    val category: ExtractedField<String?>,
)

internal fun detectReceiptType(texts: List<String>): ReceiptType =
    when {
        texts.any { it.contains("gcash", ignoreCase = true) } -> ReceiptType.GCASH
        texts.any { it.contains("maya", ignoreCase = true) } -> ReceiptType.MAYA
        else -> ReceiptType.PAPER
    }

internal fun extractMerchant(
    texts: List<String>,
    receiptType: ReceiptType,
): MerchantResult = if (receiptType == ReceiptType.PAPER) extractPaperMerchant(texts) else extractWalletMerchant(texts, receiptType)

/** Fuzzy-matches the top lines against [MerchantCatalog]; falls back to the first line, LOW confidence. */
private fun extractPaperMerchant(texts: List<String>): MerchantResult {
    val candidateLines = texts.filter { it.isNotBlank() }.take(TOP_LINES_FOR_MERCHANT)
    for (line in candidateLines) {
        val category = MerchantCatalog.suggestCategory(line)
        if (category != null) {
            return MerchantResult(
                field = ExtractedField(line.trim(), Confidence.HIGH),
                category = ExtractedField(category, Confidence.MEDIUM),
            )
        }
    }
    return MerchantResult(
        field = ExtractedField(texts.firstOrNull { it.isNotBlank() }?.trim().orEmpty(), Confidence.LOW),
        category = ExtractedField(null, Confidence.LOW),
    )
}

/** GCash/Maya: the merchant is the transfer recipient/biller, found via a line prefix like "To:"/"Biller:". */
private fun extractWalletMerchant(
    texts: List<String>,
    receiptType: ReceiptType,
): MerchantResult {
    val walletCategory = ExtractedField(MerchantCatalog.suggestCategory(walletKeyword(receiptType)), Confidence.MEDIUM)
    for (line in texts) {
        val trimmed = line.trim()
        val prefix = RECIPIENT_PREFIXES.firstOrNull { trimmed.uppercase(Locale.ROOT).startsWith(it) } ?: continue
        val name = trimmed.substring(prefix.length).trim(':', ' ')
        if (name.isNotBlank()) return MerchantResult(ExtractedField(name, Confidence.HIGH), walletCategory)
    }
    return MerchantResult(
        field = ExtractedField(texts.firstOrNull { it.isNotBlank() }?.trim().orEmpty(), Confidence.LOW),
        category = walletCategory,
    )
}

private fun walletKeyword(receiptType: ReceiptType): String =
    when (receiptType) {
        ReceiptType.GCASH -> "gcash"
        ReceiptType.MAYA -> "maya"
        ReceiptType.PAPER -> ""
    }
