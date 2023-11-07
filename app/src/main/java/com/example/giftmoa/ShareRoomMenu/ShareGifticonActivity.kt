package com.example.giftmoa.ShareRoomMenu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.giftmoa.R
import com.example.giftmoa.databinding.ActivityShareGifticonBinding

class ShareGifticonActivity : AppCompatActivity() {
    private lateinit var sBinding : ActivityShareGifticonBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sBinding = ActivityShareGifticonBinding.inflate(layoutInflater)
        setContentView(sBinding.root)
    }
}