package com.budgetpilot.feature.ask.presentation.navigation

import kotlinx.serialization.Serializable

@Serializable
data class AskRoute(
    val prefillQuestion: String? = null,
)
