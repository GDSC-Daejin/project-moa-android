package com.example.giftmoa.CouponTab

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.giftmoa.R
import com.example.giftmoa.databinding.ActivityCouponManualAddBinding

class CouponManualAddActivity : AppCompatActivity() {
    private lateinit var cBinding : ActivityCouponManualAddBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cBinding = ActivityCouponManualAddBinding.inflate(layoutInflater)
        setContentView(cBinding.root)
    }
}