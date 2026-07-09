package com.budgetpilot.core.presentation.money

import androidx.compose.ui.text.AnnotatedString
import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class MoneyInputVisualTransformationTest {
    private fun format(raw: String): String = MoneyInputVisualTransformation.filter(AnnotatedString(raw)).text.text

    @Test
    fun `whole amounts of four or more digits are grouped by thousands`() {
        assertThat(format("1000")).isEqualTo("1,000")
        assertThat(format("100000")).isEqualTo("100,000")
        assertThat(format("1000000")).isEqualTo("1,000,000")
    }

    @Test
    fun `whole amounts under four digits are left alone`() {
        assertThat(format("1")).isEqualTo("1")
        assertThat(format("12")).isEqualTo("12")
        assertThat(format("999")).isEqualTo("999")
    }

    @Test
    fun `fraction digits are passed through untouched`() {
        assertThat(format("1000.5")).isEqualTo("1,000.5")
        assertThat(format("1234.56")).isEqualTo("1,234.56")
        assertThat(format("1000000.09")).isEqualTo("1,000,000.09")
    }

    @Test
    fun `empty and partial input is handled without throwing`() {
        assertThat(format("")).isEqualTo("")
        assertThat(format(".5")).isEqualTo(".5")
        assertThat(format("1000.")).isEqualTo("1,000.")
    }

    @Test
    fun `offset mapping round-trips through the inserted separators`() {
        val transformed = MoneyInputVisualTransformation.filter(AnnotatedString("1000000"))
        assertThat(transformed.text.text).isEqualTo("1,000,000")

        val originalToTransformed = (0..7).map { transformed.offsetMapping.originalToTransformed(it) }
        assertThat(originalToTransformed).isEqualTo(listOf(0, 2, 3, 4, 6, 7, 8, 9))

        val transformedToOriginal = (0..9).map { transformed.offsetMapping.transformedToOriginal(it) }
        assertThat(transformedToOriginal).isEqualTo(listOf(0, 1, 1, 2, 3, 4, 4, 5, 6, 7))
    }
}
