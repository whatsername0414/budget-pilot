package com.budgetpilot.core.presentation.money

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

/**
 * Groups the whole-number part of a raw "1234.56"-style amount string with
 * thousands separators for display (e.g. "1,234.56"). The fraction digits
 * and the underlying edited value are left untouched.
 */
object MoneyInputVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val dotIndex = raw.indexOf('.')
        val wholePart = if (dotIndex == -1) raw else raw.substring(0, dotIndex)
        val suffix = if (dotIndex == -1) "" else raw.substring(dotIndex)
        val groupedWhole = groupThousands(wholePart)
        val formatted = groupedWhole + suffix

        val offsetMapping =
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    val boundedOffset = offset.coerceIn(0, raw.length)
                    return if (boundedOffset <= wholePart.length) {
                        boundedOffset + (commasIn(wholePart.length) - commasIn(wholePart.length - boundedOffset))
                    } else {
                        groupedWhole.length + (boundedOffset - wholePart.length)
                    }
                }

                override fun transformedToOriginal(offset: Int): Int {
                    val boundedOffset = offset.coerceIn(0, formatted.length)
                    return if (boundedOffset <= groupedWhole.length) {
                        boundedOffset - groupedWhole.take(boundedOffset).count { it == ',' }
                    } else {
                        wholePart.length + (boundedOffset - groupedWhole.length)
                    }
                }
            }

        return TransformedText(AnnotatedString(formatted), offsetMapping)
    }

    private fun commasIn(digitCount: Int): Int = if (digitCount <= 0) 0 else (digitCount - 1) / 3

    private fun groupThousands(digits: String): String {
        if (digits.isEmpty()) return digits
        val grouped = StringBuilder()
        val length = digits.length
        for ((index, digit) in digits.withIndex()) {
            val positionFromEnd = length - index
            if (index != 0 && positionFromEnd % 3 == 0) {
                grouped.append(',')
            }
            grouped.append(digit)
        }
        return grouped.toString()
    }
}
