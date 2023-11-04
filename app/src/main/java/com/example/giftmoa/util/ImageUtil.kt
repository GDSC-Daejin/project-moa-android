package com.example.giftmoa.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color.BLACK
import android.graphics.Color.WHITE
import android.util.TypedValue
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

class ImageUtil(private val context: Context) {

    fun createBarcode(code: String): Bitmap {
        val widthPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 390f,
            context.resources.displayMetrics
        )
        val heightPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 111f,
            context.resources.displayMetrics
        )
        val format: BarcodeFormat = BarcodeFormat.CODE_128
        val matrix: BitMatrix =
            MultiFormatWriter().encode(code, format, widthPx.toInt(), heightPx.toInt())
        return createBitmap(matrix)
    }

     fun createBitmap(matrix: BitMatrix): Bitmap {
        val width = matrix.width
        val height = matrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (matrix.get(x, y)) BLACK else WHITE)
            }
        }
        return bitmap
    }
}