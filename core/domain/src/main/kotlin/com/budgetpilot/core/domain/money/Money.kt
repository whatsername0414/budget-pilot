package com.budgetpilot.core.domain.money

/**
 * A peso amount stored as centavos to avoid floating-point rounding errors.
 */
@JvmInline
value class Money private constructor(
    val centavos: Long,
) : Comparable<Money> {
    operator fun plus(other: Money): Money = Money(centavos + other.centavos)

    operator fun minus(other: Money): Money = Money(centavos - other.centavos)

    operator fun times(factor: Int): Money = Money(centavos * factor)

    operator fun unaryMinus(): Money = Money(-centavos)

    override fun compareTo(other: Money): Int = centavos.compareTo(other.centavos)

    /** Returns what percentage of [whole] this amount represents; 0 when [whole] is zero. */
    fun percentOf(whole: Money): Double {
        if (whole.centavos == 0L) return 0.0
        return centavos.toDouble() / whole.centavos.toDouble() * 100.0
    }

    companion object {
        val ZERO = Money(0)

        fun ofCentavos(centavos: Long): Money = Money(centavos)

        /**
         * Parses a peso string such as "1,234.56", "₱1,234.56", "PHP 50", or "-20.5".
         * Throws [IllegalArgumentException] for malformed input or sub-centavo precision.
         */
        fun fromPesos(pesos: String): Money {
            val cleaned =
                pesos
                    .trim()
                    .removePrefix("₱")
                    .removePrefix("PHP")
                    .trim()
                    .replace(",", "")
            require(cleaned.isNotEmpty()) { "Invalid peso amount: $pesos" }

            val negative = cleaned.startsWith("-")
            val unsigned = cleaned.removePrefix("-").removePrefix("+")

            val parts = unsigned.split(".")
            require(parts.size in 1..2) { "Invalid peso amount: $pesos" }

            val wholeText = parts[0].ifEmpty { "0" }
            val whole = wholeText.toLongOrNull()
            requireNotNull(whole) { "Invalid peso amount: $pesos" }

            val fractionText = if (parts.size == 2) parts[1] else ""
            require(fractionText.length <= 2) { "Invalid peso amount: $pesos" }
            val fraction = fractionText.padEnd(2, '0').ifEmpty { "00" }.toLongOrNull()
            requireNotNull(fraction) { "Invalid peso amount: $pesos" }

            val totalCentavos = whole * 100 + fraction
            return Money(if (negative) -totalCentavos else totalCentavos)
        }
    }
}
