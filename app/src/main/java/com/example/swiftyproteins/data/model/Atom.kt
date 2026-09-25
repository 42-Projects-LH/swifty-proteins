package com.example.swiftyproteins.data.model

data class Atom(
    val id: String,
    val symbol: String,
    val x: Float,
    val y: Float,
    val z: Float
) {
    // CPK standard color mapping (hex)
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
            else -> 0xFFFF1493.toInt()  // Default / Other: Deep Pink
        }
}
