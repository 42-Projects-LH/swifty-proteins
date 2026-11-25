# Swifty Proteins: Documentation Index

This directory contains technical and functional documentation for **Swifty Proteins**. Each manual in [`assets/man/`](man/) details the architecture, feature specifications, codebase references, and step-by-step testing and verification procedures.

## Mandatory Features

The mandatory part of the evaluation comprises four core functional areas. All features in this section are fully implemented, verified, and testable on both Android emulators and physical devices.

### [📂 Application Icon and Launch Screen](man/feat-001-app-icon-and-launch-screen.md)
* **Application Launcher Icon**: Custom adaptive molecular launcher icon across all density buckets (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`, and vector `anydpi-v26`).
* **Launch Screen (Splash)**: Dedicated `SplashActivity` with centered branding logo and dark gradient, enforced to stay visible for **at least 1.0 second** (timed at `1100 ms`) with smooth fade transition to login.
* **Autolayout**: Responsive `ConstraintLayout` architecture supporting all screen aspect ratios and portrait/landscape orientations.
* **Key Files**: [`SplashActivity.kt`](../app/src/main/java/com/example/swiftyproteins/ui/splash/SplashActivity.kt), [`activity_splash.xml`](../app/src/main/res/layout/activity_splash.xml), [`themes.xml`](../app/src/main/res/values/themes.xml).
* **Testing**: Cold start timing verification via `make run` / `adb logcat`, screen rotation tests.

### [📂 Login View](man/feat-002-login-view.md)
* **Dual-Mode Authentication**: Intuitive switching between User Registration (with password confirmation) and Credential Login.
* **Biometric Authentication**: Native AndroidX `BiometricPrompt` supporting Fingerprint and Face Unlock, auto-prompting enrolled users with graceful error alerts and password fallback.
* **Hardware-Backed Security**: Zero plaintext password storage; Android KeyStore `MasterKey` (`AES256_GCM`), `EncryptedSharedPreferences` (`AES256_SIV` / `AES256_GCM`), and salted `SHA-256` hashing (`SecureRandom` 16-byte salt).
* **Background Auto-Lock**: `ProcessLifecycleOwner` lifecycle tracking locks active sessions upon backgrounding (Home button, app switcher, lock screen), requiring re-authentication on return.
* **Key Files**: [`LoginActivity.kt`](../app/src/main/java/com/example/swiftyproteins/ui/auth/LoginActivity.kt), [`SecureStorage.kt`](../app/src/main/java/com/example/swiftyproteins/util/SecureStorage.kt), [`SwiftyProteinsApp.kt`](../app/src/main/java/com/example/swiftyproteins/SwiftyProteinsApp.kt), [`AppLifecycleObserver.kt`](../app/src/main/java/com/example/swiftyproteins/util/AppLifecycleObserver.kt).
* **Testing**: `make bio-success`, `make bio-fail`, `make clear-data`, `make inspect-prefs`, backgrounding auto-lock verification.

### [📂 Protein List View](man/feat-003-protein-list-view.md)
* **Ligand Catalog**: Complete loading of all candidate chemical ligands from bundled `ligands.txt` into an optimized `RecyclerView`.
* **Real-Time Search**: Integrated `SearchView` offering instant keystroke filtering, safe index mapping, and dedicated favorites filtering.
* **Asynchronous Fetching**: Remote retrieval from the RCSB Protein Data Bank API (`GET https://files.rcsb.org/ligands/view/{ligand}.cif`) via Retrofit 2 and OkHttp inside background coroutines (`Dispatchers.IO`).
* **Spinning Loading Wheel**: Modal progress overlay with a centered circular `ProgressBar` and dynamic status indicators during downloads.
* **Resilient Error Handling**: Descriptive `AlertDialog` popups for offline state (`UnknownHostException`), network timeouts (`SocketTimeoutException`), missing ligands (HTTP 404), or empty coordinate data.
* **Key Files**: [`ProteinListActivity.kt`](../app/src/main/java/com/example/swiftyproteins/ui/protein/ProteinListActivity.kt), [`ProteinAdapter.kt`](../app/src/main/java/com/example/swiftyproteins/ui/protein/ProteinAdapter.kt), [`NetworkClient.kt`](../app/src/main/java/com/example/swiftyproteins/data/network/NetworkClient.kt), [`CifParser.kt`](../app/src/main/java/com/example/swiftyproteins/data/parser/CifParser.kt).
* **Testing**: Substring search filtering, offline mode simulation, network error handling dialogs.

### [📂 Protein View](man/feat-004-protein-view.md)
* **Native 3D OpenGL ES 2.0 Engine**: Custom rendering pipeline using Phong reflection model shaders, directional lighting, and depth testing.
* **Ball-and-Stick Architecture**: Tessellated 3D UV spheres (`SphereMesh`) for atoms and aligned 3D cylinders (`CylinderMesh`) for chemical bonds.
* **Standard CPK Coloring**: Complete chemical elemental color mapping (Carbon: Gray, Hydrogen: White, Oxygen: Red, Nitrogen: Blue, Sulfur: Yellow, Phosphorus: Orange, Halogens: Green).
* **Gesture Controls**: 1-finger orbit rotation (clamped pitch to prevent inversion), 2-finger pinch-to-zoom, and 2-finger camera panning.
* **Raycasting & Atom Information**: High-precision 3D screen-to-world raycasting (`RaycastPicker`) selecting atoms on tap, displaying an informational tooltip with element name, symbol, ID, and 3D coordinates.
* **Export & Sharing**: In-app snapshot export capturing the active framebuffer via `glReadPixels`, writing directly to the device photo album (`Pictures/SwiftyProteins`) and opening the Android system share sheet with Hill chemical formula details.
* **Key Files**: [`ProteinViewerActivity.kt`](../app/src/main/java/com/example/swiftyproteins/ui/viewer/ProteinViewerActivity.kt), [`ProteinRenderer.kt`](../app/src/main/java/com/example/swiftyproteins/ui/viewer/gl/ProteinRenderer.kt), [`ProteinSurfaceView.kt`](../app/src/main/java/com/example/swiftyproteins/ui/viewer/gl/ProteinSurfaceView.kt), [`RaycastPicker.kt`](../app/src/main/java/com/example/swiftyproteins/ui/viewer/gl/RaycastPicker.kt).
* **Testing**: 3D gestures, atom selection & tooltip, gallery export, `make pull-photos`.

## Bonus Features

The bonus section extends the application with advanced biochemical visualization, performance optimizations, and enhanced interactions.

### [📂 Multiple Visualization Models](man/bonus-001-multiple-visualization-models.md)
* **Four Render Modes**:
  1. **Ball & Stick**: Baseline representation with atomic spheres and cylindrical covalent bonds.
  2. **Space-Filling (CPK / Calotte)**: Scaled by empirical Van der Waals radii displaying true steric volume.
  3. **Stick (Licorice)**: Thick cylindrical bonds with small atom joint caps for clear bond topology.
  4. **Wireframe**: Lightweight glowing line network (`GL_LINES`) displaying only the bonds without atom spheres.
* **In-Place UI Switching**: Floating toggle button cycles through models instantaneously without reloading or re-fetching data.
* **Adaptive Raycasting**: Raycast hit detection automatically expands in Space-Filling mode to match Van der Waals boundaries.
* **Key Files**: [`ProteinRenderer.kt`](../app/src/main/java/com/example/swiftyproteins/ui/viewer/gl/ProteinRenderer.kt), [`ProteinViewerActivity.kt`](../app/src/main/java/com/example/swiftyproteins/ui/viewer/ProteinViewerActivity.kt), [`Atom.kt`](../app/src/main/java/com/example/swiftyproteins/data/model/Atom.kt).

### [📂 Advanced User Interface](man/bonus-002-advanced-user-interface.md)
* **Custom Card Cells**: High-density list cards with molecular ring (`⬡`) avatars, cached availability (`offline`) pills, and bookmarking stars.
* **Smooth Navigation Animations**: Custom fade transitions across all activities and loading states.
* **Modern Dark Theme**: OLED-friendly palette (`#10141D` background, `#161B26` card surfaces, `#01EFFE` cyan accents).
* **Contextual Controls**: Camera reset, label overlays, model switching, and favorites filtering integrated into clean toolbars.
* **Key Files**: [`item_protein.xml`](../app/src/main/res/layout/item_protein.xml), [`colors.xml`](../app/src/main/res/values/colors.xml), [`themes.xml`](../app/src/main/res/values/themes.xml).

### [📂 Enhanced Molecular Interactions](man/bonus-003-enhanced-molecular-interactions.md)
* **Dual-Layer Highlighting**: Selected atoms gain a pulsing cyan selection halo, while all other atoms of the same chemical element illuminate with a warm gold glow.
* **Floating Projected Atom Labels**: 2D overlay (`AtomLabelOverlay`) transforming 3D world coordinates through MVP matrices to project floating CPK-tinted elemental pills above heavy atoms.
* **Hill Chemical Notation**: Automatic computation of empirical chemical formulas (e.g. `C10H16N5O13P3` for ATP) displayed in stats and share messages.
* **Camera Centering & Reset**: Centroid-based camera alignment and one-tap reset button.
* **Key Files**: [`AtomLabelOverlay.kt`](../app/src/main/java/com/example/swiftyproteins/ui/viewer/AtomLabelOverlay.kt), [`ProteinRenderer.kt`](../app/src/main/java/com/example/swiftyproteins/ui/viewer/gl/ProteinRenderer.kt), [`Molecule.kt`](../app/src/main/java/com/example/swiftyproteins/data/model/Molecule.kt).

### [📂 Performance and Caching](man/bonus-004-performance-and-caching.md)
* **Local Disk Caching**: Downloaded CIF files are stored in `filesDir/cif_cache/`, providing instant offline reloading and saving bandwidth.
* **Multi-Thread Coroutine Architecture**: Network and disk I/O on `Dispatchers.IO`, CPU-intensive CIF tokenization on `Dispatchers.Default`, UI rendering on `Dispatchers.Main`.
* **Zero-Allocation Render Loop**: Pre-allocated FloatBuffers and static transformation matrices avoid JVM garbage collection pauses.
* **60–120 FPS Guarantee**: Unthrottled 120 FPS frame pacing, backface culling (`GL_CULL_FACE`), and hardware depth testing (`GL_DEPTH_TEST`).
* **Key Files**: [`CifCache.kt`](../app/src/main/java/com/example/swiftyproteins/data/cache/CifCache.kt), [`CifParser.kt`](../app/src/main/java/com/example/swiftyproteins/data/parser/CifParser.kt), [`MoleculeHolder.kt`](../app/src/main/java/com/example/swiftyproteins/data/model/MoleculeHolder.kt).

### [📂 Extended Sharing and Export](man/bonus-005-extended-sharing-and-export.md)
* **Formatted Share Messages**: Enriched metadata payload including ligand name, Hill formula, and structural metrics.
* **Direct Gallery Export**: Scoped storage compliant export to device photo album (`Pictures/SwiftyProteins`) via Android `MediaStore`.
* **FileProvider Integration**: Secure content URI exposure (`content://...`) for the Android system share sheet.
* **Favorites System**: Persistent bookmarking with `FavoritesManager` in `SharedPreferences`, supporting real-time catalog filtering.
* **Key Files**: [`ProteinViewerActivity.kt`](../app/src/main/java/com/example/swiftyproteins/ui/viewer/ProteinViewerActivity.kt), [`FavoritesManager.kt`](../app/src/main/java/com/example/swiftyproteins/data/favorites/FavoritesManager.kt), [`file_paths.xml`](../app/src/main/res/xml/file_paths.xml).

## Environment & Tooling

### [📂 Android Emulator and 42 School Environment Setup](man/env-001-emulator-setup.md)
* **Makefile SDK & Emulator Automation**: Automatic `ANDROID_HOME` detection across `~/goinfre`, `/sgoinfre`, and `$HOME`, stale `local.properties` cleanup, and background emulator auto-boot via `make start-emu` / `make emu`.
* **42 School PC (`goinfre`) Setup**: Step-by-step commands to symlink `~/.gradle` and `~/.android/avd` into `~/goinfre` to avoid `$HOME` NFS disk quota limits.
* **Headless CLI Emulator Installation**: Terminal-only setup using `sdkmanager` and `avdmanager` to install `emulator`, download an x86_64 system image, and create a `pixel` virtual device without opening Android Studio.
* **Biometric Simulation**: Enrolling and testing virtual fingerprints with `make bio-success` and `make bio-fail`.
* **Key Files**: [`Makefile`](../Makefile).

## Quick Reference Index

| ID | Title | Category | Status | Documentation Link |
|---|---|---|---|---|
| `env-001` | Android Emulator and 42 School Environment Setup | Environment | Completed | [man/env-001-emulator-setup.md](man/env-001-emulator-setup.md) |
| `feat-001` | Application Icon and Launch Screen | Mandatory | Completed | [man/feat-001-app-icon-and-launch-screen.md](man/feat-001-app-icon-and-launch-screen.md) |
| `feat-002` | Login View | Mandatory | Completed | [man/feat-002-login-view.md](man/feat-002-login-view.md) |
| `feat-003` | Protein List View | Mandatory | Completed | [man/feat-003-protein-list-view.md](man/feat-003-protein-list-view.md) |
| `feat-004` | Protein View | Mandatory | Completed | [man/feat-004-protein-view.md](man/feat-004-protein-view.md) |
| `bonus-001` | Multiple Visualization Models | Bonus | Completed | [man/bonus-001-multiple-visualization-models.md](man/bonus-001-multiple-visualization-models.md) |
| `bonus-002` | Advanced User Interface | Bonus | Completed | [man/bonus-002-advanced-user-interface.md](man/bonus-002-advanced-user-interface.md) |
| `bonus-003` | Enhanced Molecular Interactions | Bonus | Completed | [man/bonus-003-enhanced-molecular-interactions.md](man/bonus-003-enhanced-molecular-interactions.md) |
| `bonus-004` | Performance and Caching | Bonus | Completed | [man/bonus-004-performance-and-caching.md](man/bonus-004-performance-and-caching.md) |
| `bonus-005` | Extended Sharing and Export | Bonus | Completed | [man/bonus-005-extended-sharing-and-export.md](man/bonus-005-extended-sharing-and-export.md) |
