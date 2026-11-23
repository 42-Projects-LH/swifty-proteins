package com.example.swiftyproteins.data.parser

import com.example.swiftyproteins.data.model.Atom
import com.example.swiftyproteins.data.model.Bond
import com.example.swiftyproteins.data.model.Molecule

object CifParser {

    private val tokenRegex = Regex("""'([^']*)'|"([^"]*)"|(\S+)""")

    private fun tokenizeCifLine(line: String): List<String> {
        return tokenRegex.findAll(line).map { match ->
            match.groups[1]?.value ?: match.groups[2]?.value ?: match.groups[3]!!.value
        }.toList()
    }

    private data class RawAtomRecord(
        val id: String,
        val symbol: String,
        val modelX: Float?,
        val modelY: Float?,
        val modelZ: Float?,
        val idealX: Float?,
        val idealY: Float?,
        val idealZ: Float?
    )

    fun parse(ligandId: String, cifContent: String): Molecule {
        val rawAtoms = mutableListOf<RawAtomRecord>()
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
                atomHeaders.clear()
                bondHeaders.clear()
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
                val tokens = tokenizeCifLine(trimmed)
                val atomIdIdx = atomHeaders.indexOf("atom_id")
                val symbolIdx = atomHeaders.indexOf("type_symbol")
                val xIdx = atomHeaders.indexOf("model_Cartn_x")
                val yIdx = atomHeaders.indexOf("model_Cartn_y")
                val zIdx = atomHeaders.indexOf("model_Cartn_z")
                val idealXIdx = atomHeaders.indexOf("pdbx_model_Cartn_x_ideal")
                val idealYIdx = atomHeaders.indexOf("pdbx_model_Cartn_y_ideal")
                val idealZIdx = atomHeaders.indexOf("pdbx_model_Cartn_z_ideal")

                if (tokens.size >= atomHeaders.size && atomIdIdx != -1) {
                    val id = tokens[atomIdIdx]
                    val symbol = if (symbolIdx != -1) tokens[symbolIdx] else id.takeWhile { it.isLetter() }

                    val modelX = if (xIdx != -1) tokens[xIdx].toFloatOrNull() else null
                    val modelY = if (yIdx != -1) tokens[yIdx].toFloatOrNull() else null
                    val modelZ = if (zIdx != -1) tokens[zIdx].toFloatOrNull() else null

                    val idealX = if (idealXIdx != -1) tokens[idealXIdx].toFloatOrNull() else null
                    val idealY = if (idealYIdx != -1) tokens[idealYIdx].toFloatOrNull() else null
                    val idealZ = if (idealZIdx != -1) tokens[idealZIdx].toFloatOrNull() else null

                    rawAtoms.add(
                        RawAtomRecord(
                            id = id,
                            symbol = symbol,
                            modelX = modelX,
                            modelY = modelY,
                            modelZ = modelZ,
                            idealX = idealX,
                            idealY = idealY,
                            idealZ = idealZ
                        )
                    )
                }
            }

            if (inBondLoop && bondHeaders.isNotEmpty()) {
                if (trimmed.startsWith("_")) {
                    inBondLoop = false
                    continue
                }
                val tokens = tokenizeCifLine(trimmed)
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

        // Do not mix coordinate systems (model_Cartn_* can be at arbitrary crystal lattice offsets
        // like Z=60 or Z=100, while pdbx_model_Cartn_*_ideal is centered at the origin).
        // Prefer experimental model coordinates when all atoms have them; otherwise use ideal
        // coordinates when all atoms have them; otherwise pick whichever system has the most atoms.
        val allHaveModel = rawAtoms.isNotEmpty() && rawAtoms.all { it.modelX != null && it.modelY != null && it.modelZ != null }
        val allHaveIdeal = rawAtoms.isNotEmpty() && rawAtoms.all { it.idealX != null && it.idealY != null && it.idealZ != null }
        val modelCount = rawAtoms.count { it.modelX != null && it.modelY != null && it.modelZ != null }
        val idealCount = rawAtoms.count { it.idealX != null && it.idealY != null && it.idealZ != null }

        val useIdeal = when {
            allHaveModel -> false
            allHaveIdeal -> true
            else -> idealCount >= modelCount
        }

        val atoms = rawAtoms.mapNotNull { rec ->
            val x = if (useIdeal) rec.idealX else rec.modelX
            val y = if (useIdeal) rec.idealY else rec.modelY
            val z = if (useIdeal) rec.idealZ else rec.modelZ
            if (x != null && y != null && z != null) {
                Atom(id = rec.id, symbol = rec.symbol, x = x, y = y, z = z)
            } else {
                null
            }
        }

        val validAtomIds = atoms.map { it.id }.toHashSet()
        val validBonds = bonds.filter { it.atom1Id in validAtomIds && it.atom2Id in validAtomIds }

        return Molecule(name = ligandId, atoms = atoms, bonds = validBonds)
    }
}
