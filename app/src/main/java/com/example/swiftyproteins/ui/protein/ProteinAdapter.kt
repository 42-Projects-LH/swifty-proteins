package com.example.swiftyproteins.ui.protein

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.swiftyproteins.databinding.ItemProteinBinding

class ProteinAdapter(
    private val originalList: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<ProteinAdapter.ProteinViewHolder>() {

    private var filteredList: List<String> = originalList.toList()

    inner class ProteinViewHolder(val binding: ItemProteinBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProteinViewHolder {
        val binding = ItemProteinBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProteinViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProteinViewHolder, position: Int) {
        val proteinId = filteredList[position]
        holder.binding.tvProteinId.text = proteinId
        holder.binding.root.setOnClickListener {
            onItemClick(proteinId)
        }
    }

    override fun getItemCount(): Int = filteredList.size

    fun filter(query: String) {
        filteredList = if (query.isEmpty()) {
            originalList.toList()
        } else {
            originalList.filter { it.contains(query, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }
}
