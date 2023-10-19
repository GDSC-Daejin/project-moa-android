package com.example.giftmoa

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.giftmoa.databinding.ActivityManualRegistrationBinding

class ManualRegistrationActivity : AppCompatActivity() {

    private val TAG = "ManualRegistrationActivity"
    private lateinit var binding: ActivityManualRegistrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityManualRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)



    }
}