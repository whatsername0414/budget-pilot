package com.budgetpilot.feature.capture.presentation.fake

import com.budgetpilot.core.domain.DataError
import com.budgetpilot.core.domain.Result
import com.budgetpilot.core.domain.model.Category
import com.budgetpilot.core.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeCategoryRepository(
    private val categories: List<Category> = DefaultCategories,
) : CategoryRepository {
    override fun observeCategories(): Flow<List<Category>> = flowOf(categories)

    override suspend fun getCategoryById(id: Long): Result<Category, DataError.Local> =
        categories
            .find { it.id == id }
            ?.let { Result.Success(it) }
            ?: Result.Error(DataError.Local.NOT_FOUND)

    companion object {
        val DefaultCategories =
            listOf(
                Category(id = 1, name = "Food", iconKey = "restaurant", colorKey = "food", isDefault = true),
                Category(id = 2, name = "Transport", iconKey = "directions_bus", colorKey = "transport", isDefault = true),
            )
    }
}
