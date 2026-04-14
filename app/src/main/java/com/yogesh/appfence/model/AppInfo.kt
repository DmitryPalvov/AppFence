package com.yogesh.appfence.model

import android.graphics.drawable.Drawable

/**
 * UI model representing an installed application with its metadata.
 * Used in the app list screen to display app info alongside network rules.
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable?,
    val isSystemApp: Boolean,
    val uid: Int
)

/**
 * Enum representing the current network access status for an app.
 * Used to display a colored badge in the app list.
 */
enum class AppStatus(val label: String) {
    ALLOWED("Allowed"),
    WIFI_ONLY("Wi-Fi Only"),
    DATA_ONLY("Data Only"),
    BLOCKED("Blocked")
}

/**
 * Combined UI state for an app row: app metadata + current rules.
 */
data class AppUiState(
    val appInfo: AppInfo,
    val wifiAllowed: Boolean = true,
    val mobileAllowed: Boolean = true
) {
    val status: AppStatus
        get() = when {
            wifiAllowed && mobileAllowed -> AppStatus.ALLOWED
            wifiAllowed && !mobileAllowed -> AppStatus.WIFI_ONLY
            !wifiAllowed && mobileAllowed -> AppStatus.DATA_ONLY
            else -> AppStatus.BLOCKED
        }
}
