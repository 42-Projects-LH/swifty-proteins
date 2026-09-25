package com.example.swiftyproteins.data.model

data class Molecule(
    val name: String,
    val atoms: List<Atom>,
    val bonds: List<Bond>
)
