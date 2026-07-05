package com.budgetpilot.feature.capture.presentation.fake

import com.budgetpilot.core.domain.DataError
import com.budgetpilot.core.domain.EmptyResult
import com.budgetpilot.core.domain.Result
import com.budgetpilot.feature.capture.domain.ReceiptImageStore

class FakeReceiptImageStore : ReceiptImageStore {
    var shouldReturnError = false
    val savedBytes = mutableListOf<ByteArray>()
    private var nextPath = 0

    override suspend fun save(imageBytes: ByteArray): Result<String, DataError.Local> {
        if (shouldReturnError) return Result.Error(DataError.Local.DISK_FULL)
        savedBytes.add(imageBytes)
        return Result.Success("receipts/${nextPath++}.jpg")
    }

    override suspend fun delete(path: String): EmptyResult<DataError.Local> = Result.Success(Unit)
}
