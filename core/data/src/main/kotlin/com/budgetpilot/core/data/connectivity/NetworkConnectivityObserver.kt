package com.budgetpilot.core.data.connectivity

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Tracks default-network availability via [ConnectivityManager.NetworkCallback]
 * rather than a one-off `activeNetwork` query, so callers reading [isOnline]
 * (e.g. `ExtractionRouter`, wired as a plain `ConnectivityObserver` lambda by
 * whichever module needs it) always see the current state without querying
 * the system service on every call.
 */
class NetworkConnectivityObserver(
    context: Context,
) {
    private val online = AtomicBoolean(false)

    init {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        val request = NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build()
        connectivityManager.registerNetworkCallback(
            request,
            object : ConnectivityManager.NetworkCallback() {
                override fun onAvailable(network: Network) {
                    online.set(true)
                }

                override fun onLost(network: Network) {
                    online.set(false)
                }
            },
        )
    }

    fun isOnline(): Boolean = online.get()
}
