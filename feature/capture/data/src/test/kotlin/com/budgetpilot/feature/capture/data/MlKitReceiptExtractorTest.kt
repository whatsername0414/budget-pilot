package com.budgetpilot.feature.capture.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.budgetpilot.core.domain.Result
import com.budgetpilot.feature.capture.domain.ExtractionError
import com.budgetpilot.feature.capture.domain.ReceiptImage
import com.budgetpilot.feature.capture.domain.ReceiptParser
import com.budgetpilot.feature.capture.domain.model.OcrLine
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class MlKitReceiptExtractorTest {
    private val clock = Clock.fixed(Instant.parse("2026-07-07T12:00:00Z"), ZoneOffset.UTC)
    private val parser = ReceiptParser(clock)

    @Test
    fun `hands recognized OCR lines off to the parser and returns its result`() =
        runTest {
            val lines =
                listOf(
                    OcrLine("JOLLIBEE", 0.95f),
                    OcrLine("07/03/2026", 0.9f),
                    OcrLine("TOTAL 199.00", 0.92f),
                )
            val extractor = MlKitReceiptExtractor(FakeOcrLineRecognizer(Result.Success(lines)), parser)

            val result = extractor.extract(image())

            assertThat(result).isEqualTo(parser.parse(lines))
        }

    @Test
    fun `propagates a recognizer failure without calling the parser`() =
        runTest {
            val extractor =
                MlKitReceiptExtractor(FakeOcrLineRecognizer(Result.Error(ExtractionError.ImageUnreadable)), parser)

            val result = extractor.extract(image())

            assertThat(result).isEqualTo(Result.Error(ExtractionError.ImageUnreadable))
        }

    private fun image(): ReceiptImage = ReceiptImage(path = "receipt.jpg") { ByteArray(0) }

    private class FakeOcrLineRecognizer(
        private val result: Result<List<OcrLine>, ExtractionError>,
    ) : OcrLineRecognizer {
        override suspend fun recognize(image: ReceiptImage): Result<List<OcrLine>, ExtractionError> = result
    }
}
