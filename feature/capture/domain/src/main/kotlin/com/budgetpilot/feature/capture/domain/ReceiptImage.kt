package com.budgetpilot.feature.capture.domain

class ReceiptImage(
    val path: String,
    private val bytesProvider: suspend () -> ByteArray,
) {
    suspend fun readBytes(): ByteArray = bytesProvider()
}
