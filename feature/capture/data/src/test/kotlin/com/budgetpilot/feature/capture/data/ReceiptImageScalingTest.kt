package com.budgetpilot.feature.capture.data

import assertk.assertThat
import assertk.assertions.isEqualTo
import org.junit.jupiter.api.Test

class ReceiptImageScalingTest {
    @Test
    fun `calculateInSampleSize returns 1 when already smaller than target`() {
        val sampleSize = ReceiptImageScaling.calculateInSampleSize(width = 800, height = 600, maxDimension = 1024)

        assertThat(sampleSize).isEqualTo(1)
    }

    @Test
    fun `calculateInSampleSize halves until either side would drop below target`() {
        val sampleSize = ReceiptImageScaling.calculateInSampleSize(width = 4096, height = 3072, maxDimension = 1024)

        assertThat(sampleSize).isEqualTo(2)
    }

    @Test
    fun `calculateInSampleSize halves repeatedly for a square image`() {
        val sampleSize = ReceiptImageScaling.calculateInSampleSize(width = 8192, height = 8192, maxDimension = 1024)

        assertThat(sampleSize).isEqualTo(8)
    }

    @Test
    fun `computeTargetDimensions leaves a smaller image untouched`() {
        val (width, height) = ReceiptImageScaling.computeTargetDimensions(width = 800, height = 600, maxDimension = 1024)

        assertThat(width).isEqualTo(800)
        assertThat(height).isEqualTo(600)
    }

    @Test
    fun `computeTargetDimensions scales down the longest side to maxDimension preserving aspect ratio`() {
        val (width, height) = ReceiptImageScaling.computeTargetDimensions(width = 2048, height = 1024, maxDimension = 1024)

        assertThat(width).isEqualTo(1024)
        assertThat(height).isEqualTo(512)
    }

    @Test
    fun `computeTargetDimensions handles a portrait receipt photo`() {
        val (width, height) = ReceiptImageScaling.computeTargetDimensions(width = 800, height = 3200, maxDimension = 800)

        assertThat(width).isEqualTo(200)
        assertThat(height).isEqualTo(800)
    }
}
