package com.budgetpilot.core.presentation.money

import com.budgetpilot.core.domain.money.Money
import kotlin.math.abs

/**
 * Formats [Money] as "₱1,234.56", independent of device locale so peso
 * grouping/decimal conventions stay consistent regardless of the user's
 * system locale.
 */
object PesoFormatter {

    fun format(money: Money): String {
        val centavos = money.centavos
        val sign = if (centavos < 0) "-" else ""
        val absCentavos = abs(centavos)

        val whole = absCentavos / 100
        val fraction = absCentavos % 100

        return "$sign₱${groupThousands(whole)}.${fraction.toString().padStart(2, '0')}"
    }

    private fun groupThousands(value: Long): String {
        val digits = value.toString()
        val grouped = StringBuilder()
        for ((index, digit) in digits.withIndex()) {
            val positionFromEnd = digits.length - index
            if (index != 0 && positionFromEnd % 3 == 0) {
                grouped.append(',')
            }
            grouped.append(digit)
        }
        return grouped.toString()
    }
}
