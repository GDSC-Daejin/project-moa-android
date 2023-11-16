package com.example.giftmoa.ShareRoomMenu

import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.example.giftmoa.*
import com.example.giftmoa.Adapter.ShareRoomGifticonAdapter
import com.example.giftmoa.Data.*
import com.example.giftmoa.GridSpacingItemDecoration
import com.example.giftmoa.databinding.ActivityShareGifticonBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShareGifticonActivity : AppCompatActivity() {
    private lateinit var sBinding : ActivityShareGifticonBinding

    private var gifticonList = ArrayList<ShareRoomGifticon>()

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
            if (selectGifticonList.isNotEmpty()) {
                CoroutineScope(Dispatchers.IO).launch {
                    for (i in selectGifticonList.indices) {
                        val temp = TeamShareGiftIcon(teamId,selectGifticonList[i].gifticonId.toInt())
                        Retrofit2Generator.create(this@ShareGifticonActivity).teamShareGificon(temp).enqueue(object : Callback<ShareRoomGifticonResponseData> {
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
            } else {
                Toast.makeText(this@ShareGifticonActivity, "공유할 기프티콘을 선택해주세요", Toast.LENGTH_SHORT).show()
            }
        }

        sBinding.backArrow.setOnClickListener {
            this.finish()
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
        Retrofit2Generator.create(this@ShareGifticonActivity).getShareGifticonList(size = 10, page = page).enqueue(object :
            Callback<GetGifticonListResponse> {
            override fun onResponse(call: Call<GetGifticonListResponse>, response: Response<GetGifticonListResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    for (i in responseBody?.data?.dataList?.indices!!) {
                        if (page == 0) {
                            // 첫 페이지인 경우 리스트를 새로 채웁니다.
                            gifticonList.clear()
                        }
                        // 새로운 데이터를 리스트에 추가합니다.
                        gifticonList.add(ShareRoomGifticon(
                            responseBody.data.dataList[i].id!!.toInt(),
                            responseBody.data.dataList[i].name!!,
                            "null",
                            responseBody.data.dataList[i].gifticonImagePath!!,
                            responseBody.data.dataList[i].exchangePlace!!,
                            responseBody.data.dataList[i].dueDate!!,
                            responseBody.data.dataList[i].gifticonType!!,
                            "null",
                            responseBody.data.dataList[i].status!!,
                            responseBody.data.dataList[i].usedDate,
                            responseBody.data.dataList[i].author,
                            responseBody.data.dataList[i].category,
                            "null",
                            false
                        ))
                    }
                    giftAdapter!!.notifyDataSetChanged()

                    println("GIfticonList" + gifticonList)
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
            override fun onClick(view: View, position: Int, itemId: Int) {
                //선택한 기프티콘에 같은게 없을 경우 추가
                //println(selectGifticonList)
                if (selectGifticonList.contains(gifticonList[position])) {
                    //println(selectGifticonList.removeIf { it.gifticonId == gifticonList[position].gifticonId })
                    selectGifticonList.removeIf { it.gifticonId == gifticonList[position].gifticonId }
                    println("contains")
                    //println(selectGifticonList)

                    if (selectGifticonList.size == 0) {
                        sBinding.shareSelectLl.visibility = View.GONE
                        //sBinding.shareSelectTv.text = selectGifticonList.size.toString() +"개 쿠폰 선택됨"
                    } else {
                        sBinding.shareSelectLl.visibility = View.VISIBLE
                        sBinding.shareSelectTv.text = selectGifticonList.size.toString() +"개 쿠폰 선택됨"
                    }
                } else {
                    selectGifticonList.add(gifticonList[position])
                    sBinding.shareSelectLl.visibility = View.VISIBLE
                    sBinding.shareSelectTv.text = selectGifticonList.size.toString() +"개 쿠폰 선택됨"
                }
            }
        })
    }
}