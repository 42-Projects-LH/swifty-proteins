# Enhanced Molecular Interactions

The **Enhanced Molecular Interactions** features add advanced interactions for exploring molecular structures.

## Features List

Status | Feature | Description
:--- | :--- | :---
⚪ | **Atom Highlighting** | When hovering over or selecting an atom, highlight all atoms of the same element type.
⚫ | **Bond Information** | Display bond types (single, double, triple) and bond lengths when tapping on bonds.
⚫ | **Measurement Tools** | Allow users to measure distances between atoms or angles between bonds.
⚪ | **Atom Labels** | Option to display element symbols directly on atoms in the 3D view (toggleable).
⚫ | **Center on Atom** | Double-tap an atom to center the camera on it with a smooth animation.

>[!NOTE]
>- ⚪ Implemented
>- ⚫ Not implemented

## Atom Highlighting

When a user taps an atom in the 3D viewport, `ProteinSurfaceView` dispatches the touch coordinates to `RaycastPicker.pickAtom()`, which unprojects a 3D ray through the inverted Model-View-Projection matrix and tests ray-sphere intersections against the active atom radii (`vdwRadius` in Space-Filling mode or `ballRadius` in other modes, plus a `0.25 Å` touch tolerance). Once an atom is selected, `ProteinViewerActivity` updates `renderer.selectedAtom` and sets `renderer.highlightElement = atom.symbol`, triggering a dual-layer additive-blended (`GL_SRC_ALPHA`, `GL_ONE`) glow pass in `ProteinRenderer.onDrawFrame()` while displaying the floating `cardAtomInfo` tooltip with the element name, symbol, atom ID, CPK color badge (`viewAtomColor`), and Cartesian coordinates (`X | Y | Z`). In the 3D scene, `drawAtomHighlight()` renders a translucent cyan halo (`rgba(0.01f, 0.94f, 1.0f, 0.45f)`, shininess `64.0f`) scaled to `1.35×` the atom's radius around the selected atom, while `drawElementHighlight()` simultaneously illuminates **all other atoms of the same chemical element** across the molecule with a distinctive gold-amber halo (`rgba(1.0f, 0.85f, 0.2f, 0.35f)`, shininess `32.0f`) scaled to `1.25×` their radius.

### Relevant Files in the Codebase

| File | Type | Description |
|---|---|---|
| [`ProteinRenderer.kt`](../../app/src/main/java/com/example/swiftyproteins/ui/viewer/gl/ProteinRenderer.kt) | Kotlin Source | Manages `selectedAtom` and `highlightElement` state and renders `drawAtomHighlight()` (cyan selection halo) and `drawElementHighlight()` (gold element-wide co-highlighting). |
| [`RaycastPicker.kt`](../../app/src/main/java/com/example/swiftyproteins/ui/viewer/gl/RaycastPicker.kt) | Kotlin Source | Unprojects 2D screen touch coordinates into a 3D world ray and performs ray-sphere intersection testing to identify the tapped atom. |
| [`ProteinSurfaceView.kt`](../../app/src/main/java/com/example/swiftyproteins/ui/viewer/gl/ProteinSurfaceView.kt) | Kotlin Source | Detects single-tap gestures via `GestureDetector.SimpleOnGestureListener`, invokes `renderer.pickAtom()`, and triggers `onAtomSelectedListener`. |
| [`ProteinViewerActivity.kt`](../../app/src/main/java/com/example/swiftyproteins/ui/viewer/ProteinViewerActivity.kt) | Kotlin Source | Binds `onAtomSelectedListener` to set `renderer.highlightElement = atom.symbol` and populates/hides the `cardAtomInfo` tooltip card. |
| [`Atom.kt`](../../app/src/main/java/com/example/swiftyproteins/data/model/Atom.kt) | Kotlin Source | Defines elemental symbols, full element names, CPK colors (`colorHex` / `colorRgba`), and `ballRadius` / `vdwRadius` dimensions. |

### How to test

1. Open a multi-element ligand such as `ATP` in `ProteinViewerActivity`.
2. Tap any Phosphorus (`P`, orange) sphere in the triphosphate chain: verify that the tapped atom receives a bright cyan glowing halo (`1.35×` radius), all other Phosphorus atoms in the molecule simultaneously receive a gold glowing halo (`1.25×` radius), and the bottom `cardAtomInfo` tooltip card appears showing `Phosphorus (P)`, its atom ID, and 3D coordinates.
3. Tap a Nitrogen (`N`, blue) or Oxygen (`O`, red) atom: verify that the cyan selection halo moves to the newly tapped atom and the gold co-highlighting switches to all other atoms of that newly selected element type.
4. Tap empty space in the background (or the close `✕` button `btnCloseTooltip` on the tooltip card): verify that both the cyan and gold halos clear and the tooltip card disappears.

## Atom Labels

`AtomLabelOverlay` provides a toggleable, real-time 2D canvas overlay layered directly over `ProteinSurfaceView` in `activity_protein_viewer.xml` that projects 3D atomic coordinates onto the screen to render floating element symbol badges. When toggled on via the **Labels** button (`btnToggleLabels` in `ProteinViewerActivity`, which switches `atomLabelOverlay.showLabels` and updates the button's `alpha` between `0.45f` and `1.0f`), `AtomLabelOverlay.onDraw()` multiplies the renderer's live `viewMatrix`, `modelMatrix`, and `projectionMatrix` (`mvp = projectionMatrix × viewMatrix × modelMatrix`) to transform each atom's centroid-centered 3D position `(atom.x - cx, atom.y - cy, atom.z - cz, 1f)` into clip space and perspective-divided screen pixel coordinates `(sx, sy)`. To keep complex structures readable and prevent visual occlusion, Hydrogen (`H`) atoms are automatically filtered out (`if (atom.symbol.equals("H", ignoreCase = true)) continue`), while all heavy atoms (`C`, `N`, `O`, `P`, `S`, etc.) display a rounded pill badge (`12px` corner radius tinted with the atom's CPK `colorHex` at `0xCC` alpha opacity) with bold white monospace text (`28px`) positioned `36px` above the atom sphere (`labelY = sy - 36f`).

### Relevant Files in the Codebase

| File | Type | Description |
|---|---|---|
| [`AtomLabelOverlay.kt`](../../app/src/main/java/com/example/swiftyproteins/ui/viewer/AtomLabelOverlay.kt) | Kotlin Source | Custom transparent `View` computing 3D-to-2D MVP coordinate projections, filtering out hydrogens, and drawing CPK-tinted rounded symbol pills. |
| [`ProteinViewerActivity.kt`](../../app/src/main/java/com/example/swiftyproteins/ui/viewer/ProteinViewerActivity.kt) | Kotlin Source | Wires `atomLabelOverlay` to `renderer`, `centerOffset`, and `molecule.atoms`, and handles the `btnToggleLabels` click listener. |
| [`activity_protein_viewer.xml`](../../app/src/main/res/layout/activity_protein_viewer.xml) | XML Layout | Positions `AtomLabelOverlay` full-screen over `ProteinSurfaceView` and defines `btnToggleLabels` in the bottom control bar. |
| [`ProteinRenderer.kt`](../../app/src/main/java/com/example/swiftyproteins/ui/viewer/gl/ProteinRenderer.kt) | Kotlin Source | Exposes live `projectionMatrix`, `viewMatrix`, `modelMatrix`, and `centerOffset` arrays consumed by `AtomLabelOverlay`. |

### How to test

1. Open any ligand (e.g., `ATP` or `HEM`) in `ProteinViewerActivity` and tap the **Labels** button (`btnToggleLabels`) in the bottom control bar.
2. Verify that the **Labels** button opacity increases to full brightness (`alpha = 1.0`) and floating CPK-colored pill badges (`C`, `N`, `O`, `P`, `FE`, etc.) appear above all non-hydrogen atoms.
3. Orbit-rotate (1-finger drag), pinch-to-zoom (2-finger pinch), and pan (2-finger drag) the 3D molecule: confirm that all label badges smoothly track their corresponding 3D atoms in real time without lag.
4. Tap the **Labels** button again: verify that the button dims back to `alpha = 0.45` and all floating atom labels immediately disappear.
