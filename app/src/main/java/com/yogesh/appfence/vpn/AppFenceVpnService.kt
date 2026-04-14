package com.yogesh.appfence.vpn

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.core.app.NotificationCompat
import com.yogesh.appfence.R
import com.yogesh.appfence.data.AppRepository
import com.yogesh.appfence.data.AppRuleDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Local VPN service that enforces per-app network blocking.
 *
 * Strategy:
 * - All traffic is routed through the VPN tunnel by default.
 * - Apps that are ALLOWED on the current network type are added via
 *   addDisallowedApplication(), meaning they bypass the tunnel entirely.
 * - Apps NOT in the disallowed list have their traffic enter the tunnel,
 *   where it is simply NOT forwarded — effectively blocking them.
 * - No packets are read, parsed, or sent to any remote server.
 *   This is purely a local packet-drop mechanism.
 *
 * When the network type changes (Wi-Fi ↔ cellular), the tunnel is rebuilt
 * with the appropriate allowed/blocked app lists from the Room database.
 */
class AppFenceVpnService : VpnService() {

    companion object {
        private const val TAG = "NetGuardVpn"
        private const val CHANNEL_ID = "netguard_vpn_channel"
        private const val NOTIFICATION_ID = 1
        const val ACTION_START = "com.yogesh.appfence.vpn.START"
        const val ACTION_STOP = "com.yogesh.appfence.vpn.STOP"
        const val ACTION_REBUILD = "com.yogesh.appfence.vpn.REBUILD"

        /** Check if the VPN is currently running. */
        @Volatile
        var isRunning: Boolean = false
            private set
    }

    private var vpnInterface: ParcelFileDescriptor? = null
    private lateinit var repository: AppRepository
    private lateinit var networkMonitor: NetworkMonitor
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var networkMonitorJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        repository = AppRepository(applicationContext)
        networkMonitor = NetworkMonitor(applicationContext)
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                stopVpn()
                stopSelf()
                return START_NOT_STICKY
            }
            ACTION_REBUILD -> {
                // Rebuild the tunnel with updated rules
                serviceScope.launch {
                    rebuildTunnel()
                }
                return START_STICKY
            }
            ACTION_START, null -> {
                startVpn()
                return START_STICKY
            }
            else -> return START_NOT_STICKY
        }
    }

    /**
     * Start the VPN: show foreground notification, build tunnel, monitor network changes.
     */
    private fun startVpn() {
        Log.i(TAG, "Starting VPN service")
        startForeground(NOTIFICATION_ID, buildNotification())
        isRunning = true

        // Build the initial tunnel
        serviceScope.launch {
            rebuildTunnel()
        }

        // Monitor network type changes and rebuild tunnel when switching Wi-Fi ↔ mobile
        networkMonitorJob = serviceScope.launch {
            networkMonitor.networkType.collectLatest { networkType ->
                Log.i(TAG, "Network changed to: $networkType — rebuilding tunnel")
                rebuildTunnel()
            }
        }
    }

    /**
     * Rebuild the VPN tunnel with the current blocked app list for the active network type.
     *
     * This is the core blocking mechanism:
     * 1. Query Room for which apps are blocked on the current network type.
     * 2. Close the existing tunnel (if any).
     * 3. Build a new tunnel where ALLOWED apps are disallowed from the VPN
     *    (they bypass it and use the real network directly).
     * 4. Blocked apps are NOT disallowed, so their traffic enters the tunnel
     *    and gets silently dropped (we never read from the tunnel fd).
     */
    private suspend fun rebuildTunnel() {
        try {
            // Close existing tunnel
            vpnInterface?.close()
            vpnInterface = null

            val currentNetwork = networkMonitor.networkType.value
            if (currentNetwork == NetworkType.NONE) {
                Log.i(TAG, "No active network — skipping tunnel build")
                return
            }

            // Get the list of BLOCKED packages for the current network type
            val blockedPackages = when (currentNetwork) {
                NetworkType.WIFI -> repository.getBlockedForWifi()
                NetworkType.MOBILE -> repository.getBlockedForMobile()
                NetworkType.NONE -> emptyList()
            }

            Log.i(TAG, "Building tunnel: ${blockedPackages.size} apps blocked on $currentNetwork")

            // Build the VPN tunnel
            val builder = Builder()
                .setSession("AppFence")
                .addAddress("10.1.10.1", 32)       // Local tunnel IPv4 address
                .addRoute("0.0.0.0", 0)             // Capture all IPv4 traffic
                .addAddress("fd00:1:2::1", 128)      // Local tunnel IPv6 address
                .addRoute("::", 0)                    // Capture all IPv6 traffic
                .setBlocking(false)                   // Non-blocking mode
                .setMtu(1500)

            // -------------------------------------------------------------------
            // KEY INSIGHT: We use addDisallowedApplication to let ALLOWED apps
            // bypass the tunnel. Everything else gets caught and silently dropped.
            //
            // We need to know ALL installed packages, then disallow (exclude)
            // every package that is NOT in the blocked list.
            // -------------------------------------------------------------------

            val pm = packageManager
            val allApps = pm.getInstalledApplications(0)

            // Always exclude our own app to prevent a routing loop
            try {
                builder.addDisallowedApplication(packageName)
            } catch (e: Exception) {
                Log.w(TAG, "Could not exclude own package: ${e.message}")
            }

            // Exclude (disallow from VPN) all apps that are NOT blocked
            val blockedSet = blockedPackages.toSet()
            for (app in allApps) {
                if (app.packageName == packageName) continue // Already excluded
                if (app.packageName !in blockedSet) {
                    try {
                        builder.addDisallowedApplication(app.packageName)
                    } catch (e: Exception) {
                        // Package may have been uninstalled between query and here
                        Log.w(TAG, "Could not exclude ${app.packageName}: ${e.message}")
                    }
                }
            }

            // If no apps are blocked, still establish (but exclude everything)
            // so the VPN stays "running" from a UI perspective
            vpnInterface = builder.establish()

            if (vpnInterface != null) {
                Log.i(TAG, "VPN tunnel established successfully")
            } else {
                Log.e(TAG, "VPN tunnel establishment returned null — user may not have granted permission")
            }

        } catch (e: Exception) {
            Log.e(TAG, "Error rebuilding tunnel", e)
        }
    }

    /**
     * Stop the VPN: close tunnel, unregister monitors, update state.
     */
    private fun stopVpn() {
        Log.i(TAG, "Stopping VPN service")
        networkMonitorJob?.cancel()
        vpnInterface?.close()
        vpnInterface = null
        isRunning = false
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onRevoke() {
        // Called when the user revokes VPN permission from system settings
        Log.i(TAG, "VPN permission revoked by user")
        stopVpn()
        stopSelf()
        super.onRevoke()
    }

    override fun onDestroy() {
        stopVpn()
        networkMonitor.unregister()
        serviceScope.cancel()
        super.onDestroy()
    }

    // ─── Notification ───────────────────────────────────────────────────────────

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            getString(R.string.vpn_notification_channel),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Shows when AppFence VPN is active"
            setShowBadge(false)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun buildNotification() =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.vpn_notification_title))
            .setContentText(getString(R.string.vpn_notification_text))
            .setSmallIcon(android.R.drawable.ic_lock_lock)
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(
                PendingIntent.getActivity(
                    this,
                    0,
                    packageManager.getLaunchIntentForPackage(packageName),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .addAction(
                android.R.drawable.ic_menu_close_clear_cancel,
                "Stop",
                PendingIntent.getService(
                    this,
                    0,
                    Intent(this, AppFenceVpnService::class.java).apply {
                        action = ACTION_STOP
                    },
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            .build()
}
