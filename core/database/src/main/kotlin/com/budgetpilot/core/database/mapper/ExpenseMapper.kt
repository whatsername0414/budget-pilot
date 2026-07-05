package com.budgetpilot.core.database.mapper

import com.budgetpilot.core.database.entity.ExpenseEntity
import com.budgetpilot.core.domain.model.Expense
import com.budgetpilot.core.domain.money.Money
import com.budgetpilot.core.database.entity.ExpenseSource as ExpenseSourceEntity
import com.budgetpilot.core.domain.model.ExpenseSource as ExpenseSourceDomain

fun ExpenseEntity.toExpense(): Expense =
    Expense(
        id = id,
        amount = Money.ofCentavos(amountCentavos),
        merchant = merchant,
        categoryId = categoryId,
        date = date,
        note = note,
        source = source.toDomain(),
        imageUri = imageUri,
        createdAt = createdAt,
    )

fun Expense.toEntity(): ExpenseEntity =
    ExpenseEntity(
        id = id,
        amountCentavos = amount.centavos,
        merchant = merchant,
        categoryId = categoryId,
        date = date,
        note = note,
        source = source.toEntity(),
        imageUri = imageUri,
        createdAt = createdAt,
    )

fun ExpenseSourceEntity.toDomain(): ExpenseSourceDomain =
    when (this) {
        ExpenseSourceEntity.MANUAL -> ExpenseSourceDomain.MANUAL
        ExpenseSourceEntity.RECEIPT -> ExpenseSourceDomain.RECEIPT
        ExpenseSourceEntity.GCASH -> ExpenseSourceDomain.GCASH
        ExpenseSourceEntity.MAYA -> ExpenseSourceDomain.MAYA
    }

fun ExpenseSourceDomain.toEntity(): ExpenseSourceEntity =
    when (this) {
        ExpenseSourceDomain.MANUAL -> ExpenseSourceEntity.MANUAL
        ExpenseSourceDomain.RECEIPT -> ExpenseSourceEntity.RECEIPT
        ExpenseSourceDomain.GCASH -> ExpenseSourceEntity.GCASH
        ExpenseSourceDomain.MAYA -> ExpenseSourceEntity.MAYA
    }
