package com.budgetpilot.feature.capture.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
data object CaptureRoute

/**
 * Stub destination — the real `ConfirmExpenseScreen` (editable fields,
 * `ConfidenceField`, extraction wiring) lands in a later Phase 2 step.
 */
@Serializable
data class ConfirmExpenseRoute(
    val imagePath: String,
)
