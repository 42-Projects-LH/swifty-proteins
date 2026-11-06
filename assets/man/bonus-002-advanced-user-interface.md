# Advanced User Interface

## Overview

The **Advanced User Interface** bonus enhances the user experience and visual aesthetic of Swifty Proteins far beyond stock Android views. It incorporates custom elevation card lists, smooth activity transitions, an OLED-optimized dark theme palette, informative onboarding states, and streamlined contextual toolbar controls.

---

## Feature Specifications

### 1. Custom Table/List Cells
The protein catalog uses a custom Material card layout (`item_protein.xml`) specifically designed for density and readability:
- **Elemental Badge**: Circular gradient container displaying the first letter of the ligand code.
- **Ligand Identifier**: Crisp, high-contrast monospace typography.
- **Local Cache Pill**: Visual `CACHED` badge (`bg_badge_cached.xml`) appearing dynamically if the CIF file is already saved to local disk.
- **Interactive Favorite Toggle**: Quick-action star icon (`ic_star_filled` / `ic_star_outline`) allowing users to bookmark molecules directly from the list without entering the viewer.
- **Touch Feedback**: Bounded ripple effect providing immediate tactile confirmation upon tapping.

### 2. Smooth Animations
- **Activity Fade Transitions**: Custom transition pairs (`android.R.anim.fade_in` and `android.R.anim.fade_out`) smooth out navigation between Splash, Login, Catalog, and Viewer views.
- **Modal Loading Transitions**: Smooth alpha fading of the centered loading wheel overlay when network fetches initiate or resolve.
- **Camera Interpolation**: One-tap camera reset smoothly re-aligns view transforms to initial angles and zoom levels.

### 3. Dark Mode & Modern Color Palette
- Optimized for modern OLED and AMOLED mobile displays to minimize battery drain and reduce eye strain in low-light environments.
- Defined in `res/values/colors.xml` and `res/values-night/themes.xml`:
  - **Surface / Background**: Deep space navy (`#10141D` and `#0B0E14`).
  - **Card Surfaces**: Elevated dark slate (`#1A2232` and `#222D42`).
  - **Primary Accents**: High-visibility cyan (`#00E5FF` / `#02F0FF`) and gold (`#FFD700`).
  - **Text Hierarchy**: White (`#FFFFFF`) for primary titles, muted silver (`#9AA0A6`) for secondary metrics and coordinates.

### 4. Onboarding & Usability Guidance
- **Empty Catalog Guidance**: When search filters return zero matching ligands, the catalog provides immediate feedback without UI glitching.
- **Registration vs. Login Guidance**: Automatic mode selection based on whether an account exists, with intuitive error dialogs guiding users through password requirements (>= 4 characters) or username collision resolution.
- **Biometric Enrollment Guidance**: Direct deep-linking to Android Security Settings (`Settings.ACTION_SECURITY_SETTINGS`) if the user attempts biometric authentication without previously enrolling fingerprints.

### 5. Settings Screen & Controls
Contextual controls embedded directly in the 3D viewer toolbar:
- **Reset Camera Control**: Restores canonical orientation with a single tap.
- **Floating Atom Labels**: One-tap toggle button displaying elemental symbol pills above heavy atoms.
- **Model Switcher**: Direct cycling between Ball & Stick, Space-Filling, Stick, and Wireframe.
- **Favorites Catalog Filter**: Instant filter toggle isolating favorited ligands in the catalog.

---

## Relevant Files in the Codebase

| File | Type | Description |
|---|---|---|
| [`item_protein.xml`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/res/layout/item_protein.xml) | XML Layout | Custom card cell layout featuring elemental badge, cache pill, and favorite star. |
| [`activity_protein_list.xml`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/res/layout/activity_protein_list.xml) | XML Layout | Catalog layout incorporating top search bar, favorites filter button, and loading overlay. |
| [`activity_protein_viewer.xml`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/res/layout/activity_protein_viewer.xml) | XML Layout | Viewer layout with floating control buttons, stats bar, and atom tooltip card. |
| [`colors.xml`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/res/values/colors.xml) | XML Resource | Defines modern color palette, accents, and CPK mapping references. |
| [`themes.xml`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/res/values/themes.xml) | XML Resource | Application-wide styling, full-screen splash theme, and widget styling. |
| [`ProteinAdapter.kt`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/java/com/example/swiftyproteins/ui/protein/ProteinAdapter.kt) | Kotlin Source | Binds custom cell views, handles favorite toggles, and updates cached badges. |

---

## Testing and Verification

### 1. Manual Evaluation Scenarios

#### Scenario A: Custom List Cell Verification
1. Open the protein list view.
2. Confirm the custom cell structure:
   - Left: Circular letter avatar.
   - Middle: Bold ligand ID.
   - Right: Star toggle button.
3. Tap the star icon on any ligand (e.g. `ATP`). Confirm the icon toggles to a filled yellow star immediately.
4. Tap the top **Favorites Filter** button. Confirm that only starred molecules remain visible.

#### Scenario B: Dark Mode & Contrast Verification
1. Verify readability under system Dark Mode and Default modes.
2. Ensure text contrast against dark cards satisfies accessibility standards (no dark gray text on black backgrounds).
3. Confirm that the 3D viewer canvas background (`#10141D`) smoothly matches the surrounding chrome.

#### Scenario C: Navigation Animations
1. Launch the app and note the fade transition from the splash screen to login view.
2. Log in and note the fade transition into `ProteinListActivity`.
3. Open a molecule and return. Confirm no screen tearing or abrupt layout jumps occur.

### 2. Evaluation Checklist Alignment
- [x] Custom designed list cells with badges and state indicators.
- [x] Polished, consistent dark mode color system.
- [x] Smooth transition animations across activities.
- [x] Clear guidance and dialog feedback throughout the application.
