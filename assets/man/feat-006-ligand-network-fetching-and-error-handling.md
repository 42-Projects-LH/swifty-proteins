# Feature: Ligand Network Fetching & Error Handling

## Overview
Handles remote acquisition of molecular structure data in PDBx/mmCIF format from the RCSB Protein Data Bank, complete with visual loading progress feedback and robust error alerts.

---

## Evaluation Requirements
* **Standard Remote Endpoint**: Fetches ligand structure data from public repositories in open molecular formats, specifically RCSB `.cif` format:
  `https://files.rcsb.org/ligands/view/{ligand}.cif`
* **Spinning Wheel Indicator**: A clean spinning wheel or loading animation (`ProgressBar`) must be prominently displayed while the ligand data is being downloaded.
* **Failure Warning Popup**: If loading a ligand fails for any reason (e.g., no internet connection, invalid URL, HTTP 404, or corrupt data), a **warning alert popup must appear explaining the specific failure reason**.

---

## Technical Specifications
* **Network Stack**:
  * Retrofit 2 (`com.squareup.retrofit2:retrofit`)
  * Scalars Converter (`converter-scalars`) for raw CIF text acquisition.
  * OkHttp 3 with logging interceptor and 15-second connect/read timeouts.
* **API Service Interface**:
  ```kotlin
  @GET("ligands/view/{ligand}.cif")
  suspend fun getLigandCif(@Path("ligand") ligandId: String): Response<String>
  ```
* **Asynchronous Execution**:
  * Kotlin Coroutines launched within `lifecycleScope` on `Dispatchers.IO`.
  * Background parsing performed on `Dispatchers.Default` using `CifParser`.
* **State Management**:
  * **Loading**: `binding.loadingOverlay.visibility = View.VISIBLE` with dynamic text (`"Fetching <ID> from RCSB..."`), touch events locked via dimmed scrim.
  * **Success**: Dismiss spinner $\rightarrow$ parse CIF via `CifParser` $\rightarrow$ proceed to 3D visualization.
  * **Error**: Dismiss spinner $\rightarrow$ present `AlertDialog` with user-friendly error description.

---

## Error Scenarios Handled
| Failure Cause | Diagnostic / Detection | Alert Dialog Presentation |
| :--- | :--- | :--- |
| **No Internet Connection / Offline** | `UnknownHostException` / `IOException` | "Network Error: Unable to connect to RCSB PDB server. Please check your internet connection." |
| **Ligand Not Found (404)** | HTTP 404 response code | "Loading Failed: Ligand '{ID}' was not found on the RCSB database (HTTP 404)." |
| **Server Timeout** | `SocketTimeoutException` | "Connection Timeout: The request to download ligand '{ID}' timed out. Please try again." |
| **Parsing Failure** | Empty atom array returned | "Parsing Error: The ligand '{ID}' does not contain readable 3D coordinate atoms in its CIF record." |

---

## Verification & Testing
1. **Online Verification**:
   * Connect to Wi-Fi/mobile data $\rightarrow$ tap a ligand (e.g. `ATP`).
   * Observed result: Centered spinning wheel overlay appears with status text `"Fetching ATP from RCSB..."`, displaying during the network transfer, followed by successful parsing.
2. **Offline Verification (Airplane Mode)**:
   * Enable Airplane Mode $\rightarrow$ tap any ligand in the list.
   * Observed result: Because the OS detects network disconnection instantly, DNS resolution immediately throws an `UnknownHostException` without waiting for the 15s timeout. The loading overlay is instantly dismissed and the modal Alert Dialog ("Network Error: Unable to connect to RCSB PDB server...") appears right away.
   * *Note on Instantaneous Alert*: This immediate alert display is expected Android socket behavior when offline (fail-fast architecture), ensuring users are never left waiting on unnecessary timeouts when there is no network connection.

