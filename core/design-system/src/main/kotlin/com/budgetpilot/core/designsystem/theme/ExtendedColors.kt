package com.budgetpilot.core.designsystem.theme

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color

/** M3's [androidx.compose.material3.ColorScheme] has no "warning" role. */
data class ExtendedColors(
    val warning: Color,
    val onWarning: Color,
    val warningContainer: Color,
    val onWarningContainer: Color,
)

val AppExtendedColors =
    ExtendedColors(
        warning = Warning,
        onWarning = OnWarning,
        warningContainer = WarningContainer,
        onWarningContainer = OnWarningContainer,
    )

/**
 * Backing store for [BudgetPilotTheme.extendedColors]. The app only ever has
 * one active theme at a time, so a Snapshot-backed holder kept in sync via
 * `SideEffect` gives the same observable, ambient-style read that a custom
 * CompositionLocal would, without introducing one.
 */
internal var currentExtendedColors = mutableStateOf(AppExtendedColors)
