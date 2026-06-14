# WODTimer

> A modern CrossFit workout timer for Android, built with Kotlin and Jetpack Compose.

WODTimer is a feature-rich timer application designed for CrossFit and functional fitness athletes. It supports multiple timing formats, workout history tracking, and customizable audio/visual feedback — all in a clean Material 3 interface.

## Features

### Timer Modes
| Mode | Description |
|---|---|
| **For Time** | Countdown timer for completing a workout as fast as possible |
| **EMOM** | Every Minute On the Minute — configurable intervals with rounds |
| **Tabata** | 20s work / 10s rest cycles (configurable) for 8 rounds |
| **AMRAP** | As Many Rounds As Possible — countdown with configurable time cap |
| **Interval** | Work / rest / set cycles with fully configurable durations |
| **Custom WOD** | Free-form workouts with a sequence of blocks (warmup, work, rest, cooldown) |

### Smart Timer
- Prepare countdown (3-2-1-Go) with text-to-speech announcements
- Work / Rest / Cooldown phase tracking
- Round and set counters
- Pause, resume, and finish controls
- Customizable time caps and manual round input

### Audio & Haptic Feedback
- Programmatic sine-wave beeps for phase transitions
- Text-to-speech countdown and workout announcements
- Customizable vibration patterns
- All feedback channels independently toggleable

### Workout Management
- Create, save, edit, and delete workouts
- Favorite system for quick access
- Persistent storage via Room database
- Workout history with elapsed time, rounds, and notes

### Settings
- Sound toggle with volume control
- Vibration and TTS toggles
- Prepare countdown duration
- Dark theme (default) / light theme
- Keep screen awake during workouts
- Beep at each minute and last-minute warning

## Tech Stack

| Layer | Technology |
|---|---|
| Language | [Kotlin](https://kotlinlang.org/) |
| UI | [Jetpack Compose](https://developer.android.com/jetpack/compose) + [Material 3](https://m3.material.io/) |
| Architecture | MVVM + Clean Architecture |
| DI | [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) + KSP |
| Database | [Room](https://developer.android.com/training/data-storage/room) |
| Navigation | [Navigation Compose](https://developer.android.com/guide/navigation/navigation-getting-started) |
| Testing | JUnit, Mockito, Turbine, Compose UI Test |
| Min SDK | 29 |
| Target SDK | 34 |

## Screenshots

<!-- TODO: Add screenshots of the app -->

## Getting Started

### Prerequisites

- [Android Studio](https://developer.android.com/studio) Hedgehog or later
- JDK 17
- Android SDK 34 (build tools)
- An Android device or emulator running API 29+

### Setup

1. Clone the repository and open it in Android Studio:
   ```sh
   git clone https://github.com/yourusername/wodtimer.git
   cd wodtimer
   ```

2. Sync the project with Gradle files (Android Studio will prompt you).

3. Run the app:
   - From Android Studio: select a device/emulator and click **Run**
   - Or via command line:
     ```sh
     ./gradlew installDebug
     ```

### Building

```sh
# Debug APK
./gradlew assembleDebug

# Release APK (uses bundled keystore)
./gradlew assembleRelease
```

### Testing

```sh
# Run unit tests
./gradlew testDebugUnitTest

# Generate JaCoCo coverage report
./gradlew jacocoTestReport
# Report available at: build/reports/coverage/
```

## Architecture

The project follows **Clean Architecture** with three layers:

```
com.wodtimer.app/
├── domain/          # Business logic, models, repository interfaces
├── data/            # Room database, DAOs, repository implementations
└── presentation/    # Compose screens, ViewModels, navigation
```

- **UI** layer uses Jetpack Compose with unidirectional data flow via `StateFlow`.
- **Domain** layer contains use cases and pure Kotlin models with no Android dependencies.
- **Data** layer implements repositories backed by Room, with DAOs for persistence.

## Project Structure

```
app/
├── src/main/java/com/wodtimer/app/
│   ├── data/local/         # Room database, DAOs, entities, repositories
│   ├── di/                 # Hilt dependency injection modules
│   ├── domain/             # Models, repository interfaces, use cases
│   ├── presentation/       # Compose screens and ViewModels
│   │   ├── home/          # Home screen
│   │   ├── timer/         # Timer screen + controls
│   │   ├── workout/       # Workout editor
│   │   ├── history/       # Workout history
│   │   ├── settings/      # Settings screen
│   │   ├── navigation/    # Nav graph and route definitions
│   │   └── theme/         # Material 3 theme (colors, typography)
│   ├── service/           # Sound, TTS, vibration managers
│   └── util/              # Constants, formatters, clock abstraction
└── src/test/              # Unit tests
```

## License

```
Copyright 2024 WODTimer

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
