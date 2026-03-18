# PinLevel

Android pinball machine leveler app built with Jetpack Compose + Material3.

## Features

- Live accelerometer-based pitch and roll readings (0.1° precision)
- Visual bubble level with 4-leg markers (FL, FR, RL, RR)
- Color-coded indicators: green (±0.2°), yellow (±0.5°), red (out of range)
- Two-step flat/flip calibration to remove sensor bias
- 15 pre-loaded popular pinball machines (default 6.5° target angle)
- Presets: Modern SS (6.5°), EM/Classic (4.0°), Competition (7.0°), Custom
- Per-machine angle override with DataStore persistence
- Dark theme optimized for pinball room lighting

## Tech Stack

- Kotlin 2.0 + Jetpack Compose + Material3
- MVVM with ViewModel + StateFlow
- DataStore (preferences) for calibration and machine profiles
- Pure SensorManager accelerometer (no external libraries)
- Canvas-based visual level and leg markers
- Navigation Compose with bottom nav

## Building

Requires Android Studio or a JDK 17+ installation with Android SDK.

```bash
./gradlew assembleDebug
```

Min SDK: 24 (Android 7.0) | Target SDK: 35
