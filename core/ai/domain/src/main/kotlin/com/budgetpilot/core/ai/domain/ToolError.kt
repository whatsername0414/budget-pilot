package com.budgetpilot.core.ai.domain

import com.budgetpilot.core.domain.Error

data class ToolError(
    val message: String,
) : Error
