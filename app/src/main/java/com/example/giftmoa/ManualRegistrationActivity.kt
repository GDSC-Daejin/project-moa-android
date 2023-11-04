package com.example.giftmoa

import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.giftmoa.Data.GifticonDetailItem
import com.example.giftmoa.Data.ParsedGifticon
import com.example.giftmoa.databinding.ActivityManualRegistrationBinding

class ManualRegistrationActivity : AppCompatActivity() {

    private val TAG = "ManualRegistrationActivity"
    private lateinit var binding: ActivityManualRegistrationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityManualRegistrationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // intent로 받아온 데이터
        val gifticon = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra("gifticon", GifticonDetailItem::class.java)
        } else {
            intent.getParcelableExtra<GifticonDetailItem>("gifticon")
        }

        // gifticon이 null이 아니면
        if (gifticon != null) {
            // gifticon의 데이터를 뷰에 적용
            binding.etCouponName.setText(gifticon.name)
            binding.etBarcodeNumber.setText(gifticon.barcodeNumber.toString())
            binding.etExchangePlace.setText(gifticon.exchangePlace)
            binding.etDueDate.setText("${gifticon.dueDate}    ${gifticon.amount}원")
        }
    }
}