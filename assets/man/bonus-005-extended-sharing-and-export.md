# Extended Sharing and Export

The **Extended Sharing and Export** features enhance the application's sharing capabilities.

## Features List

Status | Feature | Decription
:--- | :--- | :---
⚪ | **Custom Share Messages** | When sharing, include a custom message with the ligand name, number of atoms, and molecular formula.
⚫ | **Multiple Export Formats** | Allow exporting the view as different image formats (PNG, JPEG) or even as a 3D model file.
⚫ | **Video Recording** | Record a short video of the molecule rotating and share it.
⚪ | **Favorites System** | Allow users to favorite ligands and maintain a favorites list for quick access.
⚫ | **Comparison View** | Display two molecules side-by-side for comparison.

>[!NOTE]
>- ⚪ Implemented
>- ⚫ Not implemented

## Custom Share Messages

When the user taps the share action button (`btnShare`) in `ProteinViewerActivity`, `captureAndShareModel()` captures the live OpenGL ES 2.0 framebuffer via `GLES20.glReadPixels()`, flips and converts the pixel buffer from bottom-up `RGBA` to top-down `ARGB_8888`, saves a lossless PNG copy to the device photo gallery (`MediaStore` under `Pictures/SwiftyProteins/Protein_{ligandName}_{timestamp}.png`), and shares the rendered snapshot (`cacheDir/images/shared_protein_{ligandName}.png` via `FileProvider`) alongside a dynamically generated scientific message in `shareBitmap()`. The custom share payload includes the ligand identifier (`🔬 Ligand: {ligandName}`), the empirical chemical formula (`Formula: {formula}`) computed automatically in `Molecule.molecularFormula` using standard Hill system notation (`C` first, `H` second, followed by all remaining elements in alphabetical order, e.g., `C10H16N5O13P3` for `ATP`), the exact atom and bond counts (`Structure: {atomCount} atoms · {bondCount} bonds`), and attribution (`Rendered with Swifty Proteins — RCSB PDB`), attached via `Intent.EXTRA_TEXT` and `Intent.EXTRA_SUBJECT` (`"Swifty Proteins: $ligandName ($formula)"`).

### Relevant Files in the Codebase

| File | Type | Description |
|---|---|---|
| [`ProteinViewerActivity.kt`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/java/com/example/swiftyproteins/ui/viewer/ProteinViewerActivity.kt) | Kotlin Source | Implements `captureAndShareModel()`, `saveToPhotoAlbum()`, and `shareBitmap()`, building the formatted Hill-formula share message and launching `Intent.ACTION_SEND`. |
| [`Molecule.kt`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/java/com/example/swiftyproteins/data/model/Molecule.kt) | Kotlin Source | Computes `molecularFormula` in Hill notation by grouping and counting elemental symbols across `atoms`. |
| [`ProteinRenderer.kt`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/java/com/example/swiftyproteins/ui/viewer/gl/ProteinRenderer.kt) | Kotlin Source | Executes `captureAndDispatchScreenshot()` on the GL thread using `GLES20.glReadPixels()` and vertical row inversion. |
| [`file_paths.xml`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/res/xml/file_paths.xml) | XML Resource | Declares the `cache-path` (`images/`) exposed by `androidx.core.content.FileProvider` for secure image URI sharing. |
| [`AndroidManifest.xml`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/AndroidManifest.xml) | Manifest | Registers the `FileProvider` authority (`${applicationId}.fileprovider`) with `grantUriPermissions="true"`. |

### How to test

1. Open any ligand (e.g., `ATP` or `HEM`) in `ProteinViewerActivity`, rotate or zoom to a desired angle, and tap the top-right **Share** button (`btnShare`).
2. Verify that a toast confirms `"Saved snapshot to Photo Album"` and the Android system share chooser appears displaying both the 3D PNG preview and the custom text message containing the ligand ID, Hill molecular formula (e.g., `Formula: C10H16N5O13P3`), and atom/bond counts.
3. Share the payload to any target app (e.g., Gmail, Messages, Drive, or Notes) or copy the text, and verify that both the rendered PNG image and the formatted scientific metadata are delivered intact.

## Favorites System

`FavoritesManager` provides persistent ligand bookmarking backed by private `SharedPreferences` (`swifty_favorites` with string-set key `favorites_set`), exposing `getAll()`, `isFavorite()`, and `toggle()` operations normalized to uppercase ligand identifiers. In the catalog (`ProteinListActivity` and `ProteinAdapter`), every custom card cell (`item_protein.xml`) displays an interactive star button (`btnFavorite`) bound to `@drawable/ic_star_filled` (`#FFC832`) when bookmarked or `@drawable/ic_star_outline` when unbookmarked, allowing users to add or remove favorites with a single tap directly from the list. Tapping the top-right star filter button (`btnFavoritesFilter` in `activity_protein_list.xml`) toggles `adapter.setShowFavoritesOnly(!active)`, which combines with the live `SearchView` query in `ProteinAdapter.rebuildFilter()` so users can view and search exclusively within their saved favorites list across app restarts.

### Relevant Files in the Codebase

| File | Type | Description |
|---|---|---|
| [`FavoritesManager.kt`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/java/com/example/swiftyproteins/data/favorites/FavoritesManager.kt) | Kotlin Source | Singleton persisting the set of favorited ligand IDs in `SharedPreferences` (`swifty_favorites`). |
| [`ProteinAdapter.kt`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/java/com/example/swiftyproteins/ui/protein/ProteinAdapter.kt) | Kotlin Source | Binds star icon states, handles `btnFavorite` click toggles, and filters the dataset via `setShowFavoritesOnly()` and `rebuildFilter()`. |
| [`ProteinListActivity.kt`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/java/com/example/swiftyproteins/ui/protein/ProteinListActivity.kt) | Kotlin Source | Wires the header `btnFavoritesFilter` button to toggle favorite-only filtering on the adapter. |
| [`activity_protein_list.xml`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/res/layout/activity_protein_list.xml) | XML Layout | Places the `btnFavoritesFilter` star button alongside the `SearchView` in the catalog header. |
| [`item_protein.xml`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/res/layout/item_protein.xml) | XML Layout | Defines the per-row `btnFavorite` star toggle button inside each ligand card. |

### How to test

1. In `ProteinListActivity`, tap the star icon (`btnFavorite`) on 2–3 ligands (e.g., `0E5`, `ATP`, `HEM`): verify that each star immediately switches to the gold filled icon (`ic_star_filled`).
2. Tap the top-right favorites filter button (`btnFavoritesFilter`) next to the search bar: verify that the list filters immediately to display only the bookmarked ligands, and type a query in `SearchView` to confirm search works within the filtered favorites.
3. Un-star one ligand while the favorites filter is active: verify that it is immediately removed from the filtered favorites list.
4. Close and relaunch the application (`make restart`), log back in, and verify that all bookmarked ligands retain their gold star state from `SharedPreferences`.
