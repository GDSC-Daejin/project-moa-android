package com.example.giftmoa.utils

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.giftmoa.BottomMenu.CouponFragment

class FileGalleryPermissionUtil {

    private val TAG = "FileGalleryPermissionUtil"

    fun loadImage(imageLoadLauncher: ActivityResultLauncher<String>) {
        try {
            imageLoadLauncher.launch("image/*")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load image: ${e.message}")
        }
    }

    fun checkPermission(context: Context, imageLoadLauncher: ActivityResultLauncher<String>) {
        when {
            ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED -> loadImage(imageLoadLauncher)
            ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, android.Manifest.permission.READ_EXTERNAL_STORAGE) -> showPermissionInfoDialog(context)
            else -> requestReadExternalStoragePermission(context)
        }
    }

    fun showPermissionInfoDialog(context: Context) {
        AlertDialog.Builder(context).apply {
            setMessage("이미지를 가져오기 위해서, 외부 저장소 읽기 권한이 필요합니다.")
            setPositiveButton("동의") { _, _ -> requestReadExternalStoragePermission(context) }
            setNegativeButton("취소", null)
        }.show()
    }

    private fun requestReadExternalStoragePermission(context: Context) {
        ActivityCompat.requestPermissions(
            context as Activity, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
            CouponFragment.REQUEST_READ_EXTERNAL_STORAGE
        )
    }
}