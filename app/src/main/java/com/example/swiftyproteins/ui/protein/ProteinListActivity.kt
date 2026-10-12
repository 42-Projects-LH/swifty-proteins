package com.example.swiftyproteins.ui.protein

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.swiftyproteins.databinding.ActivityProteinListBinding
import java.io.BufferedReader
import java.io.InputStreamReader

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
        Toast.makeText(this, "Selected: $proteinId", Toast.LENGTH_SHORT).show()
    }
}
