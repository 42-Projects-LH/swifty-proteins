# Feature: Ligand List & Search Filter

## Overview
Presents the complete catalog of candidate ligands loaded from bundled asset data and provides instant, responsive search filtering for rapid ligand selection.

---

## Evaluation Requirements
* **Full Ligand Display**: All ligands contained in the [`ligands.txt`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/assets/ligands.txt) file must be loaded and displayed in the list view.
* **Search Functionality (`UISearchBar` / `SearchView`)**: The user must be able to search for any ligand code/ID using the search bar.
* **Accurate Selection**: Selecting a ligand from either the full list or filtered search results must load that specific ligand.

---

## Technical Specifications
* **Data Source**:
  * Bundled asset file: `app/src/main/assets/ligands.txt`.
  * Loaded into memory via `InputStreamReader` and `BufferedReader` with whitespace trimming.
* **UI Components**:
  * Container: `androidx.recyclerview.widget.RecyclerView` with vertical `LinearLayoutManager`.
  * Search Bar: `androidx.appcompat.widget.SearchView`.
  * View Binding: `ActivityProteinListBinding` and `ItemProteinBinding`.
* **Filtering Mechanism**:
  * `ProteinAdapter.filter(query: String)`:
    * Performs case-insensitive substring matching against the master list.
    * Updates internal `filteredList` and triggers adapter notifications.
    * Empty query immediately restores the full dataset.

---

## User Flow
1. User enters `ProteinListActivity` after authenticating.
2. The complete list of ligand codes (e.g., `001`, `002`, `ATP`, `HEM`, `NAG`) is displayed alphabetically.
3. User types into the search bar (e.g., "HEM").
4. List instantly updates to display only matching entries.
5. User taps an item $\rightarrow$ triggers selection handler for that specific ligand.

---

## Verification & Testing
1. Verify item count: Ensure all non-empty lines from `ligands.txt` are loaded.
2. Test search: Type a common code like "ATP"; verify only "ATP" (and substrings) appear.
3. Clear search: Delete search text; verify full list restores.
4. Test item selection: Tap a filtered item; verify selection matches the tapped code.
