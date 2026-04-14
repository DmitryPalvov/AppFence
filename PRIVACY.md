# Privacy Policy for AppFence

**Last updated: April 15, 2026**

AppFence ("we", "our", "the app") is an open-source Android application that provides per-app internet access control. This Privacy Policy explains what information the app does and does not collect, in plain language.

**TL;DR — AppFence collects nothing. No tracking. No analytics. No servers. No data ever leaves your device.**

---

## 1. Information We Collect

**We collect absolutely no personal data.** AppFence does not:

- ❌ Collect your name, email, phone number, or any account information
- ❌ Track your location
- ❌ Record your browsing history or the content of your network traffic
- ❌ Collect device identifiers (IMEI, Android ID, Advertising ID)
- ❌ Use any third-party analytics (no Firebase, no Google Analytics, no Crashlytics, no ads SDK)
- ❌ Send any information off your device to any server — ever

## 2. Information Stored Locally on Your Device

The app stores the following **only on your device** — this data never leaves your phone:

| Data | Purpose | Storage Location |
|------|---------|------------------|
| List of installed apps & their Wi-Fi / mobile rules | To apply your blocking preferences | Local Room database (SQLite) |
| Your preferences (e.g., "Start on boot", "Block new apps by default") | To remember your settings | Local SharedPreferences |

You can wipe all of this at any time by clearing app data in **Settings → Apps → AppFence → Storage → Clear data** or uninstalling the app.

## 3. VPN Service

AppFence uses Android's built-in `VpnService` API **locally on your device only**. This is how it works:

- A **local, on-device VPN tunnel** is created to intercept traffic from blocked apps
- Traffic from blocked apps is **silently dropped** — never forwarded to any server
- Traffic from allowed apps **bypasses the tunnel entirely** and uses your real network connection directly
- **No traffic is logged, inspected, decrypted, or routed through any external server**
- **No VPN server is involved.** The "VPN" exists solely on your device — it is a technical mechanism to enable per-app filtering, not a traditional VPN that routes traffic elsewhere

## 4. Permissions Used

AppFence requests the following Android permissions, each used **exclusively on-device**:

| Permission | Why it's needed |
|------------|-----------------|
| `BIND_VPN_SERVICE` | Create the local on-device VPN tunnel |
| `FOREGROUND_SERVICE` / `FOREGROUND_SERVICE_SPECIAL_USE` | Keep the VPN service running reliably |
| `RECEIVE_BOOT_COMPLETED` | Optionally restart the VPN after device reboot |
| `ACCESS_NETWORK_STATE` | Detect Wi-Fi vs. cellular to apply the correct rules |
| `QUERY_ALL_PACKAGES` | List installed apps so you can toggle each one |
| `POST_NOTIFICATIONS` | Show the mandatory VPN status notification |

None of these permissions are used to collect or transmit data.

## 5. Data Sharing

We do not share, sell, rent, or transmit any data because **we do not collect any data**.

## 6. Third-Party Services

AppFence uses **no third-party services, SDKs, analytics, or advertising networks**.

## 7. Children's Privacy

AppFence is safe for users of all ages. We do not knowingly collect information from anyone, including children under 13.

## 8. Security

Since no data ever leaves your device, there is no server-side data to be breached. Your rules and preferences are stored in the app's private storage, which Android isolates from other apps by default.

## 9. Open Source

AppFence is fully open-source under the GPL-3.0 license. You can verify every claim in this policy by inspecting the source code:

👉 [https://github.com/yogesh-7/AppFence](https://github.com/yogesh-7/AppFence)

## 10. Changes to This Policy

If this policy ever changes, the updated version will be published at this URL with a revised "Last updated" date. Material changes will also be announced in the app's release notes on the GitHub Releases page.

## 11. Contact

For questions about this Privacy Policy or the app, open an issue on GitHub:

👉 [https://github.com/yogesh-7/AppFence/issues](https://github.com/yogesh-7/AppFence/issues)

---

*This privacy policy is released into the public domain (CC0). Feel free to adapt it for your own open-source privacy-respecting apps.*
