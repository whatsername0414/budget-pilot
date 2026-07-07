package com.budgetpilot.feature.capture.domain

import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.feature.capture.domain.model.Confidence
import com.budgetpilot.feature.capture.domain.model.ExtractedField
import java.util.Locale

private const val MAX_PLAUSIBLE_CENTAVOS = 1_000_000_00L
private const val TENDERED_KEYWORD = "CASH TENDERED"
private const val CHANGE_KEYWORD = "CHANGE"
private val STRONG_TOTAL_KEYWORDS = listOf("AMOUNT DUE", "GRAND TOTAL", "TOTAL", "AMOUNT")
private val DECIMAL_AMOUNT_REGEX = Regex("""(\d[\d,]*\.\d{1,2})""")
private val WHOLE_AMOUNT_REGEX = Regex("""(\d[\d,]*)""")

/**
 * Ranks candidate totals by keyword strength (AMOUNT DUE / GRAND TOTAL / TOTAL first, HIGH
 * confidence) before falling back to a tendered-minus-change computation (MEDIUM confidence) —
 * see PLAN.md §6 Phase 6.
 */
internal fun extractTotal(texts: List<String>): ExtractedField<Money>? {
    for (keyword in STRONG_TOTAL_KEYWORDS) {
        val amount = findAmountForKeyword(texts, keyword)
        if (amount != null) return ExtractedField(amount, Confidence.HIGH)
    }

    val tendered = findAmountForKeyword(texts, TENDERED_KEYWORD)
    val change = findAmountForKeyword(texts, CHANGE_KEYWORD)
    if (tendered != null && change != null) {
        val computed = tendered - change
        if (isPlausible(computed)) return ExtractedField(computed, Confidence.MEDIUM)
    }
    if (tendered != null && isPlausible(tendered)) return ExtractedField(tendered, Confidence.MEDIUM)

    return null
}

/** Bottom-weighted: scans from the last line up, so the receipt's final total wins over any earlier one. */
private fun findAmountForKeyword(
    texts: List<String>,
    keyword: String,
): Money? {
    for (index in texts.indices.reversed()) {
        val line = texts[index]
        val upper = line.uppercase(Locale.ROOT)
        val isSubtotalLine = keyword == "TOTAL" && upper.contains("SUBTOTAL")
        if (!upper.contains(keyword) || isSubtotalLine) continue

        val amount = extractMoney(line) ?: extractMoney(texts.getOrNull(index + 1).orEmpty())
        if (amount != null && isPlausible(amount)) return amount
    }
    return null
}

private fun extractMoney(line: String): Money? {
    val raw =
        DECIMAL_AMOUNT_REGEX.find(line)?.groupValues?.get(1)
            ?: WHOLE_AMOUNT_REGEX.find(line)?.groupValues?.get(1)
    return raw?.let { runCatching { Money.fromPesos(it) }.getOrNull() }
}

private fun isPlausible(amount: Money): Boolean = amount.centavos in 1..MAX_PLAUSIBLE_CENTAVOS
