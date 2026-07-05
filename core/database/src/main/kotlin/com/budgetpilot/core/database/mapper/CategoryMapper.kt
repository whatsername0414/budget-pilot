package com.budgetpilot.core.database.mapper

import com.budgetpilot.core.database.entity.CategoryEntity
import com.budgetpilot.core.domain.model.Category

fun CategoryEntity.toCategory(): Category =
    Category(
        id = id,
        name = name,
        iconKey = iconKey,
        colorKey = colorKey,
        isDefault = isDefault,
    )

fun Category.toEntity(): CategoryEntity =
    CategoryEntity(
        id = id,
        name = name,
        iconKey = iconKey,
        colorKey = colorKey,
        isDefault = isDefault,
    )
