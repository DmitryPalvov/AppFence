package com.yogesh.appfence.vpn

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Represents the current device network type.
 */
enum class NetworkType {
    WIFI,
    MOBILE,
    NONE
}

/**
 * Monitors the device's active network connection and exposes the current
 * network type as a reactive StateFlow.
 *
 * Used by both the VPN service (to apply correct per-network rules) and
 * the UI (to show current network status).
 */
class NetworkMonitor(context: Context) {

    private val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _networkType = MutableStateFlow(getCurrentNetworkType())
    val networkType: StateFlow<NetworkType> = _networkType.asStateFlow()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onCapabilitiesChanged(
            network: Network,
            capabilities: NetworkCapabilities
        ) {
            _networkType.value = determineType(capabilities)
        }

        override fun onLost(network: Network) {
            // Re-evaluate — there may be another active network
            _networkType.value = getCurrentNetworkType()
        }

        override fun onAvailable(network: Network) {
            _networkType.value = getCurrentNetworkType()
        }
    }

    init {
        // Register for default network changes
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, networkCallback)
    }

    /**
     * Determine the current network type by querying ConnectivityManager directly.
     */
    private fun getCurrentNetworkType(): NetworkType {
        val activeNetwork = connectivityManager.activeNetwork ?: return NetworkType.NONE
        val capabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            ?: return NetworkType.NONE
        return determineType(capabilities)
    }

    /**
     * Map NetworkCapabilities to our simplified NetworkType enum.
     */
    private fun determineType(capabilities: NetworkCapabilities): NetworkType {
        return when {
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> NetworkType.WIFI
            capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> NetworkType.MOBILE
            else -> NetworkType.NONE
        }
    }

    /**
     * Unregister the callback when no longer needed.
     */
    fun unregister() {
        try {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        } catch (_: Exception) {
            // Already unregistered
        }
    }
}
