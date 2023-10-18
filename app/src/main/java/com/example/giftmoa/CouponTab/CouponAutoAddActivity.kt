package com.example.giftmoa.CouponTab

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.giftmoa.R
import com.example.giftmoa.databinding.ActivityCouponAutoAddBinding

class CouponAutoAddActivity : AppCompatActivity() {
    private lateinit var cBinding : ActivityCouponAutoAddBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        cBinding = ActivityCouponAutoAddBinding.inflate(layoutInflater)

        setContentView(cBinding.root)
    }
}