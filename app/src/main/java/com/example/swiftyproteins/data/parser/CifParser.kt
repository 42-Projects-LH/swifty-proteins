package com.example.swiftyproteins.data.parser

import com.example.swiftyproteins.data.model.Atom
import com.example.swiftyproteins.data.model.Bond
import com.example.swiftyproteins.data.model.Molecule

object CifParser {

    fun parse(ligandId: String, cifContent: String): Molecule {
        val atoms = mutableListOf<Atom>()
        val bonds = mutableListOf<Bond>()

        val lines = cifContent.lines()
        var inAtomLoop = false
        var inBondLoop = false

        val atomHeaders = mutableListOf<String>()
        val bondHeaders = mutableListOf<String>()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty() || trimmed.startsWith("#")) continue

            if (trimmed.startsWith("loop_")) {
                inAtomLoop = false
                inBondLoop = false
                continue
            }

            if (trimmed.startsWith("_chem_comp_atom.")) {
                inAtomLoop = true
                atomHeaders.add(trimmed.substringAfter("_chem_comp_atom."))
                continue
            }

            if (trimmed.startsWith("_chem_comp_bond.")) {
                inBondLoop = true
                bondHeaders.add(trimmed.substringAfter("_chem_comp_bond."))
                continue
            }

            if (inAtomLoop && atomHeaders.isNotEmpty()) {
                if (trimmed.startsWith("_")) {
                    inAtomLoop = false
                    continue
                }
                val tokens = trimmed.split(Regex("\\s+"))
                val atomIdIdx = atomHeaders.indexOf("atom_id")
                val symbolIdx = atomHeaders.indexOf("type_symbol")
                val xIdx = atomHeaders.indexOf("model_Cartn_x")
                val yIdx = atomHeaders.indexOf("model_Cartn_y")
                val zIdx = atomHeaders.indexOf("model_Cartn_z")

                if (tokens.size >= atomHeaders.size && atomIdIdx != -1 && xIdx != -1 && yIdx != -1 && zIdx != -1) {
                    val id = tokens[atomIdIdx]
                    val symbol = if (symbolIdx != -1) tokens[symbolIdx] else id.takeWhile { it.isLetter() }
                    val x = tokens[xIdx].toFloatOrNull() ?: 0f
                    val y = tokens[yIdx].toFloatOrNull() ?: 0f
                    val z = tokens[zIdx].toFloatOrNull() ?: 0f

                    atoms.add(Atom(id = id, symbol = symbol, x = x, y = y, z = z))
                }
            }

            if (inBondLoop && bondHeaders.isNotEmpty()) {
                if (trimmed.startsWith("_")) {
                    inBondLoop = false
                    continue
                }
                val tokens = trimmed.split(Regex("\\s+"))
                val atom1Idx = bondHeaders.indexOf("atom_id_1")
                val atom2Idx = bondHeaders.indexOf("atom_id_2")
                val typeIdx = bondHeaders.indexOf("value_order")

                if (tokens.size >= bondHeaders.size && atom1Idx != -1 && atom2Idx != -1) {
                    val atom1 = tokens[atom1Idx]
                    val atom2 = tokens[atom2Idx]
                    val type = if (typeIdx != -1) tokens[typeIdx] else "SING"

                    bonds.add(Bond(atom1Id = atom1, atom2Id = atom2, type = type))
                }
            }
        }

        return Molecule(name = ligandId, atoms = atoms, bonds = bonds)
    }
}
