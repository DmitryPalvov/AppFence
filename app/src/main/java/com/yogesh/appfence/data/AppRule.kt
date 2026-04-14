package com.yogesh.appfence.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity representing per-app network access rules.
 * Each installed app can have independent Wi-Fi and mobile data access control.
 */
@Entity(tableName = "app_rules")
data class AppRule(
    @PrimaryKey
    val packageName: String,
    val uid: Int,
    val wifiAllowed: Boolean = true,
    val mobileAllowed: Boolean = true
)
