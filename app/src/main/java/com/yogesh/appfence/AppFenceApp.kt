package com.yogesh.appfence

import android.app.Application

/**
 * Application class for AppFence.
 * Initializes any app-wide singletons at startup.
 */
class AppFenceApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Room database is lazily initialized on first access.
        // No additional initialization needed here.
    }
}
