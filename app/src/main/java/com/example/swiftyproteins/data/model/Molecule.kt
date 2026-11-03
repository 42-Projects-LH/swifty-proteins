package com.example.swiftyproteins.data.model

import java.io.Serializable

data class Molecule(
    val name: String,
    val atoms: List<Atom>,
    val bonds: List<Bond>
) : Serializable {

    // Hill notation: C first, H second, then others alphabetically
    val molecularFormula: String
        get() {
            val counts = atoms.groupingBy { it.symbol.uppercase() }.eachCount()
            val sb = StringBuilder()
            counts["C"]?.let { sb.append("C"); if (it > 1) sb.append(it) }
            counts["H"]?.let { sb.append("H"); if (it > 1) sb.append(it) }
            counts.entries
                .filter { it.key != "C" && it.key != "H" }
                .sortedBy { it.key }
                .forEach { (sym, n) -> sb.append(sym); if (n > 1) sb.append(n) }
            return sb.toString().ifEmpty { name }
        }
}

