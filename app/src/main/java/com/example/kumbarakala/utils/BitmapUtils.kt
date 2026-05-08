package com.example.kumbarakala.utils

import android.content.Context
import android.graphics.*
import androidx.core.content.ContextCompat
import com.example.kumbarakala.R

object BitmapUtils {

    fun generateStoryCard(
        context: Context,
        productName: String,
        healthBenefit: String,
        ecoBenefit: String,
        otherDetails: String,
        artisanName: String,
        artisanPhone: String,
        makerTitle: String,
        makerLocation: String,
        makerExperience: String,
        makerSpecialization: String,
        makerEmail: String,
        productBitmap: Bitmap?,
        fallbackImageResId: Int?
    ): Bitmap {
        val width = 1080
        val height = 1920
        
        // Create an empty bitmap and a canvas to draw on it
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Draw the elegant traditional background texture
        val bgDrawable = ContextCompat.getDrawable(context, R.drawable.traditional_texture)
        bgDrawable?.setBounds(0, 0, width, height)
        bgDrawable?.draw(canvas)
        
        // If background texture fails or to ensure opacity, draw a semi-transparent dark overlay
        val overlayPaint = Paint().apply {
            color = Color.parseColor("#991E1E1E") // Dark overlay
        }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), overlayPaint)

        // Draw product image in the top half
        val resolvedBitmap = productBitmap ?: fallbackImageResId?.let {
            BitmapFactory.decodeResource(context.resources, it)
        }
        val scaledProductHeight = 900
        val scaledProductWidth = resolvedBitmap?.let {
            (it.width.toFloat() / it.height.toFloat() * scaledProductHeight).toInt()
        } ?: 700
        
        val left = (width - scaledProductWidth) / 2
        val top = 150
        
        // Draw a neat border/frame for the product image
        val framePaint = Paint().apply {
            color = Color.parseColor("#D4AF37") // Elegant Gold
            style = Paint.Style.STROKE
            strokeWidth = 10f
        }
        val rect = Rect(left, top, left + scaledProductWidth, top + scaledProductHeight)
        if (resolvedBitmap != null) {
            canvas.drawBitmap(resolvedBitmap, null, rect, null)
        } else {
            val placeholderPaint = Paint().apply { color = Color.parseColor("#5A5A5A") }
            canvas.drawRect(rect, placeholderPaint)
        }
        canvas.drawRect(rect, framePaint)
        
        // Draw Texts
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 80f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        
        val headingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D4AF37") // Gold
            textSize = 55f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 45f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        
        // Product Name
        var currentY = top + scaledProductHeight + 120f
        canvas.drawText(productName.uppercase(), width / 2f, currentY, titlePaint)
        
        // Health Benefit
        currentY += 120f
        canvas.drawText("HEALTH BENEFIT", width / 2f, currentY, headingPaint)
        currentY += 60f
        drawMultilineText(canvas, healthBenefit, width / 2f, currentY, bodyPaint, width - 100)
        
        // Eco Benefit
        currentY += 150f
        canvas.drawText("ECO BENEFIT", width / 2f, currentY, headingPaint)
        currentY += 60f
        drawMultilineText(canvas, ecoBenefit, width / 2f, currentY, bodyPaint, width - 100)

        if (otherDetails.isNotBlank()) {
            currentY += 150f
            canvas.drawText("OTHER DETAILS", width / 2f, currentY, headingPaint)
            currentY += 60f
            drawMultilineText(canvas, otherDetails, width / 2f, currentY, bodyPaint, width - 100)
        }

        val makerDetails = listOfNotNull(
            makerTitle.takeIf { it.isNotBlank() }?.let { "Title: $it" },
            makerSpecialization.takeIf { it.isNotBlank() }?.let { "Specialization: $it" },
            makerExperience.takeIf { it.isNotBlank() }?.let { "Experience: $it" },
            makerLocation.takeIf { it.isNotBlank() }?.let { "Location: $it" },
            makerEmail.takeIf { it.isNotBlank() }?.let { "Email: $it" }
        )
        if (makerDetails.isNotEmpty()) {
            currentY += 140f
            canvas.drawText("MEET THE MAKER", width / 2f, currentY, headingPaint)
            currentY += 55f
            drawMultilineText(
                canvas = canvas,
                text = makerDetails.joinToString(" | "),
                x = width / 2f,
                y = currentY,
                paint = bodyPaint.apply { textSize = 38f },
                maxWidth = width - 100
            )
        }
        
        // Artisan Details at the bottom
        val bottomPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F7F1E3")
            textSize = 40f
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            textAlign = Paint.Align.CENTER
        }
        
        val bottomY = height - 120f
        canvas.drawText("Handcrafted by: $artisanName", width / 2f, bottomY, bottomPaint)
        if (artisanPhone.isNotBlank()) {
            canvas.drawText("Contact: $artisanPhone", width / 2f, bottomY + 60f, bottomPaint)
        }
        
        return bitmap
    }
    
    private fun drawMultilineText(canvas: Canvas, text: String, x: Float, y: Float, paint: Paint, maxWidth: Int) {
        val words = text.split(" ")
        var currentLine = ""
        var currentY = y
        
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            val testWidth = paint.measureText(testLine)
            
            if (testWidth > maxWidth) {
                canvas.drawText(currentLine, x, currentY, paint)
                currentLine = word
                currentY += paint.descent() - paint.ascent()
            } else {
                currentLine = testLine
            }
        }
        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine, x, currentY, paint)
        }
    }
}
