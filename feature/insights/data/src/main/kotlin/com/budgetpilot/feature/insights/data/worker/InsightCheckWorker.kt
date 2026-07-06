package com.budgetpilot.feature.insights.data.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker.Result
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import com.budgetpilot.feature.insights.data.InsightCheckResult
import com.budgetpilot.feature.insights.data.InsightCheckUseCase
import com.budgetpilot.feature.insights.data.notification.InsightNotifier
import kotlinx.coroutines.CancellationException
import java.time.Duration

/**
 * Periodic background check (PLAN.md §6 Phase 5): delegates to [InsightCheckUseCase] and, on a
 * newly stored insight, posts a notification via [InsightNotifier]. A throttled or absent
 * candidate is a normal, successful run — only an unexpected exception asks WorkManager to retry.
 */
class InsightCheckWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val useCase: InsightCheckUseCase,
    private val notifier: InsightNotifier,
) : CoroutineWorker(context, workerParams) {
    @Suppress("SwallowedException")
    override suspend fun doWork(): Result =
        try {
            when (val result = useCase.check()) {
                is InsightCheckResult.Stored -> {
                    notifier.notify(result.insight)
                    Result.success()
                }
                InsightCheckResult.Throttled, InsightCheckResult.NoCandidate -> Result.success()
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.retry()
        }

    companion object {
        const val WORK_NAME = "insight_check"
        private val CHECK_INTERVAL: Duration = Duration.ofHours(12)

        fun periodicRequest(): PeriodicWorkRequest =
            PeriodicWorkRequestBuilder<InsightCheckWorker>(CHECK_INTERVAL)
                .setConstraints(Constraints.Builder().setRequiresBatteryNotLow(true).build())
                .build()
    }
}
