# AppFunctions Testing Agent

AppFunctions Testing Agent is a testing and debugging tool for Android AppFunctions.
It allows developers to troubleshoot integration issues, manually invoke functions with
deterministic input, and test functions using an LLM-based agent.

## Prerequisites

-   **ADB**: Ensure you have `adb` installed and in your PATH.
-   **Device/Emulator**: A connected Android device or emulator with developer options enabled.

## Building and Running

To build, install, and launch the app with elevated privileges (required for some AppFunctions APIs),
use the `run_privileged.sh` script.

### Usage

```bash
./run_privileged.sh [OPTIONS]
```

### Options

-   `-b, --build`: Force a rebuild of the app before installing.
-   `-r, --release`: Use the release build type instead of debug.
-   `-f, --flavor FLAVOR`: Specify the build flavor (`standard` or `retail`). Defaults to `standard`.
-   `-k, --api-key KEY`: The Gemini API key (required for `retail` builds if not set elsewhere).
-   `-h, --help`: Show the help message.

### Examples

**Standard Debug Build**
```bash
./run_privileged.sh --build
```

**Retail Debug Build with API Key**
```bash
./run_privileged.sh --build --flavor retail --api-key YOUR_API_KEY
```

### Android TV / Google TV Keyboard Configuration

When developing or testing on Android TV / Google TV emulators using a physical workstation keyboard, you may want to disable the on-screen keyboard (Gboard) and Voice Input popups for a more natural typing experience:

```bash
# Disable Gboard (On-screen keyboard)
adb shell pm disable-user --user 0 com.google.android.inputmethod.latin

# Disable Google Assistant Voice Input (Katniss)
adb shell pm disable-user --user 0 com.google.android.katniss

# Disable Google TTS Voice Input Method Service (Speech popup)
adb shell pm disable --user 0 com.google.android.tts/com.google.android.apps.speech.tts.googletts.settings.asr.voiceime.VoiceInputMethodService
```

To re-enable them later:
```bash
adb shell pm enable com.google.android.inputmethod.latin
adb shell pm enable com.google.android.katniss
adb shell pm enable com.google.android.tts/com.google.android.apps.speech.tts.googletts.settings.asr.voiceime.VoiceInputMethodService
```

### Overlay Mode Permission (Android TV)

To display the chat interface as a screen overlay on top of another app, the `SYSTEM_ALERT_WINDOW` ("Display over other apps") permission is required. 
If not granted, clicking an application from the **Overlay Mode** dialog will automatically open the Google TV Special Access settings screen so you can enable it.

You can also grant it via ADB:
```bash
adb shell appops set com.example.appfunctions.agent SYSTEM_ALERT_WINDOW allow
```

## Testing

Always run relevant tests to verify your changes before committing.

### Unit Tests
Run unit tests for all variants:
```bash
./gradlew test
```
Or for a specific variant:
```bash
./gradlew testStandardDebugUnitTest
./gradlew testRetailDebugUnitTest -PGEMINI_API_KEY=YOUR_API_KEY
```

### Instrumented Tests
Run instrumented tests on a connected device:
```bash
./gradlew connectedAndroidTest
```
*Note: Some instrumented tests require a Gemini API key.
Pass it using `-Pandroid.testInstrumentationRunnerArguments.gemini_api_key=YOUR_API_KEY`.*

### Screenshot Tests
Verify UI changes with screenshot tests:
```bash
./gradlew updateDebugScreenshotTest
```

## Releasing (Bundling)

To create a distributable test suite bundle, use the `bundle.sh` script.
This script creates a zip file containing the APK, instructions, and a launch script.

### Usage

```bash
./bundle.sh [OPTIONS]
```

### Options

-   `-o, --output-dir DIR`: Directory to save the bundle (default: current directory).
-   `-r, --release`: Build the release version instead of debug.
-   `-f, --flavor FLAVOR`: Specify the build flavor (`standard` or `retail`). Defaults to `standard`.
-   `-k, --api-key KEY`: The Gemini API key (required for `retail` builds).

### Examples

**Bundle Standard Debug Version**
```bash
./bundle.sh
```

**Bundle Retail Debug Version**
```bash
./bundle.sh --flavor retail --api-key YOUR_API_KEY
```
