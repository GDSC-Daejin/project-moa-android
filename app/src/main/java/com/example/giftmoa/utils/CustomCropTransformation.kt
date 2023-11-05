package com.example.giftmoa.utils

import android.graphics.Bitmap
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.nio.charset.Charset
import java.security.MessageDigest

class CustomCropTransformation(private val startX: Int, private val startY: Int, private val width: Int, private val height: Int) : BitmapTransformation() {
    override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        if (startX + width <= toTransform.width && startY + height <= toTransform.height) {
            return Bitmap.createBitmap(toTransform, startX, startY, width, height)
        } else {
            throw IllegalArgumentException("The crop dimensions are outside of the source bitmap bounds.")
        }
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(("crop" + startX + startY + width + height).toByteArray(Charset.forName("UTF-8")))
    }
}