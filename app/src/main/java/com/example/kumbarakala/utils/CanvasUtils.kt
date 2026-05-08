package com.example.kumbarakala.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.core.content.ContextCompat
import com.example.kumbarakala.R

object CanvasUtils {

    fun generateStoryCard(
        context: Context,
        productBitmap: Bitmap,
        itemType: String,
        artisanName: String,
        artisanPhone: String
    ): Bitmap {
        // Create a mutable copy of the bitmap
        val config = productBitmap.config ?: Bitmap.Config.ARGB_8888
        val resultBitmap = productBitmap.copy(config, true)
        val canvas = Canvas(resultBitmap)

        // Map for health benefits
        val healthBenefitsMap = mapOf(
            "Curd Pot" to "Maintains pH balance & enhances taste",
            "Water Jug" to "Natural cooling & alkaline water",
            "Cookware" to "Retains 100% nutrients & slow cooking",
            "Planter" to "Breathable clay for healthy roots",
            "Default" to "Eco-friendly, Non-toxic & Sustainable"
        )

        val benefitText = healthBenefitsMap[itemType] ?: healthBenefitsMap["Default"]!!
        val brandFooterText = "Crafted by: $artisanName | $artisanPhone"

        // Paint for Health Benefit Text (Top)
        val benefitPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 64f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            setShadowLayer(5f, 2f, 2f, Color.BLACK)
            textAlign = Paint.Align.CENTER
        }

        // Paint for Artisan Footer Text (Bottom)
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFD700") // Goldish
            textSize = 48f
            typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
            setShadowLayer(5f, 2f, 2f, Color.BLACK)
            textAlign = Paint.Align.CENTER
        }

        val width = canvas.width.toFloat()
        val height = canvas.height.toFloat()

        // Draw Health Benefit
        val textBounds = Rect()
        benefitPaint.getTextBounds(benefitText, 0, benefitText.length, textBounds)
        canvas.drawText(benefitText, width / 2, 100f + textBounds.height(), benefitPaint)

        // Draw Semi-transparent background for footer
        val bgPaint = Paint().apply {
            color = Color.parseColor("#80000000") // 50% Black
        }
        canvas.drawRect(0f, height - 120f, width, height, bgPaint)

        // Draw Brand Footer
        canvas.drawText(brandFooterText, width / 2, height - 40f, footerPaint)

        return resultBitmap
    }
}
