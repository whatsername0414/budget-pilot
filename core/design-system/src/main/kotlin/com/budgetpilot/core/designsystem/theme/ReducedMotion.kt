package com.budgetpilot.core.designsystem.theme

import android.content.ContentResolver
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext

/**
 * Backing store for [BudgetPilotTheme.reducedMotionEnabled] — same
 * Snapshot-backed-holder pattern as [currentExtendedColors] rather than a
 * custom CompositionLocal.
 */
internal var currentReducedMotionEnabled = mutableStateOf(false)

private fun isReducedMotionEnabled(contentResolver: ContentResolver): Boolean =
    Settings.Global.getFloat(contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) == 0f

/**
 * Keeps [currentReducedMotionEnabled] in sync with the system-wide "Remove
 * animations" accessibility setting. Compose's animation APIs don't read
 * this setting automatically (unlike View-based animators), so components
 * with a continuous/looping animation (e.g. the loading shimmer) must check
 * it themselves.
 */
@Composable
internal fun ObserveReducedMotionSetting() {
    val contentResolver = LocalContext.current.contentResolver
    DisposableEffect(contentResolver) {
        currentReducedMotionEnabled.value = isReducedMotionEnabled(contentResolver)
        val observer =
            object : ContentObserver(Handler(Looper.getMainLooper())) {
                override fun onChange(selfChange: Boolean) {
                    currentReducedMotionEnabled.value = isReducedMotionEnabled(contentResolver)
                }
            }
        contentResolver.registerContentObserver(
            Settings.Global.getUriFor(Settings.Global.ANIMATOR_DURATION_SCALE),
            false,
            observer,
        )
        onDispose { contentResolver.unregisterContentObserver(observer) }
    }
}
