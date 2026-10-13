# Feature: Application Icon & Launch Screen

## Overview
Provides visual branding for the Swifty Proteins application on the Android system launcher and displays a dedicated launch (splash) screen on application start.

---

## Evaluation Requirements
* **Home Screen App Icon**: The application must display a custom icon on the device launcher/home screen instead of the default platform template.
* **Launch Screen (Splash Screen)**: A dedicated launch screen must be presented upon launching the application and **remain visible for at least 1 second** before transitioning to the authentication view.

---

## Technical Specifications
* **Icon Specifications**:
  * Format: Android Adaptive Icons (`mipmap-anydpi/ic_launcher.xml` and `ic_launcher_round.xml`).
  * Layers: Vector or high-density PNG foreground layer paired with background layer.
  * Density Buckets: `mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi` (providing full support for varying display pixel densities).
* **Launch Screen Specifications**:
  * Implementation: Android `androidx.core:core-splashscreen` API or a dedicated `SplashActivity`.
  * Display Duration: Minimum 1000ms (1 second), configured with a timer or `setKeepOnScreenCondition`.
  * Branding: Displays the molecular logo and app name with Material 3 styling.

---

## User Flow
1. User taps the **Swifty Proteins** icon on the home screen.
2. The launch screen immediately displays with the app branding and icon.
3. The launch screen remains visible for $\ge 1$ second.
4. Seamless transition to `LoginActivity`.

---

## Verification & Testing
1. Check device launcher: Ensure the custom molecular icon is shown with no default Android robot assets.
2. Launch app from cold start: Confirm the splash screen stays visible for at least 1 second before revealing the login view.
