package com.example.swiftyproteins.ui.protein

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.swiftyproteins.R
import com.example.swiftyproteins.data.cache.CifCache
import com.example.swiftyproteins.data.favorites.FavoritesManager
import com.example.swiftyproteins.databinding.ItemProteinBinding

class ProteinAdapter(
    private val context: Context,
    private val originalList: List<String>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<ProteinAdapter.ProteinViewHolder>() {

    private var filteredList: List<String> = originalList.toList()
    private var showFavoritesOnly = false
    private var lastQuery = ""

    inner class ProteinViewHolder(val binding: ItemProteinBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProteinViewHolder {
        val binding = ItemProteinBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ProteinViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProteinViewHolder, position: Int) {
        val proteinId = filteredList[position]

        holder.binding.tvProteinId.text = proteinId

        holder.binding.tvCachedBadge.visibility =
            if (CifCache.has(context, proteinId)) View.VISIBLE else View.GONE

        val isFav = FavoritesManager.isFavorite(context, proteinId)
        holder.binding.btnFavorite.setImageResource(
            if (isFav) R.drawable.ic_star_filled else R.drawable.ic_star_outline
        )

        holder.binding.btnFavorite.setOnClickListener {
            val nowFav = FavoritesManager.toggle(context, proteinId)
            holder.binding.btnFavorite.setImageResource(
                if (nowFav) R.drawable.ic_star_filled else R.drawable.ic_star_outline
            )
            if (showFavoritesOnly) {
                rebuildFilter()
                notifyDataSetChanged()
            }
        }

        holder.binding.root.setOnClickListener { onItemClick(proteinId) }
    }

    override fun getItemCount(): Int = filteredList.size

    fun filter(query: String) {
        lastQuery = query
        rebuildFilter()
        notifyDataSetChanged()
    }

    fun setShowFavoritesOnly(enabled: Boolean) {
        showFavoritesOnly = enabled
        rebuildFilter()
        notifyDataSetChanged()
    }

    private fun rebuildFilter() {
        val base = if (showFavoritesOnly)
            originalList.filter { FavoritesManager.isFavorite(context, it) }
        else
            originalList

        filteredList = if (lastQuery.isEmpty()) base
        else base.filter { it.contains(lastQuery, ignoreCase = true) }
    }
}
