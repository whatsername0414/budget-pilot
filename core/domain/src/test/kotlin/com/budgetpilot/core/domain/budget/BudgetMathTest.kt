package com.budgetpilot.core.domain.budget

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.budgetpilot.core.domain.money.Money
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class BudgetMathTest {
    @Test
    fun `statusFor is ON_TRACK below the warning threshold`() {
        val status = BudgetMath.statusFor(spent = Money.ofCentavos(7_900), budget = Money.ofCentavos(10_000))

        assertThat(status).isEqualTo(BudgetStatus.ON_TRACK)
    }

    @Test
    fun `statusFor is WARNING exactly at the 80 percent boundary`() {
        val status = BudgetMath.statusFor(spent = Money.ofCentavos(8_000), budget = Money.ofCentavos(10_000))

        assertThat(status).isEqualTo(BudgetStatus.WARNING)
    }

    @Test
    fun `statusFor is WARNING between the 80 and 100 percent boundaries`() {
        val status = BudgetMath.statusFor(spent = Money.ofCentavos(9_000), budget = Money.ofCentavos(10_000))

        assertThat(status).isEqualTo(BudgetStatus.WARNING)
    }

    @Test
    fun `statusFor is OVER_BUDGET exactly at the 100 percent boundary`() {
        val status = BudgetMath.statusFor(spent = Money.ofCentavos(10_000), budget = Money.ofCentavos(10_000))

        assertThat(status).isEqualTo(BudgetStatus.OVER_BUDGET)
    }

    @Test
    fun `statusFor is OVER_BUDGET above the 100 percent boundary`() {
        val status = BudgetMath.statusFor(spent = Money.ofCentavos(12_000), budget = Money.ofCentavos(10_000))

        assertThat(status).isEqualTo(BudgetStatus.OVER_BUDGET)
    }

    @Test
    fun `percentUsed delegates to Money percentOf`() {
        val percent = BudgetMath.percentUsed(spent = Money.ofCentavos(5_000), budget = Money.ofCentavos(10_000))

        assertThat(percent).isEqualTo(50.0)
    }

    @Test
    fun `projectedOverspend on day 1 extrapolates the full month from a single day`() {
        val overspend =
            BudgetMath.projectedOverspend(
                spent = Money.ofCentavos(1_000),
                budget = Money.ofCentavos(20_000),
                dayOfMonth = 1,
                daysInMonth = 30,
            )

        // 1_000/day * 30 days = 30_000 projected, 10_000 over a 20_000 budget.
        assertThat(overspend).isEqualTo(Money.ofCentavos(10_000))
    }

    @Test
    fun `projectedOverspend on the last day of the month uses actual spend as the projection`() {
        val overspend =
            BudgetMath.projectedOverspend(
                spent = Money.ofCentavos(25_000),
                budget = Money.ofCentavos(20_000),
                dayOfMonth = 30,
                daysInMonth = 30,
            )

        assertThat(overspend).isEqualTo(Money.ofCentavos(5_000))
    }

    @Test
    fun `projectedOverspend is zero when the pace stays under budget`() {
        val overspend =
            BudgetMath.projectedOverspend(
                spent = Money.ofCentavos(200),
                budget = Money.ofCentavos(100_000),
                dayOfMonth = 15,
                daysInMonth = 30,
            )

        assertThat(overspend).isEqualTo(Money.ZERO)
    }

    @Test
    fun `projectedOverspend is zero on the last day when actual spend is under budget`() {
        val overspend =
            BudgetMath.projectedOverspend(
                spent = Money.ofCentavos(15_000),
                budget = Money.ofCentavos(20_000),
                dayOfMonth = 30,
                daysInMonth = 30,
            )

        assertThat(overspend).isEqualTo(Money.ZERO)
    }

    @Test
    fun `projectedOverspend rejects a dayOfMonth outside the month`() {
        assertThrows<IllegalArgumentException> {
            BudgetMath.projectedOverspend(
                spent = Money.ofCentavos(1_000),
                budget = Money.ofCentavos(10_000),
                dayOfMonth = 31,
                daysInMonth = 30,
            )
        }
    }

    @Test
    fun `projectedOverspend rejects a dayOfMonth of zero`() {
        assertThrows<IllegalArgumentException> {
            BudgetMath.projectedOverspend(
                spent = Money.ofCentavos(1_000),
                budget = Money.ofCentavos(10_000),
                dayOfMonth = 0,
                daysInMonth = 30,
            )
        }
    }
}
