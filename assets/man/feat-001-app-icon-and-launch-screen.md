# Application Icon and Launch Screen

## Overview

The **Application Icon and Launch Screen** feature fulfills the mandatory first impression and branding requirements defined by the 42 project subject. The application provides a customized, high-resolution adaptive launcher icon across all standard Android screen densities and introduces a dedicated launch (splash) screen that stays displayed for at least **1 second** before transitioning seamlessly into the authentication flow.

---

## Feature Specifications

### 1. Application Launcher Icon
- **Adaptive Icon Architecture**: Supports Android 8.0+ (API 26+) adaptive icons with layered foreground and background drawables.
- **Multi-Density Support**: Packaged across all standard device density buckets:
  - `mipmap-mdpi` (160 dpi)
  - `mipmap-hdpi` (240 dpi)
  - `mipmap-xhdpi` (320 dpi)
  - `mipmap-xxhdpi` (480 dpi)
  - `mipmap-xxxhdpi` (640 dpi)
  - `mipmap-anydpi-v26` (Vector adaptive icon definition)
- **Visual Branding**: Distinctive molecular protein geometry rendered with gradient depth and clean circular masking support (`ic_launcher_round.xml`).

### 2. Launch Screen (Splash Screen)
- **Dedicated Splash Activity**: Handled by `SplashActivity` configured with the custom theme `@style/Theme.SwiftyProteins.Splash`.
- **Minimum 1-Second Display**: Explicitly enforces the strict 42 evaluation rule that the launch screen must remain visible for **at least 1 second** (`1100 ms` via main thread `Handler` delay) even on ultra-fast modern processors.
- **Visual Presentation**: High-resolution molecular brand logo (`logo_splash.png`) centered over a deep gradient background (`bg_splash_gradient.xml`).
- **Smooth Transition**: Employs a polished cross-fade animation transition (`overridePendingTransition(fade_in, fade_out)`) when navigating to `LoginActivity`.

### 3. Autolayout & Screen Adaptability
- Root layout configured using Android's `ConstraintLayout`.
- UI components maintain proper centering, aspect ratios, and padding across varied screen aspect ratios (16:9, 18:9, 19.5:9, 21:9), tablets, and device orientations (portrait and landscape).

---

## Relevant Files in the Codebase

| File | Type | Description |
|---|---|---|
| [`SplashActivity.kt`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/java/com/example/swiftyproteins/ui/splash/SplashActivity.kt) | Kotlin Source | Cold-start entry point managing the 1100ms display delay and fade transition to LoginActivity. |
| [`activity_splash.xml`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/res/layout/activity_splash.xml) | XML Layout | ConstraintLayout defining the centered brand logo and layout hierarchy. |
| [`AndroidManifest.xml`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/AndroidManifest.xml) | Manifest | Declares `SplashActivity` with `MAIN` and `LAUNCHER` intent filters and application icon references. |
| [`themes.xml`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/res/values/themes.xml) | Styles | Defines `Theme.SwiftyProteins.Splash` with full-screen flags and window background. |
| [`bg_splash_gradient.xml`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/res/drawable/bg_splash_gradient.xml) | Drawable | Linear dark gradient background resource. |
| [`ic_launcher.xml`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/res/mipmap-anydpi/ic_launcher.xml) | Mipmap | Vector adaptive icon definition combining foreground and background. |
| [`ic_launcher_round.xml`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/res/mipmap-anydpi/ic_launcher_round.xml) | Mipmap | Circular adaptive launcher icon definition for round icon launchers. |

---

## Testing and Verification

### 1. Manual Evaluation Steps
1. **Launcher Icon Verification**:
   - Inspect the Android home screen launcher and app drawer.
   - Verify that the custom Swifty Proteins icon appears clearly, without pixelation or stretching.
   - Verify that both square/squircle and round icon styles render correctly according to device launcher settings.
2. **Launch Screen Duration**:
   - Launch the application from cold state (tap icon from home screen).
   - Use a stopwatch or screen recorder to confirm that the splash screen remains visible for **at least 1.0 second** before the login screen appears.
   - Confirm that the transition from splash to login view is smooth and flicker-free.
3. **Screen Rotation**:
   - Rotate the device into landscape during or immediately after the splash screen.
   - Confirm that elements remain properly centered and do not clip or distort.

### 2. ADB / Makefile Commands
- **Cold start the application**:
  ```bash
  make run
  # or
  adb shell am start -S -W -a android.intent.action.MAIN -c android.intent.category.LAUNCHER com.example.swiftyproteins/.ui.splash.SplashActivity
  ```
- **Measure cold start transition timing via logcat**:
  ```bash
  adb logcat -d | grep -E "ActivityTaskManager.*Displayed com.example.swiftyproteins"
  ```
- **Capture screenshot of the launch screen**:
  ```bash
  make screenshot
  ```

### 3. Evaluation Checklist Alignment
- [x] Application launcher icon is custom, unique, and present on the home screen.
- [x] Launch screen is displayed upon opening the app.
- [x] Launch screen stays visible for at least 1 second.
- [x] Autolayout behaves cleanly with no visual distortion across orientations.
