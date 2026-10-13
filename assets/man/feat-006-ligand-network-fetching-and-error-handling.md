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
  * Kotlin Coroutines launched within `lifecycleScope`.
* **State Management**:
  * **Loading**: `binding.progressBar.visibility = View.VISIBLE`, UI interaction locked.
  * **Success**: Dismiss spinner $\rightarrow$ parse CIF via `CifParser` $\rightarrow$ launch 3D viewer.
  * **Error**: Dismiss spinner $\rightarrow$ present `AlertDialog` with user-friendly error description.

---

## Error Scenarios Handled
| Failure Cause | Diagnostic / Detection | Alert Dialog Presentation |
| :--- | :--- | :--- |
| **No Internet Connection** | `UnknownHostException` / `IOException` | "Network Error: Please check your internet connection." |
| **Ligand Not Found (404)** | HTTP 404 response code | "Not Found: Ligand data for '{ID}' is unavailable on RCSB." |
| **Server Timeout** | `SocketTimeoutException` | "Timeout Error: RCSB server took too long to respond." |
| **Parsing Failure** | Empty atom array returned | "Parse Error: Could not extract 3D molecular coordinates." |

---

## Verification & Testing
1. Test normal fetch: Select "ATP" with active Wi-Fi; confirm loading spinner displays while fetching, then opens viewer.
2. Test offline mode: Turn on Airplane mode $\rightarrow$ select any ligand $\rightarrow$ confirm spinner shows briefly then error alert popup appears explaining network failure.
