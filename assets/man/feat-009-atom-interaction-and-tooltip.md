# Feature: Atom Interaction & Tooltip Display

## Overview
Enables interactive exploration of molecular structures by allowing users to tap on individual atoms in the 3D scene to inspect chemical metadata, with automatic dismissal upon tapping elsewhere.

---

## Evaluation Requirements
* **Atom Selection / Tap**: When the user taps directly on an atom in the 3D model, a tooltip or popup must display information about the tapped atom.
* **Information Displayed**: The tooltip must display identifying information about the atom, such as its chemical symbol (e.g., C for carbon, O for oxygen, N for nitrogen), atom name/ID, and coordinates.
* **Dismissal**: The tooltip must automatically disappear whenever the user taps anywhere else on the screen.

---

## Technical Specifications
* **3D Hit-Testing (Raycasting / Color-Picking)**:
  * **Option A: Raycasting**:
    * Computes a ray from camera position through the normalized device coordinates (NDC) corresponding to the screen tap $(x_{tap}, y_{tap})$.
    * Tests intersection between the ray and bounding spheres of all atoms in the molecule:
      $$\|\mathbf{p}_{atom} - (\mathbf{r}_0 + t \mathbf{d})\|^2 \le r_{sphere}^2$$
    * Identifies the closest intersecting atom (smallest $t > 0$).
  * **Option B: ID Color Picking**:
    * Renders an off-screen pass where each atom is shaded with an encoded RGB color ID.
    * Reads pixel at touch position to resolve exact atom index in $O(1)$ time.
* **UI Tooltip Presentation**:
  * An animated Material card overlay positioned over the 3D view (or pinned to screen top/bottom).
  * Displays:
    * Element Symbol & Name (e.g., "Carbon (C)")
    * Atom Identifier (e.g., "Atom ID: C1")
    * Spatial Coordinates: $(X, Y, Z)$
* **Dismissal Logic**:
  * Any tap that does not intersect an atom hides the tooltip overlay.

---

## User Flow
1. User spots an oxygen atom (red sphere) in the 3D viewer.
2. User taps the red sphere.
3. A clean tooltip appears: `"Oxygen (O) - ID: O1"`.
4. User taps on empty space in the background.
5. The tooltip smoothly dismisses.

---

## Verification & Testing
1. Tap various atoms: Tap carbon, nitrogen, and oxygen spheres; verify tooltip appears showing correct symbols and IDs.
2. Tap outside: Tap background void; verify tooltip immediately disappears.
3. Tap another atom: Tap atom A, then atom B; verify tooltip updates to atom B without needing manual dismissal first.
