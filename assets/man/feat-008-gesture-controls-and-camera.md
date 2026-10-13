# Feature: Gesture Controls & Camera Navigation

## Overview
Provides fluid multi-touch gesture interaction allowing users to rotate, pinch-to-zoom, and pan 3D molecular structures in real time at consistent 60–120 FPS.

---

## Evaluation Requirements
* **3D Model Manipulation**: The user must be able to zoom in/out and rotate the molecular model to inspect it from different angles.
* **Rotation Control**: Smooth drag/swipe gesture rotates the molecule in 3D space.
* **Zoom Control**: Smooth pinch gesture zooms the camera/model in and out.
* **Responsiveness**: All touch interactions must be smooth, fluid, and responsive without perceptible lag.

---

## Technical Specifications
* **Implementation**:
  * Located in `ProteinSurfaceView.kt`.
  * Integrates Android's `ScaleGestureDetector` for 2-finger pinch zoom and `GestureDetector` for single-tap detection.
* **Rotation**:
  * 1-finger drag calculates horizontal delta $dx$ (yaw) and vertical delta $dy$ (pitch).
  * Pitch angle clamped to $[-89.0^\circ, 89.0^\circ]$ to prevent gimbal inversion.
  * Direct uniform matrix rotation applied in `ProteinRenderer`.
* **Zoom**:
  * 2-finger pinch modifies camera distance:
    $$\text{dist}_{new} = \frac{\text{dist}}{\text{scaleFactor}}$$
  * Clamped within safe thresholds $[1.5, 150.0]$ to prevent near-plane clipping or disappearing into distance.
* **Pan**:
  * 2-finger drag translates the model origin along the camera viewport plane $(panX, panY)$, scaled proportional to current zoom distance.
* **Camera Reset**:
  * "Center" button on bottom control bar resets rotation to initial isometric angle ($25^\circ, 35^\circ$), resets distance to bounding radius baseline, and clears pan offsets.

---

## Gesture Mapping Table
| Gesture | Finger Count | Action | Effect |
| :--- | :--- | :--- | :--- |
| **Drag / Swipe** | 1 Finger | Orbit / Rotate | Rotates molecule across yaw and pitch axes |
| **Pinch In / Out** | 2 Fingers | Zoom | Adjusts camera distance smoothly |
| **Two-finger Drag** | 2 Fingers | Pan | Translates camera along viewport plane |
| **Single Tap** | 1 Finger | Atom Pick / Dismiss | Selects atom or dismisses tooltip |

---

## Verification & Testing
1. **Drag Rotation**: Verified smooth 360-degree rotation across horizontal and vertical axes without stutter.
2. **Pinch Zoom**: Verified fluid zooming in for close-up bond examination and zooming out to see full molecule.
3. **Center Button**: Verified tapping "Center" restores initial bounding view.
