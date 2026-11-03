package com.example.swiftyproteins.data.favorites

import android.content.Context

object FavoritesManager {

    private const val PREFS_NAME = "swifty_favorites"
    private const val KEY_FAVORITES = "favorites_set"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getAll(context: Context): Set<String> =
        prefs(context).getStringSet(KEY_FAVORITES, emptySet()) ?: emptySet()

    fun isFavorite(context: Context, ligandId: String): Boolean =
        getAll(context).contains(ligandId.uppercase())

    fun toggle(context: Context, ligandId: String): Boolean {
        val id = ligandId.uppercase()
        val current = getAll(context).toMutableSet()
        return if (current.contains(id)) {
            current.remove(id)
            prefs(context).edit().putStringSet(KEY_FAVORITES, current).apply()
            false
        } else {
            current.add(id)
            prefs(context).edit().putStringSet(KEY_FAVORITES, current).apply()
            true
        }
    }
}
