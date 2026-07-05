package com.budgetpilot.feature.capture.domain

import com.budgetpilot.core.domain.DataError
import com.budgetpilot.core.domain.EmptyResult
import com.budgetpilot.core.domain.Result

interface ReceiptImageStore {
    suspend fun save(imageBytes: ByteArray): Result<String, DataError.Local>

    suspend fun delete(path: String): EmptyResult<DataError.Local>
}
