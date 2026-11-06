# Bonus Feature: Multiple Visualization Models

## Overview
Extends molecular rendering beyond the mandatory Ball-and-Stick representation by providing multiple standard scientific visualization models: Space-Filling (CPK / van der Waals), Stick, and Wireframe models. Users can switch dynamically between models with a single tap without reloading the molecule structure.

---

## Evaluation Requirements
* **Additional Models**: Implement additional molecular visualization models beyond the mandatory Ball-and-Stick model.
* **UI Control**: Confirm that users can switch between different models (e.g., Space-Filling, Wireframe, Stick) using a UI control.
* **Smooth Transitions**: Switching between models works smoothly without reloading the molecule or stuttering.
* **Scientific Standards**: Each model is correctly implemented according to molecular visualization conventions.

---

## Implemented Visualization Models
| Model Mode | Atom Representation | Bond Representation | Scientific Usage |
| :--- | :--- | :--- | :--- |
| **Ball-and-Stick** *(Default)* | Spheres scaled by ball radius ($\sim 0.35\text{Å}$) | 3D Cylinders ($r = 0.12\text{Å}$) | Highlights both molecular geometry and chemical connectivity. |
| **Space-Filling** *(CPK / vdW)* | Spheres scaled by van der Waals radius ($1.2\text{Å} - 2.0\text{Å}$) | Hidden inside vdW spheres | Illustrates actual steric volume, molecular surface, and packing. |
| **Stick** | Small atom sphere caps ($r = 0.22\text{Å}$) | Thicker 3D Cylinders ($r = 0.22\text{Å}$) | Focuses on carbon backbone, rings, and bond connectivity. |
| **Wireframe** | Minimal atom points | 3D Anti-aliased Vector Lines (`GL_LINES`) | Fast rendering for dense macromolecules and bond topology. |

---

## Technical Specifications
* **Implementation**:
  * Enum `RenderModel` in `ProteinRenderer.kt`:
    ```kotlin
    enum class RenderModel {
        BALL_AND_STICK,
        SPACE_FILLING,
        STICK,
        WIREFRAME
    }
    ```
  * UI toggle button `btnToggleModel` in `ProteinViewerActivity` cycles through all 4 modes on click.
  * Dynamically updates the active rendering pipeline in `ProteinRenderer` on the next frame without discarding geometry or reloading network data.
* **Van der Waals Radii Data**:
  * Configured per chemical element in `Atom.kt`:
    * Hydrogen ($1.20\text{Å}$)
    * Carbon ($1.70\text{Å}$)
    * Nitrogen ($1.55\text{Å}$)
    * Oxygen ($1.52\text{Å}$)
    * Fluorine ($1.47\text{Å}$)
    * Phosphorus ($1.80\text{Å}$)
    * Sulfur ($1.80\text{Å}$)
    * Chlorine ($1.75\text{Å}$)
    * Bromine ($1.85\text{Å}$)
    * Iodine ($1.98\text{Å}$)
    * Iron ($2.00\text{Å}$)

---

## Verification & Testing
1. **Dynamic Switching**: Tapped the model switcher button in `ProteinViewerActivity`; verified smooth transition between Ball & Stick $\rightarrow$ Space-Filling $\rightarrow$ Stick $\rightarrow$ Wireframe without reloading the ligand.
2. **Space-Filling Inspection**: Verified atoms expand to their respective van der Waals sizes with Blinn-Phong lighting.
3. **Wireframe Inspection**: Verified vector line connections rendering bond networks.
