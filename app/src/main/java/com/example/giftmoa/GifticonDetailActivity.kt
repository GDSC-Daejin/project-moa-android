package com.example.giftmoa

import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.giftmoa.Adapter.UsageHistoryAdapter
import com.example.giftmoa.Data.AddCategoryResponse
import com.example.giftmoa.Data.GetGifticonDetailResponse
import com.example.giftmoa.Data.GetGifticonHistoryListResponse
import com.example.giftmoa.Data.GetGifticonListResponse
import com.example.giftmoa.Data.GifticonDetailData
import com.example.giftmoa.Data.GifticonDetailItem
import com.example.giftmoa.Data.GifticonHistoryData
import com.example.giftmoa.Data.GifticonHistoryResponse
import com.example.giftmoa.Data.UpdateGifticonHistoryRequest
import com.example.giftmoa.Data.UpdateGifticonRequest
import com.example.giftmoa.Data.UsageHistoryItem
import com.example.giftmoa.databinding.ActivityGifticonDetailBinding
import com.example.giftmoa.utils.AssetLoader
import com.example.giftmoa.utils.CustomCropTransformation
import com.example.giftmoa.utils.FormatUtil
import com.example.giftmoa.utils.ImageUtil
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GifticonDetailActivity: AppCompatActivity() {

    private lateinit var binding: ActivityGifticonDetailBinding
    private val TAG = "GifticonDetailActivity"
    private var gifticonDetail: GifticonDetailData? = null
    private lateinit var usageHistoryAdapter: UsageHistoryAdapter
    private var usageHistoryList = mutableListOf<GifticonHistoryData>()

    private var gifticonId: Long = 0

    private val WHITE: Int = 0xFFFFFFFF.toInt()
    private val BLACK: Int = 0xFF000000.toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGifticonDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gifticonId = intent.getLongExtra("gifticonId", 0)

        usageHistoryAdapter = UsageHistoryAdapter()

        getGiftionDetailFromServer()
        getUsageHistoryFromServer()

        Log.d(TAG, "gifticonDetail: $gifticonDetail")

        initRecyclerView()

        binding.tvEnterCouponUsedAmount.setOnClickListener {
            // 5,000 -> 5000
            val usedAmount = binding.etCouponUsedAmount.text.toString().replace(",", "").toIntOrNull() ?: 0
            var remainAmount = binding.etCouponRemainAmount.text.toString().replace(",", "").toIntOrNull() ?: 0

            if (usedAmount > remainAmount) {
                binding.etCouponUsedAmount.setText("")
                Snackbar.make(binding.root, "사용금액이 남은 금액보다 많습니다.", Snackbar.LENGTH_SHORT).show()
            } else {
                remainAmount -= usedAmount
                val formattedAmount = remainAmount.let { String.format("%,d", it) }

                binding.etCouponRemainAmount.setText(formattedAmount)
                binding.etCouponUsedAmount.setText("")
                /*usageHistoryList.add(UsageHistoryItem(4, "Jade", "https://ca.slack-edge.com/T02BE2ERU5A-U02CPD32N3D-g0beff217a80-512", FormatUtil().DateToString("2023-12-26T00:00:00.000Z"), usedAmount.toLong()))
                usageHistoryAdapter.submitList(usageHistoryList)
                usageHistoryAdapter.notifyDataSetChanged()*/

                sendUsageHistoryToServer(usedAmount)
            }
        }

        binding.btnUsedComplete.setOnClickListener {
            sendUseGifticonToServer(gifticonId)
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

    private fun getGiftionDetailFromServer() {
        Retrofit2Generator.create(this).getGifticonDetail(gifticonId = gifticonId).enqueue(object :
            Callback<GetGifticonDetailResponse> {
            override fun onResponse(call: Call<GetGifticonDetailResponse>, response: Response<GetGifticonDetailResponse>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Retrofit onResponse: ${response.body()}")
                    val responseBody = response.body()

                    if (responseBody != null) {
                        if (responseBody.data != null) {
                            gifticonDetail = responseBody.data

                            Log.d(TAG, "gifticonDetail: $gifticonDetail")

                            binding.tvToolbarCategoryName.text = gifticonDetail?.gifticon?.category?.categoryName
                            binding.tvCouponName.text = gifticonDetail?.gifticon?.name
                            // 자르고 싶은 위치와 크기 지정
                            val cropX = 20 // X 시작 위치
                            val cropY = 20 // Y 시작 위치
                            val cropWidth = 220 // 잘라낼 너비
                            val cropHeight = 205 // 잘라낼 높이

                            Glide.with(binding.ivCouponImage.context)
                                .asBitmap()
                                .load(gifticonDetail?.gifticon?.gifticonImagePath)
                                .apply(RequestOptions().transform(CustomCropTransformation(cropX, cropY, cropWidth, cropHeight)))
                                .into(binding.ivCouponImage)

                            binding.tvBarcodeNumber.text = gifticonDetail?.gifticon?.barcodeNumber
                            binding.tvCouponExchangePlace.text = gifticonDetail?.gifticon?.exchangePlace
                            binding.tvCouponDueDate.text = FormatUtil().DateToString(gifticonDetail?.gifticon?.dueDate.toString())
                            binding.tvCouponOrderNumber.text = gifticonDetail?.gifticon?.orderNumber

                            if (gifticonDetail?.gifticon?.gifticonType == "MONEY") {
                                binding.llCouponMoneyInfo.visibility = android.view.View.VISIBLE
                                val formattedAmount = gifticonDetail?.gifticon?.gifticonMoney?.toLongOrNull()?.let { String.format("%,d", it) }
                                binding.etCouponRemainAmount.setText(formattedAmount)
                            } else {
                                binding.llCouponMoneyInfo.visibility = android.view.View.GONE
                            }

                            val barcode = ImageUtil(this@GifticonDetailActivity).createBarcode(gifticonDetail?.gifticon?.barcodeNumber.toString().trim())
                            binding.ivBarcodeImage.setImageBitmap(barcode)

                            if (gifticonDetail?.teamList?.isEmpty() == false) {
                                binding.switchCouponAmount.isChecked = true
                                binding.cardViewShareRoom.visibility = android.view.View.VISIBLE
                                Glide.with(binding.ivShareRoomImage.context)
                                    .load(gifticonDetail?.teamList?.get(0)?.teamImage)
                                    .into(binding.ivShareRoomImage)
                            } else {
                                binding.switchCouponAmount.isChecked = false
                                binding.cardViewShareRoom.visibility = android.view.View.GONE
                            }

                            if (gifticonDetail?.gifticon?.status == "AVAILABLE") {
                                binding.btnUsedComplete.text = "사용 완료"
                            } else {
                                binding.btnUsedComplete.text = "사용 취소"
                            }
                        }
                    }


                } else {
                    Log.e(TAG, "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<GetGifticonDetailResponse>, t: Throwable) {
                Log.e(TAG, "Retrofit onFailure: ", t)
            }
        })
    }

    private fun getUsageHistoryFromServer() {
        Retrofit2Generator.create(this).getGifticonHistoryList(gifticonId = gifticonId).enqueue(object :
            Callback<GetGifticonHistoryListResponse> {
            override fun onResponse(call: Call<GetGifticonHistoryListResponse>, response: Response<GetGifticonHistoryListResponse>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Retrofit onResponse: ${response.body()}")
                    val responseBody = response.body()

                    if (responseBody != null) {
                        if (responseBody.data?.isNotEmpty() == true) {
                            for (usageHistory in responseBody.data) {
                                usageHistoryList.add(usageHistory)
                            }
                            binding.rvCouponUsageHistory.visibility = android.view.View.VISIBLE

                            /*// usageHistroyList 가장 마지막의 leftprice를 가져와서 etCouponRemainAmount에 넣어줌
                            val lastLeftPrice = usageHistoryList[usageHistoryList.size - 1].leftPrice
                            val formattedAmount = gifticonDetail?.gifticon?.gifticonMoney?.toLongOrNull()?.let { String.format("%,d", it) }
                            binding.etCouponRemainAmount.setText(formattedAmount)*/

                            usageHistoryAdapter.submitList(usageHistoryList)
                            //usageHistoryAdapter.notifyDataSetChanged()
                        } else {
                            binding.rvCouponUsageHistory.visibility = android.view.View.GONE
                        }
                    }
                } else {
                    Log.e(TAG, "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<GetGifticonHistoryListResponse>, t: Throwable) {
                Log.e(TAG, "Retrofit onFailure: ", t)
            }
        })
    }

    private fun sendUsageHistoryToServer(usedAmount: Int) {
        val requestBody = UpdateGifticonHistoryRequest(
            gifticonId = gifticonId,
            money = usedAmount.toLong()
        )

        Retrofit2Generator.create(this).updateGifticonHistory(requestBody).enqueue(object :
            Callback<GifticonHistoryResponse> {
            override fun onResponse(call: Call<GifticonHistoryResponse>, response: Response<GifticonHistoryResponse>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Retrofit onResponse: ${response.body()}")
                    val responseBody = response.body()

                    if (responseBody != null) {
                        val usageHistory = responseBody.data

                        if (usageHistory != null) {
                            usageHistoryList.add(usageHistory)
                        }
                        binding.rvCouponUsageHistory.visibility = android.view.View.VISIBLE

                        usageHistoryAdapter.submitList(usageHistoryList)
                        usageHistoryAdapter.notifyItemInserted(0)
                    }
                } else {
                    Log.e(TAG, "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<GifticonHistoryResponse>, t: Throwable) {
                Log.e(TAG, "Retrofit onFailure: ", t)
            }
        })
    }

    private fun sendUseGifticonToServer(gifticonId: Long) {
        Retrofit2Generator.create(this).useGifticon(gifticonId).enqueue(object :
            Callback<GetGifticonDetailResponse> {
            override fun onResponse(call: Call<GetGifticonDetailResponse>, response: Response<GetGifticonDetailResponse>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Retrofit onResponse: ${response.body()}")
                    val responseBody = response.body()

                    if (responseBody != null) {
                        val gifticon = responseBody.data

                        if (gifticon != null) {
                            gifticonDetail = gifticon
                        }

                        if (gifticonDetail?.gifticon?.status == "AVAILABLE") {
                            binding.btnUsedComplete.text = "사용 완료"
                        } else {
                            binding.btnUsedComplete.text = "사용 취소"
                        }
                    }
                } else {
                    Log.e(TAG, "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<GetGifticonDetailResponse>, t: Throwable) {
                Log.e(TAG, "Retrofit onFailure: ", t)
            }
        })
    }

    /*private fun getJsonData() {
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
    }*/
}