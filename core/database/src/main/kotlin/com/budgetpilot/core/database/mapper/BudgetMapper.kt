package com.budgetpilot.core.database.mapper

import com.budgetpilot.core.database.entity.BudgetEntity
import com.budgetpilot.core.domain.model.Budget
import com.budgetpilot.core.domain.money.Money

fun BudgetEntity.toBudget(): Budget =
    Budget(
        id = id,
        categoryId = categoryId,
        month = month,
        amount = Money.ofCentavos(amountCentavos),
    )

fun Budget.toEntity(): BudgetEntity =
    BudgetEntity(
        id = id,
        categoryId = categoryId,
        month = month,
        amountCentavos = amount.centavos,
    )
