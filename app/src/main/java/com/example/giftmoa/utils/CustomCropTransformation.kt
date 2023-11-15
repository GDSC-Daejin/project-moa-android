package com.example.giftmoa.utils

import android.graphics.Bitmap
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool
import com.bumptech.glide.load.resource.bitmap.BitmapTransformation
import java.nio.charset.Charset
import java.security.MessageDigest

class CustomCropTransformation(
    private val startX: Int,
    private val startY: Int,
    private val width: Int,
    private val height: Int
) : BitmapTransformation() {

    public override fun transform(pool: BitmapPool, toTransform: Bitmap, outWidth: Int, outHeight: Int): Bitmap {
        // 비트맵의 경계 내에서 자르기 차원이 있는지 확인합니다.
        val actualWidth = toTransform.width
        val actualHeight = toTransform.height

        // 자르기 시작 위치와 자를 크기를 검사하고 조정합니다.
        val safeStartX = if (startX < actualWidth) startX else 0
        val safeStartY = if (startY < actualHeight) startY else 0
        val safeWidth = if (safeStartX + width > actualWidth) actualWidth - safeStartX else width
        val safeHeight = if (safeStartY + height > actualHeight) actualHeight - safeStartY else height

        // 비트맵에서 안전한 차원으로 자릅니다.
        return Bitmap.createBitmap(toTransform, safeStartX, safeStartY, safeWidth, safeHeight)
    }

    override fun updateDiskCacheKey(messageDigest: MessageDigest) {
        messageDigest.update(("crop" + startX + startY + width + height).toByteArray(Charset.forName("UTF-8")))
    }
}
