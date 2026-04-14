package com.yogesh.appfence.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for app_rules table.
 * Provides reactive queries via Flow and suspend functions for writes.
 */
@Dao
interface AppRuleDao {

    /** Observe all rules reactively. */
    @Query("SELECT * FROM app_rules ORDER BY packageName ASC")
    fun getAllRules(): Flow<List<AppRule>>

    /** Get a single rule by package name (non-reactive, for VPN service). */
    @Query("SELECT * FROM app_rules WHERE packageName = :packageName LIMIT 1")
    suspend fun getByPackageName(packageName: String): AppRule?

    /** Get all packages blocked on Wi-Fi (for VPN tunnel rebuild). */
    @Query("SELECT packageName FROM app_rules WHERE wifiAllowed = 0")
    suspend fun getBlockedForWifi(): List<String>

    /** Get all packages blocked on mobile data (for VPN tunnel rebuild). */
    @Query("SELECT packageName FROM app_rules WHERE mobileAllowed = 0")
    suspend fun getBlockedForMobile(): List<String>

    /** Get all rules as a plain list (non-reactive, for boot/VPN). */
    @Query("SELECT * FROM app_rules")
    suspend fun getAllRulesSnapshot(): List<AppRule>

    /** Insert or update a rule. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(rule: AppRule)

    /** Insert or update multiple rules at once. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateAll(rules: List<AppRule>)

    /** Delete a rule by package name. */
    @Query("DELETE FROM app_rules WHERE packageName = :packageName")
    suspend fun deleteByPackageName(packageName: String)
}
