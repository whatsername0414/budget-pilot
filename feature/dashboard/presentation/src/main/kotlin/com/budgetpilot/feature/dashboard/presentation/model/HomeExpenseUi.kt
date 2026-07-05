package com.budgetpilot.feature.dashboard.presentation.model

import com.budgetpilot.core.domain.model.Category
import com.budgetpilot.core.domain.model.Expense
import com.budgetpilot.core.domain.model.ExpenseSource
import com.budgetpilot.core.domain.money.Money
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class HomeExpenseUi(
    val id: Long,
    val merchant: String,
    val categoryName: String,
    val categoryIconKey: String,
    val categoryColorKey: String,
    val amount: Money,
    val formattedTime: String,
    val source: ExpenseSource,
)

private val TimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)

fun Expense.toHomeExpenseUi(category: Category?): HomeExpenseUi =
    HomeExpenseUi(
        id = id,
        merchant = merchant,
        categoryName = category?.name ?: "Other",
        categoryIconKey = category?.iconKey ?: "category",
        categoryColorKey = category?.colorKey ?: "other",
        amount = amount,
        formattedTime = createdAt.atZone(ZoneId.systemDefault()).toLocalTime().format(TimeFormatter),
        source = source,
    )
