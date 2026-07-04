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

val LightExtendedColors = ExtendedColors(
    warning = WarningLight,
    onWarning = OnWarningLight,
    warningContainer = WarningContainerLight,
    onWarningContainer = OnWarningContainerLight,
)

val DarkExtendedColors = ExtendedColors(
    warning = WarningDark,
    onWarning = OnWarningDark,
    warningContainer = WarningContainerDark,
    onWarningContainer = OnWarningContainerDark,
)

/**
 * Backing store for [BudgetPilotTheme.extendedColors]. The app only ever has
 * one active theme at a time, so a Snapshot-backed holder kept in sync via
 * `SideEffect` gives the same observable, ambient-style read that a custom
 * CompositionLocal would, without introducing one.
 */
internal var currentExtendedColors = mutableStateOf(LightExtendedColors)
