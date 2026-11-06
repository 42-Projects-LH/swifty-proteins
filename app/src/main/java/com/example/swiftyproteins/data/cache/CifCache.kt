package com.example.swiftyproteins.data.cache

import android.content.Context
import java.io.File

object CifCache {

    private fun cacheDir(context: Context): File =
        File(context.filesDir, "cif_cache").also { it.mkdirs() }

    private fun fileFor(context: Context, ligandId: String): File =
        File(cacheDir(context), "${ligandId.uppercase()}.cif")

    fun has(context: Context, ligandId: String): Boolean =
        fileFor(context, ligandId).exists()

    fun read(context: Context, ligandId: String): String? =
        fileFor(context, ligandId).takeIf { it.exists() }?.readText()

    fun write(context: Context, ligandId: String, content: String) {
        fileFor(context, ligandId).writeText(content)
    }
}
