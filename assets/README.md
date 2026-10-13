# Swifty Proteins - Features & Specifications Index

This directory contains the complete technical and functional documentation for the **Swifty Proteins** application, indexed according to the official **42 Evaluation Scale** and project subject guidelines.

---

## 1. Project Setup & Environment Prerequisites

Before compiling or evaluating the project, verify that the environment meets the development requirements:

* [**`prj-setup.md`**](man/prj-setup.md) — **Project Setup & Environment Prerequisites**
  * **Java Environment**: JDK 21 (or JDK 17). Note that Gradle 9.6.0 daemon requires Java 17–25 (Java 27 is unsupported).
  * **Android Studio & SDK**: Android Studio with Android SDK API 37, Build-Tools, Platform-Tools, and Emulator.
  * **Linux Development Tools**: `android-tools` (`adb`, `fastboot`) and `android-udev` device rules.
  * **User Permissions**: Membership in `kvm` (for hardware-accelerated emulator virtualization) and `adbusers` (for USB physical device debugging).
  * **Hyprland / Wayland Notes**: `QT_QPA_PLATFORM=xcb` fallback instructions for emulator stability.

---

## 2. Mandatory Features

The following features constitute the mandatory part of the evaluation. All items in this section must be fully functional and verifiable on both emulator and physical devices.

### Section I: Application Setup & Branding
* [**`feat-001-icon-and-launchscreen.md`**](man/feat-001-icon-and-launchscreen.md) — **Application Icon & Launch Screen**
  * Custom home screen adaptive launcher icon across all screen densities.
  * Dedicated launch (splash) screen that remains visible for **at least 1 second**.
  * Status: **Completed**

### Section II: First View — Authentication & Security
* [**`feat-002-biometric-and-password-authentication.md`**](man/feat-002-biometric-and-password-authentication.md) — **Biometric & Password Authentication**
  * Account creation and login with username and password.
  * Native biometric verification via Android `BiometricPrompt` (fingerprint / Face Unlock).
  * Informative alert popup dialog when biometric authentication fails or is cancelled.
  * Username/password fallback when biometric hardware is unavailable or not enrolled.
  * Status: **Completed**

* [**`feat-003-password-security-and-storage.md`**](man/feat-003-password-security-and-storage.md) — **Password Security & Encrypted Storage**
  * Zero plaintext password storage.
  * Hardware-backed encryption via Android KeyStore `MasterKey` (AES256_GCM).
  * Cryptographic password hashing using SHA-256 before storage in `EncryptedSharedPreferences`.
  * Status: **Completed**

* [**`feat-004-session-security-and-background-lock.md`**](man/feat-004-session-security-and-background-lock.md) — **Session Security & Background Auto-Lock**
  * Whole-application lifecycle tracking via `ProcessLifecycleOwner`.
  * Automatic session locking when the user navigates away or presses the device Home button.
  * Mandatory re-authentication prompt upon returning to the application.
  * Status: **Completed**

### Section III: Second View — Ligand List View
* [**`feat-005-ligand-list-and-search.md`**](man/feat-005-ligand-list-and-search.md) — **Ligand Catalog List & Search Filter**
  * Complete loading of all candidate ligands from bundled `ligands.txt` into a `RecyclerView`.
  * Real-time search and query filtering via `SearchView`.
  * Accurate selection handling for filtered and unfiltered items.
  * Status: **Completed**

* [**`feat-006-ligand-network-fetching-and-error-handling.md`**](man/feat-006-ligand-network-fetching-and-error-handling.md) — **Ligand Network Fetching & Error Handling**
  * Remote structure fetching from RCSB PDB in `.cif` format (`https://files.rcsb.org/ligands/view/{ligand}.cif`).
  * Visible spinning wheel loading animation (`ProgressBar`) during asynchronous download.
  * Warning alert popup describing the exact failure cause (offline, timeout, HTTP 404, parse error).
  * Status: **Completed**

### Section IV: Third View — 3D Molecular Visualization
* [**`feat-007-3d-molecular-visualization.md`**](man/feat-007-3d-molecular-visualization.md) — **3D Molecular Visualization (Ball-and-Stick Model)**
  * Native in-app 3D rendering pipeline (spheres for atoms, cylinders for chemical bonds).
  * Normalized bounding box and automatic camera centering.
  * Standard CPK chemical coloring (C: Gray, H: White, O: Red, N: Blue, S: Yellow, P: Orange, Halogens: Green).
  * Status: **Completed**

* [**`feat-008-gesture-controls-and-camera.md`**](man/feat-008-gesture-controls-and-camera.md) — **Gesture Controls & Camera Manipulation**
  * 1-finger swipe/drag smooth 3D rotation (trackball / orbit controls).
  * 2-finger pinch gesture for responsive zoom in and out.
  * Fluid, lag-free performance targeting consistent 60 FPS.
  * Status: **Completed**

* [**`feat-009-atom-interaction-and-tooltip.md`**](man/feat-009-atom-interaction-and-tooltip.md) — **Atom Interaction & Tooltip Display**
  * 3D raycasting and object picking when tapping an atom.
  * Informational tooltip card displaying atom symbol, name, and coordinate data.
  * Automatic tooltip dismissal when tapping anywhere else on the screen.
  * Status: **Completed**

* [**`feat-010-model-sharing-and-gallery-export.md`**](man/feat-010-model-sharing-and-gallery-export.md) — **Model Sharing & Gallery Export**
  * In-view Share button capturing the active 3D framebuffer.
  * System share sheet integration via Android `Intent.ACTION_SEND` and `FileProvider`.
  * Direct image export to the device's photo album/gallery via Android `MediaStore` API.
  * Status: **Completed**

---

## 3. Bonus Features

Evaluated only after all mandatory features have been verified and scored perfectly.

* [**`bonus-001-multiple-visualization-models.md`**](man/bonus-001-multiple-visualization-models.md) — **Multiple Visualization Models**
  * Dynamic switching between Ball-and-Stick, Space-Filling (van der Waals radius), Wireframe, and Stick representations.
  * Status: **Completed**
* [**`bonus-002-advanced-user-interface.md`**](man/bonus-002-advanced-user-interface.md) — **Advanced User Interface**
  * Custom list cells, smooth transition animations, and dark mode support.
* [**`bonus-003-enhanced-molecular-interactions.md`**](man/bonus-003-enhanced-molecular-interactions.md) — **Enhanced Molecular Interactions**
  * Atom highlight rings, bond length measurements, and chemical property cards.
* [**`bonus-004-performance-and-caching.md`**](man/bonus-004-performance-and-caching.md) — **Performance & Local Caching**
  * Disk caching of downloaded CIF files, lazy loading, and memory optimization for smooth 60 FPS.
* [**`bonus-005-extended-sharing-and-export.md`**](man/bonus-005-extended-sharing-and-export.md) — **Extended Sharing & Export**
  * Custom formatted share messages, multi-format export (PNG, CIF export).
