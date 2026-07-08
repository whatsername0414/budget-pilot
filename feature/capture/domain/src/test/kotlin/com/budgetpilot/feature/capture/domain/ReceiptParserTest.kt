package com.budgetpilot.feature.capture.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isInstanceOf
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.feature.capture.domain.model.Confidence
import com.budgetpilot.feature.capture.domain.model.ExtractedField
import com.budgetpilot.feature.capture.domain.model.OcrLine
import com.budgetpilot.feature.capture.domain.model.ReceiptType
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/** "Today" is fixed to 2026-07-07 so the no-date fallback case is deterministic. */
class ReceiptParserTest {
    private val clock = Clock.fixed(Instant.parse("2026-07-07T12:00:00Z"), ZoneOffset.UTC)
    private val parser = ReceiptParser(clock)

    @ParameterizedTest(name = "{0}")
    @MethodSource("cases")
    fun `parses fixture OCR dumps into the expected receipt`(case: Case) {
        val result = parser.parse(loadFixture(case.fixture))

        assertThat(result).isInstanceOf<Result.Success<*>>()
        val receipt = (result as Result.Success).data

        assertThat(receipt.merchant).isEqualTo(case.expectedMerchant)
        assertThat(receipt.suggestedCategory).isEqualTo(case.expectedCategory)
        assertThat(receipt.total).isEqualTo(case.expectedTotal)
        assertThat(receipt.date).isEqualTo(case.expectedDate)
        assertThat(receipt.receiptType).isEqualTo(ExtractedField(case.expectedType, Confidence.HIGH))
    }

    @Test
    fun `receipt with no recognizable total is NothingRecognized`() {
        val result = parser.parse(loadFixture("no_total.txt"))

        assertThat(result).isEqualTo(Result.Error(ExtractionError.NothingRecognized))
    }

    data class Case(
        val fixture: String,
        val expectedMerchant: ExtractedField<String>,
        val expectedCategory: ExtractedField<String?>,
        val expectedTotal: ExtractedField<Money>,
        val expectedDate: ExtractedField<LocalDate>,
        val expectedType: ReceiptType,
    ) {
        override fun toString(): String = fixture
    }

    private fun loadFixture(fileName: String): List<OcrLine> {
        val stream =
            checkNotNull(javaClass.classLoader?.getResourceAsStream("receipts/$fileName")) {
                "Fixture not found on test classpath: $fileName"
            }
        return stream.bufferedReader().useLines { lines ->
            lines
                .filter { it.isNotBlank() }
                .map { line ->
                    val (confidence, text) = line.split(":", limit = 2)
                    OcrLine(text, confidence.toFloat())
                }.toList()
        }
    }

    private companion object {
        @JvmStatic
        fun cases(): List<Case> =
            listOf(
                Case(
                    fixture = "jollibee.txt",
                    expectedMerchant = ExtractedField("JOLLIBEE", Confidence.HIGH),
                    expectedCategory = ExtractedField("Food", Confidence.MEDIUM),
                    expectedTotal = ExtractedField(Money.fromPesos("199.00"), Confidence.HIGH),
                    expectedDate = ExtractedField(LocalDate.of(2026, 7, 3), Confidence.HIGH),
                    expectedType = ReceiptType.PAPER,
                ),
                Case(
                    fixture = "sm_supermarket.txt",
                    expectedMerchant = ExtractedField("SM SUPERMARKET STA. ROSA #04521", Confidence.HIGH),
                    expectedCategory = ExtractedField("Shopping", Confidence.MEDIUM),
                    expectedTotal = ExtractedField(Money.fromPesos("320.00"), Confidence.HIGH),
                    expectedDate = ExtractedField(LocalDate.of(2026, 7, 5), Confidence.HIGH),
                    expectedType = ReceiptType.PAPER,
                ),
                Case(
                    fixture = "puregold.txt",
                    expectedMerchant = ExtractedField("PUREGOLD PRICE CLUB", Confidence.HIGH),
                    expectedCategory = ExtractedField("Groceries", Confidence.MEDIUM),
                    expectedTotal = ExtractedField(Money.fromPesos("425.00"), Confidence.HIGH),
                    expectedDate = ExtractedField(LocalDate.of(2026, 7, 3), Confidence.HIGH),
                    expectedType = ReceiptType.PAPER,
                ),
                Case(
                    fixture = "seven_eleven.txt",
                    expectedMerchant = ExtractedField("7-ELEVEN ORTIGAS", Confidence.HIGH),
                    expectedCategory = ExtractedField("Groceries", Confidence.MEDIUM),
                    expectedTotal = ExtractedField(Money.fromPesos("105.00"), Confidence.HIGH),
                    expectedDate = ExtractedField(LocalDate.of(2026, 7, 4), Confidence.HIGH),
                    expectedType = ReceiptType.PAPER,
                ),
                Case(
                    fixture = "mercury_drug.txt",
                    expectedMerchant = ExtractedField("MERCURY DRUG CORPORATION", Confidence.HIGH),
                    expectedCategory = ExtractedField("Health", Confidence.MEDIUM),
                    // No TOTAL/AMOUNT DUE line on this one — falls back to tendered (200.00) minus change (35.00).
                    expectedTotal = ExtractedField(Money.fromPesos("165.00"), Confidence.MEDIUM),
                    expectedDate = ExtractedField(LocalDate.of(2026, 7, 6), Confidence.HIGH),
                    expectedType = ReceiptType.PAPER,
                ),
                Case(
                    fixture = "gcash_send.txt",
                    expectedMerchant = ExtractedField("Juan Dela Cruz", Confidence.HIGH),
                    expectedCategory = ExtractedField("Bills", Confidence.MEDIUM),
                    expectedTotal = ExtractedField(Money.fromPesos("500.00"), Confidence.HIGH),
                    expectedDate = ExtractedField(LocalDate.of(2026, 7, 5), Confidence.HIGH),
                    expectedType = ReceiptType.GCASH,
                ),
                Case(
                    fixture = "gcash_billpay.txt",
                    expectedMerchant = ExtractedField("Meralco", Confidence.HIGH),
                    expectedCategory = ExtractedField("Bills", Confidence.MEDIUM),
                    expectedTotal = ExtractedField(Money.fromPesos("1532.00"), Confidence.HIGH),
                    expectedDate = ExtractedField(LocalDate.of(2026, 7, 3), Confidence.HIGH),
                    expectedType = ReceiptType.GCASH,
                ),
                Case(
                    fixture = "maya_send.txt",
                    expectedMerchant = ExtractedField("Maria Santos", Confidence.HIGH),
                    expectedCategory = ExtractedField("Bills", Confidence.MEDIUM),
                    expectedTotal = ExtractedField(Money.fromPesos("750.00"), Confidence.HIGH),
                    expectedDate = ExtractedField(LocalDate.of(2026, 7, 4), Confidence.HIGH),
                    expectedType = ReceiptType.MAYA,
                ),
                Case(
                    fixture = "degraded_ocr.txt",
                    // Merchant and date lines are marked low-confidence (garbled OCR) and excluded,
                    // so both fall back to LOW-confidence guesses even though the total survives intact.
                    expectedMerchant = ExtractedField("1PC CH1CKENJ0Y RIC3 89.00", Confidence.LOW),
                    expectedCategory = ExtractedField(null, Confidence.LOW),
                    expectedTotal = ExtractedField(Money.fromPesos("199.00"), Confidence.HIGH),
                    expectedDate = ExtractedField(LocalDate.of(2026, 7, 7), Confidence.LOW),
                    expectedType = ReceiptType.PAPER,
                ),
            )
    }
}
