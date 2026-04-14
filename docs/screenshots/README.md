# Screenshots

This folder contains screenshots of AppFence used in the main project README and promotional material.

## Current Screenshots

| File | Description |
|------|-------------|
| `1-main-home.png` | Main screen — app list with AppFence header |
| `2-main-search.png` | Main screen — search bar expanded |
| `3-main-filter-all.png` | Main screen — "All" filter selected |
| `4-settings.png` | Settings screen — VPN control and preferences |

## Guidelines for Adding Screenshots

- **Resolution:** 1080×2400 or higher (phone screenshots)
- **Format:** PNG (prefer) or JPG
- **File size:** Keep under 500 KB each — compress with [tinypng.com](https://tinypng.com) if needed
- **Naming:** Use kebab-case with a numeric prefix for ordering (e.g. `1-main-home.png`)
- **Status bar:** Clean status bar is nice-to-have but not required
- **Frame:** Phone frames optional — pick one style and stick with it for consistency

## How to Capture

On a connected Android device:

```bash
adb shell screencap -p /sdcard/screen.png
adb pull /sdcard/screen.png docs/screenshots/1-main-home.png
adb shell rm /sdcard/screen.png
```

Or use Android Studio → **View → Tool Windows → Device Explorer** → screenshot button.
