package com.example.kumbarakala.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

object ShareUtils {

    fun shareBitmapToWhatsApp(context: Context, bitmap: Bitmap) {
        try {
            // Save bitmap to cache directory
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs() // don't forget to make the directory
            val file = File(cachePath, "story_card.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            // Get the URI using FileProvider
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            // Create Share Intent
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_TEXT, "Check out the traditional and health benefits of our handcrafted clay products! \uD83C\uDFFA\u2728")
                
                // Try to target WhatsApp specifically
                setPackage("com.whatsapp")
                
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            // Start activity
            try {
                context.startActivity(shareIntent)
            } catch (e: Exception) {
                // If WhatsApp is not installed, fallback to general share
                shareIntent.setPackage(null)
                context.startActivity(Intent.createChooser(shareIntent, "Share Story Card via"))
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
