package com.example.giftmoa.ShareRoomMenu

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.ViewModelProvider
import com.example.giftmoa.Adapter.SharedTabAdapter
import com.example.giftmoa.BottomSheetFragment.SortBottomSheet
import com.example.giftmoa.Data.GetTeamGifticonListResponse
import com.example.giftmoa.Retrofit2Generator
import com.example.giftmoa.databinding.ActivitySharedLockerBinding
import com.google.android.material.tabs.TabLayoutMediator
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SharedLockerActivity : AppCompatActivity() {
    private lateinit var sBinding : ActivitySharedLockerBinding
    private val tabTextList = listOf("전체", "사용가능", "사용완료")
    private var teamId = 0

    private val TAG = "SharedLockerActivity"

    private lateinit var teamGifticonViewModel: TeamGifticonViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        teamGifticonViewModel = ViewModelProvider(this, ViewModelProvider.NewInstanceFactory())[TeamGifticonViewModel::class.java]

        sBinding = ActivitySharedLockerBinding.inflate(layoutInflater)
        setContentView(sBinding.root)
        sBinding.viewpager.adapter = SharedTabAdapter(this)

        teamId = intent.getLongExtra("teamId", 0L).toInt()

        val sharedPref = getSharedPreferences("readTeamId", Context.MODE_PRIVATE)
        with (sharedPref.edit()) {
            putInt("teamId", teamId)
            apply()
        }

        TabLayoutMediator(sBinding.categoryTabLayout, sBinding.viewpager) { tab, pos ->
            tab.text = tabTextList[pos]
        }.attach()

        setSharedGiftData()

        sBinding.backArrow.setOnClickListener {
            this.finish()
        }

        sBinding.tvSort.setOnClickListener {
            val bottomSheet = SortBottomSheet()
            bottomSheet.show(this.supportFragmentManager, bottomSheet.tag)
            bottomSheet.apply {
                setCallback(object : SortBottomSheet.OnSendFromBottomSheetDialog{
                    override fun sendValue(value: String) {
                        Log.d("test", "BottomSheetDialog -> 액티비티로 전달된 값 : $value")
                        sBinding.tvSort.text = value
                        teamGifticonViewModel.sortCouponList(value)
                    }
                })
            }
        }
    }

    private fun setSharedGiftData() {
        Retrofit2Generator.create(this)
            .getTeamGifticonList(teamId.toLong(), 0, 20)
            .enqueue(object : Callback<GetTeamGifticonListResponse> {
                override fun onResponse(
                    call: Call<GetTeamGifticonListResponse>,
                    response: Response<GetTeamGifticonListResponse>
                ) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        responseBody?.data?.dataList?.let { newList ->
                            // newList를 gifticonViewModel의 addCoupon함수를 이용해서 넣어준다.
                            // id가 높은 것 부터 넣어줘야 한다.
                            newList.forEach { gifticon ->
                                teamGifticonViewModel.addCoupon(gifticon)
                            }
                            teamGifticonViewModel.sortCouponList(sBinding.tvSort.text.toString())

                            Log.d(TAG, "newList: ${newList}")

                            Log.d(TAG, "allCouponList: ${teamGifticonViewModel.allCouponList.value}")
                            Log.d(TAG, "availableCouponList: ${teamGifticonViewModel.availableCouponList.value}")
                            Log.d(TAG, "usedCouponList: ${teamGifticonViewModel.usedCouponList.value}")

                        }
                    } else {
                        Log.e(TAG, "Error: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<GetTeamGifticonListResponse>, t: Throwable) {
                    Log.e(TAG, "Error: ${t.message}")
                }

            })
    }
}