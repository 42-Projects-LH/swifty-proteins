# Enhanced Molecular Interactions

## Overview

The **Enhanced Molecular Interactions** bonus feature enriches the 3D exploration experience with advanced interactive tools: dual-layer atom highlighting (selected atom glow plus elemental co-highlighting), floating projected atom label overlays, structural Hill formula computation, and precision coordinate inspection.

---

## Feature Specifications

### 1. Dual-Layer Atom Highlighting
When an atom is selected via single tap:
- **Primary Selection Halo** (`drawAtomHighlight`):
  - A pulsing, translucent cyan halo (`#02F0FF` with alpha blending $45\%$) surrounds the selected atom sphere.
  - Sized at $135\%$ of the base atom radius, rendering with additive blending (`GL_SRC_ALPHA`, `GL_ONE`) to ensure visibility from any camera angle.
- **Secondary Element Co-Highlighting** (`drawElementHighlight`):
  - Simultaneously highlights **all other atoms of the same chemical element** across the entire molecule.
  - Shaded with a distinctive gold-amber glow (`rgba(1.0, 0.85, 0.2, 0.35)`).
  - Allows instant visual identification of elemental distribution (e.g., highlighting all Nitrogen atoms or all Sulfur bridges in a complex ligand).

### 2. Bond Information & Molecular Metrics
- **Hill System Formula Calculation**: `Molecule.molecularFormula` computes the standard chemical empirical formula following Hill system rules (Carbon first, Hydrogen second, followed by all remaining elements in alphabetical order):
  ```kotlin
  val counts = atoms.groupingBy { it.symbol.uppercase() }.eachCount()
  // Generates e.g. "C10H16N5O13P3" for ATP
  ```
- **Structural Statistics**: The viewer header displays real-time molecule metrics: `"{Atoms Count} Atoms • {Bonds Count} Bonds"`.

### 3. Measurement Tools & Coordinate Inspection
- **Precision Coordinate Readout**: The atom tooltip displays Cartesian coordinates $(X, Y, Z)$ formatted to three decimal places in Angstroms ($\text{\AA}$):
  $$\text{Coordinates: } X: \pm0.000\text{ \AA} \mid Y: \pm0.000\text{ \AA} \mid Z: \pm0.000\text{ \AA}$$
- **Automatic Centroid & Bounding Radius**:
  The centroid $(\bar{x}, \bar{y}, \bar{z})$ and maximum bounding distance $R_{\max}$ are calculated on CIF ingestion:
  $$\bar{x} = \frac{1}{N}\sum_{i=1}^{N} x_i, \quad \bar{y} = \frac{1}{N}\sum_{i=1}^{N} y_i, \quad \bar{z} = \frac{1}{N}\sum_{i=1}^{N} z_i$$
  $$R_{\max} = \max_{1 \le i \le N} \sqrt{(x_i - \bar{x})^2 + (y_i - \bar{y})^2 + (z_i - \bar{z})^2}$$

### 4. Floating Atom Labels Overlay
- **3D-to-2D Coordinate Projection**: Implemented in `AtomLabelOverlay.kt` as a transparent canvas overlay sitting on top of the OpenGL surface view.
- **Matrix Unprojection**:
  World coordinates are multiplied by the combined Model-View-Projection (MVP) matrix:
  $$\mathbf{p}_{\text{clip}} = \mathbf{M}_{\text{MVP}} \cdot \begin{bmatrix} x - \bar{x} \\ y - \bar{y} \\ z - \bar{z} \\ 1.0 \end{bmatrix}$$
  Screen pixels $(s_x, s_y)$ are extracted via perspective division:
  $$s_x = \frac{x_{\text{clip}} / w_{\text{clip}} + 1.0}{2.0} \times W, \quad s_y = \frac{1.0 - y_{\text{clip}} / w_{\text{clip}}}{2.0} \times H$$
- **Smart Filtering**: Hydrogen ($H$) atoms are intentionally omitted from label rendering to prevent visual occlusion, while heavy atoms (C, N, O, P, S, etc.) display rounded badges tinted with their CPK color.
- **One-Tap Toggle**: Users can toggle floating labels on/off at any time via the label toggle button (`btnToggleLabels`).

### 5. Center on Molecule & Reset Camera
- Dedicated reset button (`btnResetCamera`) restores camera pitch ($25^\circ$), yaw ($35^\circ$), pan offset $(0, 0)$, and distance ($R_{\max} \times 3.2$).

---

## Relevant Files in the Codebase

| File | Type | Description |
|---|---|---|
| [`AtomLabelOverlay.kt`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/java/com/example/swiftyproteins/ui/viewer/AtomLabelOverlay.kt) | Kotlin Source | 2D projection overlay calculating screen coordinates and rendering floating CPK label badges. |
| [`ProteinRenderer.kt`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/java/com/example/swiftyproteins/ui/viewer/gl/ProteinRenderer.kt) | Kotlin Source | Implements `drawAtomHighlight` (cyan glow) and `drawElementHighlight` (gold glow), and exposes transformation matrices. |
| [`Molecule.kt`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/java/com/example/swiftyproteins/data/model/Molecule.kt) | Kotlin Source | Contains Hill notation molecular formula calculation and molecule model definition. |
| [`ProteinViewerActivity.kt`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/java/com/example/swiftyproteins/ui/viewer/ProteinViewerActivity.kt) | Kotlin Source | Coordinates label overlay visibility and binds selection events between renderer and UI. |
| [`Atom.kt`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/java/com/example/swiftyproteins/data/model/Atom.kt) | Kotlin Source | Supplies element names, symbols, coordinates, and CPK colors. |

---

## Testing and Verification

### 1. Manual Evaluation Scenarios

#### Scenario A: Atom Selection & Element Highlighting
1. Open ligand `ATP`.
2. Tap on any Phosphorus atom (orange sphere).
3. Verify that:
   - The tapped Phosphorus receives a bright cyan glowing halo.
   - All other Phosphorus atoms in the triphosphate tail simultaneously receive a gold glowing halo.
   - The bottom card displays `Phosphorus (P)` with exact coordinates.
4. Tap on a Nitrogen atom (blue sphere). Verify that all Nitrogen atoms highlight in gold, while the tapped Nitrogen highlights in cyan.

#### Scenario B: Floating Atom Labels Overlay
1. Tap the **Layers / Labels** button (`ic_layers`) in the viewer toolbar.
2. Confirm that floating CPK badges appear above all non-hydrogen atoms displaying their elemental symbol (`C`, `N`, `O`, `P`).
3. Rotate and pinch-to-zoom the molecule. Confirm that labels dynamically track the 3D movement of their respective atoms in real time.
4. Tap the button again. Confirm that labels disappear immediately.

#### Scenario C: Camera Reset
1. Pan and rotate the molecule until it moves partially off-screen.
2. Tap the **Reset Camera** button.
3. Confirm the molecule instantly returns to the exact center of the screen with optimal initial framing.

### 2. Evaluation Checklist Alignment
- [x] Atom highlighting with cyan selection halo and gold element-wide co-highlighting.
- [x] Dynamic Hill formula computation and molecular statistics.
- [x] Real-time 3D projected floating atom labels with toggle control.
- [x] Precise 3D coordinate inspection via tooltip card.
- [x] Centering and camera reset functionality.
