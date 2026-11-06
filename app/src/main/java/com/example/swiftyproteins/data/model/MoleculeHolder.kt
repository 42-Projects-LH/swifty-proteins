package com.example.swiftyproteins.data.model

/**
 * Singleton holder to retain the currently loaded Molecule in memory
 * when navigating from ProteinListActivity to ProteinViewerActivity.
 * Eliminates Android Binder TransactionTooLargeException for large molecules.
 */
object MoleculeHolder {
    var currentMolecule: Molecule? = null

    fun clear() {
        currentMolecule = null
    }
}
