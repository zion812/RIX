package com.rio.rostry.core.media

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import java.security.MessageDigest

/**
 * Utility for generating cover thumbnails for fowls
 * Creates tiny thumbnails for list cards and profile headers to reduce cold-start cost
 */
class CoverThumbnailGenerator(private val context: Context) {
    
    /**
     * Generate a cover thumbnail from a list of proof URLs
     * Returns the URL of the first available proof image, or null if none available
     */
    fun generateCoverThumbnail(proofUrls: List<String>): String? {
        // In a real implementation, we would:
        // 1. Download the first proof image
        // 2. Generate a tiny thumbnail (e.g., 40x40 pixels)
        // 3. Store it in a cache
        // 4. Return the URL of the cached thumbnail
        
        // For now, we'll just return the first proof URL if available
        return proofUrls.firstOrNull()
    }
    
    /**
     * Generate a placeholder cover thumbnail with colored circle and initials
     * Useful when no proof images are available
     */
    fun generatePlaceholderThumbnail(name: String?, color: String, size: Int = 40): Bitmap {
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Draw background circle
        val paint = Paint().apply {
            this.isAntiAlias = true
            style = Paint.Style.FILL
            this.color = getColorFromString(color)
        }
        
        val radius = size / 2f
        canvas.drawCircle(radius, radius, radius, paint)
        
        // Draw initials if name is available
        if (!name.isNullOrEmpty()) {
            val initials = getInitials(name)
            
            paint.color = android.graphics.Color.WHITE
            paint.textSize = size / 2.5f
            paint.textAlign = Paint.Align.CENTER
            paint.isFakeBoldText = true
            
            val textBounds = Rect()
            paint.getTextBounds(initials, 0, initials.length, textBounds)
            
            val x = size / 2f
            val y = (size / 2f) - textBounds.exactCenterY()
            
            canvas.drawText(initials, x, y, paint)
        }
        
        return getRoundedCornerBitmap(bitmap, size / 10)
    }
    
    /**
     * Generate a unique color based on a string
     */
    private fun getColorFromString(input: String): Int {
        val bytes = MessageDigest.getInstance("MD5").digest(input.toByteArray())
        val hash = bytes.fold(0L) { acc, byte -> (acc shl 8) + (byte.toLong() and 0xff) }
        
        // Generate a vibrant color with good contrast
        val hue = (hash % 360).toInt()
        val saturation = 0.7f + (hash % 30) / 100f // 0.7 to 1.0
        val lightness = 0.4f + (hash % 30) / 100f  // 0.4 to 0.7
        
        return hslToRgb(hue, saturation, lightness)
    }
    
    /**
     * Convert HSL color values to RGB
     */
    private fun hslToRgb(hue: Int, saturation: Float, lightness: Float): Int {
        val h = hue / 60f
        val c = (1 - kotlin.math.abs(2 * lightness - 1)) * saturation
        val x = c * (1 - kotlin.math.abs(h % 2 - 1))
        val m = lightness - c / 2
        
        val (r, g, b) = when (h.toInt()) {
            0 -> arrayOf(c, x, 0f)
            1 -> arrayOf(x, c, 0f)
            2 -> arrayOf(0f, c, x)
            3 -> arrayOf(0f, x, c)
            4 -> arrayOf(x, 0f, c)
            5 -> arrayOf(c, 0f, x)
            else -> arrayOf(0f, 0f, 0f)
        }
        
        return android.graphics.Color.rgb(
            ((r + m) * 255).toInt(),
            ((g + m) * 255).toInt(),
            ((b + m) * 255).toInt()
        )
    }
    
    /**
     * Get initials from a name
     */
    private fun getInitials(name: String): String {
        val words = name.trim().split("\\s+".toRegex())
        return when (words.size) {
            0 -> ""
            1 -> words[0].take(2).uppercase()
            else -> (words[0].firstOrNull() ?: ' ') + "" + (words[1].firstOrNull() ?: ' ')
        }
    }
    
    /**
     * Create a rounded corner bitmap
     */
    fun getRoundedCornerBitmap(bitmap: Bitmap, pixels: Int): Bitmap {
        val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        
        val color = android.graphics.Color.RED
        val paint = Paint()
        val rect = Rect(0, 0, bitmap.width, bitmap.height)
        val rectF = RectF(rect)
        
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        canvas.drawRoundRect(rectF, pixels.toFloat(), pixels.toFloat(), paint)
        
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, rect, rect, paint)
        
        return output
    }
    
    /**
     * Update the fowl entity with the generated cover thumbnail URL
     * In a real implementation, this would update the database
     */
    fun updateFowlWithCoverThumbnail(fowlId: String, thumbnailUrl: String?) {
        // This is a placeholder method
        // In a real implementation, we would update the FowlEntity in the database
    }
}