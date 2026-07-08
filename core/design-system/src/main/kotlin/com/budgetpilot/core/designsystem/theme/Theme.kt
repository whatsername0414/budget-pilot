package com.budgetpilot.core.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect

private val AppColorScheme: ColorScheme =
    darkColorScheme(
        primary = Primary,
        onPrimary = OnPrimary,
        primaryContainer = PrimaryContainer,
        onPrimaryContainer = OnPrimaryContainer,
        secondary = Secondary,
        onSecondary = OnSecondary,
        secondaryContainer = SecondaryContainer,
        onSecondaryContainer = OnSecondaryContainer,
        tertiary = Tertiary,
        onTertiary = OnTertiary,
        tertiaryContainer = TertiaryContainer,
        onTertiaryContainer = OnTertiaryContainer,
        error = ErrorColor,
        onError = OnError,
        errorContainer = ErrorContainer,
        onErrorContainer = OnErrorContainer,
        background = Background,
        onBackground = OnBackground,
        surface = Surface,
        onSurface = OnSurface,
        surfaceVariant = SurfaceVariant,
        onSurfaceVariant = OnSurfaceVariant,
        outline = Outline,
        outlineVariant = OutlineVariant,
        // M3 defaults (Card, BottomSheet, SearchBar, menus, ...) read these
        // surfaceContainer* roles instead of `surface` directly; left unset
        // they fall back to M3's stock gray tokens, clashing with the
        // slate/navy palette (same class of issue as the 2026-07-05 fix for
        // onSurfaceVariant/outline/outlineVariant/surfaceVariant). Pinned to
        // existing tokens only, no new palette colors.
        surfaceContainerLowest = Background,
        surfaceContainerLow = Background,
        surfaceContainer = Surface,
        surfaceContainerHigh = Surface,
        surfaceContainerHighest = Surface,
        surfaceDim = Background,
        surfaceBright = SurfaceVariant,
    )

/** Namespace for design tokens that M3's [MaterialTheme] has no slot for. */
object BudgetPilotTheme {
    val extendedColors: ExtendedColors
        @Composable get() = currentExtendedColors.value

    /** Whether the system "Remove animations" accessibility setting is on. */
    val reducedMotionEnabled: Boolean
        @Composable get() = currentReducedMotionEnabled.value
}

/**
 * App theme: dark-only M3 color scheme and IBM Plex Sans typography from
 * PLAN.md §4.1–4.2. Dynamic color is deferred to Phase 7.
 */
@Composable
fun BudgetPilotTheme(content: @Composable () -> Unit) {
    SideEffect { currentExtendedColors.value = AppExtendedColors }
    ObserveReducedMotionSetting()

    MaterialTheme(
        colorScheme = AppColorScheme,
        typography = BudgetPilotTypography,
        content = content,
    )
}
