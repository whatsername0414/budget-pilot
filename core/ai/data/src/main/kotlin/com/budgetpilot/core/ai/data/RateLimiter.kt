package com.budgetpilot.core.ai.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Mutex-guarded minimum-interval gate for the free-tier Gemini API (PLAN.md §5.2). Callers are
 * fully serialized — each [withRateLimit] call holds the lock for its entire duration, so
 * concurrent callers queue rather than race past the gate — and each call waits out whatever is
 * left of [minIntervalMillis] since the previous call *started*.
 *
 * [now] and [delayFn] are injectable so tests can run this with a fully virtual, instant clock
 * instead of real or coroutine-virtual time.
 */
class RateLimiter(
    private val minIntervalMillis: Long = DEFAULT_MIN_INTERVAL_MILLIS,
    private val now: () -> Long = System::currentTimeMillis,
    private val delayFn: suspend (Long) -> Unit = ::delay,
) {
    private val mutex = Mutex()
    private var lastCallAt: Long? = null

    suspend fun <T> withRateLimit(block: suspend () -> T): T =
        mutex.withLock {
            val lastAt = lastCallAt
            if (lastAt != null) {
                val waitMillis = minIntervalMillis - (now() - lastAt)
                if (waitMillis > 0) delayFn(waitMillis)
            }
            lastCallAt = now()
            block()
        }

    companion object {
        const val DEFAULT_MIN_INTERVAL_MILLIS = 6_000L
    }
}
