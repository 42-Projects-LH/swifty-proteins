# Feature: Gesture Controls & Camera Manipulation

## Overview
Provides intuitive, fluid multi-touch camera and model controls allowing users to rotate and zoom into 3D molecular structures from any perspective without lag or jitter.

---

## Evaluation Requirements
* **3D Model Manipulation**: The user must be able to zoom in/out and rotate the molecular model to inspect it from different angles.
* **Rotation Control**: Smooth drag/swipe gesture rotates the molecule in 3D space.
* **Zoom Control**: Smooth pinch gesture zooms the camera/model in and out.
* **Responsiveness**: All touch interactions must be smooth, fluid, and responsive without perceptible lag.

---

## Technical Specifications
* **Touch Event Processing**:
  * Custom `OnTouchListener` attached to the 3D surface view.
  * Integration with Android `ScaleGestureDetector` (for pinch zoom) and standard drag velocity tracking (for rotation).
* **Mathematical Transformations**:
  * **Arcball / Trackball Rotation**:
    * Maps 2D screen coordinate delta $(\Delta x, \Delta y)$ to rotational angle and axis.
    * Updates model rotation matrix:
      $$\mathbf{R}_{new} = \mathbf{R}_{\Delta} \times \mathbf{R}_{current}$$
  * **Pinch-to-Zoom**:
    * Scale factor $s$ computed by `ScaleGestureDetector.scaleFactor`.
    * Modifies camera distance or field of view (FOV), clamped within safe min/max bounds (e.g. $0.3\times$ to $5.0\times$) to prevent clipping.
  * **Damping & Inertia (Optional)**: Gentle friction decay upon release for smooth organic rotation feel.

---

## Gesture Mapping Table
| Gesture | Finger Count | Action | Effect |
| :--- | :--- | :--- | :--- |
| **Drag / Swipe** | 1 Finger | Orbit / Rotate | Rotates molecule along horizontal and vertical axes |
| **Pinch** | 2 Fingers | Zoom | Increases or decreases camera distance to molecule |
| **Two-finger Drag** | 2 Fingers | Pan (Optional) | Translates camera along view plane |

---

## Verification & Testing
1. Test rotation: Drag finger across screen; verify molecule rotates smoothly in corresponding direction.
2. Test zoom: Pinch inwards and outwards with two fingers; verify smooth zoom in/out without jumping.
3. Test edge cases: Rotate rapidly and zoom simultaneously; verify framerate remains consistent at 60 FPS.
