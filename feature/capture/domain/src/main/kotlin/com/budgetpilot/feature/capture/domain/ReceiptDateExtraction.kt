package com.budgetpilot.feature.capture.domain

import java.time.LocalDate
import java.time.format.DateTimeFormatterBuilder
import java.util.Locale

private const val TWO_DIGIT_YEAR_BASE = 2000
private val SLASH_DATE_REGEX = Regex("""\b(\d{1,2})/(\d{1,2})/(\d{4})\b""")
private val DASH_DATE_REGEX = Regex("""\b(\d{1,2})-(\d{1,2})-(\d{2})\b""")
private val MONTH_NAME_DATE_REGEX = Regex("""\b(\d{1,2})\s+([A-Za-z]{3,9})\s+(\d{4})\b""")
private val MONTH_NAME_FORMATTER =
    DateTimeFormatterBuilder()
        .parseCaseInsensitive()
        .appendPattern("d MMM yyyy")
        .toFormatter(Locale.ENGLISH)

/** Tries each PH receipt date format in turn: MM/DD/YYYY, DD-MM-YY, then "03 JUL 2026". */
internal fun parseReceiptDate(line: String): LocalDate? = parseSlashDate(line) ?: parseDashDate(line) ?: parseMonthNameDate(line)

private fun parseSlashDate(line: String): LocalDate? {
    val match = SLASH_DATE_REGEX.find(line) ?: return null
    val (month, day, year) = match.destructured
    return runCatching { LocalDate.of(year.toInt(), month.toInt(), day.toInt()) }.getOrNull()
}

private fun parseDashDate(line: String): LocalDate? {
    val match = DASH_DATE_REGEX.find(line) ?: return null
    val (day, month, year) = match.destructured
    return runCatching { LocalDate.of(TWO_DIGIT_YEAR_BASE + year.toInt(), month.toInt(), day.toInt()) }.getOrNull()
}

private fun parseMonthNameDate(line: String): LocalDate? {
    val match = MONTH_NAME_DATE_REGEX.find(line) ?: return null
    return runCatching { LocalDate.parse(match.value, MONTH_NAME_FORMATTER) }.getOrNull()
}
