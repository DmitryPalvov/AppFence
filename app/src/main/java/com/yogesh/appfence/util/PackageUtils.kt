package com.yogesh.appfence.util

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.yogesh.appfence.model.AppInfo

/**
 * Utility to load all installed applications from the device.
 * Uses PackageManager with MATCH_ALL to discover user and system apps.
 */
object PackageUtils {

    /**
     * Load all installed apps and map them to [AppInfo] UI models.
     * Sorted alphabetically by app name.
     *
     * @param context Application context for accessing PackageManager.
     * @return Sorted list of all installed applications.
     */
    fun getInstalledApps(context: Context): List<AppInfo> {
        val pm = context.packageManager
        val apps = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        return apps.mapNotNull { appInfo ->
            try {
                AppInfo(
                    packageName = appInfo.packageName,
                    appName = pm.getApplicationLabel(appInfo).toString(),
                    icon = pm.getApplicationIcon(appInfo),
                    isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                    uid = appInfo.uid
                )
            } catch (e: Exception) {
                // Skip apps that can't be queried
                null
            }
        }.sortedBy { it.appName.lowercase() }
    }
}
