package com.budgetpilot.core.domain.model

data class Category(
    val id: Long,
    val name: String,
    val iconKey: String,
    val colorKey: String,
    val isDefault: Boolean,
)
