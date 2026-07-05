package com.budgetpilot.core.database.repository

import android.database.sqlite.SQLiteFullException
import com.budgetpilot.core.domain.DataError
import com.budgetpilot.core.domain.Result
import kotlinx.coroutines.CancellationException

@Suppress("SwallowedException")
internal inline fun <T> runLocalCatching(block: () -> Result<T, DataError.Local>): Result<T, DataError.Local> =
    try {
        block()
    } catch (e: SQLiteFullException) {
        Result.Error(DataError.Local.DISK_FULL)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.Error(DataError.Local.UNKNOWN)
    }
