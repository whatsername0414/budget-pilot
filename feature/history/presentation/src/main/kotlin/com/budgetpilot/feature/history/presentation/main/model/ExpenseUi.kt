package com.budgetpilot.feature.history.presentation.main.model

import androidx.compose.runtime.Stable
import com.budgetpilot.core.domain.model.Category
import com.budgetpilot.core.domain.model.Expense
import com.budgetpilot.core.domain.model.ExpenseSource
import com.budgetpilot.core.domain.money.Money
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class ExpenseUi(
    val id: Long,
    val merchant: String,
    val categoryName: String,
    val categoryIconKey: String,
    val categoryColorKey: String,
    val amount: Money,
    val formattedTime: String,
    val source: ExpenseSource,
)

@Stable
data class ExpenseDayGroupUi(
    val date: LocalDate,
    val dateLabel: String,
    val totalAmount: Money,
    val expenses: List<ExpenseUi>,
)

private val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
private val DayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.US)

fun Expense.toExpenseUi(category: Category?): ExpenseUi =
    ExpenseUi(
        id = id,
        merchant = merchant,
        categoryName = category?.name ?: "Other",
        categoryIconKey = category?.iconKey ?: "category",
        categoryColorKey = category?.colorKey ?: "other",
        amount = amount,
        formattedTime = createdAt.atZone(ZoneId.systemDefault()).toLocalTime().format(TimeFormatter),
        source = source,
    )

fun List<Expense>.toDayGroups(
    categoriesById: Map<Long, Category>,
    today: LocalDate = LocalDate.now(),
): List<ExpenseDayGroupUi> =
    sortedWith(compareByDescending<Expense> { it.date }.thenByDescending { it.createdAt })
        .groupBy { it.date }
        .map { (date, expenses) ->
            ExpenseDayGroupUi(
                date = date,
                dateLabel = date.toDayLabel(today),
                totalAmount = expenses.fold(Money.ZERO) { acc, expense -> acc + expense.amount },
                expenses = expenses.map { it.toExpenseUi(categoriesById[it.categoryId]) },
            )
        }

private fun LocalDate.toDayLabel(today: LocalDate): String =
    when (this) {
        today -> "Today"
        today.minusDays(1) -> "Yesterday"
        else -> format(DayFormatter)
    }
