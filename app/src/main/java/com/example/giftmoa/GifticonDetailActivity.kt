package com.example.giftmoa

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.giftmoa.databinding.ActivityGifticonDetailBinding

class GifticonDetailActivity: AppCompatActivity() {

    private lateinit var binding: ActivityGifticonDetailBinding
    private val TAG = "GifticonDetailActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGifticonDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)


    }
}