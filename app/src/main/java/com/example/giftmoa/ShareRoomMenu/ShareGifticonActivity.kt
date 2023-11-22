package com.example.giftmoa.ShareRoomMenu

import android.content.Intent
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
        teamId = intent.getLongExtra("teamId", 0L).toInt()
        initSharedRecyclerView()

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
                                    Toast.makeText(this@ShareGifticonActivity, "공유가 완료되었습니다.", Toast.LENGTH_SHORT).show()

                                    val intent = Intent(this@ShareGifticonActivity, ShareRoomReadActivity::class.java).apply {

                                    }

                                    this@ShareGifticonActivity.finish()
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
        Retrofit2Generator.create(this@ShareGifticonActivity).getNotShareTeamGifticonData(teamId,size = 20, page = page).enqueue(object :
            Callback<GetUsedTeamGifticonResponseData> {
            override fun onResponse(call: Call<GetUsedTeamGifticonResponseData>, response: Response<GetUsedTeamGifticonResponseData>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    gifticonList.clear()
                    for (i in responseBody?.data?.dataList?.indices!!) {
                        println(responseBody?.data?.dataList)
                        // 새로운 데이터를 리스트에 추가합니다.

                        if (responseBody.data.dataList[i].status == "UNAVAILABLE") {
                            continue
                        }
                        gifticonList.add(ShareRoomGifticon(
                            responseBody.data.dataList[i].id!!.toInt(),
                            responseBody.data.dataList[i].name!!,
                            "null",
                            responseBody.data.dataList[i].gifticonImagePath,
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
                        Log.d("Success", "gifticonList: ${gifticonList}")
                    }
                    giftAdapter!!.notifyDataSetChanged()

                    println("GIfticonList" + gifticonList)
                } else {
                    Log.e("ERROR", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<GetUsedTeamGifticonResponseData>, t: Throwable) {
                Log.e("ERROR", "Retrofit onFailure: ", t)
            }
        })
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