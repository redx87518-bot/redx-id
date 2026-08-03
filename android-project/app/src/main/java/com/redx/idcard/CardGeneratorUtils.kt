package com.redx.idcard

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import androidx.core.content.ContextCompat

object CardGeneratorUtils {

    /**
     * Pakistan CNIC – front side overlay positions.
     * Template image is the original NADRA card photo supplied as an asset.
     * Coordinates are expressed as fractions of [0..1] relative to the card
     * sub-image inside the asset (the top card in the scan).
     *
     * After loading the asset we crop to just the front card, scale it to a
     * canonical width (1200 px), then place overlays using pixel offsets derived
     * from visual inspection of the template.
     */
    fun generatePakistanCard(
        context: Context,
        name: String,
        dob: String,
        photo: Bitmap
    ): Bitmap {
        // Load the full template asset
        val assetBmp = loadAssetBitmap(context, "pakistan_template.jpg")

        // The front card occupies roughly the top half of the scan image.
        // Crop it out: y from 10% to 52% of total height
        val totalH = assetBmp.height
        val totalW = assetBmp.width
        val cropTop = (totalH * 0.12).toInt()
        val cropBottom = (totalH * 0.53).toInt()
        val cardBmp = Bitmap.createBitmap(assetBmp, 0, cropTop, totalW, cropBottom - cropTop)

        // Scale to canonical 1200×756 (roughly 1.586:1 ID card ratio)
        val canW = 1200
        val canH = 756
        val canvas_bmp = Bitmap.createScaledBitmap(cardBmp, canW, canH, true).copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(canvas_bmp)

        // ── Photo overlay ──────────────────────────────────────────────────
        // Photo box on Pakistan CNIC front: right side, roughly
        //   left=800, top=130, right=1010, bottom=450
        val photoLeft = 805
        val photoTop = 125
        val photoRight = 1010
        val photoBottom = 450
        val photoW = photoRight - photoLeft
        val photoH = photoBottom - photoTop

        val scaledPhoto = cropToRect(photo, photoW, photoH)
        canvas.drawBitmap(scaledPhoto, photoLeft.toFloat(), photoTop.toFloat(), null)

        // ── Name text overlay ──────────────────────────────────────────────
        // Whiteout the old name first
        val whitePaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        // Name row: approx x=220..590, y=225..270
        canvas.drawRect(220f, 220f, 590f, 275f, whitePaint)

        val namePaint = Paint().apply {
            color = Color.BLACK
            textSize = 38f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(name, 222f, 262f, namePaint)

        // ── Date of Birth overlay ──────────────────────────────────────────
        // DOB cell: approx x=720..870, y=410..455
        canvas.drawRect(718f, 408f, 870f, 458f, whitePaint)

        val dobPaint = Paint().apply {
            color = Color.BLACK
            textSize = 32f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }
        canvas.drawText(dob, 720f, 448f, dobPaint)

        assetBmp.recycle()
        cardBmp.recycle()
        return canvas_bmp
    }

    /**
     * Nigeria NIN Digital Slip overlay.
     * The template image already shows a single card.
     */
    fun generateNigeriaCard(
        context: Context,
        surname: String,
        givenNames: String,
        dob: String,
        photo: Bitmap
    ): Bitmap {
        val assetBmp = loadAssetBitmap(context, "nigeria_template.jpg")

        // Scale to canonical 1200×756
        val canW = 1200
        val canH = 756
        val canvas_bmp = Bitmap.createScaledBitmap(assetBmp, canW, canH, true).copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(canvas_bmp)

        val whitePaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }

        // ── Photo overlay ──────────────────────────────────────────────────
        // NIN photo box: left side, approx left=60, top=175, right=225, bottom=400
        val photoLeft = 62
        val photoTop = 178
        val photoRight = 228
        val photoBottom = 402
        val photoW = photoRight - photoLeft
        val photoH = photoBottom - photoTop

        val scaledPhoto = cropToRect(photo, photoW, photoH)
        canvas.drawBitmap(scaledPhoto, photoLeft.toFloat(), photoTop.toFloat(), null)

        // ── Surname overlay ────────────────────────────────────────────────
        // Surname row: approx x=240..600, y=220..260
        canvas.drawRect(238f, 218f, 620f, 265f, whitePaint)
        val surnamePaint = Paint().apply {
            color = Color.BLACK
            textSize = 36f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }
        canvas.drawText(surname.uppercase(), 240f, 258f, surnamePaint)

        // ── Given names overlay ────────────────────────────────────────────
        canvas.drawRect(238f, 275f, 620f, 318f, whitePaint)
        val givenPaint = Paint().apply {
            color = Color.BLACK
            textSize = 34f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }
        canvas.drawText(givenNames.uppercase(), 240f, 312f, givenPaint)

        // ── Date of Birth overlay ──────────────────────────────────────────
        // DOB row: approx x=238..480, y=330..372
        canvas.drawRect(238f, 330f, 500f, 375f, whitePaint)
        val dobPaint = Paint().apply {
            color = Color.BLACK
            textSize = 32f
            typeface = Typeface.DEFAULT
            isAntiAlias = true
        }
        canvas.drawText(dob, 240f, 368f, dobPaint)

        assetBmp.recycle()
        return canvas_bmp
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private fun loadAssetBitmap(context: Context, filename: String): Bitmap {
        return context.assets.open(filename).use { BitmapFactory.decodeStream(it) }
    }

    /**
     * Scale + centre-crop [src] to fill [targetW] × [targetH].
     */
    private fun cropToRect(src: Bitmap, targetW: Int, targetH: Int): Bitmap {
        val srcAspect = src.width.toFloat() / src.height
        val dstAspect = targetW.toFloat() / targetH

        val scaledW: Int
        val scaledH: Int
        if (srcAspect > dstAspect) {
            scaledH = targetH
            scaledW = (targetH * srcAspect).toInt()
        } else {
            scaledW = targetW
            scaledH = (targetW / srcAspect).toInt()
        }

        val scaled = Bitmap.createScaledBitmap(src, scaledW, scaledH, true)
        val xOffset = (scaledW - targetW) / 2
        val yOffset = (scaledH - targetH) / 2
        val cropped = Bitmap.createBitmap(scaled, xOffset, yOffset, targetW, targetH)
        if (scaled != src) scaled.recycle()
        return cropped
    }
}
