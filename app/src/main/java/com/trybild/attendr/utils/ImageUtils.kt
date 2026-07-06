package com.trybild.attendr.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.IOException

object ImageUtils {
    private const val MAX_DIMENSION = 1024
    private const val JPEG_QUALITY = 80

    /** Thrown by [compressImageFromUri] with a message that's safe to show directly to the user. */
    class ImageReadException(message: String) : IOException(message)

    /**
     * Downscales the image at [uri] to at most 1024px on its longest side and re-encodes it as JPEG.
     * Reads the URI into memory once — some picker providers (e.g. Google Photos-backed picker
     * results) only allow a single openInputStream() read per selection, so decoding bounds and
     * pixels from two separate stream opens (the old approach) intermittently failed with a null
     * stream on the second call, surfacing as a generic "Could not read selected image".
     */
    fun compressImageFromUri(context: Context, uri: Uri): ByteArray {
        val raw = try {
            context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        } catch (e: IOException) {
            null
        } ?: throw ImageReadException("Couldn't open that image — try a different one")

        if (raw.isEmpty())
            throw ImageReadException("Couldn't open that image — try a different one")

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(raw, 0, raw.size, bounds)

        var sampleSize = 1
        val largestSide = maxOf(bounds.outWidth, bounds.outHeight)
        while (largestSide / sampleSize > MAX_DIMENSION * 2) {
            sampleSize *= 2
        }

        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val bitmap = BitmapFactory.decodeByteArray(raw, 0, raw.size, options)
            // Fallback: some formats fail to decode with inSampleSize set but decode fine at full resolution
            ?: BitmapFactory.decodeByteArray(raw, 0, raw.size)
            ?: throw ImageReadException("That file doesn't look like a valid image")

        val scaled = scaleDownIfNeeded(bitmap)

        return ByteArrayOutputStream().use { stream ->
            scaled.compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, stream)
            stream.toByteArray()
        }
    }

    private fun scaleDownIfNeeded(bitmap: Bitmap): Bitmap {
        val largestSide = maxOf(bitmap.width, bitmap.height)
        if (largestSide <= MAX_DIMENSION) return bitmap
        val scale = MAX_DIMENSION.toFloat() / largestSide
        val newWidth = (bitmap.width * scale).toInt()
        val newHeight = (bitmap.height * scale).toInt()
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
}
