package com.budgetpilot.core.database.repository

import com.budgetpilot.core.database.dao.CategoryDao
import com.budgetpilot.core.database.mapper.toCategory
import com.budgetpilot.core.domain.DataError
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.model.Category
import com.budgetpilot.core.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCategoryRepository(
    private val dao: CategoryDao,
) : CategoryRepository {
    override fun observeCategories(): Flow<List<Category>> = dao.observeCategories().map { entities -> entities.map { it.toCategory() } }

    override suspend fun getCategoryById(id: Long): Result<Category, DataError.Local> =
        runLocalCatching {
            val entity = dao.getCategoryById(id)
            if (entity != null) Result.Success(entity.toCategory()) else Result.Error(DataError.Local.NOT_FOUND)
        }
}
