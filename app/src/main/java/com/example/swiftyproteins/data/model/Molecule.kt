package com.example.swiftyproteins.data.model

import java.io.Serializable

data class Molecule(
    val name: String,
    val atoms: List<Atom>,
    val bonds: List<Bond>
) : Serializable

