package com.budgetpilot.core.presentation.money

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.budgetpilot.core.domain.money.Money
import org.junit.jupiter.api.Test

class PesoFormatterTest {
    @Test
    fun `zero formats as peso zero`() {
        assertThat(PesoFormatter.format(Money.ZERO)).isEqualTo("₱0.00")
    }

    @Test
    fun `exact centavos are preserved`() {
        assertThat(PesoFormatter.format(Money.ofCentavos(5))).isEqualTo("₱0.05")
        assertThat(PesoFormatter.format(Money.ofCentavos(100))).isEqualTo("₱1.00")
        assertThat(PesoFormatter.format(Money.ofCentavos(109))).isEqualTo("₱1.09")
    }

    @Test
    fun `negative amounts are prefixed with a minus sign before the peso symbol`() {
        assertThat(PesoFormatter.format(Money.ofCentavos(-150))).isEqualTo("-₱1.50")
    }

    @Test
    fun `large amounts are grouped by thousands`() {
        assertThat(PesoFormatter.format(Money.ofCentavos(123_456))).isEqualTo("₱1,234.56")
        assertThat(PesoFormatter.format(Money.ofCentavos(1_000_000_00))).isEqualTo("₱1,000,000.00")
    }

    @Test
    fun `four digit whole amount is grouped correctly`() {
        assertThat(PesoFormatter.format(Money.ofCentavos(1_000_00))).isEqualTo("₱1,000.00")
    }

    @Test
    fun `three digit whole amount has no grouping separator`() {
        assertThat(PesoFormatter.format(Money.ofCentavos(999_00))).isEqualTo("₱999.00")
    }
}
