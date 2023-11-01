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

        if (gifticonDetail != null) {
            binding.tvCouponName.text = gifticonDetail?.name
            Glide.with(binding.ivCouponImage.context)
                .load(gifticonDetail?.gifticonImagePath)
                .centerCrop()
                .into(binding.ivCouponImage)

            binding.tvBarcodeNumber.text = gifticonDetail?.barcodeNumber
            binding.tvCouponExchangePlace.text = gifticonDetail?.exchangePlace
            binding.tvCouponDueDate.text = gifticonDetail?.dueDate
            binding.etCouponRemainAmount.setText(gifticonDetail?.amount.toString())

            val barcode = createBarcode(gifticonDetail?.barcodeNumber.toString())
            binding.ivBarcodeImage.setImageBitmap(barcode)
        }

        initRecyclerView()
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

    fun createBarcode(code: String): Bitmap {
        val widthPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 390f,
            resources.displayMetrics
        )
        val heightPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, 111f,
            resources.displayMetrics
        )
        val format: BarcodeFormat = BarcodeFormat.CODE_128
        val matrix: BitMatrix =
            MultiFormatWriter().encode(code, format, widthPx.toInt(), heightPx.toInt())
        return createBitmap(matrix)
    }

    private fun createBitmap(matrix: BitMatrix): Bitmap {
        val width = matrix.width
        val height = matrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(x, y, if (matrix.get(x, y)) BLACK else WHITE)
            }
        }
        return bitmap
    }
}