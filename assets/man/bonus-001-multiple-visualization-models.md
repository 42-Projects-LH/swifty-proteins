# Multiple Visualization Models

## Overview

The **Multiple Visualization Models** bonus feature expands the rendering pipeline beyond the mandatory Ball-and-Stick model by providing four distinct molecular representations recognized in structural biology:
1. **Ball & Stick** (Mandatory baseline)
2. **Space-Filling (CPK / Calotte Model)**
3. **Stick (Licorice Model)**
4. **Wireframe (Line Model)**

Users can cycle dynamically between representations with a single tap of an on-screen UI control. Model transitions occur instantaneously in memory without reloading, re-parsing, or re-downloading the molecule data.

---

## Model Specifications

### 1. Space-Filling Model (CPK Model)
- **Concept**: Depicts atoms as solid spheres sized proportionally to their empirical **Van der Waals (vdW)** radii.
- **Visual Appearance**: Overlapping atomic spheres showing the true steric hindrance and volumetric surface boundary of the molecule. Chemical bonds are fully enveloped within the atom volumes and are omitted from rendering.
- **Physical Accuracy**: Uses standard Bondi and IUPAC van der Waals radii defined in `Atom.vdwRadius`:
  - Hydrogen ($H$): $1.20\text{ \AA}$
  - Carbon ($C$): $1.70\text{ \AA}$
  - Nitrogen ($N$): $1.55\text{ \AA}$
  - Oxygen ($O$): $1.52\text{ \AA}$
  - Fluorine ($F$): $1.47\text{ \AA}$
  - Chlorine ($Cl$): $1.75\text{ \AA}$
  - Sulfur ($S$): $1.80\text{ \AA}$
  - Phosphorus ($P$): $1.80\text{ \AA}$
  - Iron ($Fe$): $2.00\text{ \AA}$
- **Adaptive Raycasting**: When the Space-Filling model is active, `RaycastPicker` automatically expands its hit-detection radii to match the vdW sphere boundaries.

### 2. Stick Model (Licorice Model)
- **Concept**: Prioritizes bond topology and chemical connectivity over individual atom sizes.
- **Visual Appearance**: Renders covalent bonds as thick 3D cylinders (`bondRadius = 0.22f`) connected at atom junctions with small spherical caps (`capRadius = 0.22f`) carrying the respective atom's CPK color.
- **Use Case**: Preferred by biochemists for examining complex ring systems, aromatic rings, and large macrocycles without atom spheres obscuring back-facing bonds.

### 3. Wireframe Model
- **Concept**: Ultra-lightweight skeletal line rendering illustrating purely the bonding network.
- **Visual Appearance**:
  - Uses native OpenGL ES line primitives (`GLES20.glDrawArrays(GL_LINES)` with line width $3.0\text{f}$).
  - Shaded with a glowing cyan line shader (`#02F0FF`) and tiny atom position nodes (`capRadius = 0.15f`).
- **Performance**: Extremely lightweight geometry ideal for low-end GPU hardware or massive macromolecular assemblies.

### 4. UI Control & State Management
- **One-Tap Switching**: Handled via the model switcher button (`btnToggleModel`) located in the bottom-left floating toolbar.
- **Active Label Indicator**: The button label updates in real-time to reflect the active mode:
  - `Ball & Stick` → `Space-Filling` → `Stick` → `Wireframe` → *(cycles back)*
- **Zero-Latency Transition**: Handled via the `@Volatile var renderModel: RenderModel` enum in `ProteinRenderer`. No memory reallocations or CIF file reads are triggered during switches.

---

## Relevant Files in the Codebase

| File | Type | Description |
|---|---|---|
| [`ProteinRenderer.kt`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/java/com/example/swiftyproteins/ui/viewer/gl/ProteinRenderer.kt) | Kotlin Source | Defines `RenderModel` enum and executes specialized draw routines (`drawAtoms`, `drawBonds`, `drawSmallAtomCaps`, `drawWireframe`). |
| [`ProteinViewerActivity.kt`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/java/com/example/swiftyproteins/ui/viewer/ProteinViewerActivity.kt) | Kotlin Source | Configures the `btnToggleModel` click listener and model cycling logic. |
| [`RaycastPicker.kt`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/java/com/example/swiftyproteins/ui/viewer/gl/RaycastPicker.kt) | Kotlin Source | Dynamically tests intersections against either `vdwRadius` (Space-Filling) or `ballRadius` (other modes). |
| [`Atom.kt`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/java/com/example/swiftyproteins/data/model/Atom.kt) | Kotlin Source | Stores standardized Van der Waals (`vdwRadius`) and Ball (`ballRadius`) radius measurements per chemical element. |
| [`SphereMesh.kt`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/java/com/example/swiftyproteins/ui/viewer/gl/SphereMesh.kt) | Kotlin Source | Provides tessellated sphere vertex buffers reused across Ball & Stick, Space-Filling, and joint caps. |
| [`CylinderMesh.kt`](file:///home/firstmate/GitHub/swifty-proteins/app/src/main/java/com/example/swiftyproteins/ui/viewer/gl/CylinderMesh.kt) | Kotlin Source | Provides cylinder vertex buffers dynamically scaled for normal vs thick stick representations. |

---

## Testing and Verification

### 1. Manual Evaluation Scenarios

#### Scenario A: Model Switching Cycle
1. Open any ligand (e.g., `HEM` or `ATP`).
2. Observe initial default rendering: **Ball & Stick** (normal spheres and cylinders).
3. Tap the **"Ball & Stick"** button in the lower toolbar.
4. Verify transition to **Space-Filling**:
   - Molecule transforms into large, interlocking Van der Waals spheres.
   - Cylinders are completely hidden.
   - Button text updates to `"Space-Filling"`.
5. Tap the button again.
6. Verify transition to **Stick**:
   - Bonds become noticeably thicker cylinders.
   - Atoms become subtle caps connecting the bonds.
   - Button text updates to `"Stick"`.
7. Tap the button again.
8. Verify transition to **Wireframe**:
   - Model turns into sharp glowing cyan lines with node points.
   - Button text updates to `"Wireframe"`.
9. Tap once more. Verify it smoothly returns to **Ball & Stick**.

#### Scenario B: Seamless Performance (Zero Reload)
1. Rapidly tap the model toggle button 10 times in succession.
2. Confirm that there is zero flickering, zero loading wheel appearance, and zero frame stutter.
3. Verify that rotation angle, pan position, and zoom level are completely preserved across transitions.

#### Scenario C: Space-Filling Raycast Picking
1. Switch to **Space-Filling** mode.
2. Tap on the outer boundary of an expanded oxygen or sulfur sphere.
3. Verify that the raycaster accurately detects the touch based on the expanded Van der Waals radius and presents the tooltip card.

### 2. Evaluation Checklist Alignment
- [x] Implemented multiple molecular visualization models beyond Ball-and-Stick.
- [x] User can switch between Space-Filling, Stick, and Wireframe via an accessible UI control.
- [x] Model switching operates smoothly in-place without reloading or network requests.
- [x] Each representation strictly follows chemical and molecular visualization conventions.
