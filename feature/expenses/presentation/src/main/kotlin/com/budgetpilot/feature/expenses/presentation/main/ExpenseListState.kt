package com.budgetpilot.feature.expenses.presentation.main

import androidx.compose.runtime.Stable
import com.budgetpilot.core.domain.model.Category
import com.budgetpilot.core.presentation.UiText
import com.budgetpilot.feature.expenses.presentation.main.model.DateRangePreset
import com.budgetpilot.feature.expenses.presentation.main.model.ExpenseDayGroupUi

@Stable
data class ExpenseListState(
    val dayGroups: List<ExpenseDayGroupUi> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategoryId: Long? = null,
    val searchQuery: String = "",
    val dateRangePreset: DateRangePreset = DateRangePreset.THIS_MONTH,
    val isFilterSheetVisible: Boolean = false,
    val isLoading: Boolean = true,
    val error: UiText? = null,
) {
    val isEmpty: Boolean
        get() = dayGroups.isEmpty()

    val hasActiveFilter: Boolean
        get() =
            selectedCategoryId != null ||
                searchQuery.isNotBlank() ||
                dateRangePreset != DateRangePreset.THIS_MONTH
}
