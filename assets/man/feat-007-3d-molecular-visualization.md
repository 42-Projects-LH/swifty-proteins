# Feature: 3D Molecular Visualization (Ball-and-Stick Model)

## Overview
Renders molecular structures in a native 3D environment using the standard Ball-and-Stick representation, mapping chemical elements to CPK colors and positioning atoms according to spatial coordinates from mmCIF records.

---

## Evaluation Requirements
* **In-App 3D Rendering**: The ligand structure must be rendered natively in 3D (SceneKit on iOS, OpenGL ES or SceneView/Filament on Android).
* **Ball-and-Stick Model**:
  * Atoms rendered as 3D spheres.
  * Chemical bonds rendered as 3D sticks/cylinders connecting atom pairs.
* **CPK Color Scheme**: Atoms must be colored according to the standard Corey-Pauling-Koltun (CPK) coloring convention:
  * Carbon (C): Gray / Black
  * Hydrogen (H): White
  * Oxygen (O): Red
  * Nitrogen (N): Blue
  * Sulfur (S): Yellow
  * Phosphorus (P): Orange
  * Halogens (F, Cl): Green
  * Bromine (Br): Dark Red
  * Iodine (I): Violet
  * Other elements: Distinct fallback colors

---

## Technical Specifications
* **Coordinate System & Parsing**:
  * Extracted from `_chem_comp_atom.model_Cartn_x/y/z` records in `.cif`.
  * Normalization: Calculates geometric centroid $(c_x, c_y, c_z)$ and bounding radius $R_{max}$. Translates centroid to origin $(0, 0, 0)$ and scales by $1 / R_{max}$ so any ligand automatically fits into the camera viewport.
* **Rendering Pipeline**:
  * Geometry: Sphere meshes for atoms ($N$ subdivisions), Cylinder meshes for bonds.
  * Lighting: Ambient light + directional key light providing depth and specular highlights.
  * Materials: Diffuse + specular shading using CPK hex colors.
* **Bond Alignment Algorithm**:
  * For each bond connecting atom $A_1(\mathbf{p}_1)$ and $A_2(\mathbf{p}_2)$:
    * Center position: $\mathbf{p}_{mid} = \frac{\mathbf{p}_1 + \mathbf{p}_2}{2}$.
    * Length: $L = \|\mathbf{p}_2 - \mathbf{p}_1\|$.
    * Orientation: Rotation quaternion/matrix aligning cylinder's default axis with unit vector $\frac{\mathbf{p}_2 - \mathbf{p}_1}{L}$.

---

## CPK Color Palette Reference
```kotlin
when (symbol.uppercase()) {
    "C" -> 0xFF909090  // Carbon: Gray
    "H" -> 0xFFFFFFFF  // Hydrogen: White
    "O" -> 0xFFFF0D0D  // Oxygen: Red
    "N" -> 0xFF3050F8  // Nitrogen: Blue
    "S" -> 0xFFFFC832  // Sulfur: Yellow
    "P" -> 0xFFFF8000  // Phosphorus: Orange
    "F", "CL" -> 0xFF1FF01F // Halogens: Green
    "BR" -> 0xFFA62929 // Bromine: Dark Red
    "I"  -> 0xFF940094 // Iodine: Violet
    "FE" -> 0xE0663300 // Iron: Brown
    else -> 0xFFFF1493 // Other: Deep Pink
}
```

---

## Verification & Testing
1. Visual Inspection: Verify ligand displays in 3D with distinct spheres (atoms) connected by cylinders (bonds).
2. Color Verification: Verify Carbons are gray, Oxygens are red, Nitrogens are blue, Hydrogens are white.
3. Scaling: Load small ligands (e.g., `001`) and large ligands (e.g., `HEM`, `ATP`); verify both are centered and properly proportioned.
