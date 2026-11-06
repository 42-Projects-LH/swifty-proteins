# Feature: Model Sharing & Gallery Export

## Overview
Allows users to capture high-resolution snapshots of rendered 3D molecules, export them to the device photo gallery, and share them across external applications via Android's native system share sheet.

---

## Evaluation Requirements
* **Share Button**: The 3D viewer interface must include an accessible Share button (top action bar).
* **System Sharing**: Tapping the share button launches the system share sheet (`Intent.ACTION_SEND`), allowing the image to be shared to messaging, email, or social apps.
* **Save to Photo Album**: The captured image must be saved directly to the device's photo gallery / album.

---

## Technical Specifications
* **OpenGL Framebuffer Capture**:
  * Utilizes `glReadPixels` on the OpenGL render thread to capture the active framebuffer.
  * Flips pixel rows vertically to map from OpenGL bottom-to-top convention to Android standard top-to-bottom `ARGB_8888` `Bitmap`.
* **Photo Album Export (MediaStore API)**:
  * Uses modern Scoped Storage via Android `MediaStore.Images.Media` (`API 29+` compliant).
  * Automatically creates album folder `Pictures/SwiftyProteins/`.
  * Files named: `Protein_<LIGAND>_<TIMESTAMP>.png`.
  * Employs `IS_PENDING` transaction workflow for atomic writes and instant indexing in Google Photos / Gallery.
* **System Share Sheet Integration**:
  * Writes temporary PNG copy to `cacheDir/images/`.
  * Generates content URI via Android `FileProvider` (`${applicationId}.fileprovider`) with `FLAG_GRANT_READ_URI_PERMISSION`.
  * Launches `Intent.createChooser(shareIntent, "Share Protein Model")` with title, image preview, and molecular description.

---

## Architecture Flow
```
[ User Taps Share Button ]
           │
           ▼
[ Framebuffer Snapshot via glReadPixels ]
           │
           ├───────────────────────────────┐
           ▼                               ▼
 [ Save to Photo Album ]         [ Export via FileProvider ]
 [ MediaStore API ]              [ Content URI Generated ]
           │                               │
           ▼                               ▼
 [ Image in Photos/Gallery ]     [ Launch Android Share Sheet ]
```

---

## Verification & Testing
1. **Share Action**: Tapped Share button in `ProteinViewerActivity`; system share chooser opened immediately.
2. **Photo Album Verification**: Verified saved snapshot in `/sdcard/Pictures/SwiftyProteins/Protein_ATP_*.png` with high-resolution image rendering.
