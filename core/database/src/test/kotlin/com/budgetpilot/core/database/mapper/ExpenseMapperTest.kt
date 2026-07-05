package com.budgetpilot.core.database.mapper

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.budgetpilot.core.database.entity.ExpenseEntity
import com.budgetpilot.core.domain.model.Expense
import com.budgetpilot.core.domain.money.Money
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import com.budgetpilot.core.database.entity.ExpenseSource as ExpenseSourceEntity
import com.budgetpilot.core.domain.model.ExpenseSource as ExpenseSourceDomain

class ExpenseMapperTest {
    private val createdAt = Instant.parse("2026-07-01T08:00:00Z")
    private val date = LocalDate.of(2026, 7, 1)

    private val entity =
        ExpenseEntity(
            id = 1,
            amountCentavos = 15_050,
            merchant = "Jollibee",
            categoryId = 2,
            date = date,
            note = "Lunch",
            source = ExpenseSourceEntity.MANUAL,
            imageUri = null,
            createdAt = createdAt,
        )

    private val domain =
        Expense(
            id = 1,
            amount = Money.ofCentavos(15_050),
            merchant = "Jollibee",
            categoryId = 2,
            date = date,
            note = "Lunch",
            source = ExpenseSourceDomain.MANUAL,
            imageUri = null,
            createdAt = createdAt,
        )

    @Test
    fun `entity maps to domain`() {
        assertThat(entity.toExpense()).isEqualTo(domain)
    }

    @Test
    fun `domain maps to entity`() {
        assertThat(domain.toEntity()).isEqualTo(entity)
    }

    @Test
    fun `entity to domain and back round-trips`() {
        assertThat(entity.toExpense().toEntity()).isEqualTo(entity)
    }

    @Test
    fun `every ExpenseSource maps to domain and back`() {
        ExpenseSourceEntity.entries.forEach { source ->
            assertThat(source.toDomain().toEntity()).isEqualTo(source)
        }
    }
}
