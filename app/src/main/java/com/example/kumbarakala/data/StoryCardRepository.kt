package com.example.kumbarakala.data

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

data class SavedStoryCard(
    val id: String,
    val productName: String,
    val createdAtMillis: Long,
    val fileName: String,
    val healthBenefit: String = "",
    val ecoBenefit: String = "",
    val extraDetails: String = "",
    /** Catalog product id, or `"custom"` for custom-only cards; null on legacy saves. */
    val catalogProductId: String? = null,
    /** File name under [customAssetsDir], not a full path. */
    val customImageFileName: String? = null
)

class StoryCardRepository(context: Context) {

    private val appContext = context.applicationContext
    private val prefs: SharedPreferences =
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val cardsDir: File
        get() {
            val dir = File(appContext.filesDir, CARDS_SUBDIR)
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    private val customAssetsDir: File
        get() {
            val dir = File(appContext.filesDir, ASSETS_SUBDIR)
            if (!dir.exists()) dir.mkdirs()
            return dir
        }

    fun listSavedCards(): List<SavedStoryCard> {
        val raw = prefs.getString(KEY_INDEX, null) ?: return emptyList()
        return try {
            val arr = JSONArray(raw)
            buildList {
                for (i in 0 until arr.length()) {
                    val o = arr.getJSONObject(i)
                    add(parseCard(o))
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun getSavedCard(id: String): SavedStoryCard? =
        listSavedCards().find { it.id == id }

    fun saveCard(
        bitmap: Bitmap,
        productName: String,
        healthBenefit: String,
        ecoBenefit: String,
        extraDetails: String,
        catalogProductId: String?,
        customImageUri: Uri?
    ): SavedStoryCard {
        val id = UUID.randomUUID().toString()
        val fileName = "$id.png"
        val file = File(cardsDir, fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        val customFile = customImageUri?.let { copyUriToAssetFile(it) }
        val entry = SavedStoryCard(
            id = id,
            productName = productName.ifBlank { "Story card" },
            createdAtMillis = System.currentTimeMillis(),
            fileName = fileName,
            healthBenefit = healthBenefit,
            ecoBenefit = ecoBenefit,
            extraDetails = extraDetails,
            catalogProductId = catalogProductId?.takeIf { it.isNotBlank() },
            customImageFileName = customFile
        )
        val current = listSavedCards().toMutableList()
        current.add(0, entry)
        persistIndex(current)
        return entry
    }

    /**
     * Replaces the PNG and metadata for an existing gallery item.
     * @param newCustomImageUri when non-null, replaces any stored custom product photo.
     */
    fun updateSavedCard(
        id: String,
        bitmap: Bitmap,
        productName: String,
        healthBenefit: String,
        ecoBenefit: String,
        extraDetails: String,
        catalogProductId: String?,
        newCustomImageUri: Uri?
    ): Boolean {
        val current = listSavedCards().toMutableList()
        val index = current.indexOfFirst { it.id == id }
        if (index < 0) return false
        val old = current[index]
        val file = File(cardsDir, old.fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        var customName = old.customImageFileName
        if (newCustomImageUri != null) {
            old.customImageFileName?.let { deleteAssetFile(it) }
            customName = copyUriToAssetFile(newCustomImageUri)
        }
        val updated = old.copy(
            productName = productName.ifBlank { "Story card" },
            healthBenefit = healthBenefit,
            ecoBenefit = ecoBenefit,
            extraDetails = extraDetails,
            catalogProductId = catalogProductId?.takeIf { it.isNotBlank() },
            customImageFileName = customName
        )
        current[index] = updated
        persistIndex(current)
        return true
    }

    fun getCardFile(card: SavedStoryCard): File = File(cardsDir, card.fileName)

    fun getCustomImageFile(card: SavedStoryCard): File? =
        card.customImageFileName?.let { File(customAssetsDir, it) }

    fun deleteCard(id: String): Boolean {
        val current = listSavedCards().toMutableList()
        val removed = current.find { it.id == id } ?: return false
        File(cardsDir, removed.fileName).delete()
        removed.customImageFileName?.let { deleteAssetFile(it) }
        current.removeAll { it.id == id }
        persistIndex(current)
        return true
    }

    private fun deleteAssetFile(assetFileName: String) {
        File(customAssetsDir, assetFileName).delete()
    }

    private fun copyUriToAssetFile(uri: Uri): String {
        val name = "${UUID.randomUUID()}.jpg"
        val outFile = File(customAssetsDir, name)
        appContext.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(outFile).use { output -> input.copyTo(output) }
        } ?: throw IllegalStateException("Could not read image")
        return name
    }

    private fun parseCard(o: JSONObject): SavedStoryCard {
        return SavedStoryCard(
            id = o.getString("id"),
            productName = o.getString("productName"),
            createdAtMillis = o.getLong("createdAtMillis"),
            fileName = o.getString("fileName"),
            healthBenefit = o.optString("healthBenefit", ""),
            ecoBenefit = o.optString("ecoBenefit", ""),
            extraDetails = o.optString("extraDetails", ""),
            catalogProductId = o.optString("catalogProductId", "").takeIf { it.isNotEmpty() },
            customImageFileName = o.optString("customImageFileName", "").takeIf { it.isNotEmpty() }
        )
    }

    private fun persistIndex(list: List<SavedStoryCard>) {
        val arr = JSONArray()
        for (c in list) {
            val o = JSONObject()
            o.put("id", c.id)
            o.put("productName", c.productName)
            o.put("createdAtMillis", c.createdAtMillis)
            o.put("fileName", c.fileName)
            o.put("healthBenefit", c.healthBenefit)
            o.put("ecoBenefit", c.ecoBenefit)
            o.put("extraDetails", c.extraDetails)
            o.put("catalogProductId", c.catalogProductId ?: "")
            o.put("customImageFileName", c.customImageFileName ?: "")
            arr.put(o)
        }
        prefs.edit().putString(KEY_INDEX, arr.toString()).apply()
    }

    companion object {
        private const val PREFS_NAME = "story_cards_index"
        private const val KEY_INDEX = "cards_json"
        private const val CARDS_SUBDIR = "story_cards"
        private const val ASSETS_SUBDIR = "story_card_assets"
    }
}
