package com.budgetpilot.feature.capture.domain

fun interface CloudAiPolicy {
    fun isCloudAiAllowed(): Boolean
}
