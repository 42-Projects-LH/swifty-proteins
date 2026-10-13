# Feature: Model Sharing & Gallery Export

## Overview
Allows users to capture high-resolution snapshots of rendered 3D molecules, export them to the device photo gallery, and share them across external apps via Android's native system share sheet.

---

## Evaluation Requirements
* **Share Button**: The 3D viewer interface must include an accessible Share button (typically in the top action bar or floating overlay).
* **System Sharing**: Tapping the share button launches the system share sheet (`Intent.ACTION_SEND`), allowing the image to be shared to messaging, email, or social apps.
* **Save to Photo Album**: The captured image must be saved directly to the device's photo gallery / album.

---

## Technical Specifications
* **Snapshot Capture**:
  * Captures the active framebuffer of the 3D surface view into an Android `Bitmap` using `PixelCopy` API (or OpenGL `glReadPixels`).
* **Gallery Export (MediaStore API)**:
  * Uses Android `MediaStore.Images.Media` API (scoped storage compliant for modern Android 10+ / API 29–37).
  * Writes JPEG/PNG bitmap stream into `Pictures/SwiftyProteins/` directory.
  * Inserts entry into MediaStore with `IS_PENDING` flag workflow, ensuring immediate visibility in Google Photos / Gallery apps without requiring legacy storage permissions.
* **Share Intent**:
  * Saves temporary image file to app cache.
  * Obtains content URI via Android `FileProvider` (`androidx.core.content.FileProvider`).
  * Creates `Intent(Intent.ACTION_SEND)`:
    * Type: `image/png`
    * Extra: `Intent.EXTRA_STREAM` with content URI.
    * Flags: `FLAG_GRANT_READ_URI_PERMISSION`.
  * Invokes `Intent.createChooser()`.

---

## Architecture Flow
```
[ User Taps Share Button ]
           │
           ▼
[ Framebuffer Snapshot (PixelCopy / glReadPixels) ]
           │
           ├───────────────────────────────┐
           ▼                               ▼
 [ Save to Gallery ]             [ Export via FileProvider ]
 [ MediaStore API ]              [ Content URI Generated ]
           │                               │
           ▼                               ▼
 [ Image in Photos/Gallery ]     [ Launch Android Share Sheet ]
```

---

## Verification & Testing
1. Tap the Share button in the 3D viewer.
2. System share sheet appears with available messaging/sharing apps.
3. Open the device's Photos / Gallery app: verify the molecular image is saved and visible.
