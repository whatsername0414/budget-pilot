package com.budgetpilot.core.ai.data

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class RateLimiterTest {
    @Test
    fun `waits remaining interval before second call`() =
        runTest {
            var virtualNow = 0L
            val delays = mutableListOf<Long>()
            val rateLimiter =
                RateLimiter(
                    minIntervalMillis = 6_000L,
                    now = { virtualNow },
                    delayFn = { millis ->
                        delays += millis
                        virtualNow += millis
                    },
                )

            rateLimiter.withRateLimit { virtualNow += 500L }
            rateLimiter.withRateLimit { }

            assertThat(delays).containsExactly(5_500L)
        }

    @Test
    fun `does not wait when the interval has already elapsed`() =
        runTest {
            var virtualNow = 0L
            val delays = mutableListOf<Long>()
            val rateLimiter =
                RateLimiter(
                    minIntervalMillis = 6_000L,
                    now = { virtualNow },
                    delayFn = { millis ->
                        delays += millis
                        virtualNow += millis
                    },
                )

            rateLimiter.withRateLimit { }
            virtualNow += 10_000L
            rateLimiter.withRateLimit { }

            assertThat(delays).isEmpty()
        }

    @Test
    fun `first call never waits`() =
        runTest {
            val delays = mutableListOf<Long>()
            val rateLimiter =
                RateLimiter(
                    minIntervalMillis = 6_000L,
                    now = { 0L },
                    delayFn = { millis -> delays += millis },
                )

            val result = rateLimiter.withRateLimit { "done" }

            assertThat(result).isEqualTo("done")
            assertThat(delays).isEmpty()
        }
}
