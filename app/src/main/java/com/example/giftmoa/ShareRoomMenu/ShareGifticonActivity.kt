package com.example.giftmoa.ShareRoomMenu

import android.content.res.Resources
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import com.example.giftmoa.Adapter.GifticonListAdapter
import com.example.giftmoa.Adapter.ShareRoomGifticonAdapter
import com.example.giftmoa.Data.*
import com.example.giftmoa.GridSpacingItemDecoration
import com.example.giftmoa.R
import com.example.giftmoa.Retrofit2Generator
import com.example.giftmoa.databinding.ActivityShareGifticonBinding
import com.google.android.material.chip.Chip
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ShareGifticonActivity : AppCompatActivity() {
    private lateinit var sBinding : ActivityShareGifticonBinding

    private lateinit var giftAdapter: ShareRoomGifticonAdapter

    var gifticonList = ArrayList<ShareRoomGifticon>()

    private var categoryList = mutableListOf<CategoryItem>()

    private var gridManager = GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sBinding = ActivityShareGifticonBinding.inflate(layoutInflater)
        setContentView(sBinding.root)
        initSharedRecyclerView()
        getCategoryListFromServer()


        giftAdapter!!.setItemClickListener(object : ShareRoomGifticonAdapter.ItemClickListener {
            override fun onClick(view: View, position: Int, itemId: String) {

            }
        })
    }

    private fun initSharedRecyclerView() {
        getAllGifticonListFromServer(0)
        sBinding.shareGifticonRv.apply {
            adapter = giftAdapter
            layoutManager = gridManager
            sBinding.shareGifticonRv.addItemDecoration(
                GridSpacingItemDecoration(spanCount = 2, spacing = 10f.fromDpToPx())
            )
        }
        giftAdapter.shareRoomGifticonItemData = gifticonList
        giftAdapter.setHasStableIds(true)
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
}