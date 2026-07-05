package com.budgetpilot.core.database.mapper

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.budgetpilot.core.database.entity.BudgetEntity
import com.budgetpilot.core.domain.model.Budget
import com.budgetpilot.core.domain.money.Money
import org.junit.jupiter.api.Test

class BudgetMapperTest {
    private val entity =
        BudgetEntity(
            id = 1,
            categoryId = 2,
            month = "2026-07",
            amountCentavos = 500_000,
        )

    private val domain =
        Budget(
            id = 1,
            categoryId = 2,
            month = "2026-07",
            amount = Money.ofCentavos(500_000),
        )

    @Test
    fun `entity maps to domain`() {
        assertThat(entity.toBudget()).isEqualTo(domain)
    }

    @Test
    fun `domain maps to entity`() {
        assertThat(domain.toEntity()).isEqualTo(entity)
    }

    @Test
    fun `entity to domain and back round-trips`() {
        assertThat(entity.toBudget().toEntity()).isEqualTo(entity)
    }
}
