package com.budgetpilot.feature.capture.data

import android.graphics.BitmapFactory
import com.budgetpilot.core.domain.Result
import com.budgetpilot.feature.capture.domain.ExtractionError
import com.budgetpilot.feature.capture.domain.ReceiptImage
import com.budgetpilot.feature.capture.domain.model.OcrLine
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognizer
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class MlKitOcrLineRecognizer(
    private val textRecognizer: TextRecognizer,
) : OcrLineRecognizer {
    override suspend fun recognize(image: ReceiptImage): Result<List<OcrLine>, ExtractionError> {
        val bytes = image.readBytes()
        val bitmap =
            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                ?: return Result.Error(ExtractionError.ImageUnreadable)

        return suspendCancellableCoroutine { continuation ->
            textRecognizer
                .process(InputImage.fromBitmap(bitmap, 0))
                .addOnSuccessListener { text ->
                    val lines = text.textBlocks.flatMap { it.lines }.map { OcrLine(it.text, it.confidence) }
                    continuation.resume(Result.Success(lines))
                }.addOnFailureListener {
                    continuation.resume(Result.Error(ExtractionError.ImageUnreadable))
                }
        }
    }
}
