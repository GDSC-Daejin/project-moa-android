package com.example.giftmoa

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.giftmoa.databinding.ActivityMyProfileBinding

class MyProfileActivity: AppCompatActivity() {

    private val TAG = "MyProfileActivity"
    private lateinit var binding: ActivityMyProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMyProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

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