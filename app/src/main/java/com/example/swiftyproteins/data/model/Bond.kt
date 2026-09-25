package com.example.swiftyproteins.data.model

data class Bond(
    val atom1Id: String,
    val atom2Id: String,
    val type: String // e.g., "SING", "DOUB", "TRIP"
)
