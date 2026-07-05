package com.budgetpilot.feature.capture.data

/**
 * Pure sizing math for [FileReceiptImageStore], kept free of Android Bitmap
 * types so it's testable on the plain JVM without Robolectric.
 */
object ReceiptImageScaling {
    const val MAX_DIMENSION = 1024
    const val JPEG_QUALITY = 85

    /**
     * Power-of-two downsample factor for `BitmapFactory.Options.inSampleSize`,
     * per Android's own decode-bounds-first guidance: the smallest factor
     * that still leaves both dimensions >= [maxDimension].
     */
    fun calculateInSampleSize(
        width: Int,
        height: Int,
        maxDimension: Int = MAX_DIMENSION,
    ): Int {
        var inSampleSize = 1
        var sampledWidth = width
        var sampledHeight = height
        while (sampledWidth / 2 >= maxDimension && sampledHeight / 2 >= maxDimension) {
            sampledWidth /= 2
            sampledHeight /= 2
            inSampleSize *= 2
        }
        return inSampleSize
    }

    /**
     * Final target dimensions after sample-size decoding, preserving aspect
     * ratio and never upscaling an image already smaller than [maxDimension].
     */
    fun computeTargetDimensions(
        width: Int,
        height: Int,
        maxDimension: Int = MAX_DIMENSION,
    ): Pair<Int, Int> {
        val largestSide = maxOf(width, height)
        if (largestSide <= maxDimension) return width to height

        val scale = maxDimension.toDouble() / largestSide.toDouble()
        val targetWidth = (width * scale).toInt().coerceAtLeast(1)
        val targetHeight = (height * scale).toInt().coerceAtLeast(1)
        return targetWidth to targetHeight
    }
}
