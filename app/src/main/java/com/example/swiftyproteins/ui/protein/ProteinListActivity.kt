package com.example.swiftyproteins.ui.protein

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.swiftyproteins.SwiftyProteinsApp
import com.example.swiftyproteins.data.network.NetworkClient
import com.example.swiftyproteins.data.parser.CifParser
import com.example.swiftyproteins.databinding.ActivityProteinListBinding
import com.example.swiftyproteins.ui.auth.LoginActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.SocketTimeoutException
import java.net.UnknownHostException

class ProteinListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProteinListBinding
    private lateinit var adapter: ProteinAdapter
    private val proteinList: MutableList<String> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProteinListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadLigandsFromAssets()
        setupRecyclerView()
        setupSearchView()
    }

    override fun onResume() {
        super.onResume()
        if (SwiftyProteinsApp.isSessionLocked) {
            SwiftyProteinsApp.isSessionLocked = false
            val intent = Intent(this, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(intent)
            finish()
        }
    }

    private fun loadLigandsFromAssets() {
        try {
            val inputStream = assets.open("ligands.txt")
            val reader = BufferedReader(InputStreamReader(inputStream))
            reader.forEachLine { line ->
                val trimmed = line.trim()
                if (trimmed.isNotEmpty()) {
                    proteinList.add(trimmed)
                }
            }
            reader.close()
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to load protein list", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupRecyclerView() {
        adapter = ProteinAdapter(proteinList) { selectedProtein ->
            onProteinSelected(selectedProtein)
        }
        binding.rvProteins.layoutManager = LinearLayoutManager(this)
        binding.rvProteins.adapter = adapter
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText ?: "")
                return true
            }
        })
    }

    private fun onProteinSelected(proteinId: String) {
        // Show spinning wheel loading overlay
        binding.loadingOverlay.visibility = View.VISIBLE
        binding.tvLoadingMessage.text = "Fetching $proteinId from RCSB..."

        lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    NetworkClient.apiService.getLigandCif(proteinId)
                }

                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    val cifContent = response.body()!!
                    val molecule = withContext(Dispatchers.Default) {
                        CifParser.parse(proteinId, cifContent)
                    }

                    binding.loadingOverlay.visibility = View.GONE

                    if (molecule.atoms.isEmpty()) {
                        showWarningDialog(
                            title = "Parsing Error",
                            message = "The ligand '$proteinId' does not contain readable 3D coordinate atoms in its CIF record."
                        )
                        return@launch
                    }

                    Toast.makeText(
                        this@ProteinListActivity,
                        "Loaded $proteinId: ${molecule.atoms.size} atoms, ${molecule.bonds.size} bonds",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Next milestone: transition to ProteinViewerActivity
                } else {
                    binding.loadingOverlay.visibility = View.GONE
                    val code = response.code()
                    val errorMsg = when (code) {
                        404 -> "Ligand '$proteinId' was not found on the RCSB database (HTTP 404)."
                        else -> "Failed to load ligand '$proteinId' from server (HTTP $code: ${response.message()})."
                    }
                    showWarningDialog("Loading Failed", errorMsg)
                }
            } catch (e: UnknownHostException) {
                binding.loadingOverlay.visibility = View.GONE
                showWarningDialog(
                    "Network Error",
                    "Unable to connect to RCSB PDB server.\nPlease check your internet connection."
                )
            } catch (e: SocketTimeoutException) {
                binding.loadingOverlay.visibility = View.GONE
                showWarningDialog(
                    "Connection Timeout",
                    "The request to download ligand '$proteinId' timed out. Please try again."
                )
            } catch (e: Exception) {
                binding.loadingOverlay.visibility = View.GONE
                showWarningDialog(
                    "Error",
                    "An unexpected error occurred while fetching '$proteinId':\n${e.localizedMessage ?: e.message}"
                )
            }
        }
    }

    private fun showWarningDialog(title: String, message: String) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton("OK", null)
            .show()
    }
}
