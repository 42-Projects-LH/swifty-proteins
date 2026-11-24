package com.example.swiftyproteins.ui.viewer

import android.content.ContentValues
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.swiftyproteins.SwiftyProteinsApp
import com.example.swiftyproteins.data.model.Atom
import com.example.swiftyproteins.data.model.MoleculeHolder
import com.example.swiftyproteins.databinding.ActivityProteinViewerBinding
import com.example.swiftyproteins.ui.auth.LoginActivity
import com.example.swiftyproteins.ui.viewer.gl.RenderModel
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

class ProteinViewerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProteinViewerBinding
    private var currentModelIndex = 0
    private val modelOptions = listOf(
        RenderModel.BALL_AND_STICK to "Ball & Stick",
        RenderModel.SPACE_FILLING to "Space-Filling",
        RenderModel.STICK to "Stick",
        RenderModel.WIREFRAME to "Wireframe"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProteinViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val molecule = MoleculeHolder.currentMolecule
        if (molecule == null) {
            Toast.makeText(this, "No molecule data available", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Setup Header Information
        val baseStatsText = "${molecule.atoms.size} Atoms • ${molecule.bonds.size} Bonds"
        binding.tvLigandName.text = molecule.name
        binding.tvMoleculeStats.text = baseStatsText

        // Pass molecule structure to OpenGL renderer
        binding.glSurfaceView.renderer.setMolecule(molecule)

        // Update live FPS readout in header subtitle (capped at 120 FPS)
        binding.glSurfaceView.renderer.onFpsUpdateListener = { fps ->
            runOnUiThread {
                binding.tvMoleculeStats.text = "$baseStatsText • $fps FPS"
            }
        }

        // Setup Atom Selection, Tooltip and element highlight
        binding.glSurfaceView.onAtomSelectedListener = { atom ->
            runOnUiThread {
                if (atom != null) {
                    showAtomTooltip(atom)
                    binding.glSurfaceView.renderer.highlightElement = atom.symbol
                } else {
                    hideAtomTooltip()
                    binding.glSurfaceView.renderer.highlightElement = null
                }
            }
        }

        binding.btnCloseTooltip.setOnClickListener {
            binding.glSurfaceView.renderer.selectedAtom = null
            binding.glSurfaceView.renderer.highlightElement = null
            hideAtomTooltip()
        }

        // Wire up atom label overlay — share the same centerOffset array as the renderer
        binding.atomLabelOverlay.renderer = binding.glSurfaceView.renderer
        binding.atomLabelOverlay.centerOffset = binding.glSurfaceView.renderer.centerOffset
        binding.atomLabelOverlay.atoms = molecule.atoms

        binding.btnToggleLabels.setOnClickListener {
            val nowEnabled = !binding.atomLabelOverlay.showLabels
            binding.atomLabelOverlay.showLabels = nowEnabled
            binding.btnToggleLabels.alpha = if (nowEnabled) 1f else 0.45f
        }

        // Back Navigation
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Reset View Button
        binding.btnResetCamera.setOnClickListener {
            binding.glSurfaceView.renderer.resetCamera()
        }

        // Multiple Visualization Models Switcher
        binding.btnToggleModel.setOnClickListener {
            currentModelIndex = (currentModelIndex + 1) % modelOptions.size
            val (selectedModel, label) = modelOptions[currentModelIndex]
            binding.glSurfaceView.renderer.renderModel = selectedModel
            binding.btnToggleModel.text = label
        }

        // Share & Save to Photo Album
        binding.btnShare.setOnClickListener {
            captureAndShareModel(molecule.name)
        }
    }

    private fun showAtomTooltip(atom: Atom) {
        binding.cardAtomInfo.visibility = View.VISIBLE
        binding.tvAtomTitle.text = "${atom.elementName} (${atom.symbol})"
        binding.tvAtomId.text = "Atom ID: ${atom.id}"
        binding.tvAtomCoords.text = String.format(
            Locale.US,
            "Coordinates: X: %.3f | Y: %.3f | Z: %.3f",
            atom.x, atom.y, atom.z
        )
        binding.viewAtomColor.backgroundTintList = ColorStateList.valueOf(atom.colorHex)
    }

    private fun hideAtomTooltip() {
        binding.cardAtomInfo.visibility = View.GONE
    }

    private fun captureAndShareModel(ligandName: String) {
        Toast.makeText(this, "Exporting 3D snapshot...", Toast.LENGTH_SHORT).show()

        binding.glSurfaceView.renderer.requestScreenshot { bitmap ->
            val mol = MoleculeHolder.currentMolecule
            saveToPhotoAlbum(bitmap, ligandName)
            shareBitmap(bitmap, ligandName, mol?.molecularFormula, mol?.atoms?.size, mol?.bonds?.size)
        }
    }

    private fun saveToPhotoAlbum(bitmap: Bitmap, ligandName: String) {
        try {
            val fileName = "Protein_${ligandName}_${System.currentTimeMillis()}.png"
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/SwiftyProteins")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }

            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            if (uri != null) {
                contentResolver.openOutputStream(uri)?.use { stream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                    contentResolver.update(uri, contentValues, null, null)
                }
                Toast.makeText(this, "Saved snapshot to Photo Album", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun shareBitmap(
        bitmap: Bitmap,
        ligandName: String,
        formula: String? = null,
        atomCount: Int? = null,
        bondCount: Int? = null
    ) {
        try {
            val cacheDir = File(cacheDir, "images").apply { mkdirs() }
            val shareFile = File(cacheDir, "shared_protein_${ligandName}.png")
            FileOutputStream(shareFile).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }

            val contentUri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                shareFile
            )

            val shareText = buildString {
                appendLine("🔬 Ligand: $ligandName")
                if (formula != null) appendLine("Formula: $formula")
                if (atomCount != null && bondCount != null)
                    appendLine("Structure: $atomCount atoms · $bondCount bonds")
                appendLine()
                append("Rendered with Swifty Proteins — RCSB PDB")
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Swifty Proteins: $ligandName ($formula)")
                putExtra(Intent.EXTRA_TEXT, shareText)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            SwiftyProteinsApp.isHandlingExternalIntent = true
            startActivity(Intent.createChooser(shareIntent, "Share Protein Model"))
        } catch (e: Exception) {
            SwiftyProteinsApp.isHandlingExternalIntent = false
            Toast.makeText(this, "Failed to share: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        if (SwiftyProteinsApp.isSessionLocked) {
            SwiftyProteinsApp.isSessionLocked = false
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
            finish()
            return
        }
        binding.glSurfaceView.onResume()
    }

    override fun onPause() {
        super.onPause()
        binding.glSurfaceView.onPause()
    }
}
