# NetGuard Lite — Per-App Internet Control (Android)

A native Android app (Kotlin + Jetpack Compose) that lets you control Wi-Fi and mobile data access on a per-app basis, **without requiring root**.

## How It Works

NetGuard Lite uses Android's `VpnService` to create a **local, on-device VPN**. No traffic is sent to any remote server.

### Blocking Mechanism

1. When the VPN is active, **all device traffic** is routed through a local tunnel.
2. Apps that you've marked as **allowed** are added via `addDisallowedApplication()` — they bypass the tunnel entirely and use the real network.
3. Apps that are **blocked** have their traffic enter the tunnel, where it is silently dropped (never forwarded anywhere).
4. When you toggle an app's access, the tunnel is **rebuilt** in < 1 second with updated rules.

### Per-Network-Type Control

- Each app has **independent** Wi-Fi and Mobile Data toggles.
- The app monitors the current network type via `ConnectivityManager.NetworkCallback`.
- When the device switches between Wi-Fi and cellular, the tunnel is automatically rebuilt with the correct set of blocked apps for that network type.

## Permissions

| Permission | Why |
|---|---|
| `BIND_VPN_SERVICE` | Required to create the local VPN tunnel |
| `FOREGROUND_SERVICE` | VPN must run as a foreground service (Android requirement) |
| `FOREGROUND_SERVICE_SPECIAL_USE` | Required for VPN foreground service type on Android 14+ |
| `RECEIVE_BOOT_COMPLETED` | Auto-start VPN on device boot (optional) |
| `ACCESS_NETWORK_STATE` | Detect Wi-Fi vs cellular network |
| `QUERY_ALL_PACKAGES` | List all installed applications |
| `POST_NOTIFICATIONS` | Show VPN status notification (Android 13+) |

## Build Instructions

### Prerequisites

- Android Studio Hedgehog (2024.1) or later
- JDK 17+
- Android SDK with `compileSdk 35`

### Steps

```bash
# Clone the project
cd "NetGuard Lite"

# Build the debug APK
./gradlew assembleDebug

# The APK will be at: app/build/outputs/apk/debug/app-debug.apk
```

Or simply open the project in Android Studio and click **Run**.

### First Run

1. Grant the VPN permission when prompted (Android system dialog).
2. The main screen shows all installed apps.
3. Toggle Wi-Fi and/or Mobile Data switches for any app.
4. Go to **Settings** to start/stop the VPN service.

## Tech Stack

| Component | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM (ViewModel + StateFlow) |
| Database | Room |
| Min SDK | 29 (Android 10) |
| Target SDK | 35 (Android 15) |
| Background | Foreground VpnService |
| Boot | BroadcastReceiver (BOOT_COMPLETED) |

## Project Structure

```
com.netguardlite/
├── NetGuardLiteApp.kt              # Application class
├── data/
│   ├── AppRule.kt                  # Room entity
│   ├── AppRuleDao.kt               # Room DAO
│   ├── AppRuleDatabase.kt          # Room database
│   └── AppRepository.kt            # Repository
├── vpn/
│   ├── NetGuardVpnService.kt       # VPN service (blocking logic)
│   └── NetworkMonitor.kt           # Wi-Fi/cellular detection
├── receiver/
│   └── BootReceiver.kt             # Auto-start on boot
├── ui/
│   ├── MainActivity.kt             # Single-activity host
│   ├── theme/                       # Material 3 dark theme
│   ├── screens/
│   │   ├── MainScreen.kt           # App list with toggles
│   │   ├── SettingsScreen.kt        # VPN control & preferences
│   │   └── OnboardingScreen.kt     # First-run welcome
│   ├── components/
│   │   ├── AppListItem.kt          # App row with toggles
│   │   ├── FilterBar.kt            # All/User/System/Blocked chips
│   │   └── StatusBadge.kt          # Allowed/Blocked badge
│   └── viewmodel/
│       ├── MainViewModel.kt        # App list + rules
│       └── SettingsViewModel.kt    # VPN state + preferences
├── model/
│   └── AppInfo.kt                  # UI models
└── util/
    └── PackageUtils.kt             # Load installed apps
```

## Principles

- **No root.** Uses the standard Android VpnService API.
- **No ads.** No analytics. No network calls.
- **Fully offline.** All data stays on-device.
- **Battery-conscious.** No packet parsing — OS-level routing only.
- **Open architecture.** Clean MVVM with separated concerns.

## Inspiration

Functionally inspired by the open-source [NetGuard](https://github.com/M66B/NetGuard) app by M66B, reimagined with a Compose-based UI and simplified architecture.

## License

This project is provided as-is for personal use. Not affiliated with or derived from NetGuard.
