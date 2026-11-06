# Feature: 3D Molecular Visualization (Ball-and-Stick Model)

## Overview
Renders molecular structures in a native, hardware-accelerated 3D environment using OpenGL ES 2.0. Employs the standard Ball-and-Stick representation, mapping chemical elements to CPK colors, positioning atoms according to spatial Cartesian coordinates from mmCIF records, and connecting atom pairs with 3D cylinders.

---

## Evaluation Requirements
* **In-App 3D Rendering**: The ligand structure must be rendered natively in 3D (OpenGL ES 2.0 on Android via `GLSurfaceView`).
* **Ball-and-Stick Model**:
  * Atoms rendered as smooth 3D spheres.
  * Chemical bonds rendered as 3D sticks/cylinders connecting atom pairs.
* **CPK Color Scheme**: Atoms must be colored according to the standard Corey-Pauling-Koltun (CPK) coloring convention:
  * Carbon (C): Gray (`#909090`)
  * Hydrogen (H): White (`#FFFFFF`)
  * Oxygen (O): Red (`#FF0D0D`)
  * Nitrogen (N): Blue (`#3050F8`)
  * Sulfur (S): Yellow (`#FFFFC832`)
  * Phosphorus (P): Orange (`#FFFF8000`)
  * Halogens (F, Cl): Green (`#1FF01F`)
  * Bromine (Br): Dark Red (`#FFA62929`)
  * Iodine (I): Violet (`#FF940094`)
  * Iron (Fe): Brown (`#E0663300`)
  * Other elements: Distinct fallback colors (Deep Pink `#FFFF1493`)

---

## Technical Specifications
* **OpenGL ES 2.0 Pipeline**:
  * **View**: `ProteinSurfaceView` extending `GLSurfaceView` with continuous rendering.
  * **Renderer**: `ProteinRenderer` implementing `GLSurfaceView.Renderer`.
  * **Coordinate Normalization**:
    * Computes centroid $\mathbf{C} = (\frac{\sum x}{N}, \frac{\sum y}{N}, \frac{\sum z}{N})$ and bounding radius $R = \max_i \|\mathbf{r}_i - \mathbf{C}\|$.
    * Centers the molecule at origin $(0, 0, 0)$.
    * Sets initial camera distance to $R \times 3.2$ with $45^\circ$ FOV so any molecule is automatically proportioned and centered.
* **Geometry Meshes**:
  * `SphereMesh`: Procedural UV sphere (stacks = 20, slices = 20) with counter-clockwise winding order and unit normal vectors.
  * `CylinderMesh`: Procedural unit cylinder (segments = 16) along the Z-axis with outward radial normals.
* **Bond Alignment Transform**:
  * For each bond connecting $A(x_1, y_1, z_1)$ and $B(x_2, y_2, z_2)$:
    * Translation to $A$.
    * Rotation aligning default Z-vector $(0, 0, 1)$ with direction $\frac{B - A}{\|B - A\|}$.
    * Non-uniform scale: $(r_{bond}, r_{bond}, \|B - A\|)$.
* **Blinn-Phong Lighting**:
  * Ambient term ($30\%$), directional eye-space key light diffuse term ($70\%$), and specular highlight (shininess exponent = 32.0).

---

## Verification & Testing
1. **Visual Inspection**: Verified with ligands such as `ATP`, `NAD`, `HEM`: displays crisp 3D spheres connected by solid cylinders.
2. **CPK Coloring**: Carbons are gray, Oxygens are red, Nitrogens are blue, Hydrogens are white, Phosphorus are orange.
3. **Centering & Scaling**: Any chosen ligand automatically centers at origin and fits within the viewport.
