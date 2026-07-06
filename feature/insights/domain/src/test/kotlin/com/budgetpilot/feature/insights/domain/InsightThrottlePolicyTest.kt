package com.budgetpilot.feature.insights.domain

import assertk.assertThat
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.budgetpilot.feature.insights.domain.model.InsightType
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset

private class FakeInsightHistoryStore(
    private val lastShownAt: Instant?,
    private val shownTypesByMonth: Set<Pair<InsightType, String>> = emptySet(),
) : InsightHistoryStore {
    override suspend fun lastShownAt(): Instant? = lastShownAt

    override suspend fun wasShown(
        type: InsightType,
        month: String,
    ): Boolean = (type to month) in shownTypesByMonth
}

/** "Now" is fixed to 2026-07-15T12:00:00Z for every case here. */
class InsightThrottlePolicyTest {
    private val now = Instant.parse("2026-07-15T12:00:00Z")
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    @Test
    fun `empty history allows showing`() =
        runTest {
            val policy = InsightThrottlePolicy(FakeInsightHistoryStore(lastShownAt = null), clock)
            assertThat(policy.canShow(InsightType.BUDGET_EXCEEDED, "2026-07")).isTrue()
        }

    @Test
    fun `just under 48h since last insight blocks`() =
        runTest {
            val lastShownAt = now.minus(Duration.ofHours(48).minusSeconds(1))
            val policy = InsightThrottlePolicy(FakeInsightHistoryStore(lastShownAt = lastShownAt), clock)
            assertThat(policy.canShow(InsightType.BUDGET_EXCEEDED, "2026-07")).isFalse()
        }

    @Test
    fun `exactly 48h since last insight allows`() =
        runTest {
            val lastShownAt = now.minus(Duration.ofHours(48))
            val policy = InsightThrottlePolicy(FakeInsightHistoryStore(lastShownAt = lastShownAt), clock)
            assertThat(policy.canShow(InsightType.BUDGET_EXCEEDED, "2026-07")).isTrue()
        }

    @Test
    fun `just over 48h since last insight allows`() =
        runTest {
            val lastShownAt = now.minus(Duration.ofHours(48).plusSeconds(1))
            val policy = InsightThrottlePolicy(FakeInsightHistoryStore(lastShownAt = lastShownAt), clock)
            assertThat(policy.canShow(InsightType.BUDGET_EXCEEDED, "2026-07")).isTrue()
        }

    @Test
    fun `same type and month already shown blocks even after cooldown`() =
        runTest {
            val store =
                FakeInsightHistoryStore(
                    lastShownAt = now.minus(Duration.ofDays(10)),
                    shownTypesByMonth = setOf(InsightType.BUDGET_EXCEEDED to "2026-07"),
                )
            val policy = InsightThrottlePolicy(store, clock)
            assertThat(policy.canShow(InsightType.BUDGET_EXCEEDED, "2026-07")).isFalse()
        }

    @Test
    fun `same type but a different month is allowed`() =
        runTest {
            val store =
                FakeInsightHistoryStore(
                    lastShownAt = now.minus(Duration.ofDays(10)),
                    shownTypesByMonth = setOf(InsightType.BUDGET_EXCEEDED to "2026-06"),
                )
            val policy = InsightThrottlePolicy(store, clock)
            assertThat(policy.canShow(InsightType.BUDGET_EXCEEDED, "2026-07")).isTrue()
        }

    @Test
    fun `a different type in the same month is allowed`() =
        runTest {
            val store =
                FakeInsightHistoryStore(
                    lastShownAt = now.minus(Duration.ofDays(10)),
                    shownTypesByMonth = setOf(InsightType.BUDGET_EXCEEDED to "2026-07"),
                )
            val policy = InsightThrottlePolicy(store, clock)
            assertThat(policy.canShow(InsightType.CATEGORY_SPIKE, "2026-07")).isTrue()
        }
}
