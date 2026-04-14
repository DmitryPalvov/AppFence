package com.yogesh.appfence.ui.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.VpnService
import androidx.lifecycle.AndroidViewModel
import com.yogesh.appfence.vpn.AppFenceVpnService
import com.yogesh.appfence.vpn.NetworkMonitor
import com.yogesh.appfence.vpn.NetworkType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for the Settings screen.
 * Manages VPN start/stop, preferences, and network status display.
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("netguard_prefs", Context.MODE_PRIVATE)
    val networkMonitor = NetworkMonitor(application)

    // ─── VPN Status ────────────────────────────────────────────────────────

    private val _vpnRunning = MutableStateFlow(AppFenceVpnService.isRunning)
    val vpnRunning: StateFlow<Boolean> = _vpnRunning.asStateFlow()

    fun refreshVpnStatus() {
        _vpnRunning.value = AppFenceVpnService.isRunning
    }

    // ─── Preferences ───────────────────────────────────────────────────────

    private val _blockNewApps = MutableStateFlow(prefs.getBoolean("block_new_apps", false))
    val blockNewApps: StateFlow<Boolean> = _blockNewApps.asStateFlow()

    private val _startOnBoot = MutableStateFlow(prefs.getBoolean("start_on_boot", false))
    val startOnBoot: StateFlow<Boolean> = _startOnBoot.asStateFlow()

    fun setBlockNewApps(enabled: Boolean) {
        prefs.edit().putBoolean("block_new_apps", enabled).apply()
        _blockNewApps.value = enabled
    }

    fun setStartOnBoot(enabled: Boolean) {
        prefs.edit().putBoolean("start_on_boot", enabled).apply()
        _startOnBoot.value = enabled
    }

    // ─── Onboarding ────────────────────────────────────────────────────────

    private val _onboardingCompleted = MutableStateFlow(
        prefs.getBoolean("onboarding_completed", false)
    )
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    fun completeOnboarding() {
        prefs.edit().putBoolean("onboarding_completed", true).apply()
        _onboardingCompleted.value = true
    }

    // ─── VPN Control ───────────────────────────────────────────────────────

    /**
     * Check if VPN permission has been granted.
     * Returns the prepare intent if permission is needed, null if already granted.
     */
    fun getVpnPrepareIntent(): Intent? {
        return VpnService.prepare(getApplication())
    }

    /**
     * Start the VPN service.
     */
    fun startVpn() {
        val context = getApplication<Application>()
        val intent = Intent(context, AppFenceVpnService::class.java).apply {
            action = AppFenceVpnService.ACTION_START
        }
        context.startForegroundService(intent)
        _vpnRunning.value = true
    }

    /**
     * Stop the VPN service.
     */
    fun stopVpn() {
        val context = getApplication<Application>()
        val intent = Intent(context, AppFenceVpnService::class.java).apply {
            action = AppFenceVpnService.ACTION_STOP
        }
        context.startService(intent)
        _vpnRunning.value = false
    }

    override fun onCleared() {
        networkMonitor.unregister()
        super.onCleared()
    }
}
