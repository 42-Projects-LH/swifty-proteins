package com.example.swiftyproteins.data.model

import java.io.Serializable

data class Atom(
    val id: String,
    val symbol: String,
    val x: Float,
    val y: Float,
    val z: Float
) : Serializable {

    val elementName: String
        get() = when (symbol.uppercase()) {
            "H" -> "Hydrogen"
            "C" -> "Carbon"
            "N" -> "Nitrogen"
            "O" -> "Oxygen"
            "F" -> "Fluorine"
            "P" -> "Phosphorus"
            "S" -> "Sulfur"
            "CL" -> "Chlorine"
            "BR" -> "Bromine"
            "I" -> "Iodine"
            "FE" -> "Iron"
            "CA" -> "Calcium"
            "ZN" -> "Zinc"
            "MG" -> "Magnesium"
            "NA" -> "Sodium"
            "K" -> "Potassium"
            "B" -> "Boron"
            "SI" -> "Silicon"
            "CU" -> "Copper"
            else -> "Element ($symbol)"
        }

    // CPK standard color mapping (hex ARGB)
    val colorHex: Int
        get() = when (symbol.uppercase()) {
            "C" -> 0xFF909090.toInt()  // Carbon: Gray
            "H" -> 0xFFFFFFFF.toInt()  // Hydrogen: White
            "O" -> 0xFFFF0D0D.toInt()  // Oxygen: Red
            "N" -> 0xFF3050F8.toInt()  // Nitrogen: Blue
            "S" -> 0xFFFFC832.toInt()  // Sulfur: Yellow
            "P" -> 0xFFFF8000.toInt()  // Phosphorus: Orange
            "F", "CL" -> 0xFF1FF01F.toInt() // Chlorine/Fluorine: Green
            "BR" -> 0xFFA62929.toInt()  // Bromine: Dark Red
            "I"  -> 0xFF940094.toInt()  // Iodine: Dark Violet
            "FE" -> 0xE0663300.toInt()  // Iron: Brown
            "CA" -> 0xFF3DFF00.toInt()  // Calcium: Light Green
            "ZN", "MG" -> 0xFF7D80B0.toInt() // Zinc/Magnesium: Slate
            "NA" -> 0xFFAB5CF2.toInt()  // Sodium: Violet
            "K"  -> 0xFF8F40D4.toInt()  // Potassium: Purple
            "B"  -> 0xFFFFB5B5.toInt()  // Boron: Salmon Pink
            "SI" -> 0xFFDAA520.toInt()  // Silicon: Golden
            "CU" -> 0xFFC88033.toInt()  // Copper: Bronze
            else -> 0xFFFF1493.toInt()  // Default / Other: Deep Pink
        }

    // OpenGL RGBA normalized float array [r, g, b, a]
    val colorRgba: FloatArray
        get() {
            val argb = colorHex
            val r = ((argb shr 16) and 0xFF) / 255.0f
            val g = ((argb shr 8) and 0xFF) / 255.0f
            val b = (argb and 0xFF) / 255.0f
            return floatArrayOf(r, g, b, 1.0f)
        }

    // Ball-and-Stick sphere radius in Angstroms
    val ballRadius: Float
        get() = when (symbol.uppercase()) {
            "H" -> 0.25f
            "C" -> 0.38f
            "N" -> 0.35f
            "O" -> 0.35f
            "F" -> 0.32f
            "P" -> 0.44f
            "S" -> 0.45f
            "CL" -> 0.42f
            "BR" -> 0.46f
            "I" -> 0.50f
            "FE" -> 0.50f
            else -> 0.38f
        }

    // Van der Waals radius in Angstroms for Space-Filling model
    val vdwRadius: Float
        get() = when (symbol.uppercase()) {
            "H" -> 1.20f
            "C" -> 1.70f
            "N" -> 1.55f
            "O" -> 1.52f
            "F" -> 1.47f
            "P" -> 1.80f
            "S" -> 1.80f
            "CL" -> 1.75f
            "BR" -> 1.85f
            "I" -> 1.98f
            "FE" -> 2.00f
            else -> 1.60f
        }
}

