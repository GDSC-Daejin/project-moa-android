package com.example.giftmoa.ShareRoomMenu

import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.giftmoa.*
import com.example.giftmoa.Adapter.GifticonListAdapter
import com.example.giftmoa.Adapter.ShareRoomGifticonAdapter
import com.example.giftmoa.Data.*
import com.example.giftmoa.GridSpacingItemDecoration
import com.example.giftmoa.databinding.ActivityShareGifticonBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class ShareGifticonActivity : AppCompatActivity() {
    private lateinit var sBinding : ActivityShareGifticonBinding

    var gifticonList = ArrayList<ShareRoomGifticon>()

    private var giftAdapter: ShareRoomGifticonAdapter? = null

    private var categoryList = mutableListOf<CategoryItem>()

    private var gridManager = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)

    private var selectGifticonList = ArrayList<ShareRoomGifticon>()

    private var teamId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sBinding = ActivityShareGifticonBinding.inflate(layoutInflater)
        setContentView(sBinding.root)
        initSharedRecyclerView()
        getCategoryListFromServer()

        teamId = intent.getIntExtra("teamId", 0).toInt()

        sBinding.shareGifticonTv.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val saveSharedPreference = SaveSharedPreference()
                val token = saveSharedPreference.getToken(this@ShareGifticonActivity).toString()
                val getExpireDate = saveSharedPreference.getExpireDate(this@ShareGifticonActivity).toString()

                val SERVER_URL = BuildConfig.server_URL
                val retrofit = Retrofit.Builder().baseUrl(SERVER_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                //.client(clientBuilder)

                //Authorization jwt토큰 로그인
                val interceptor = Interceptor { chain ->

                    var newRequest: Request
                    if (token != null && token != "") { // 토큰이 없는 경우
                        // Authorization 헤더에 토큰 추가
                        newRequest =
                            chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                        val expireDate: Long = getExpireDate.toLong()
                        if (expireDate <= System.currentTimeMillis()) { // 토큰 만료 여부 체크
                            //refresh 들어갈 곳
                            newRequest =
                                chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
                            return@Interceptor chain.proceed(newRequest)
                        }
                    } else newRequest = chain.request()
                    chain.proceed(newRequest)
                }
                val builder = OkHttpClient.Builder()
                builder.interceptors().add(interceptor)
                val client: OkHttpClient = builder.build()
                retrofit.client(client)
                val retrofit2: Retrofit = retrofit.build()
                val api = retrofit2.create(MoaInterface::class.java)

                for (i in selectGifticonList.indices) {
                    val temp = TeamShareGiftIcon(teamId,selectGifticonList[i].gifticonId)
                    api.teamShareGificon(temp).enqueue(object : Callback<ShareRoomGifticonResponseData> {
                        override fun onResponse(
                            call: Call<ShareRoomGifticonResponseData>,
                            response: Response<ShareRoomGifticonResponseData>
                        ) {
                            if (response.isSuccessful) {
                                println("ssisisisi")
                            } else {
                                println("faafa")
                                Log.d("test", response.errorBody()?.string()!!)
                                Log.d("message", call.request().toString())
                                println(response.code())
                            }
                        }

                        override fun onFailure(
                            call: Call<ShareRoomGifticonResponseData>,
                            t: Throwable
                        ) {
                            Log.e("ERROR", t.message.toString())
                        }

                    })
                }
            }
        }




    }

    private fun initSharedRecyclerView() {
        getAllGifticonListFromServer(0)
        sBinding.shareGifticonRv.apply {
            giftAdapter = ShareRoomGifticonAdapter()
            adapter = giftAdapter
            giftAdapter!!.shareRoomGifticonItemData = gifticonList
            layoutManager = gridManager
            sBinding.shareGifticonRv.addItemDecoration(
                GridSpacingItemDecoration(spanCount = 2, spacing = 10f.fromDpToPx())
            )
        }
    }

    private fun Float.fromDpToPx(): Int =
        (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun getAllGifticonListFromServer(page: Int) {
        Retrofit2Generator.create(this@ShareGifticonActivity).getAllGifticonList(size = 30, page = page).enqueue(object :
            Callback<GetGifticonListResponse> {
            override fun onResponse(call: Call<GetGifticonListResponse>, response: Response<GetGifticonListResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    responseBody?.data?.dataList?.let { newList ->
                        if (page == 0) {
                            // 첫 페이지인 경우 리스트를 새로 채웁니다.
                            gifticonList.clear()
                        }
                        // 새로운 데이터를 리스트에 추가합니다.
                        val currentPosition = gifticonList.size
                        gifticonList.addAll(listOf(newList as ShareRoomGifticon))

                        giftAdapter!!.notifyDataSetChanged()
                    }
                } else {
                    Log.e("ERROR", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<GetGifticonListResponse>, t: Throwable) {
                Log.e("ERROR", "Retrofit onFailure: ", t)
            }
        })
    }

    private fun getCategoryListFromServer() {
        Retrofit2Generator.create(this@ShareGifticonActivity).getCategoryList().enqueue(object : Callback<GetCategoryListResponse> {
            override fun onResponse(call: Call<GetCategoryListResponse>, response: Response<GetCategoryListResponse>) {
                if (response.isSuccessful) {
                    Log.d("Success", "Retrofit onResponse: ${response.body()}")
                    val responseBody = response.body()
                    if (responseBody != null) {
                        val resposeBody = responseBody.data

                        if (resposeBody != null) {
                            for (category in resposeBody) {
                                if (category.categoryName == null) {
                                    continue
                                }
                                if (category.categoryName == "미분류") {
                                    // 항상 가장 마지막에 미분류 카테고리가 추가되도록
                                    // categoryList의 맨 뒤에 추가
                                    sBinding.chipUnclassified.visibility = View.VISIBLE
                                    categoryList.add(category)
                                    continue
                                }
                                val chip = category.categoryName!!.let { createNewChip(it) }

                                // 마지막 Chip 뷰의 인덱스를 계산
                                val lastChildIndex = sBinding.chipGroupCategory.childCount - 1

                                // 마지막 Chip 뷰의 인덱스가 0보다 큰 경우에만
                                // 현재 Chip을 바로 그 앞에 추가
                                if (lastChildIndex >= 0) {
                                    sBinding.chipGroupCategory.addView(chip, lastChildIndex)
                                } else {
                                    // ChipGroup에 자식이 없는 경우, 그냥 추가
                                    sBinding.chipGroupCategory.addView(chip)
                                }

                                categoryList.add(category)
                            }
                        }
                    }
                } else {
                    Log.e("ERROR", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<GetCategoryListResponse>, t: Throwable) {
                Log.e("ERROR", "Retrofit onFailure: ", t)
            }
        })
    }

    private fun createNewChip(text: String): Chip {
        val chip = layoutInflater.inflate(R.layout.category_chip_layout, null, false) as Chip
        chip.text = text
        //chip.isCloseIconVisible = false
        chip.setOnCloseIconClickListener {
            // 닫기 아이콘 클릭 시 Chip 제거
            (it.parent as? ViewGroup)?.removeView(it)
        }
        return chip
    }

    override fun onResume() {
        super.onResume()
        giftAdapter!!.setItemClickListener(object : ShareRoomGifticonAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int, itemId: String) {
                selectGifticonList.add(gifticonList[position])

                sBinding.shareSelectLl.visibility = View.VISIBLE
                sBinding.shareSelectTv.text = selectGifticonList.size.toString() +"개 쿠폰 선택됨"
            }
        })
    }
}