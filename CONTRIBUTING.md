# Contributing to AppFence

First off — thank you for considering contributing to **AppFence**! 🎉
Every contribution, no matter how small, is appreciated.

---

## 🌱 Ways to Contribute

You don't need to write code to help:

- 🐛 **Report bugs** — Open an issue with steps to reproduce
- 💡 **Suggest features** — Open a feature request issue
- 📝 **Improve docs** — Fix typos, clarify instructions, translate
- 🎨 **Design** — Share mockups, icons, screenshots
- 🌍 **Translate** — Add support for your language (coming soon)
- ⭐ **Star the repo** — It genuinely helps visibility
- 💬 **Spread the word** — Tell your friends!

---

## 🚀 Development Setup

### Prerequisites

- **Android Studio** Hedgehog (2024.1) or later
- **JDK 17** or higher
- **Android SDK** with `compileSdk 35`
- A physical Android device (API 29+) or emulator — *note: VPN testing is more reliable on physical devices*
- Basic familiarity with **Kotlin** and **Jetpack Compose**

### Get the Code

```bash
# 1. Fork the repo on GitHub, then clone your fork
git clone https://github.com/YOUR-USERNAME/AppFence.git
cd AppFence

# 2. Add the upstream remote
git remote add upstream https://github.com/yogesh-7/AppFence.git

# 3. Open in Android Studio (File > Open > select the AppFence folder)
```

### Build & Run

```bash
# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run tests
./gradlew test
```

Or just press **Run ▶** in Android Studio.

---

## 🔁 Contribution Workflow

1. **Find or create an issue** — Check [existing issues](https://github.com/yogesh-7/AppFence/issues) first. Comment on one you'd like to work on so we don't duplicate effort.
2. **Fork** the repo on GitHub.
3. **Create a branch** from `main`:
   ```bash
   git checkout -b feature/short-description
   # or
   git checkout -b fix/short-description
   ```
4. **Make your changes**, following the code style below.
5. **Test your changes** on a real device if your change touches the VPN service or UI.
6. **Commit** with a clear message:
   ```bash
   git commit -m "feat: add dark/light theme toggle"
   ```
   We loosely follow [Conventional Commits](https://www.conventionalcommits.org/) — use prefixes like `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`.
7. **Push** to your fork:
   ```bash
   git push origin feature/short-description
   ```
8. **Open a Pull Request** against `main`. Fill out the PR template.

---

## 🎨 Code Style

- **Kotlin conventions** — Follow the [official Kotlin style guide](https://kotlinlang.org/docs/coding-conventions.html)
- **Compose** — Prefer stateless composables; hoist state to ViewModels
- **Architecture** — Stick to MVVM; don't add business logic to composables
- **Naming** — PascalCase for classes/composables, camelCase for functions/variables
- **No hard-coded strings** — Use `strings.xml`
- **No hard-coded colors** — Use the Material 3 theme
- **Indentation** — 4 spaces, no tabs
- **Line length** — Aim for ≤ 120 characters

Run `./gradlew lint` before submitting.

---

## 🧪 Testing Guidelines

- Add unit tests for data layer changes (Room DAOs, repository logic).
- Manually test VPN-related changes on a physical device — emulators don't always reflect real VPN behavior.
- For UI changes, include a before/after screenshot in your PR.

---

## 📝 Pull Request Checklist

Before submitting, confirm:

- [ ] Code compiles with no warnings
- [ ] `./gradlew lint` passes
- [ ] New behavior is tested (unit test or manual steps documented)
- [ ] README / docs updated if applicable
- [ ] PR description references the related issue (e.g. `Closes #42`)
- [ ] Commits have clear, descriptive messages

---

## 🎃 Hacktoberfest

This repo participates in **Hacktoberfest**! During October:

- Look for issues labeled [`hacktoberfest`](https://github.com/yogesh-7/AppFence/labels/hacktoberfest)
- Quality PRs only — spam PRs will be marked `invalid` / `spam`
- Make sure your PRs are meaningful (not just whitespace/typo spam)

---

## 🆘 Need Help?

- Open a [Discussion](https://github.com/yogesh-7/AppFence/discussions) for questions
- Open an [Issue](https://github.com/yogesh-7/AppFence/issues) for bugs
- Tag `@yogesh-7` in comments if stuck

---

## 📜 Code of Conduct

Be kind. Be respectful. Assume good intent. We're all here to build something cool together.

Harassment, hate speech, or personal attacks will result in removal from the project.

---

**Thank you for making AppFence better! 🙏**
