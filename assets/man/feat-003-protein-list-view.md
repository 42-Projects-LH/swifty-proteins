# Protein List View

## Overview

The **Protein List View** serves as the primary catalog and discovery screen of the application. It loads the candidate chemical ligand identifiers bundled in the app assets, renders them inside a high-performance scrollable `RecyclerView`, and allows instantaneous substring search filtering. When an item is selected, the application asynchronously fetches the corresponding Crystallographic Information File (`.cif`) from the **RCSB Protein Data Bank**, displaying a centered spinning loading wheel until data download and parsing are complete, backed by resilient error handling.

---

## Feature Specifications

### 1. Catalog Loading & Dataset
- **Asset Ingestion**: On creation, `ProteinListActivity` reads `ligands.txt` line-by-line from bundled assets via `BufferedReader`.
- **High-Performance RecyclerView**: Utilizes `ProteinAdapter` with `LinearLayoutManager` for smooth scrolling and view recycling.
- **Card Design**: Custom list item cards (`item_protein.xml`) displaying the ligand identifier, elemental badge icon, local cache availability badge (`CACHED`), and interactive favorite toggle star.

### 2. Search & Real-Time Query Filtering
- Integrated Android `SearchView` at the top of the view.
- **Instantaneous Filtering**: List updates on each keystroke via `SearchView.OnQueryTextListener.onQueryTextChange()`.
- **Accurate Selection**: Selecting an item from filtered results opens the exact clicked ligand, avoiding index mismatch errors between filtered and raw lists.
- **Favorites Filter**: Dedicated star filter button in the action bar to toggle between displaying all ligands or only user-favorited items.

### 3. Remote Data Fetching (RCSB PDB API)
- Connects directly to the official RCSB PDB repository:
  `GET https://files.rcsb.org/ligands/view/{ligand}.cif`
- **Network Pipeline**: Built with Square **Retrofit 2** and **OkHttp 3** using a scalar string converter for raw CIF text retrieval.
- **Asynchronous Execution**: Executed off the main thread inside Kotlin Coroutines (`lifecycleScope.launch(Dispatchers.IO)`), preventing any UI frame drops or ANR (Application Not Responding) warnings.
- **Local Disk Cache Bypass**: If the requested ligand has been previously downloaded, `CifCache` reads directly from local storage, skipping network latency.

### 4. Spinning Wheel Loading Animation
- In accordance with the 42 subject requirements, whenever a ligand is tapped:
  - A centered modal loading overlay (`loadingOverlay`) becomes visible with a circular spinning wheel (`ProgressBar`).
  - The status message updates dynamically:
    - `"Loading {ID}..."` (from cache)
    - `"Fetching {ID} from RCSB..."` (from network)
  - The overlay remains visible throughout downloading and parsing, and disappears automatically once the 3D viewer is ready.

### 5. Resilient Error Handling & User Alerts
Any failure encountered during retrieval or parsing triggers an informative `AlertDialog` with descriptive guidance:
- **No Internet / Offline**: Catches `UnknownHostException` and displays an alert indicating network disconnection.
- **Connection Timeout**: Catches `SocketTimeoutException` (15-second connect/read timeout) and prompts the user to retry.
- **HTTP 404 / Missing Ligand**: Inspects HTTP status codes; informs the user if a record does not exist on the RCSB servers.
- **Malformed / Empty CIF Data**: Checks if parsed molecule contains 0 atoms; alerts the user that coordinate data is missing.

---

## Relevant Files in the Codebase

| File | Type | Description |
|---|---|---|
| [`ProteinListActivity.kt`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/java/com/example/swiftyproteins/ui/protein/ProteinListActivity.kt) | Kotlin Source | Coordinates catalog loading, search bar events, coroutine network dispatch, and loading overlay state. |
| [`ProteinAdapter.kt`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/java/com/example/swiftyproteins/ui/protein/ProteinAdapter.kt) | Kotlin Source | RecyclerView adapter supporting real-time filtering, favorites filtering, and cache badges. |
| [`NetworkClient.kt`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/java/com/example/swiftyproteins/data/network/NetworkClient.kt) | Kotlin Source | Configures Retrofit and OkHttp with logging interceptor and 15s timeout thresholds. |
| [`RcsbApiService.kt`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/java/com/example/swiftyproteins/data/network/RcsbApiService.kt) | Kotlin Source | Retrofit interface for fetching ligand CIF content by ID. |
| [`CifParser.kt`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/java/com/example/swiftyproteins/data/parser/CifParser.kt) | Kotlin Source | Parses CIF loops (`_chem_comp_atom` and `_chem_comp_bond`) into 3D `Molecule` models. |
| [`CifCache.kt`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/java/com/example/swiftyproteins/data/cache/CifCache.kt) | Kotlin Source | Manages disk caching under `filesDir/cif_cache/` to minimize network overhead. |
| [`activity_protein_list.xml`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/res/layout/activity_protein_list.xml) | XML Layout | Layout defining search bar, favorites filter button, RecyclerView, and spinning wheel overlay. |
| [`item_protein.xml`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/res/layout/item_protein.xml) | XML Layout | Layout for individual ligand cards in the catalog. |
| [`ligands.txt`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/assets/ligands.txt) | Asset | Bundled text file listing candidate ligand identifiers. |

---

## Testing and Verification

### 1. Manual Evaluation Scenarios

#### Scenario A: Catalog Display & Scrolling
1. Log in to the application.
2. Verify that the list loads candidate ligands from `ligands.txt`.
3. Scroll rapidly through the list. Verify smooth 60 FPS scrolling without stutter.

#### Scenario B: Search Filter Verification
1. Tap the `SearchView` in the header.
2. Type `"ATP"` or `"HEM"`. Verify that the list narrows down in real time to matching entries.
3. Tap on the filtered result. Confirm that the exact chosen ligand is opened (not an off-target item due to indexing bugs).
4. Clear the search bar. Verify the full list is restored.

#### Scenario C: Spinning Wheel & Loading Flow
1. Select an uncached ligand (e.g. `NAG`, `FAD`, or `GLC`).
2. Verify that the loading overlay immediately appears with a visible spinning wheel (`ProgressBar`) and message `"Fetching ... from RCSB..."`.
3. Verify that once download and parsing complete, a toast appears confirming atom/bond count, and `ProteinViewerActivity` launches.

#### Scenario D: Error Handling (Offline & Bad Ligands)
1. **Offline Mode**: Put device into Airplane Mode or disable Wi-Fi/cellular data via ADB:
   ```bash
   adb shell svc wifi disable && adb shell svc data disable
   ```
2. Select an uncached ligand.
3. Verify that the spinning wheel stops and an alert dialog titled **"Network Error"** appears informing the user to check their internet connection.
4. Re-enable network:
   ```bash
   adb shell svc wifi enable && adb shell svc data enable
   ```

### 2. ADB / Makefile Commands

```bash
# Monitor live network fetching and parsing in logcat
make logs

# Inspect locally cached CIF files on device
adb shell run-as com.example.swiftyproteins ls -la /data/data/com.example.swiftyproteins/files/cif_cache/

# Clear cached CIF files to force fresh network downloads
adb shell run-as com.example.swiftyproteins rm -rf /data/data/com.example.swiftyproteins/files/cif_cache/*
```

### 3. Evaluation Checklist Alignment
- [x] Complete list of ligands is loaded from `ligands.txt` into a scrollable list.
- [x] Search bar filters list items in real time.
- [x] Selecting a filtered item opens the correct ligand structure.
- [x] Spinning loading wheel appears during remote download.
- [x] Informative alert popup displays if network fails or ligand cannot be loaded.
