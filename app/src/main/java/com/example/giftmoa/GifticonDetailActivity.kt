package com.example.giftmoa

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.giftmoa.Adapter.UsageHistoryAdapter
import com.example.giftmoa.BottomSheetFragment.CategoryBottomSheet
import com.example.giftmoa.BottomSheetFragment.ShareBottomSheet
import com.example.giftmoa.Data.CategoryItem
import com.example.giftmoa.Data.GetGifticonDetailResponse
import com.example.giftmoa.Data.GetGifticonHistoryListResponse
import com.example.giftmoa.Data.Gifticon
import com.example.giftmoa.Data.GifticonDetailData
import com.example.giftmoa.Data.GifticonHistoryData
import com.example.giftmoa.Data.GifticonHistoryResponse
import com.example.giftmoa.Data.Team
import com.example.giftmoa.Data.UpdateGifticonHistoryRequest
import com.example.giftmoa.HomeTab.GifticonViewModel
import com.example.giftmoa.ShareRoomMenu.ShareRoomReadActivity
import com.example.giftmoa.databinding.ActivityGifticonDetailBinding
import com.example.giftmoa.utils.CustomCropTransformation
import com.example.giftmoa.utils.FormatUtil
import com.example.giftmoa.utils.ImageUtil
import com.example.giftmoa.utils.LeftMarginItemDecoration
import com.example.giftmoa.utils.RightMarginItemDecoration
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


interface ShareBottomSheetListener {
    fun onTeamUpdated(team: Team)
}

class GifticonDetailActivity: AppCompatActivity(), ShareBottomSheetListener {

    private lateinit var binding: ActivityGifticonDetailBinding
    private val TAG = "GifticonDetailActivity"
    private var gifticonDetail: GifticonDetailData? = null
    private lateinit var usageHistoryAdapter: UsageHistoryAdapter
    private var usageHistoryList = mutableListOf<GifticonHistoryData>()

    private var gifticonId: Long = 0

    private lateinit var gifticonViewModel: GifticonViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gifticonViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())[GifticonViewModel::class.java]

        binding = ActivityGifticonDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        gifticonId = intent.getLongExtra("gifticonId", 0)

        usageHistoryAdapter = UsageHistoryAdapter()

        getGiftionDetailFromServer()

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

                sendUsageHistoryToServer(usedAmount)
            }
        }

        binding.ivShareRoomImage.setOnClickListener {
            val intent = Intent(this, ShareRoomReadActivity::class.java).apply {
                putExtra("type", "READ")
                putExtra("data", gifticonDetail?.teamList?.get(0))
            }
            startActivity(intent)
        }

        binding.tvToolbarConfirm.setOnClickListener {
            finish()
        }

        binding.btnShare.setOnClickListener {
            showShareBottomSheet()
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
        binding.viewLoading.visibility = android.view.View.VISIBLE
        binding.ivProgressBar.visibility = android.view.View.VISIBLE

        Glide.with(this)
            .asGif()
            .load(R.drawable.icon_progress_bar)
            .into(binding.ivProgressBar)

        Retrofit2Generator.create(this).getGifticonDetail(gifticonId = gifticonId).enqueue(object :
            Callback<GetGifticonDetailResponse> {
            override fun onResponse(call: Call<GetGifticonDetailResponse>, response: Response<GetGifticonDetailResponse>) {
                if (response.isSuccessful) {
                    Log.d(TAG, "Retrofit onResponse: ${response.body()}")
                    val responseBody = response.body()

                    if (responseBody != null) {
                        if (responseBody.data != null) {
                            gifticonDetail = responseBody.data

                            binding.viewLoading.visibility = android.view.View.GONE
                            binding.ivProgressBar.visibility = android.view.View.GONE

                            Log.d(TAG, "gifticonDetail: $gifticonDetail")

                            binding.tvToolbarCategoryName.text = gifticonDetail?.gifticon?.category?.categoryName
                            binding.tvCouponName.text = gifticonDetail?.gifticon?.name

                            if (gifticonDetail?.gifticon?.gifticonImagePath != null) {
                                Glide.with(binding.ivCouponImage.context)
                                    .load(gifticonDetail?.gifticon?.gifticonImagePath)
                                    .into(binding.ivCouponImage)
                            } else {
                                binding.ivCouponImage.setPadding(50, 50, 50, 50)
                                binding.ivCouponImage.setBackgroundColor(binding.ivCouponImage.context.getColor(R.color.moa_gray_200))
                                binding.ivCouponImage.setImageResource(R.drawable.icon_logo)
                            }

                            binding.tvBarcodeNumber.text = gifticonDetail?.gifticon?.barcodeNumber
                            binding.tvCouponExchangePlace.text = gifticonDetail?.gifticon?.exchangePlace
                            binding.tvCouponDueDate.text = FormatUtil().DateToString(gifticonDetail?.gifticon?.dueDate.toString())
                            binding.tvCouponOrderNumber.text = gifticonDetail?.gifticon?.orderNumber

                            if (gifticonDetail?.gifticon?.gifticonType == "MONEY") {
                                binding.llCouponMoneyInfo.visibility = android.view.View.VISIBLE

                                getUsageHistoryFromServer()
                                /*val formattedAmount = gifticonDetail?.gifticon?.gifticonMoney?.toLongOrNull()?.let { String.format("%,d", it) }
                                binding.etCouponRemainAmount.setText(formattedAmount)*/
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
                                binding.tvShareRoomName.text = gifticonDetail?.teamList?.get(0)?.teamName
                                Glide.with(binding.ivShareRoomUserImage01.context)
                                    .load(gifticonDetail?.teamList?.get(0)?.teamMembers?.get(0)?.profileImageUrl)
                                    .into(binding.ivShareRoomUserImage01)
                                if (gifticonDetail?.teamList?.get(0)?.teamMembers?.size!! > 1) {
                                    binding.ivShareRoomUserImage02.visibility = ViewGroup.VISIBLE
                                    if (gifticonDetail?.teamList?.get(0)?.teamMembers?.size!! > 2) {
                                        binding.tvShareRoomCount.visibility = ViewGroup.VISIBLE
                                        "+${gifticonDetail?.teamList?.get(0)?.teamMembers?.size!! - 2}".also { binding.tvShareRoomCount.text = it }
                                    }
                                    Glide.with(binding.ivShareRoomUserImage02.context)
                                        .load(gifticonDetail?.teamList?.get(0)?.teamMembers?.get(0)?.profileImageUrl)
                                        .into(binding.ivShareRoomUserImage02)
                                }
                                binding.btnShare.apply {
                                    text = "공유됨"
                                    isClickable = false
                                }
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

                    Log.d(TAG, "responseBody: $responseBody")

                    if (responseBody != null) {
                        if (responseBody.data?.isNotEmpty() == true) {
                            for (usageHistory in responseBody.data) {
                                usageHistoryList.add(usageHistory)
                            }

                            binding.tvNoUsageHistory.visibility = android.view.View.INVISIBLE
                            binding.rvCouponUsageHistory.visibility = android.view.View.VISIBLE

                            // usageHistroyList 가장 마지막의 leftprice를 가져와서 etCouponRemainAmount에 넣어줌
                            val lastLeftPrice = usageHistoryList[0].leftPrice
                            val formattedAmount = lastLeftPrice?.let { String.format("%,d", it) }
                            binding.etCouponRemainAmount.setText(formattedAmount)

                            usageHistoryAdapter.submitList(usageHistoryList)
                            //usageHistoryAdapter.notifyDataSetChanged()
                        } else {
                            val formattedAmount = gifticonDetail?.gifticon?.gifticonMoney?.toLongOrNull()?.let { String.format("%,d", it) }
                            binding.etCouponRemainAmount.setText(formattedAmount)

                            binding.tvNoUsageHistory.visibility = android.view.View.VISIBLE
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
                            binding.tvNoUsageHistory.visibility = android.view.View.INVISIBLE
                            binding.rvCouponUsageHistory.visibility = android.view.View.VISIBLE

                            usageHistoryAdapter.submitList(usageHistoryList.toList().sortedByDescending { it.usedDate })
                            // 스크롤 위치를 맨 앞으로 이동
                            binding.rvCouponUsageHistory.scrollToPosition(0)
                        } else {
                            binding.rvCouponUsageHistory.visibility = android.view.View.GONE
                            binding.tvNoUsageHistory.visibility = android.view.View.VISIBLE
                        }

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

                        val coupon = Gifticon(
                            id = gifticonId,
                            name = gifticonDetail?.gifticon?.name,
                            gifticonImagePath = gifticonDetail?.gifticon?.gifticonImagePath,
                            exchangePlace = gifticonDetail?.gifticon?.exchangePlace,
                            dueDate = gifticonDetail?.gifticon?.dueDate,
                            gifticonType = gifticonDetail?.gifticon?.gifticonType,
                            status = gifticonDetail?.gifticon?.status,
                            usedDate = gifticonDetail?.gifticon?.usedDate,
                            author = gifticonDetail?.gifticon?.author,
                            category = gifticonDetail?.gifticon?.category
                        )

                        val data = Intent().apply {
                            putExtra("updatedGifticonWithStatus", coupon)
                            putExtra("flag", 3)
                        }
                        setResult(RESULT_OK, data)

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

    private fun showShareBottomSheet() {
        val shareBottomSheet = ShareBottomSheet(gifticonId, this)
        shareBottomSheet.setStyle(DialogFragment.STYLE_NORMAL, R.style.RoundCornerBottomSheetDialogTheme)
        shareBottomSheet.show(this.supportFragmentManager, shareBottomSheet.tag)
    }

    override fun onTeamUpdated(team: Team) {
        Log.d(TAG, "onTeamUpdated: $team")
        binding.switchCouponAmount.isChecked = true
        binding.cardViewShareRoom.visibility = android.view.View.VISIBLE
        Glide.with(binding.ivShareRoomImage.context)
            .load(team.teamImage)
            .into(binding.ivShareRoomImage)
        binding.tvShareRoomName.text = team.teamName
        Glide.with(binding.ivShareRoomUserImage01.context)
            .load(team.teamMembers?.get(0)?.profileImageUrl)
            .into(binding.ivShareRoomUserImage01)
        if (team.teamMembers?.size!! > 1) {
            binding.ivShareRoomUserImage02.visibility = ViewGroup.VISIBLE
            if (team.teamMembers?.size!! > 2) {
                binding.tvShareRoomCount.visibility = ViewGroup.VISIBLE
                "+${team.teamMembers?.size!! - 2}".also { binding.tvShareRoomCount.text = it }
            }
            Glide.with(binding.ivShareRoomUserImage02.context)
                .load(team.teamMembers?.get(0)?.profileImageUrl)
                .into(binding.ivShareRoomUserImage02)
        }
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