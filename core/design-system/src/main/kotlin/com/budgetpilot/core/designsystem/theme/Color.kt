package com.budgetpilot.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// Dark theme — deep navy surfaces (not pure black) per PLAN.md §4.1. The app
// is dark-only (CLAUDE.md §10); these are the only color tokens.
val Primary = Color(0xFF8FB0FF)
val OnPrimary = Color(0xFF002C71)
val PrimaryContainer = Color(0xFF00419E)
val OnPrimaryContainer = Color(0xFFD6E2FF)

val Secondary = Color(0xFFA8C7FF)
val OnSecondary = Color(0xFF00315B)
val SecondaryContainer = Color(0xFF1C4A82)
val OnSecondaryContainer = Color(0xFFD9E6FF)

val Tertiary = Color(0xFF5BD6A9)
val OnTertiary = Color(0xFF00382A)
val TertiaryContainer = Color(0xFF00513C)
val OnTertiaryContainer = Color(0xFF79F2C0)

val ErrorColor = Color(0xFFFFB4AB)
val OnError = Color(0xFF690005)
val ErrorContainer = Color(0xFF93000A)
val OnErrorContainer = Color(0xFFFFDAD6)

val Background = Color(0xFF0F172A)
val OnBackground = Color(0xFFE2E8F0)
val Surface = Color(0xFF101A34)
val OnSurface = Color(0xFFE2E8F0)

val Warning = Color(0xFFFFC66E)
val OnWarning = Color(0xFF452B00)
val WarningContainer = Color(0xFF613F00)
val OnWarningContainer = Color(0xFFFFDDAF)

// Slate-tinted neutral roles (DESIGN-SPEC.md §1.2) — M3's warm-gray baseline
// clashes with this theme's slate/navy palette.
val OnSurfaceVariant = Color(0xFF94A3B8)
val Outline = Color(0xFF334155)
val OutlineVariant = Color(0xFF1E293B)
val SurfaceVariant = Color(0xFF16213C)

// Category identity colors (DESIGN-SPEC.md §1.3) — identity, not status.
val CategoryFood = Color(0xFFF87171)
val CategoryTransport = Color(0xFF60A5FA)
val CategoryBills = Color(0xFFA78BFA)
val CategoryGroceries = Color(0xFF34D399)
val CategoryShopping = Color(0xFFF472B6)
val CategoryHealth = Color(0xFF22D3EE)
val CategoryEntertainment = Color(0xFFFBBF24)
val CategoryOther = Color(0xFF94A3B8)
