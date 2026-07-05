package com.budgetpilot.core.database.seed

import com.budgetpilot.core.database.entity.CategoryEntity

/** Matches the seed table in DESIGN-SPEC.md §1.3; iconKey/colorKey are resolved by :core:design-system. */
object DefaultCategories {
    val all: List<CategoryEntity> =
        listOf(
            CategoryEntity(name = "Food", iconKey = "restaurant", colorKey = "food", isDefault = true),
            CategoryEntity(name = "Transport", iconKey = "directions_bus", colorKey = "transport", isDefault = true),
            CategoryEntity(name = "Bills", iconKey = "bolt", colorKey = "bills", isDefault = true),
            CategoryEntity(name = "Groceries", iconKey = "shopping_cart", colorKey = "groceries", isDefault = true),
            CategoryEntity(name = "Shopping", iconKey = "shopping_bag", colorKey = "shopping", isDefault = true),
            CategoryEntity(name = "Health", iconKey = "favorite", colorKey = "health", isDefault = true),
            CategoryEntity(name = "Entertainment", iconKey = "movie", colorKey = "entertainment", isDefault = true),
            CategoryEntity(name = "Other", iconKey = "category", colorKey = "other", isDefault = true),
        )
}
