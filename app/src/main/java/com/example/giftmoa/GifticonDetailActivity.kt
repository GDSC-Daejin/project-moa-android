package com.example.giftmoa

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.giftmoa.Adapter.UsageHistoryAdapter
import com.example.giftmoa.Data.GifticonDetailItem
import com.example.giftmoa.Data.UsageHistoryItem
import com.example.giftmoa.databinding.ActivityGifticonDetailBinding
import com.example.giftmoa.util.FormatUtil
import com.example.giftmoa.util.ImageUtil
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix

class GifticonDetailActivity: AppCompatActivity() {

    private lateinit var binding: ActivityGifticonDetailBinding
    private val TAG = "GifticonDetailActivity"
    private var gifticonDetail: GifticonDetailItem? = null
    private lateinit var usageHistoryAdapter: UsageHistoryAdapter
    private var usageHistoryList = mutableListOf<UsageHistoryItem>()

    private val WHITE: Int = 0xFFFFFFFF.toInt()
    private val BLACK: Int = 0xFF000000.toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGifticonDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        usageHistoryAdapter = UsageHistoryAdapter()

        getJsonData()

        initRecyclerView()

        if (gifticonDetail != null) {
            binding.tvCouponName.text = gifticonDetail?.name
            Glide.with(binding.ivCouponImage.context)
                .load(gifticonDetail?.gifticonImagePath)
                .centerCrop()
                .into(binding.ivCouponImage)

            binding.tvBarcodeNumber.text = gifticonDetail?.barcodeNumber
            binding.tvCouponExchangePlace.text = gifticonDetail?.exchangePlace
            binding.tvCouponDueDate.text = FormatUtil().DateToString(gifticonDetail?.dueDate.toString())
            binding.etCouponRemainAmount.setText(gifticonDetail?.amount.toString())

            val barcode = ImageUtil(this).createBarcode(gifticonDetail?.barcodeNumber.toString().trim())
            binding.ivBarcodeImage.setImageBitmap(barcode)
        }

        binding.tvEnterCouponUsedAmount.setOnClickListener {
            val usedAmount = binding.etCouponUsedAmount.text.toString().toInt()
            var remainAmount = binding.etCouponRemainAmount.text.toString().toInt()

            if (usedAmount > remainAmount) {
                binding.etCouponUsedAmount.setText("")
                Snackbar.make(binding.root, "사용금액이 남은 금액보다 많습니다.", Snackbar.LENGTH_SHORT).show()
            } else {
                remainAmount -= usedAmount
                binding.etCouponRemainAmount.setText(remainAmount.toString())
                binding.etCouponUsedAmount.setText("")
                usageHistoryList.add(UsageHistoryItem(4, "Jade", "https://ca.slack-edge.com/T02BE2ERU5A-U02CPD32N3D-g0beff217a80-512", FormatUtil().DateToString("2023-12-26T00:00:00.000Z"), usedAmount.toLong()))
                usageHistoryAdapter.submitList(usageHistoryList)
                usageHistoryAdapter.notifyDataSetChanged()
            }
        }

        binding.tvToolbarConfirm.setOnClickListener {
            finish()
        }
    }

    private fun initRecyclerView() {
        val dp20 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20f, resources.displayMetrics).toInt()
        val dp10 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, resources.displayMetrics).toInt()
        binding.rvCouponUsageHistory.apply {
            adapter = usageHistoryAdapter
            layoutManager = LinearLayoutManager(this@GifticonDetailActivity, LinearLayoutManager.HORIZONTAL, false)
            addItemDecoration(LeftMarginItemDecoration(dp20))
            addItemDecoration(RightMarginItemDecoration(dp10))
        }
    }

    private fun getJsonData() {
        val assetLoader = AssetLoader()
        val gifticonDetailJsonString = assetLoader.getJsonString(this, "gifticonDetail.json")

        if (!gifticonDetailJsonString.isNullOrEmpty()) {
            val gifticonDetail = Gson().fromJson(gifticonDetailJsonString, GifticonDetailItem::class.java)
            Log.d(TAG, "onCreate: $gifticonDetail")

            this.gifticonDetail = gifticonDetail

            for (usageHistory in gifticonDetail.usageHistories!!) {
                usageHistoryList.add(usageHistory)
            }

            Log.d(TAG, "getJsonData: $usageHistoryList")
            usageHistoryAdapter.submitList(usageHistoryList)
        }
    }
}