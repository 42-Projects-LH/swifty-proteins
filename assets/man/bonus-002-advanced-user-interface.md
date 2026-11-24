# Advanced User Interface

The **Advanced User Interface** bonus features enhance the user experience and visual aesthetic of Swifty Proteins.

## Features List

Status | Feature | Decription
:--- | :--- | :---
⚪ | **Custom Table/List Cells** | Design beautiful custom cells for the ligand list with icons, previews, or additional information.
⚪ | **Smooth Animations** | Add polished transitions between views, animated loading states, and micro-interactions.
⚪ | **Dark Mode** | Implement a complete dark mode theme that works throughout the app, including the 3D view with appropriate lighting adjustments.
⚫ | **Onboarding** | Create an onboarding flow for first-time users explaining how to use the app and its features.
⚫ | **Settings Screen** | Add a settings view where users can customize preferences (default visualization model, color schemes, etc.).

>[!NOTE]
>- ⚪ Implemented
>- ⚫ Not implemented

## Custom Table/List Cells

The ligand catalog in `ProteinListActivity` renders each candidate entry using a custom `MaterialCardView` cell (`item_protein.xml`) with `12dp` rounded corners, `2dp` elevation, and a responsive `ConstraintLayout` hierarchy. Each row combines a `40dp × 40dp` circular molecular ring icon (`⬡` over `@drawable/bg_circle`), bold `17sp` typography for the ligand identifier (`tvProteinId`), a conditional `"offline"` pill badge (`tvCachedBadge` styled with `@drawable/bg_badge_cached` using a translucent cyan fill `#2201EFFE` and `1dp` border `#6601EFFE`) that dynamically appears whenever `CifCache.has(context, proteinId)` detects a locally cached `.cif` structure, and an interactive `40dp × 40dp` favorite toggle button (`btnFavorite`) that switches between `@drawable/ic_star_filled` (`#FFC832`) and `@drawable/ic_star_outline` via `FavoritesManager`.

### Relevant Files in the Codebase

| File | Type | Description |
|---|---|---|
| [`item_protein.xml`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/res/layout/item_protein.xml) | XML Layout | Custom `MaterialCardView` cell defining the molecular icon avatar, ligand ID, offline cache badge, and favorite star button. |
| [`ProteinAdapter.kt`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/java/com/example/swiftyproteins/ui/protein/ProteinAdapter.kt) | Kotlin Source | `RecyclerView.Adapter` binding ligand IDs, querying `CifCache.has()` for badge visibility, and handling `FavoritesManager.toggle()` clicks. |
| [`bg_badge_cached.xml`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/res/drawable/bg_badge_cached.xml) | XML Drawable | Rounded rectangle shape (`4dp` radius) with translucent cyan background and stroke for the `"offline"` badge. |
| [`bg_circle.xml`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/res/drawable/bg_circle.xml) | XML Drawable | Circular oval shape backing the molecular ring icon container in each list card. |
| [`ic_star_filled.xml`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/res/drawable/ic_star_filled.xml) | XML Drawable | Gold (`#FFC832`) filled star vector icon indicating a bookmarked ligand. |
| [`ic_star_outline.xml`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/res/drawable/ic_star_outline.xml) | XML Drawable | Theme-aware (`?attr/colorOnSurface`) outlined star vector icon for unbookmarked ligands. |

### How to test

1. Launch the application and authenticate to reach `ProteinListActivity`.
2. Inspect the list cells and verify that each row displays a rounded card containing the left circular `⬡` icon, the bold ligand code, and the right star button.
3. Tap the star icon on several ligands (e.g., `ATP`, `HEM`): verify that the icon toggles immediately between the outline star and the gold filled star without opening the 3D viewer, and tap the top `btnFavoritesFilter` star button to confirm real-time list filtering.
4. Tap an uncached ligand to download and open it in the 3D viewer, then navigate back to the list: verify that the cyan `"offline"` pill badge (`tvCachedBadge`) is now visible underneath the ligand ID.

## Smooth Animations

View navigation, asynchronous data loading, and interactive controls incorporate smooth visual feedback across the application lifecycle. `SplashActivity` executes a cross-fade transition (`overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)`) into `LoginActivity` after the `1100 ms` launch duration, while `ProteinListActivity` presents a modal loading overlay (`loadingOverlay` over `#88000000` dim scrim) featuring an indeterminate `56dp` spinning `ProgressBar` and context-aware status messages (`"Loading {ID}..."` for disk cache hits vs. `"Fetching {ID} from RCSB..."` for network requests). Within the 3D viewer and list cards, micro-interactions provide immediate tactile and visual response, including borderless ripple feedback (`?attr/selectableItemBackgroundBorderless`) on action buttons, opacity transitions (`alpha` toggling between `1.0f` and `0.45f`) on `btnToggleLabels`, dynamic floating tooltip card visibility (`cardAtomInfo`) with CPK color tinting (`viewAtomColor`), and continuous 60 FPS VSync-locked OpenGL rendering (`RENDERMODE_CONTINUOUSLY`).

### Relevant Files in the Codebase

| File | Type | Description |
|---|---|---|
| [`SplashActivity.kt`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/java/com/example/swiftyproteins/ui/splash/SplashActivity.kt) | Kotlin Source | Triggers the cross-fade transition (`android.R.anim.fade_in`, `android.R.anim.fade_out`) when navigating from the splash screen to `LoginActivity`. |
| [`ProteinListActivity.kt`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/java/com/example/swiftyproteins/ui/protein/ProteinListActivity.kt) | Kotlin Source | Controls the animated `loadingOverlay` visibility and updates `tvLoadingMessage` during coroutine-based CIF fetching and parsing. |
| [`activity_protein_list.xml`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/res/layout/activity_protein_list.xml) | XML Layout | Declares the modal `loadingOverlay`, indeterminate `ProgressBar` spinner, and borderless ripple action buttons. |
| [`ProteinViewerActivity.kt`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/java/com/example/swiftyproteins/ui/viewer/ProteinViewerActivity.kt) | Kotlin Source | Manages micro-interactions for atom selection tooltips (`showAtomTooltip`/`hideAtomTooltip`), label button alpha states, and in-place model cycling. |
| [`ProteinSurfaceView.kt`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/java/com/example/swiftyproteins/ui/viewer/gl/ProteinSurfaceView.kt) | Kotlin Source | Configures `RENDERMODE_CONTINUOUSLY` and smooth gesture handlers for 1-finger orbit rotation, 2-finger pinch zoom, and 2-finger panning. |

### How to test

1. Cold-start the app (`make run`) and observe the smooth cross-fade transition after the `1.1s` splash screen into `LoginActivity`.
2. In `ProteinListActivity`, tap an uncached ligand and confirm that the dimmed `loadingOverlay` and spinning `ProgressBar` appear immediately with `"Fetching {ID} from RCSB..."` and dismiss cleanly once parsing completes.
3. In `ProteinViewerActivity`, tap the **Labels** button (`btnToggleLabels`) to observe its alpha state transition (`0.45` ↔ `1.0`), tap any atom sphere to trigger the floating `cardAtomInfo` tooltip and additive-blended selection halos, and tap **Ball & Stick** (`btnToggleModel`) to verify zero-flicker in-place model transitions.

## Dark Mode

Swifty Proteins implements a Day/Night theme system built on Material 3 (`Theme.Material3.DayNight.NoActionBar`) with dedicated light (`res/values/themes.xml`) and night (`res/values-night/themes.xml`) resource qualifiers backed by semantic color tokens in `res/values/colors.xml`. When system Dark Mode is enabled, window and card surfaces switch from `#F5F7FA` (`surface_light`) and `#FFFFFF` (`card_light`) with `#1A1A2E` text (`on_surface_light`) to an OLED-optimized deep space navy `#10141D` (`surface_dark`) and elevated slate `#161B26` (`card_dark`) with high-contrast `#E2E8F0` text (`on_surface_dark`), accented by primary cyan (`#01EFFE`) and secondary pink (`#FE2F63`). The 3D molecular viewer (`activity_protein_viewer.xml` and `ProteinRenderer.kt`) integrates seamlessly with this dark palette by clearing the OpenGL viewport to `#10141D` (`GLES20.glClearColor(0.063f, 0.078f, 0.114f, 1.0f)`), framing the scene with translucent dark overlays (`#CC161B26` toolbars and `#E6181D29` tooltip card with `#3301EFFE` cyan border), and applying an eye-space Blinn-Phong shader (`ambient = 0.30`, `diffuse = 0.70`, `specular = 0.60`, directional light vector `(0.35f, 0.65f, 1.0f)`) calibrated to give 3D CPK spheres and bonds strong depth and specular contrast against the dark background.

### Relevant Files in the Codebase

| File | Type | Description |
|---|---|---|
| [`colors.xml`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/res/values/colors.xml) | XML Resource | Defines the brand accents (`cyan_accent`, `pink_accent`, `star_gold`) and paired light/dark surface and typography palettes. |
| [`values/themes.xml`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/res/values/themes.xml) | XML Resource | Configures the light-mode `Base.Theme.SwiftyProteins` surface mappings and translucent splash theme. |
| [`values-night/themes.xml`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/res/values-night/themes.xml) | XML Resource | Overrides `android:windowBackground`, `colorSurface`, and `colorOnSurface` with dark palette tokens when night mode is active. |
| [`activity_protein_viewer.xml`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/res/layout/activity_protein_viewer.xml) | XML Layout | Applies `#10141D` background and translucent dark chrome (`#CC161B26`, `#E6181D29`) around the OpenGL surface. |
| [`ProteinRenderer.kt`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/java/com/example/swiftyproteins/ui/viewer/gl/ProteinRenderer.kt) | Kotlin Source | Sets the `#10141D` OpenGL clear color, compiles the Blinn-Phong lighting shaders, and renders additive cyan/gold selection halos. |

### How to test

1. Toggle Dark Mode on the Android device or emulator via the system Quick Settings tile or via ADB:
   ```bash
   # Enable Dark Mode
   adb shell cmd uimode night yes

   # Enable Light Mode
   adb shell cmd uimode night no
   ```
2. Navigate through `LoginActivity` and `ProteinListActivity` in both modes: verify that backgrounds, Material cards, text contrast, dividers, and outlined star icons (`?attr/colorOnSurface`) adapt automatically without illegible text or visual artifacts.
3. Open any ligand in `ProteinViewerActivity` under Dark Mode: confirm that the OpenGL canvas (`#10141D`) blends seamlessly with the translucent top and bottom bars (`#CC161B26`) and that directional Blinn-Phong lighting and specular highlights clearly define the 3D geometry of even dark-colored atoms.
