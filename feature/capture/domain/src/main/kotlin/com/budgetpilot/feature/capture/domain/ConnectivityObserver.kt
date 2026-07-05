package com.budgetpilot.feature.capture.domain

fun interface ConnectivityObserver {
    fun isOnline(): Boolean
}
