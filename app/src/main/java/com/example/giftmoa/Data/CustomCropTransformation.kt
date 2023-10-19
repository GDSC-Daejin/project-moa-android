package com.example.giftmoa.Data

import android.graphics.Bitmap
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.security.MessageDigest

class CustomCropTransformation(private val xPercentage: Float, private val yPercentage: Float) : BitmapTransformation() {
    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        val x = (toTransform.width * xPercentage - outWidth * 0.5).coerceIn(0.0,
            (toTransform.width - outWidth).toFloat().toDouble()
        ).toInt()
        val y = (toTransform.height * yPercentage - outHeight * 0.5).coerceIn(
            0.0,
            (toTransform.height - outHeight).toFloat().toDouble()
        ).toInt()

        return Bitmap.createBitmap(toTransform, x, y, outWidth, outHeight)
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        // 필요한 경우 여기에 구현
    }
}