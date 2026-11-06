# Feature: Atom Interaction & Information Tooltip

## Overview
Enables interactive exploration of molecular structures by allowing users to tap on individual atoms in the 3D scene to inspect chemical metadata, with automatic dismissal upon tapping elsewhere.

---

## Evaluation Requirements
* **Atom Selection / Tap**: When the user taps directly on an atom in the 3D model, a tooltip or popup must display information about the tapped atom.
* **Information Displayed**: The tooltip must display identifying information about the atom, such as its chemical symbol (e.g., C for carbon, O for oxygen, N for nitrogen), atom name/ID, and coordinates.
* **Dismissal**: The tooltip must automatically disappear whenever the user taps anywhere else on the screen.

---

## Technical Specifications
* **3D Raycasting Engine (`RaycastPicker.kt`)**:
  1. Converts screen touch $(x, y)$ to Normalized Device Coordinates (NDC) in $[-1, 1]$.
  2. Multiplies clip near $(z=-1)$ and clip far $(z=+1)$ points by the inverse of $(P \times V \times M)$ to determine the 3D ray in centered molecule space:
     $$\mathbf{O} = \mathbf{P}_{near}, \quad \hat{\mathbf{D}} = \frac{\mathbf{P}_{far} - \mathbf{P}_{near}}{\|\mathbf{P}_{far} - \mathbf{P}_{near}\|}$$
  3. Tests ray-sphere intersection against all atoms:
     $$\|\mathbf{O} + t \hat{\mathbf{D}} - \mathbf{C}_i\|^2 \le R_i^2$$
  4. Resolves the closest intersected atom (smallest $t > 0$). Includes mobile touch padding ($\Delta r = 0.25\text{Å}$) for ease of targeting smaller atoms like hydrogen.
* **Visual Selection Halo**:
  * Tapped atom is surrounded by an additive glowing translucent cyan selection halo in the 3D view.
* **UI Tooltip Card (`cardAtomInfo`)**:
  * Clean elevated card overlay at the bottom of the screen.
  * Displays:
    * CPK Color badge showing the atom's chemical tint.
    * Full element name and symbol: `"Oxygen (O)"`, `"Carbon (C)"`, `"Nitrogen (N)"`.
    * Atom ID from the CIF structure: `"Atom ID: C1"`.
    * Precise 3D Cartesian coordinates: `"Coordinates: X: 1.234 | Y: -0.567 | Z: 2.890"`.
* **Automatic Dismissal**:
  * If a tap hits the background void (no atom intersected), `onAtomSelectedListener(null)` is triggered, hiding the card and clearing the selection halo.
  * A dedicated close icon (`btnCloseTooltip`) is also available on the card.

---

## Verification & Testing
1. **Atom Tap**: Tapped atoms in `ATP` (e.g. `H5'`, `O`, `P`); verified tooltip card opens immediately with full element name, ID, and coordinates.
2. **Background Tap**: Tapped empty background void; verified tooltip dismisses immediately.
3. **Sequential Taps**: Tapped from one atom directly to another; verified tooltip smoothly updates without requiring manual dismissal.
