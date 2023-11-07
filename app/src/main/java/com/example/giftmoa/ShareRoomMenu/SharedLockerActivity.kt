package com.example.giftmoa.ShareRoomMenu

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.giftmoa.R
import com.example.giftmoa.databinding.ActivitySharedLockerBinding

class SharedLockerActivity : AppCompatActivity() {
    private lateinit var sBinding : ActivitySharedLockerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sBinding = ActivitySharedLockerBinding.inflate(layoutInflater)
        setContentView(sBinding.root)
    }
}