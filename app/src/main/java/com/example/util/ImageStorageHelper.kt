package com.example.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ImageStorageHelper {

    fun saveWatermarkedSelfie(
        context: Context,
        originalBitmap: Bitmap,
        workerName: String,
        siteName: String,
        locationText: String
    ): String? {
        try {
            val selfieDir = File(context.filesDir, "carpenter_selfies")
            if (!selfieDir.exists()) {
                selfieDir.mkdirs()
            }

            // Create mutable copy to stamp watermark
            val mutableBitmap = originalBitmap.copy(Bitmap.Config.ARGB_8888, true)
            val canvas = Canvas(mutableBitmap)

            val timeStamp = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())

            val paintBg = Paint().apply {
                color = Color.parseColor("#CC1E293B") // dark semi-transparent banner
                style = Paint.Style.FILL
            }

            val paintText = Paint().apply {
                color = Color.WHITE
                textSize = (mutableBitmap.height * 0.045f).coerceIn(24f, 44f)
                isAntiAlias = true
                isFakeBoldText = true
            }

            val paintSub = Paint().apply {
                color = Color.parseColor("#FEF08A") // yellow accent
                textSize = (mutableBitmap.height * 0.038f).coerceIn(20f, 38f)
                isAntiAlias = true
            }

            val bannerHeight = (mutableBitmap.height * 0.22f).coerceAtLeast(110f)
            val topBanner = mutableBitmap.height - bannerHeight

            // Draw dark background strip at the bottom
            canvas.drawRect(0f, topBanner, mutableBitmap.width.toFloat(), mutableBitmap.height.toFloat(), paintBg)

            // Draw worker name and time
            val line1 = "🪚 $workerName | $timeStamp"
            val line2 = "📍 ${siteName.ifEmpty { "Work Site" }}"
            val line3 = if (locationText.isNotEmpty()) "📌 $locationText" else "✅ GPS Verified Attendance"

            val padding = 20f
            val startY = topBanner + (bannerHeight * 0.32f)
            val lineSpacing = bannerHeight * 0.28f

            canvas.drawText(line1, padding, startY, paintText)
            canvas.drawText(line2, padding, startY + lineSpacing, paintSub)
            canvas.drawText(line3, padding, startY + (lineSpacing * 2), paintSub)

            val fileName = "selfie_${System.currentTimeMillis()}.jpg"
            val file = File(selfieDir, fileName)

            FileOutputStream(file).use { out ->
                mutableBitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }

            return file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
