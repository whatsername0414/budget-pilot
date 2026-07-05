package com.budgetpilot.core.database.mapper

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.budgetpilot.core.database.entity.CategoryEntity
import com.budgetpilot.core.domain.model.Category
import org.junit.jupiter.api.Test

class CategoryMapperTest {
    private val entity =
        CategoryEntity(
            id = 1,
            name = "Food",
            iconKey = "restaurant",
            colorKey = "food",
            isDefault = true,
        )

    private val domain =
        Category(
            id = 1,
            name = "Food",
            iconKey = "restaurant",
            colorKey = "food",
            isDefault = true,
        )

    @Test
    fun `entity maps to domain`() {
        assertThat(entity.toCategory()).isEqualTo(domain)
    }

    @Test
    fun `domain maps to entity`() {
        assertThat(domain.toEntity()).isEqualTo(entity)
    }

    @Test
    fun `entity to domain and back round-trips`() {
        assertThat(entity.toCategory().toEntity()).isEqualTo(entity)
    }
}
