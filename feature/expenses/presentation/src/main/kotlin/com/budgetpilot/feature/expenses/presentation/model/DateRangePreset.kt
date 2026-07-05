package com.budgetpilot.feature.expenses.presentation.model

import java.time.LocalDate
import java.time.YearMonth

enum class DateRangePreset(
    val label: String,
) {
    THIS_MONTH("This month"),
    LAST_MONTH("Last month"),
    ALL_TIME("All time"),
    ;

    fun toDateRange(today: LocalDate = LocalDate.now()): ClosedRange<LocalDate> =
        when (this) {
            THIS_MONTH -> YearMonth.from(today).let { it.atDay(1)..it.atEndOfMonth() }
            LAST_MONTH -> YearMonth.from(today).minusMonths(1).let { it.atDay(1)..it.atEndOfMonth() }
            ALL_TIME -> EARLIEST_DATE..today
        }

    private companion object {
        val EARLIEST_DATE: LocalDate = LocalDate.of(2000, 1, 1)
    }
}
