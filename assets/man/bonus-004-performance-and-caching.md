# Performance and Caching

The **Performance and Caching** features optimize the application for better performance and offline capability.

## Features List

Status | Feature | Description
:--- | :--- | :---
⚪ | **Local Caching** | Cache downloaded `.cif` files locally so previously viewed ligands can be accessed offline.
⚪ | **Lazy Loading** | Implement efficient lazy loading for the ligand list if it contains many items.
⚪ | **Background Parsing** | Parse `.cif` files on a background thread with progress indication.
⚪ | **Memory Management** | Implement efficient memory management for large molecules, possibly using level-of-detail (LOD) techniques.
⚪ | **60 FPS Guarantee** | Ensure smooth 60 FPS rendering even for complex molecules with many atoms.

>[!NOTE]
>- ⚪ Implemented
>- ⚫ Not implemented

## Local Caching

`CifCache` persists downloaded Crystallographic Information Files (`.cif`) inside the application's private internal storage directory under `context.filesDir/cif_cache/{LIGAND_ID}.cif`. Whenever a user selects a ligand in `ProteinListActivity`, `CifCache.has(context, proteinId)` checks local disk storage before initiating any network request: on a cache hit, `CifCache.read()` loads the `.cif` file directly from disk in milliseconds (enabling full offline access to previously viewed molecules), whereas on a cache miss, the structure is fetched from the RCSB PDB endpoint (`NetworkClient.apiService.getLigandCif(proteinId)`) and immediately saved to disk via `CifCache.write()`. In the catalog list, `ProteinAdapter` queries `CifCache.has()` for each visible row and displays the cyan `"offline"` pill badge (`tvCachedBadge` with `@drawable/bg_badge_cached`) on all locally cached ligands.

### Relevant Files in the Codebase

| File | Type | Description |
|---|---|---|
| [`CifCache.kt`](../../app/src/main/java/com/example/swiftyproteins/data/cache/CifCache.kt) | Kotlin Source | Manages persistent disk caching (`has`, `read`, `write`) under `context.filesDir/cif_cache/`. |
| [`ProteinListActivity.kt`](../../app/src/main/java/com/example/swiftyproteins/ui/protein/ProteinListActivity.kt) | Kotlin Source | Implements the cache-first lookup strategy in `onProteinSelected()`, bypassing network calls on cache hits and writing new downloads to `CifCache`. |
| [`ProteinAdapter.kt`](../../app/src/main/java/com/example/swiftyproteins/ui/protein/ProteinAdapter.kt) | Kotlin Source | Toggles `tvCachedBadge` (`"offline"`) visibility based on `CifCache.has(context, proteinId)`. |
| [`bg_badge_cached.xml`](../../app/src/main/res/drawable/bg_badge_cached.xml) | XML Drawable | Rounded cyan badge drawable indicating that a ligand is cached on device storage. |

### How to test

1. Select an uncached ligand (e.g., `ATP` or `HEM`) while connected to the internet: observe the `"Fetching {ID} from RCSB..."` loading message as it downloads and opens in `ProteinViewerActivity`.
2. Navigate back to `ProteinListActivity` and verify that the ligand card now displays the cyan `"offline"` badge (`tvCachedBadge`).
3. Disable network connectivity on the phone (enable **Airplane Mode** or turn off Wi-Fi and mobile data) and tap the cached ligand again: confirm that it loads instantaneously with `"Loading {ID}..."` and renders in 3D while completely offline.

## Lazy Loading

Rather than pre-downloading or parsing `.cif` structures upfront for the entire catalog in `ligands.txt`, `ProteinListActivity` ingests only the lightweight string identifiers at startup (`loadLigandsFromAssets()`) and delegates list rendering to a `RecyclerView` backed by `LinearLayoutManager` and `ProteinAdapter`. `RecyclerView` lazily inflates and recycles a small pool of `ProteinViewHolder` item views on demand as they enter the visible viewport during scrolling, while full `.cif` network retrieval and 3D coordinate parsing are deferred strictly until the user taps a specific ligand row.

### Relevant Files in the Codebase

| File | Type | Description |
|---|---|---|
| [`ProteinListActivity.kt`](../../app/src/main/java/com/example/swiftyproteins/ui/protein/ProteinListActivity.kt) | Kotlin Source | Streams lightweight ligand IDs from `assets/ligands.txt` via `BufferedReader` and triggers CIF loading only on item selection. |
| [`ProteinAdapter.kt`](../../app/src/main/java/com/example/swiftyproteins/ui/protein/ProteinAdapter.kt) | Kotlin Source | Recycles `ProteinViewHolder` instances and binds row state (`tvProteinId`, `tvCachedBadge`, `btnFavorite`) only for visible viewport items. |
| [`activity_protein_list.xml`](../../app/src/main/res/layout/activity_protein_list.xml) | XML Layout | Declares the scrollable `RecyclerView` (`rvProteins`) hosting the ligand catalog. |
| [`ligands.txt`](../../app/src/main/assets/ligands.txt) | Asset File | Bundled catalog of candidate ligand identifiers loaded lazily into the adapter. |

### How to test

1. Launch the app and log in to open `ProteinListActivity`: verify that the full catalog of ligands appears immediately on startup without network delay.
2. Fling-scroll rapidly from the top to the bottom of the ligand list: confirm that scrolling remains smooth and responsive as `RecyclerView` recycles view holders on demand.
3. Type a substring in the top `SearchView` (e.g., `"NA"`) and tap a filtered result: confirm that only the selected ligand's `.cif` data is fetched and parsed on demand.

## Background Parsing

All network I/O, disk cache access, and CIF parsing in `ProteinListActivity.onProteinSelected()` are offloaded from the Android main UI thread using Kotlin Coroutines (`lifecycleScope.launch`). As soon as a user taps a ligand, the UI thread displays the modal `loadingOverlay` with an indeterminate spinning `ProgressBar` and updates `tvLoadingMessage` (`"Loading {ID}..."` for cached files or `"Fetching {ID} from RCSB..."` for network downloads). File reading, writing, and Retrofit HTTP calls execute inside `withContext(Dispatchers.IO)`, after which the raw `.cif` payload is handed off to `withContext(Dispatchers.Default)` where `CifParser.parse()` tokenizes `_chem_comp_atom` coordinate loops and `_chem_comp_bond` connectivity loops on a background CPU worker pool before returning the completed `Molecule` to the main thread and hiding `loadingOverlay`.

### Relevant Files in the Codebase

| File | Type | Description |
|---|---|---|
| [`ProteinListActivity.kt`](../../app/src/main/java/com/example/swiftyproteins/ui/protein/ProteinListActivity.kt) | Kotlin Source | Coordinates `lifecycleScope.launch`, `withContext(Dispatchers.IO)` for I/O, `withContext(Dispatchers.Default)` for parsing, and `loadingOverlay` progress feedback. |
| [`CifParser.kt`](../../app/src/main/java/com/example/swiftyproteins/data/parser/CifParser.kt) | Kotlin Source | Parses `_chem_comp_atom` (experimental `model_Cartn_*` or ideal `pdbx_model_Cartn_*_ideal`) and `_chem_comp_bond` loops into a `Molecule` model. |
| [`NetworkClient.kt`](../../app/src/main/java/com/example/swiftyproteins/data/network/NetworkClient.kt) | Kotlin Source | Configures Retrofit 2 and OkHttp 3 with 15-second timeouts for asynchronous background CIF downloads. |
| [`activity_protein_list.xml`](../../app/src/main/res/layout/activity_protein_list.xml) | XML Layout | Defines the modal `loadingOverlay` with `progressBar` and `tvLoadingMessage` displayed during background operations. |

### How to test

1. Tap an uncached ligand in `ProteinListActivity` and observe the centered spinning `ProgressBar` and status message (`"Fetching {ID} from RCSB..."`) remain animating smoothly without UI freezes or ANR warnings while downloading and parsing occur in the background.
2. Verify that once background parsing finishes, `loadingOverlay` hides automatically, a toast displays `"Loaded {ID}: {N} atoms, {M} bonds"`, and `ProteinViewerActivity` opens.

## Memory Management

To handle complex ligands without heap churn or serialization overhead, the application implements three memory optimizations across the data and OpenGL ES 2.0 layers. First, `SphereMesh` (`stacks = 20, slices = 20`) and `CylinderMesh` (`segments = 16`) allocate a single procedural unit sphere and unit cylinder in off-heap native memory using direct NIO buffers (`ByteBuffer.allocateDirect(...).order(ByteOrder.nativeOrder())`), which `ProteinRenderer` reuses across all atoms and bonds via per-instance transformation matrices rather than duplicating mesh geometry per atom. Second, `ProteinRenderer` pre-allocates reusable 16-float transformation matrices (`projectionMatrix`, `viewMatrix`, `modelMatrix`, `mvMatrix`, `mvpMatrix`, `tempMatrix`) and a native `wireframeBuffer` once per molecule load. Third, `MoleculeHolder` transfers the parsed `Molecule` object between `ProteinListActivity` and `ProteinViewerActivity` via an in-memory singleton reference rather than `Intent` extras, avoiding heavy `Serializable`/`Parcelable` copying and Android Binder `TransactionTooLargeException`.

### Relevant Files in the Codebase

| File | Type | Description |
|---|---|---|
| [`SphereMesh.kt`](../../app/src/main/java/com/example/swiftyproteins/ui/viewer/gl/SphereMesh.kt) | Kotlin Source | Allocates off-heap direct native `FloatBuffer` and `ShortBuffer` instances for unit sphere vertices, normals, and triangle indices. |
| [`CylinderMesh.kt`](../../app/src/main/java/com/example/swiftyproteins/ui/viewer/gl/CylinderMesh.kt) | Kotlin Source | Allocates off-heap direct native `FloatBuffer` and `ShortBuffer` instances for unit cylinder bond geometry. |
| [`ProteinRenderer.kt`](../../app/src/main/java/com/example/swiftyproteins/ui/viewer/gl/ProteinRenderer.kt) | Kotlin Source | Reuses pre-allocated matrix arrays and single `SphereMesh`/`CylinderMesh` instances across all atoms and bonds in `onDrawFrame()`. |
| [`MoleculeHolder.kt`](../../app/src/main/java/com/example/swiftyproteins/data/model/MoleculeHolder.kt) | Kotlin Source | In-memory singleton passing the active `Molecule` between activities with zero Binder serialization overhead. |

### How to test

1. Open several large, atom-dense ligands in succession (e.g., `ATP`, `FAD`, `HEM`, `NAD`) and cycle through all four visualization models (`Ball & Stick`, `Space-Filling`, `Stick`, `Wireframe`).
2. Confirm that transitions between `ProteinListActivity` and `ProteinViewerActivity` are instantaneous and that switching visualization models happens in-place without memory spikes or crashes.

## 60 FPS Guarantee

`ProteinSurfaceView` drives the OpenGL ES 2.0 rendering thread in continuous mode (`renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY`) with unthrottled swap interval (`EGL14.eglSwapInterval(..., 0)`), while `ProteinRenderer` enforces a nanosecond frame-interval cap at `120 FPS` (`MIN_FRAME_INTERVAL_NS = 1_000_000_000L / 120`) and samples effective render frame times (`1_000_000_000L / (MIN_FRAME_INTERVAL_NS + renderDurationNs)`) every `250 ms` (`onFpsUpdateListener`) displayed directly in the top app bar subtitle (`tvMoleculeStats`, e.g., `"43 Atoms • 44 Bonds • 119 FPS"`). Inside `ProteinRenderer`, GPU rendering throughput is optimized by enabling hardware backface culling (`GLES20.glEnable(GLES20.GL_CULL_FACE)` with `GLES20.GL_BACK`, discarding back-facing triangles on every sphere and cylinder before fragment shading), hardware depth testing (`GLES20.glEnable(GLES20.GL_DEPTH_TEST)` with `GLES20.GL_LEQUAL`), indexed triangle drawing (`GLES20.glDrawElements`), and O(1) hash-map atom lookups (`atomMap`) when constructing bond transforms.

### Relevant Files in the Codebase

| File | Type | Description |
|---|---|---|
| [`ProteinRenderer.kt`](../../app/src/main/java/com/example/swiftyproteins/ui/viewer/gl/ProteinRenderer.kt) | Kotlin Source | Configures `GL_CULL_FACE`, `GL_DEPTH_TEST`, 120 FPS frame pacing, instantaneous frame-delta FPS measurement (`onFpsUpdateListener`), and `atomMap` O(1) bond endpoint lookup. |
| [`ProteinViewerActivity.kt`](../../app/src/main/java/com/example/swiftyproteins/ui/viewer/ProteinViewerActivity.kt) | Kotlin Source | Subscribes to `renderer.onFpsUpdateListener` and updates the live `"• {fps} FPS"` readout in `tvMoleculeStats`. |
| [`ProteinSurfaceView.kt`](../../app/src/main/java/com/example/swiftyproteins/ui/viewer/gl/ProteinSurfaceView.kt) | Kotlin Source | Initializes the OpenGL ES 2.0 context with `RENDERMODE_CONTINUOUSLY` and processes touch gestures with minimal latency. |
| [`SphereMesh.kt`](../../app/src/main/java/com/example/swiftyproteins/ui/viewer/gl/SphereMesh.kt) | Kotlin Source | Renders indexed triangle meshes via `GLES20.glDrawElements(GLES20.GL_TRIANGLES, ...)`. |
| [`CylinderMesh.kt`](../../app/src/main/java/com/example/swiftyproteins/ui/viewer/gl/CylinderMesh.kt) | Kotlin Source | Renders indexed cylinder meshes via `GLES20.glDrawElements(GLES20.GL_TRIANGLES, ...)`. |

### How to test

1. Open a complex ligand (e.g., `ATP`, `HEM`, or `FAD`) in `ProteinViewerActivity` and observe the live FPS readout in the top bar subtitle (`"{Atoms} Atoms • {Bonds} Bonds • {FPS} FPS"`).
2. Continuously rotate, pinch-to-zoom, and pan the model with **Labels** and **Atom Highlighting** active: verify that the live FPS counter maintains a steady 60–120 FPS (capped at `120 FPS` on high-refresh-rate displays) with no stutter or lag.
3. Optionally inspect GPU frame timing statistics on the connected device via ADB (targeting `< 16.6 ms` per frame):
   ```bash
   adb shell dumpsys gfxinfo com.example.swiftyproteins framestats
   ```
