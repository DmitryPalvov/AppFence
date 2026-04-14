<div align="center">

# AppFence

### Per-App Internet Control for Android — No Root Required

A lightweight, open-source Android firewall that gives you granular control over which apps can access Wi-Fi and mobile data. Built with Kotlin and Jetpack Compose.

![Android](https://img.shields.io/badge/platform-Android-green)
![Kotlin](https://img.shields.io/badge/Kotlin-2.1.10-purple)
![Min SDK](https://img.shields.io/badge/minSdk-29-blue)
![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4)
![PRs Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen)
![Hacktoberfest](https://img.shields.io/badge/Hacktoberfest-friendly-orange)
![GitHub stars](https://img.shields.io/github/stars/yogesh-7/AppFence?style=social)

</div>

---

## ✨ Why AppFence?

- 🛡️ **Block any app from the internet** — stop background data, ads, and telemetry
- 📶 **Per-network rules** — allow an app on Wi-Fi only, mobile only, both, or neither
- 🔓 **No root required** — uses Android's built-in `VpnService`
- 🔒 **100% on-device** — no servers, no analytics, no data ever leaves your phone
- 🪶 **Lightweight** — no packet parsing, minimal battery impact
- 🎨 **Modern UI** — Material 3 + Jetpack Compose

## How It Works

AppFence uses Android's `VpnService` to create a **local, on-device VPN**. No traffic is sent to any remote server.

### Blocking Mechanism

1. When the VPN is active, **all device traffic** is routed through a local tunnel.
2. Apps you mark as **allowed** are added via `addDisallowedApplication()` — they bypass the tunnel entirely and use the real network.
3. Apps that are **blocked** have their traffic enter the tunnel, where it is silently dropped (never forwarded anywhere).
4. When you toggle an app's access, the tunnel is **rebuilt** in < 1 second with updated rules.

### Per-Network-Type Control

- Each app has **independent** Wi-Fi and Mobile Data toggles.
- The app monitors the current network type via `ConnectivityManager.NetworkCallback`.
- When the device switches between Wi-Fi and cellular, the tunnel is automatically rebuilt with the correct set of blocked apps for that network type.

## 📸 Screenshots

> _Screenshots coming soon — contributions welcome!_

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog (2024.1) or later
- JDK 17+
- Android SDK with `compileSdk 35`
- A device or emulator running **Android 10 (API 29) or higher**

### Build from Source

```bash
# Clone the repository
git clone https://github.com/yogesh-7/AppFence.git
cd AppFence

# Build the debug APK
./gradlew assembleDebug

# The APK will be at:
# app/build/outputs/apk/debug/app-debug.apk
```

Or simply open the project in Android Studio and press **Run**.

### First Run

1. Grant the VPN permission when prompted (Android system dialog).
2. The main screen shows all installed apps.
3. Toggle Wi-Fi and/or Mobile Data switches for any app.
4. Go to **Settings** to start/stop the VPN service.

## 🔐 Permissions

| Permission | Why it's needed |
|---|---|
| `BIND_VPN_SERVICE` | Required to create the local VPN tunnel |
| `FOREGROUND_SERVICE` | VPN must run as a foreground service (Android requirement) |
| `FOREGROUND_SERVICE_SPECIAL_USE` | Required for VPN foreground service type on Android 14+ |
| `RECEIVE_BOOT_COMPLETED` | Auto-start VPN on device boot (optional) |
| `ACCESS_NETWORK_STATE` | Detect Wi-Fi vs cellular network |
| `QUERY_ALL_PACKAGES` | List all installed applications |
| `POST_NOTIFICATIONS` | Show VPN status notification (Android 13+) |

## 🛠️ Tech Stack

| Component | Technology |
|---|---|
| Language | Kotlin 2.1.10 |
| UI | Jetpack Compose + Material 3 |
| Architecture | MVVM (ViewModel + StateFlow) |
| Database | Room |
| Async | Kotlin Coroutines |
| Navigation | Navigation Compose |
| Min SDK | 29 (Android 10) |
| Target SDK | 35 (Android 15) |
| Background | Foreground VpnService |
| Boot | BroadcastReceiver (BOOT_COMPLETED) |

## 📁 Project Structure

```
com.yogesh.appfence/
├── AppFenceApp.kt                  # Application class
├── data/
│   ├── AppRule.kt                  # Room entity
│   ├── AppRuleDao.kt               # Room DAO
│   ├── AppRuleDatabase.kt          # Room database
│   └── AppRepository.kt            # Repository
├── vpn/
│   ├── AppFenceVpnService.kt       # VPN service (blocking logic)
│   └── NetworkMonitor.kt           # Wi-Fi/cellular detection
├── receiver/
│   └── BootReceiver.kt             # Auto-start on boot
├── ui/
│   ├── MainActivity.kt             # Single-activity host
│   ├── theme/                      # Material 3 dark theme
│   ├── screens/
│   │   ├── MainScreen.kt           # App list with toggles
│   │   ├── SettingsScreen.kt       # VPN control & preferences
│   │   └── OnboardingScreen.kt    # First-run welcome
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

## 🎯 Principles

- **No root.** Uses the standard Android VpnService API.
- **No ads. No analytics. No network calls.** Ever.
- **Fully offline.** All data stays on-device.
- **Battery-conscious.** No packet parsing — OS-level routing only.
- **Open architecture.** Clean MVVM with separated concerns.

## 🤝 Contributing

Contributions are **very welcome** — whether it's bug fixes, features, translations, documentation, or screenshots!

### Good first issues

Check out issues labeled [`good first issue`](https://github.com/yogesh-7/AppFence/labels/good%20first%20issue) or [`help wanted`](https://github.com/yogesh-7/AppFence/labels/help%20wanted).

### Hacktoberfest 🎃

This repo participates in **Hacktoberfest**! Look for issues tagged [`hacktoberfest`](https://github.com/yogesh-7/AppFence/labels/hacktoberfest) during October.

### How to contribute

1. Fork this repository
2. Create a feature branch (`git checkout -b feature/my-feature`)
3. Commit your changes (`git commit -m 'Add my feature'`)
4. Push to the branch (`git push origin feature/my-feature`)
5. Open a Pull Request

Please follow Kotlin coding conventions and the existing MVVM structure.

## 📄 License

AppFence is released under the **GNU General Public License v3.0**.
See the [LICENSE](LICENSE) file for the full text.

> In short: you are free to use, modify, and distribute this software — but any derivative work must also be open-source under GPL-3.0.

---

<div align="center">

**⭐ If you find AppFence useful, please consider starring the repo!**

Made with ❤️ by [@yogesh-7](https://github.com/yogesh-7)

</div>
