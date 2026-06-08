# Changelog

All notable changes to the **Clock In Rock** project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.1.0-nightly] - 2026-06-08
### Added
- **Concurrent Multiple Timers**: Full support for running multiple local timers simultaneously with independent titles, color accents, and sound parameters.
- **Material You Dynamic Colors**: Complete dynamic color matching reflecting current active launcher wallpapers (supported on Android 12+ / API 31+).
- **AMOLED Pure Black Mode**: Deep black background theme to improve battery efficiency, high contrast reading, and visual appeal.
- **Home Screen Widgets**: Standard retro digital clock widget with manually-triggered refresh triggers and action integrations.

### Changed
- Upgraded Compile and Target SDK parameters to Android 13/14/15 standards up to the latest SDK version 37.
- Set Java and Bytecode bytecode output compiler target to JDK 21.

---

## [1.0.0] - 2026-06-08
### Added
- Initial official release of **Clock In Rock**.
- Realtime Digital and interactive Analog clock panels.
- Highly custom Sound Synthesizer engine with local wave synthesis (Sine, Square, Triangle, Sawtooth).
- Liquid Glass card styling modifiers customizable in blur strengths and border thicknesses.
- World Clock and trackable geographic zone calculations.
- Immersive Stopwatch lap tracker.
- Fully automated OTA update checks checking local `stable.json` records.
- Continuous Integration & Deployment (CI/CD) pipelines on push events.
