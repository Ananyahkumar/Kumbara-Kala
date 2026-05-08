package com.example.kumbarakala.ui.screens

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
// import coil.load // Requires io.coil-kt:coil to be in build.gradle
import com.example.kumbarakala.R
import com.example.kumbarakala.data.ProductData
import com.example.kumbarakala.model.Product
import com.example.kumbarakala.utils.CanvasUtils
import java.io.File
import java.io.FileOutputStream

class GalleryActivity : AppCompatActivity() {

    // Note: ViewBinding would be ActivityGalleryBinding if enabled.
    // We are using standard findViewById for simplicity if ViewBinding isn't fully set up yet.
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)

        recyclerView = findViewById(R.id.recyclerViewGallery)
        
        // 1. Create a StaggeredGridLayout using RecyclerView
        val layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
        recyclerView.layoutManager = layoutManager
        
        val adapter = GalleryAdapter(ProductData.products) { product, imageView ->
            generateAndShareStory(product, imageView)
        }
        recyclerView.adapter = adapter
    }

    private fun generateAndShareStory(product: Product, imageView: ImageView) {
        val drawable = imageView.drawable
        if (drawable is BitmapDrawable) {
            val originalBitmap = drawable.bitmap
            
            // 2. Dynamic Story Card Generator using Canvas
            // Here we assume Artisan Profile data is fetched from EncryptedSharedPreferences (ViewModel)
            // Hardcoding for demonstration
            val artisanName = "Lakshmi Narayan"
            val artisanPhone = "+91 9876543210"
            
            val storyCardBitmap = CanvasUtils.generateStoryCard(
                context = this,
                productBitmap = originalBitmap,
                itemType = product.name, // or product.category
                artisanName = artisanName,
                artisanPhone = artisanPhone
            )

            // 3. WhatsApp Integration via FileProvider
            shareToWhatsApp(storyCardBitmap)
        }
    }

    private fun shareToWhatsApp(bitmap: Bitmap) {
        try {
            val cachePath = File(cacheDir, "images")
            cachePath.mkdirs() // don't forget to make the directory
            val file = File(cachePath, "story_card.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()

            val imageUri: Uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_STREAM, imageUri)
                type = "image/png"
                setPackage("com.whatsapp")
            }
            startActivity(Intent.createChooser(shareIntent, "Share your story via..."))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

class GalleryAdapter(
    private val products: List<Product>,
    private val onGenerateStoryClick: (Product, ImageView) -> Unit
) : RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder>() {

    class GalleryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val productImage: ImageView = view.findViewById(R.id.productImage)
        val productName: TextView = view.findViewById(R.id.productName)
        val generateStoryAction: TextView = view.findViewById(R.id.generateStoryAction)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GalleryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product_gallery, parent, false)
        return GalleryViewHolder(view)
    }

    override fun onBindViewHolder(holder: GalleryViewHolder, position: Int) {
        val product = products[position]
        holder.productName.text = product.name
        
        // Use Coil for image handling
        // holder.productImage.load(product.imageResId) { crossfade(true) }
        holder.productImage.setImageResource(product.imageResId) // Fallback if coil is not configured

        holder.itemView.setOnClickListener {
            onGenerateStoryClick(product, holder.productImage)
        }
    }

    override fun getItemCount() = products.size
}
