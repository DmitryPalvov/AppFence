package com.yogesh.appfence.ui.viewmodel

import android.app.Application
import android.content.Intent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yogesh.appfence.data.AppRepository
import com.yogesh.appfence.data.AppRule
import com.yogesh.appfence.model.AppInfo
import com.yogesh.appfence.model.AppUiState
import com.yogesh.appfence.ui.components.FilterOption
import com.yogesh.appfence.util.PackageUtils
import com.yogesh.appfence.vpn.AppFenceVpnService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the main app list screen.
 * Merges the installed app list with persisted rules from Room,
 * applies search and filter, and exposes the result as StateFlow.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = AppRepository(application)

    // ─── Search & Filter State ──────────────────────────────────────────────
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(FilterOption.ALL)
    val selectedFilter: StateFlow<FilterOption> = _selectedFilter.asStateFlow()

    // ─── Installed Apps ─────────────────────────────────────────────────────
    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // ─── Combined UI State ──────────────────────────────────────────────────

    /**
     * Reactive combination of installed apps, Room rules, search, and filter.
     * This is the single source of truth for the main screen list.
     */
    val appList: StateFlow<List<AppUiState>> = combine(
        _installedApps,
        repository.allRules,
        _searchQuery,
        _selectedFilter
    ) { apps, rules, query, filter ->
        val rulesMap = rules.associateBy { it.packageName }
        val prefs = getApplication<Application>()
            .getSharedPreferences("netguard_prefs", 0)
        val blockNewByDefault = prefs.getBoolean("block_new_apps", false)

        apps.map { appInfo ->
            val rule = rulesMap[appInfo.packageName]
            AppUiState(
                appInfo = appInfo,
                wifiAllowed = rule?.wifiAllowed ?: !blockNewByDefault,
                mobileAllowed = rule?.mobileAllowed ?: !blockNewByDefault
            )
        }
            .filter { state ->
                // Apply search filter
                if (query.isBlank()) true
                else state.appInfo.appName.contains(query, ignoreCase = true) ||
                        state.appInfo.packageName.contains(query, ignoreCase = true)
            }
            .filter { state ->
                // Apply category filter
                when (filter) {
                    FilterOption.ALL -> true
                    FilterOption.USER -> !state.appInfo.isSystemApp
                    FilterOption.SYSTEM -> state.appInfo.isSystemApp
                    FilterOption.BLOCKED -> !state.wifiAllowed || !state.mobileAllowed
                }
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadInstalledApps()
    }

    /**
     * Load installed apps on a background thread.
     */
    private fun loadInstalledApps() {
        viewModelScope.launch {
            _isLoading.value = true
            val apps = withContext(Dispatchers.IO) {
                PackageUtils.getInstalledApps(getApplication())
            }
            _installedApps.value = apps
            _isLoading.value = false
        }
    }

    /** Refresh the app list (pull-to-refresh). */
    fun refreshApps() {
        loadInstalledApps()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateFilter(filter: FilterOption) {
        _selectedFilter.value = filter
    }

    /**
     * Toggle Wi-Fi access for an app, persist to Room, and trigger VPN rebuild.
     */
    fun toggleWifi(packageName: String, uid: Int, allowed: Boolean) {
        viewModelScope.launch {
            repository.toggleWifi(packageName, uid, allowed)
            triggerVpnRebuild()
        }
    }

    /**
     * Toggle mobile data access for an app, persist to Room, and trigger VPN rebuild.
     */
    fun toggleMobile(packageName: String, uid: Int, allowed: Boolean) {
        viewModelScope.launch {
            repository.toggleMobile(packageName, uid, allowed)
            triggerVpnRebuild()
        }
    }

    /**
     * Tell the VPN service to rebuild its tunnel with updated rules.
     */
    private fun triggerVpnRebuild() {
        if (AppFenceVpnService.isRunning) {
            val context = getApplication<Application>()
            val intent = Intent(context, AppFenceVpnService::class.java).apply {
                action = AppFenceVpnService.ACTION_REBUILD
            }
            context.startForegroundService(intent)
        }
    }
}
