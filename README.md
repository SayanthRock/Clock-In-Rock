# Clock In Rock 🪨⏱️

A premium, production-ready Android application combining high-fidelity aesthetics, powerful temporal management engines, and real-time custom audioscapes. 

Designed with modern architectures and Material Design 3 guidelines, **Clock In Rock** features an adaptive, immersive **Liquid Glass UI**, interactive custom widgets, and an fully automated OTA update distribution mechanism.

---

## 💎 Features

### 1. Digital Clock
* Dynamic, fluid numeric animations.
* Comprehensive 12-hour/24-hour style format support.
* Informative contextual greetings matching the active time of day (Morning/Afternoon/Evening/Night).

### 2. Analog Clock
* Fully responsive, hardware-accelerated vectors.
* Glowing tick pointers, neon color sweep lines, and interactive central pivot dials.
* Custom Alphabet customizers that allow personalization of the clock layout.

### 3. Alarm Manager
* Precise alarm registration built around local alarm mechanisms.
* Recurring weekly active days and fully configurable snooze protocols.
* Custom Sound synthesis options (Sine, Square, Triangle, Sawtooth wave types) allowing deep adjustments of Pitch, Frequency, Pulsing, and Vibrato depths without requiring external bulky audio file assets.

### 4. Stopwatch
* High-precision lap tracker supporting millisecond precision.
* Responsive control loops (Start, Lap, Pause, Clear) mapped to smooth touch animations.

### 5. Timer (Concurrent Multi-State)
* Run multiple timers concurrently.
* Visually track each active timer on a central layout deck.
* Independent customizable titles, sounds, and active state loops.

### 6. World Clock
* Comprehensive time calculations across major global zones.
* Intuitive, fast world clock additions.
* Supports active UTC time differential display, making it easy to see relative offsets.

### 7. Adaptive UI Preferences
* **AMOLED Pure Black Mode**: Black surfaces mapped to deep energy savings and incredible contrast.
* **Dynamic Material You Colors**: Automatically extracts color swatches reflecting your active launcher wallpaper (Android 12+ / API 31+).
* **Display Scales**: Tuning sizes (Small, Standard, Large) to fit any physical screen density comfortably.
* **Liquid Glass customization**: Dynamic transparency, blur strengths, and border thicknesses.

### 8. Home Screen Widgets
* Minimalist retro clock widget displaying local formatted time, date, and state indicators.
* Fast, asynchronous refresh button to manually align parameters on the layout.

---

## 🛠️ Tech Stack & Platform Specs

* **Min SDK**: `26` (Android 8.0 Oreo)
* **Target SDK**: `37` (Android 15 QPR)
* **Compile SDK**: `37`
* **Language**: `Kotlin` (latest stable toolchain)
* **Bytecode Target**: `Java 21` / JVM Target `21`
* **UI toolkit**: `Jetpack Compose` (Modern Material 3 standards)
* **Architecture**: Modern MVVM (Model-View-ViewModel) + Offline-first patterns.
* **Local Persistence**: **Room Database** (local schemas running robust compilation verification via schema exports).

---

## 🚀 CI / CD & Distribution Automation

This project features a fully automated CI/CD loop powered by **GitHub Actions** (`.github/workflows/build.yml`) that triggers on push events to `main` and `nightly` branches:

### Automated Pipelines
1. **Compilation checks**: Sets up JDK 21 and validates the project structure.
2. **Analysis validation**: Runs unit and integration verification tests.
3. **Distribution channels**:
   * **Nightly Channel**: Commits to the `nightly` branch automatically trigger pre-release compilation, generate version details referencing values inside `nightly.json`, and upload pre-releases to GitHub Releases.
   * **Stable Channel**: Commits to the `main` branch parse version criteria inside `stable.json`, compile release APKs, trigger signing steps, and distribute verified production releases online.

---

## 🔑 APK Signing & Security

Our release pipeline supports robust APK signing inside secure environments using GitHub Repository Secrets.

### Required Secrets
To facilitate automated signing, populate the following parameters under **Settings > Secrets and variables > Actions** in your repository:

* `KEYSTORE_FILE`: Base64-encoded string of your `.jks` production keystore file (`cat release.jks | base64`).
* `KEYSTORE_PASSWORD`: Password of the keystore container.
* `KEY_ALIAS`: Alias identifier of the signing key.
* `KEY_PASSWORD`: Password of the specific key alias.

*Note: The pipeline is built with **graceful fail-safe fallbacks**. If these secrets are not configured or missing, the CI will automatically fallback to building unsigned/debug-verified verification APKs to ensure builds remain successful on fork repositories.*

---

## 📡 OTA Update Engine

**Clock In Rock** includes an on-demand, in-app OTA updates comparator checking:
- `stable.json` (for official channels)
- `nightly.json` (for experimental channels)

### Comparator Rules
1. Compares local `versionCode` against the JSON target.
2. Informs the user when new updates are compiled.
3. Displays the changelog notes directly in-app.
4. Provides direct download-to-install link targets.
