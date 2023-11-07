package com.example.giftmoa

import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.giftmoa.databinding.ActivityMyProfileBinding
import com.example.giftmoa.utils.FileGalleryPermissionUtil

class MyProfileActivity: AppCompatActivity() {

    private val TAG = "MyProfileActivity"
    private lateinit var binding: ActivityMyProfileBinding

    private val imageLoadLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            Glide.with(this)
                .load(it)
                .circleCrop()
                .into(binding.civUserProfileImage)
            Toast.makeText(this, "이미지 로드 성공", Toast.LENGTH_SHORT).show()
        } ?: kotlin.run {
            Toast.makeText(this, "이미지 로드 실패", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivEditProfileImage.setOnClickListener {
            FileGalleryPermissionUtil().checkPermission(this, imageLoadLauncher)
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnEdit.setOnClickListener {
            finish()
        }

        binding.ivBack.setOnClickListener {
            finish()
        }
    }
}