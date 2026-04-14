package com.yogesh.appfence.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

/**
 * Repository wrapping the Room DAO.
 * Provides a clean API for ViewModels and services to interact with app rules.
 */
class AppRepository(context: Context) {

    private val dao: AppRuleDao = AppRuleDatabase.getInstance(context).appRuleDao()

    /** Observe all rules reactively. */
    val allRules: Flow<List<AppRule>> = dao.getAllRules()

    /** Get a snapshot of all rules (for VPN service). */
    suspend fun getAllRulesSnapshot(): List<AppRule> = dao.getAllRulesSnapshot()

    /** Get packages blocked on Wi-Fi. */
    suspend fun getBlockedForWifi(): List<String> = dao.getBlockedForWifi()

    /** Get packages blocked on mobile data. */
    suspend fun getBlockedForMobile(): List<String> = dao.getBlockedForMobile()

    /** Get a single rule by package name. */
    suspend fun getByPackageName(packageName: String): AppRule? =
        dao.getByPackageName(packageName)

    /** Toggle Wi-Fi access for a specific app. Creates a new rule if none exists. */
    suspend fun toggleWifi(packageName: String, uid: Int, allowed: Boolean) {
        val existing = dao.getByPackageName(packageName)
        val rule = existing?.copy(wifiAllowed = allowed)
            ?: AppRule(packageName = packageName, uid = uid, wifiAllowed = allowed)
        dao.insertOrUpdate(rule)
    }

    /** Toggle mobile data access for a specific app. Creates a new rule if none exists. */
    suspend fun toggleMobile(packageName: String, uid: Int, allowed: Boolean) {
        val existing = dao.getByPackageName(packageName)
        val rule = existing?.copy(mobileAllowed = allowed)
            ?: AppRule(packageName = packageName, uid = uid, mobileAllowed = allowed)
        dao.insertOrUpdate(rule)
    }

    /** Insert or update a rule directly. */
    suspend fun insertOrUpdate(rule: AppRule) = dao.insertOrUpdate(rule)

    /** Insert or update multiple rules. */
    suspend fun insertOrUpdateAll(rules: List<AppRule>) = dao.insertOrUpdateAll(rules)

    /** Delete a rule. */
    suspend fun deleteByPackageName(packageName: String) = dao.deleteByPackageName(packageName)
}
