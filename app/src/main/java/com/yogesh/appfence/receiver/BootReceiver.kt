package com.yogesh.appfence.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.yogesh.appfence.vpn.AppFenceVpnService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Broadcast receiver that starts the VPN service on device boot
 * if the "start on boot" setting is enabled.
 *
 * Note: The VPN permission must have been previously granted by the user.
 * If not, the VPN tunnel will fail to establish (establish() returns null),
 * but the service will still run and can be reconfigured from the UI.
 */
class BootReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON"
        ) return

        Log.i(TAG, "Boot completed — checking if VPN should auto-start")

        // Check the "start on boot" preference
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = context.getSharedPreferences("netguard_prefs", Context.MODE_PRIVATE)
                val startOnBoot = prefs.getBoolean("start_on_boot", false)

                if (startOnBoot) {
                    Log.i(TAG, "Auto-starting VPN service")
                    val serviceIntent = Intent(context, AppFenceVpnService::class.java).apply {
                        action = AppFenceVpnService.ACTION_START
                    }
                    context.startForegroundService(serviceIntent)
                } else {
                    Log.i(TAG, "Start on boot is disabled — skipping")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in boot receiver", e)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
