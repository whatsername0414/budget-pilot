package com.budgetpilot.feature.insights.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.budgetpilot.feature.insights.data.R
import com.budgetpilot.feature.insights.domain.model.Insight

private const val CHANNEL_ID = "insights"
private const val NOTIFICATION_ID = 1001
private const val PENDING_INTENT_REQUEST_CODE = 0

/**
 * Posts a single polite, DEFAULT-priority notification for a newly stored [Insight], deep-linking
 * into the app's launcher activity (which opens on Home, PLAN.md §6 Phase 5) via the package's own
 * launch intent — this module can't reference `:app`'s `MainActivity` directly. Silently does
 * nothing if the POST_NOTIFICATIONS permission isn't granted; requesting that permission is a
 * later step, not this one.
 */
class InsightNotifier(
    private val context: Context,
) {
    fun notify(insight: Insight) {
        val permissionGranted =
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
        if (!permissionGranted) return
        ensureChannel()

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return
        val pendingIntent =
            PendingIntent.getActivity(
                context,
                PENDING_INTENT_REQUEST_CODE,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )

        val notification =
            NotificationCompat
                .Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification_insight)
                .setContentTitle(context.getString(R.string.insight_notification_title))
                .setContentText(insight.message)
                .setStyle(NotificationCompat.BigTextStyle().bigText(insight.message))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    private fun ensureChannel() {
        val channel =
            NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.insight_notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT,
            ).apply {
                description = context.getString(R.string.insight_notification_channel_description)
            }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }
}
