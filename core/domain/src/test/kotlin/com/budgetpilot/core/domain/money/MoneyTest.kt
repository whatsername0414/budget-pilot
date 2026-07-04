package com.budgetpilot.core.domain.money

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MoneyTest {

    @Test
    fun `plus adds centavos`() {
        val result = Money.ofCentavos(1_000) + Money.ofCentavos(250)

        assertThat(result).isEqualTo(Money.ofCentavos(1_250))
    }

    @Test
    fun `minus subtracts centavos`() {
        val result = Money.ofCentavos(1_000) - Money.ofCentavos(250)

        assertThat(result).isEqualTo(Money.ofCentavos(750))
    }

    @Test
    fun `minus can produce a negative amount`() {
        val result = Money.ofCentavos(250) - Money.ofCentavos(1_000)

        assertThat(result).isEqualTo(Money.ofCentavos(-750))
    }

    @Test
    fun `times multiplies by an integer factor`() {
        val result = Money.ofCentavos(150) * 3

        assertThat(result).isEqualTo(Money.ofCentavos(450))
    }

    @Test
    fun `unaryMinus negates the amount`() {
        val result = -Money.ofCentavos(500)

        assertThat(result).isEqualTo(Money.ofCentavos(-500))
    }

    @Test
    fun `compareTo orders by centavos`() {
        assertThat(Money.ofCentavos(100) < Money.ofCentavos(200)).isTrue()
        assertThat(Money.ofCentavos(200) < Money.ofCentavos(100)).isFalse()
        assertThat(Money.ofCentavos(100) == Money.ofCentavos(100)).isTrue()
    }

    @Test
    fun `percentOf computes a percentage of the whole`() {
        val spent = Money.ofCentavos(8_000)
        val budget = Money.ofCentavos(10_000)

        assertThat(spent.percentOf(budget)).isEqualTo(80.0)
    }

    @Test
    fun `percentOf a zero whole returns zero instead of dividing by zero`() {
        val spent = Money.ofCentavos(500)

        assertThat(spent.percentOf(Money.ZERO)).isEqualTo(0.0)
    }

    @Test
    fun `fromPesos parses a whole peso amount`() {
        assertThat(Money.fromPesos("100")).isEqualTo(Money.ofCentavos(10_000))
    }

    @Test
    fun `fromPesos parses a peso amount with centavos`() {
        assertThat(Money.fromPesos("100.50")).isEqualTo(Money.ofCentavos(10_050))
    }

    @Test
    fun `fromPesos pads a single fraction digit`() {
        assertThat(Money.fromPesos("10.5")).isEqualTo(Money.ofCentavos(1_050))
    }

    @Test
    fun `fromPesos strips the peso sign and thousands separators`() {
        assertThat(Money.fromPesos("₱1,234.56")).isEqualTo(Money.ofCentavos(123_456))
    }

    @Test
    fun `fromPesos strips a PHP prefix`() {
        assertThat(Money.fromPesos("PHP 50")).isEqualTo(Money.ofCentavos(5_000))
    }

    @Test
    fun `fromPesos parses a negative amount`() {
        assertThat(Money.fromPesos("-20.50")).isEqualTo(Money.ofCentavos(-2_050))
    }

    @Test
    fun `fromPesos parses zero`() {
        assertThat(Money.fromPesos("0.00")).isEqualTo(Money.ZERO)
    }

    @Test
    fun `fromPesos rejects sub-centavo precision`() {
        assertThrows<IllegalArgumentException> {
            Money.fromPesos("10.555")
        }
    }

    @Test
    fun `fromPesos rejects blank input`() {
        assertThrows<IllegalArgumentException> {
            Money.fromPesos("   ")
        }
    }

    @Test
    fun `fromPesos rejects non-numeric input`() {
        assertThrows<IllegalArgumentException> {
            Money.fromPesos("abc")
        }
    }
}
