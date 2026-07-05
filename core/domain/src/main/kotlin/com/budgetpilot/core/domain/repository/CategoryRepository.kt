package com.budgetpilot.core.domain.repository

import com.budgetpilot.core.domain.DataError
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeCategories(): Flow<List<Category>>

    suspend fun getCategoryById(id: Long): Result<Category, DataError.Local>
}
