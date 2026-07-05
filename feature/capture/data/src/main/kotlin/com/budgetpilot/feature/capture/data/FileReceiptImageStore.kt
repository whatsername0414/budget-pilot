package com.budgetpilot.feature.capture.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.budgetpilot.core.domain.DataError
import com.budgetpilot.core.domain.EmptyResult
import com.budgetpilot.core.domain.Result
import com.budgetpilot.feature.capture.domain.ReceiptImageStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Saves receipt photos to app-private storage, downscaled to
 * [ReceiptImageScaling.MAX_DIMENSION] on the longest side and re-compressed
 * as JPEG, so a full-resolution camera photo never lingers on disk.
 */
class FileReceiptImageStore(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ReceiptImageStore {
    @Suppress("SwallowedException")
    override suspend fun save(imageBytes: ByteArray): Result<String, DataError.Local> =
        withContext(ioDispatcher) {
            try {
                val bitmap = decodeDownsampled(imageBytes) ?: return@withContext Result.Error(DataError.Local.UNKNOWN)
                val file = receiptsDir().resolve("${UUID.randomUUID()}.jpg")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, ReceiptImageScaling.JPEG_QUALITY, out)
                }
                bitmap.recycle()
                Result.Success(file.absolutePath)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.Error(DataError.Local.DISK_FULL)
            }
        }

    @Suppress("SwallowedException")
    override suspend fun delete(path: String): EmptyResult<DataError.Local> =
        withContext(ioDispatcher) {
            try {
                val deleted = File(path).delete()
                if (deleted) Result.Success(Unit) else Result.Error(DataError.Local.NOT_FOUND)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Result.Error(DataError.Local.UNKNOWN)
            }
        }

    private fun decodeDownsampled(imageBytes: ByteArray): Bitmap? {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, bounds)

        val sampleSize = ReceiptImageScaling.calculateInSampleSize(bounds.outWidth, bounds.outHeight)
        val sampled =
            BitmapFactory.decodeByteArray(
                imageBytes,
                0,
                imageBytes.size,
                BitmapFactory.Options().apply { inSampleSize = sampleSize },
            ) ?: return null

        val (targetWidth, targetHeight) = ReceiptImageScaling.computeTargetDimensions(sampled.width, sampled.height)
        return if (targetWidth == sampled.width && targetHeight == sampled.height) {
            sampled
        } else {
            Bitmap.createScaledBitmap(sampled, targetWidth, targetHeight, true).also { sampled.recycle() }
        }
    }

    private fun receiptsDir(): File = File(context.filesDir, "receipts").apply { mkdirs() }
}
